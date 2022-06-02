// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "../utils/SafeMath.sol";
import "../utils/librarys.sol";
import "../utils/IVictorMid.sol";
import "../utils/IVRC20.sol";
import "../utils/IVictorlinkRequestOracle.sol";

/**
 * @title The VictorlinkClient contract
 * @notice Contract writers can inherit this contract in order to create requests for the
 * Victorlink network
 */
contract VictorlinkClientOracle {
    using Victorlink for Victorlink.Request;
    using SafeMath for uint256;

    uint256 internal constant LINK = 10**18;
    uint256 private constant AMOUNT_OVERRIDE = 0;
    address private constant SENDER_OVERRIDE = address(0);
    uint256 private constant ARGS_VERSION = 1;

    IVictorMid internal victorMid;
    IVRC20 internal token;
    IVictorlinkRequestOracle private oracle;
    uint256 private requests = 1;
    mapping(bytes32 => address) private pendingRequests;

    event VictorlinkRequested(bytes32 indexed id);
    event VictorlinkFulfilled(bytes32 indexed id);
    event VictorlinkCancelled(bytes32 indexed id);

    /**
     * @notice Creates a request that can hold additional parameters
     * @param _specId The Job Specification ID that the request will be created for
     * @param _callbackAddress The callback address that the response will be sent to
     * @param _callbackFunctionSignature The callback function signature to use for the callback address
     * @return A Victorlink Request struct in memory
     */
    function buildVictorlinkRequest(
        bytes32 _specId,
        address _callbackAddress,
        bytes4 _callbackFunctionSignature
    ) internal pure returns (Victorlink.Request memory) {
        Victorlink.Request memory req;
        return
            req.initialize(
                _specId,
                _callbackAddress,
                _callbackFunctionSignature
            );
    }

    /**
     * @notice Creates a Victorlink request to the stored oracle address
     * @dev Calls `VictorlinkRequestTo` with the stored oracle address
     * @param _req The initialized Victorlink Request
     * @param _payment The amount of LINK to send for the request
     * @return The request ID
     */
    function sendVictorlinkRequest(
        Victorlink.Request memory _req,
        uint256 _payment
    ) internal returns (bytes32) {
        return sendVictorlinkRequestTo(address(oracle), _req, _payment);
    }

    /**
     * @notice Creates a Victorlink request to the specified oracle address
     * @dev Generates and stores a request ID, increments the local nonce, and uses `transferAndCall` to
     * send LINK which creates a request on the target oracle contract.
     * Emits VictorlinkRequested event.
     * @param _oracle The address of the oracle for the request
     * @param _req The initialized Victorlink Request
     * @param _payment The amount of LINK to send for the request
     * @return requestId The request ID
     */
    function sendVictorlinkRequestTo(
        address _oracle,
        Victorlink.Request memory _req,
        uint256 _payment
    ) internal returns (bytes32 requestId) {
        requestId = keccak256(abi.encodePacked(this, requests));
        _req.nonce = requests;
        pendingRequests[requestId] = _oracle;
        emit VictorlinkRequested(requestId);
        token.approve(victorMidAddress(), _payment);
        require(
            victorMid.transferAndCall(
                address(this),
                _oracle,
                _payment,
                encodeRequest(_req)
            ),
            "unable to transferAndCall to oracle"
        );
        requests += 1;

        return requestId;
    }

    /**
     * @notice Allows a request to be cancelled if it has not been fulfilled
     * @dev Requires keeping track of the expiration value emitted from the oracle contract.
     * Deletes the request from the `pendingRequests` mapping.
     * Emits VictorlinkCancelled event.
     * @param _requestId The request ID
     * @param _payment The amount of LINK sent for the request
     * @param _callbackFunc The callback function specified for the request
     * @param _expiration The time of the expiration for the request
     */
    function cancelVictorlinkRequest(
        bytes32 _requestId,
        uint256 _payment,
        bytes4 _callbackFunc,
        uint256 _expiration
    ) internal {
        IVictorlinkRequestOracle requested = IVictorlinkRequestOracle(
            pendingRequests[_requestId]
        );
        delete pendingRequests[_requestId];
        emit VictorlinkCancelled(_requestId);
        requested.cancelOracleRequest(
            _requestId,
            _payment,
            _callbackFunc,
            _expiration
        );
    }

    /**
     * @notice Sets the stored oracle address
     * @param _oracle The address of the oracle contract
     */
    function setVictorlinkOracle(address _oracle) internal {
        oracle = IVictorlinkRequestOracle(_oracle);
    }

    /**
     * @notice Sets the LINK token address
     * @param _link The address of the LINK token contract
     */
    function setVictorlinkToken(address _link) internal {
        token = IVRC20(_link);
    }

    function setVictorMid(address _victorMid) internal {
        victorMid = IVictorMid(_victorMid);
    }

    /**
     * @notice Retrieves the stored address of the LINK token
     * @return The address of the LINK token
     */
    function victorMidAddress() public view returns (address) {
        return address(victorMid);
    }

    /**
     * @notice Retrieves the stored address of the oracle contract
     * @return The address of the oracle contract
     */
    function victorlinkOracleAddress() internal view returns (address) {
        return address(oracle);
    }

    /**
     * @notice Allows for a request which was created on another contract to be fulfilled
     * on this contract
     * @param _oracle The address of the oracle contract that will fulfill the request
     * @param _requestId The request ID used for the response
     */
    function addVictorlinkExternalRequest(address _oracle, bytes32 _requestId)
        internal
        notPendingRequest(_requestId)
    {
        pendingRequests[_requestId] = _oracle;
    }

    /**
     * @notice Encodes the request to be sent to the oracle contract
     * @dev The Victorlink node expects values to be in order for the request to be picked up. Order of types
     * will be validated in the oracle contract.
     * @param _req The initialized Victorlink Request
     * @return The bytes payload for the `transferAndCall` method
     */
    function encodeRequest(Victorlink.Request memory _req)
        private
        view
        returns (bytes memory)
    {
        return
            abi.encodeWithSelector(
                oracle.oracleRequest.selector,
                SENDER_OVERRIDE, // Sender value - overridden by onTokenTransfer by the requesting contract's address
                AMOUNT_OVERRIDE, // Amount value - overridden by onTokenTransfer by the actual amount of LINK sent
                _req.id,
                _req.callbackAddress,
                _req.callbackFunctionId,
                _req.nonce,
                ARGS_VERSION,
                _req.buf.buf
            );
    }

    /**
     * @notice Ensures that the fulfillment is valid for this contract
     * @dev Use if the contract developer prefers methods instead of modifiers for validation
     * @param _requestId The request ID for fulfillment
     */
    function validateVictorlinkCallback(bytes32 _requestId)
        internal
        recordVictorlinkFulfillment(_requestId)
    // solhint-disable-next-line no-empty-blocks
    {

    }

    /**
     * @dev Reverts if the sender is not the oracle of the request.
     * Emits VictorlinkFulfilled event.
     * @param _requestId The request ID for fulfillment
     */
    modifier recordVictorlinkFulfillment(bytes32 _requestId) {
        require(
            msg.sender == pendingRequests[_requestId],
            "Source must be the oracle of the request"
        );
        delete pendingRequests[_requestId];
        emit VictorlinkFulfilled(_requestId);
        _;
    }

    /**
     * @dev Reverts if the request is already pending
     * @param _requestId The request ID for fulfillment
     */
    modifier notPendingRequest(bytes32 _requestId) {
        require(
            pendingRequests[_requestId] == address(0),
            "Request is already pending"
        );
        _;
    }
}