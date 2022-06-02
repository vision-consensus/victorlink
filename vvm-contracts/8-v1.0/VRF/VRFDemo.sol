// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

import "./VRFConsumerBase.sol";
import "../../utils/Ownable.sol";

// todo 注释
/**
 * @notice A Chainlink VRF consumer which uses randomness to mimic the rolling
 * of a 20 sided die
 * @dev This is only an example implementation and not necessarily suitable for mainnet.
 */
contract VRFDemo is VRFConsumerBase, Ownable {
    using SafeMath for uint256;

    uint256 private constant ROLL_IN_PROGRESS = 42;

    bytes32 private s_keyHash;
    uint256 private s_fee;
    mapping(bytes32 => address) private s_rollers;
    mapping(address => uint256) private s_results;

    event DiceRolled(bytes32 indexed requestId, address indexed roller);
    event DiceLanded(bytes32 indexed requestId, uint256 indexed result);

    // todo 这里注释都改了
    /**
     * @notice Constructor inherits VRFConsumerBase
     *
     * @dev NETWORK: KOVAN
     * @dev   Chainlink VRF Coordinator address: 0xdD3782915140c8f3b190B5D67eAc6dc5760C46E9
     * @dev   LINK token address:                0xa36085F69e2889c224210F603D836748e7dC0088
     * @dev   Key Hash:   0x6c3699283bda56ad74f6b855546325b68d482e983852a7a82979cc4807b641f4
     * @dev   Fee:        0.1 LINK (100000000000000000)
     *
     * @param _vrfCoordinator address of the VRF Coordinator
     * @param _win address of the WVS token
     * @param _victorMid address of victorMid token
     * @param _keyHash bytes32 representing the hash of the VRF job
     * @param _fee uint256 fee to pay the VRF oracle
     */
    constructor(
        address _vrfCoordinator,
        address _win,
        address _victorMid,
        bytes32 _keyHash,
        uint256 _fee
    ) VRFConsumerBase(_vrfCoordinator, _win, _victorMid) {
        s_keyHash = _keyHash;
        s_fee = _fee;
    }

    /**
     * @notice Requests randomness from a user-provided seed
     * @dev Warning: if the VRF response is delayed, avoid calling requestRandomness repeatedly
     * as that would give miners/VRF operators latitude about which VRF response arrives first.
     * @dev You must review your implementation details with extreme care.
     *
     * @param userProvidedSeed uint256 unpredictable seed
     * @param roller address of the roller
     */
    function rollDice(uint256 userProvidedSeed, address roller)
        public
        returns (
            /*onlyOwner*/
            //DEBUG TODO
            bytes32 requestId
        )
    {
        require(
            victorMid.balanceOf(address(this)) >= s_fee,
            "Not enough WVS to pay fee"
        );
        //require(s_results[roller] == 0, "Already rolled"); //DEBUG TODO
        requestId = requestRandomness(s_keyHash, s_fee, userProvidedSeed);
        //s_rollers[requestId] = roller; //DEBUG TODO
        //s_results[roller] = ROLL_IN_PROGRESS; //DEBUG TODO
        emit DiceRolled(requestId, roller);
    }

    /**
     * @notice Callback function used by VRF Coordinator to return the random number
     * to this contract.
     * @dev Some action on the contract state should be taken here, like storing the result.
     * @dev WARNING: take care to avoid having multiple VRF requests in flight if their order of arrival would result
     * in contract states with different outcomes. Otherwise miners or the VRF operator would could take advantage
     * by controlling the order.
     * @dev The VRF Coordinator will only send this function verified responses, and the parent VRFConsumerBase
     * contract ensures that this method only receives randomness from the designated VRFCoordinator.
     *
     * @param requestId bytes32
     * @param randomness The random result returned by the oracle
     */
    function fulfillRandomness(bytes32 requestId, uint256 randomness)
        internal
        override
    {
        uint256 d20Value = randomness.mod(20).add(1);
        //s_results[s_rollers[requestId]] = d20Value; //DEBUG TODO
        emit DiceLanded(requestId, d20Value);
    }

    /**
     * @notice Get the house assigned to the player once the address has rolled
     * @param player address
     * @return house as a string
     */
    function house(address player) public view returns (string memory) {
        require(s_results[player] != 0, "Dice not rolled");
        require(s_results[player] != ROLL_IN_PROGRESS, "Roll in progress");
        return getHouseName(s_results[player]);
    }

    /**
     * @notice Withdraw WVS from this contract.
     * @dev this is an example only, and in a real contract withdrawals should
     * happen according to the established withdrawal pattern:
     * https://docs.soliditylang.org/en/v0.4.24/common-patterns.html#withdrawal-from-contracts
     * @param to the address to withdraw WVS to
     * @param value the amount of WVS to withdraw
     */
    function withdrawWVS(address to, uint256 value) public onlyOwner {
        token.approve(victorMidAddress(), value);
        require(
            victorMid.transferFrom(address(this), to, value),
            "Not enough WVS"
        );
    }

    /**
     * @notice Set the key hash for the oracle
     *
     * @param _keyHash bytes32
     */
    function setKeyHash(bytes32 _keyHash) public onlyOwner {
        s_keyHash = _keyHash;
    }

    /**
     * @notice Get the current key hash
     *
     * @return bytes32
     */
    function keyHash() public view returns (bytes32) {
        return s_keyHash;
    }

    /**
     * @notice Set the oracle fee for requesting randomness
     *
     * @param _fee uint256
     */
    function setFee(uint256 _fee) public onlyOwner {
        s_fee = _fee;
    }

    /**
     * @notice Get the current fee
     *
     * @return uint256
     */
    function fee() public view returns (uint256) {
        return s_fee;
    }

    /**
     * @notice Get the house namne from the id
     * @param id uint256
     * @return house name string
     */
    function getHouseName(uint256 id) private pure returns (string memory) {
        string[20] memory houseNames = [
            "Targaryen",
            "Lannister",
            "Stark",
            "Tyrell",
            "Baratheon",
            "Martell",
            "Tully",
            "Bolton",
            "Greyjoy",
            "Arryn",
            "Frey",
            "Mormont",
            "Tarley",
            "Dayne",
            "Umber",
            "Valeryon",
            "Manderly",
            "Clegane",
            "Glover",
            "Karstark"
        ];
        return houseNames[id.sub(1)];
    }
}
