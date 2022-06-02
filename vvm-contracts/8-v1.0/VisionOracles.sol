// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "../utils/IVRC20.sol";
import "../utils/IOracle.sol";
import "../utils/Ownable.sol";
import "../utils/SafeMath.sol";
import "../utils/IVictorMid.sol";
import "../utils/IVictorlinkRequestOracle.sol";

/**
 * @title The Victorlink Oracle contract
 * @notice Node operators can deploy this contract to fulfill requests sent to them
 */
contract Oracle is IVictorlinkRequestOracle, IOracle, Ownable {
    using SafeMath for uint256;

    uint256 public constant EXPIRY_TIME = 5 minutes;
    // We initialize fields to 1 instead of 0 so that the first invocation
    // does not cost more gas.
    uint256 private constant ONE_FOR_CONSISTENT_GAS_COST = 1;
    uint256 private constant SELECTOR_LENGTH = 4;
    uint256 private constant EXPECTED_REQUEST_WORDS = 2;
    uint256 private constant MINIMUM_REQUEST_LENGTH =
        SELECTOR_LENGTH + (32 * EXPECTED_REQUEST_WORDS);

    IVictorMid internal victorMid;
    IVRC20 internal token;
    mapping(bytes32 => bytes32) private commitments;
    mapping(address => bool) private authorizedNodes;
    uint256 private withdrawableTokens = ONE_FOR_CONSISTENT_GAS_COST;

    event OracleRequest(
        bytes32 indexed specId,
        address requester,
        bytes32 requestId,
        uint256 payment,
        address callbackAddr,
        bytes4 callbackFunctionId,
        uint256 cancelExpiration,
        uint256 dataVersion,
        bytes data
    );

    event CancelOracleRequest(bytes32 indexed requestId);

    /**
     * @notice Deploy with the address of the LINK token
     * @dev Sets the LinkToken address for the imported LinkTokenInterface
     * @param _link The address of the LINK token
     */
    constructor(address _link, address _victorMid) {
        token = IVRC20(_link); // external but already deployed and unalterable
        victorMid = IVictorMid(_victorMid);
    }

    /**
     * @notice Called when LINK is sent to the contract via `transferAndCall`
     * @dev The data payload's first 2 words will be overwritten by the `_sender` and `_amount`
     * values to ensure correctness. Calls oracleRequest.
     * @param _sender Address of the sender
     * @param _amount Amount of LINK sent (specified in wei)
     * @param _data Payload of the transaction
     */
    function onTokenTransfer(
        address _sender,
        uint256 _amount,
        bytes memory _data
    )
        public
        onlyVictorMid
        validRequestLength(_data)
        permittedFunctionsForLINK(_data)
    {
        assembly {
            // solhint-disable-line no-inline-assembly
            mstore(add(_data, 36), _sender) // ensure correct sender is passed
            mstore(add(_data, 68), _amount) // ensure correct amount is passed
        }
        // solhint-disable-next-line avoid-low-level-calls
        (bool success,)  = address(this).delegatecall(_data);
        require(success, "Unable to create request"); // calls oracleRequest
    }

    /**
     * @notice Retrieves the stored address of the LINK token
     * @return The address of the LINK token
     */
    function victorMidAddress() public view returns (address) {
        return address(victorMid);
    }

    /**
     * @notice Creates the Victorlink request
     * @dev Stores the hash of the params as the on-chain commitment for the request.
     * Emits OracleRequest event for the Victorlink node to detect.
     * @param _sender The sender of the request
     * @param _payment The amount of payment given (specified in wei)
     * @param _specId The Job Specification ID
     * @param _callbackAddress The callback address for the response
     * @param _callbackFunctionId The callback function ID for the response
     * @param _nonce The nonce sent by the requester
     * @param _dataVersion The specified data version
     * @param _data The CBOR payload of the request
     */
    function oracleRequest(
        address _sender,
        uint256 _payment,
        bytes32 _specId,
        address _callbackAddress,
        bytes4 _callbackFunctionId,
        uint256 _nonce,
        uint256 _dataVersion,
        bytes memory _data
    ) external override onlyVictorMid checkCallbackAddress(_callbackAddress) {
        bytes32 requestId = keccak256(abi.encodePacked(_sender, _nonce));
        require(commitments[requestId] == 0, "Must use a unique ID");
        // solhint-disable-next-line not-rely-on-time
        uint256 expiration = block.timestamp.add(EXPIRY_TIME);

        commitments[requestId] = keccak256(
            abi.encodePacked(
                _payment,
                _callbackAddress,
                _callbackFunctionId,
                expiration
            )
        );

        emit OracleRequest(
            _specId,
            _sender,
            requestId,
            _payment,
            _callbackAddress,
            _callbackFunctionId,
            expiration,
            _dataVersion,
            _data
        );
    }

    /**
     * @notice Called by the Victorlink node to fulfill requests
     * @dev Given params must hash back to the commitment stored from `oracleRequest`.
     * Will call the callback address' callback function without bubbling up error
     * checking in a `require` so that the node can get paid.
     * @param _requestId The fulfillment request ID that must match the requester's
     * @param _payment The payment amount that will be released for the oracle (specified in wei)
     * @param _callbackAddress The callback address to call for fulfillment
     * @param _callbackFunctionId The callback function ID to use for fulfillment
     * @param _expiration The expiration that the node should respond by before the requester can cancel
     * @param _data The data to return to the consuming contract
     * @return Status if the external call was successful
     */
    function fulfillOracleRequest(
        bytes32 _requestId,
        uint256 _payment,
        address _callbackAddress,
        bytes4 _callbackFunctionId,
        uint256 _expiration,
        bytes32 _data
    ) external override onlyAuthorizedNode isValidRequest(_requestId) returns (bool) {
        bytes32 paramsHash = keccak256(
            abi.encodePacked(
                _payment,
                _callbackAddress,
                _callbackFunctionId,
                _expiration
            )
        );
        require(
            commitments[_requestId] == paramsHash,
            "Params do not match request ID"
        );
        withdrawableTokens = withdrawableTokens.add(_payment);
        delete commitments[_requestId];
        // All updates to the oracle's fulfillment should come before calling the
        // callback(addr+functionId) as it is untrusted.
        // See: https://solidity.readthedocs.io/en/develop/security-considerations.html#use-the-checks-effects-interactions-pattern
        (bool success,) = _callbackAddress.call(abi.encodeWithSelector(_callbackFunctionId, _requestId, _data)); // TODO double check
        return success; // solhint-disable-line avoid-low-level-calls
    }

    /**
     * @notice Use this to check if a node is authorized for fulfilling requests
     * @param _node The address of the Victorlink node
     * @return The authorization status of the node
     */
    function getAuthorizationStatus(address _node)
        external
        override
        view
        returns (bool)
    {
        return authorizedNodes[_node];
    }

    /**
     * @notice Sets the fulfillment permission for a given node. Use `true` to allow, `false` to disallow.
     * @param _node The address of the Victorlink node
     * @param _allowed Bool value to determine if the node can fulfill requests
     */
    function setFulfillmentPermission(address _node, bool _allowed)
        external
        override
        onlyOwner
    {
        authorizedNodes[_node] = _allowed;
    }

    /**
     * @notice Allows the node operator to withdraw earned LINK to a given address
     * @dev The owner of the contract can be another wallet and does not have to be a Victorlink node
     * @param _recipient The address to send the LINK token to
     * @param _amount The amount to send (specified in wei)
     */
    function withdraw(address _recipient, uint256 _amount)
        external
        override
        onlyOwner
        hasAvailableFunds(_amount)
    {
        withdrawableTokens = withdrawableTokens.sub(_amount);
        token.approve(victorMidAddress(), _amount);
        assert(victorMid.transferFrom(address(this), _recipient, _amount));
    }

    /**
     * @notice Displays the amount of LINK that is available for the node operator to withdraw
     * @dev We use `ONE_FOR_CONSISTENT_GAS_COST` in place of 0 in storage
     * @return The amount of withdrawable LINK on the contract
     */
    function withdrawable() external override view onlyOwner returns (uint256) {
        return withdrawableTokens.sub(ONE_FOR_CONSISTENT_GAS_COST);
    }

    /**
     * @notice Allows requesters to cancel requests sent to this oracle contract. Will transfer the LINK
     * sent for the request back to the requester's address.
     * @dev Given params must hash to a commitment stored on the contract in order for the request to be valid
     * Emits CancelOracleRequest event.
     * @param _requestId The request ID
     * @param _payment The amount of payment given (specified in wei)
     * @param _callbackFunc The requester's specified callback address
     * @param _expiration The time of the expiration for the request
     */
    function cancelOracleRequest(
        bytes32 _requestId,
        uint256 _payment,
        bytes4 _callbackFunc,
        uint256 _expiration
    ) external override {
        bytes32 paramsHash = keccak256(
            abi.encodePacked(_payment, msg.sender, _callbackFunc, _expiration)
        );
        require(
            paramsHash == commitments[_requestId],
            "Params do not match request ID"
        );
        // solhint-disable-next-line not-rely-on-time
        require(_expiration <= block.timestamp, "Request is not expired");

        delete commitments[_requestId];
        emit CancelOracleRequest(_requestId);
        token.approve(victorMidAddress(), _payment);
        assert(victorMid.transferFrom(address(this), msg.sender, _payment));
    }

    // MODIFIERS

    /**
     * @dev Reverts if amount requested is greater than withdrawable balance
     * @param _amount The given amount to compare to `withdrawableTokens`
     */
    modifier hasAvailableFunds(uint256 _amount) {
        require(
            withdrawableTokens >= _amount.add(ONE_FOR_CONSISTENT_GAS_COST),
            "Amount requested is greater than withdrawable balance"
        );
        _;
    }

    /**
     * @dev Reverts if request ID does not exist
     * @param _requestId The given request ID to check in stored `commitments`
     */
    modifier isValidRequest(bytes32 _requestId) {
        require(commitments[_requestId] != 0, "Must have a valid requestId");
        _;
    }

    /**
     * @dev Reverts if `msg.sender` is not authorized to fulfill requests
     */
    modifier onlyAuthorizedNode() {
        require(
            authorizedNodes[msg.sender] || msg.sender == owner(),
            "Not an authorized node to fulfill requests"
        );
        _;
    }

    /**
     * @dev Reverts if not sent from the LINK token
     */
    modifier onlyVictorMid() {
        require(msg.sender == address(victorMid), "Must use VictorMid");
        _;
    }

    /**
     * @dev Reverts if the given data does not begin with the `oracleRequest` function selector
     * @param _data The data payload of the request
     */
    modifier permittedFunctionsForLINK(bytes memory _data) {
        bytes4 funcSelector;
        assembly {
            // solhint-disable-line no-inline-assembly
            funcSelector := mload(add(_data, 32))
        }
        require(
            funcSelector == this.oracleRequest.selector,
            "Must use whitelisted functions"
        );
        _;
    }

    /**
     * @dev Reverts if the callback address is the LINK token
     * @param _to The callback address
     */
    modifier checkCallbackAddress(address _to) {
        require(_to != address(victorMid), "Cannot callback to LINK");
        _;
    }

    /**
     * @dev Reverts if the given payload is less than needed to create a request
     * @param _data The request payload
     */
    modifier validRequestLength(bytes memory _data) {
        require(
            _data.length >= MINIMUM_REQUEST_LENGTH,
            "Invalid request length"
        );
        _;
    }
}
