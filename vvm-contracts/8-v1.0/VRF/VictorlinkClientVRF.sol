// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;
import "../../utils/SafeMath.sol";
import "../../utils/librarys.sol";
import "../../utils/IVictorMid.sol";
import "../../utils/IVRC20.sol";
import "../../utils/IVictorlinkRequestVRF.sol";

/**
 * @title The VictorlinkClient contract
 * @notice Contract writers can inherit this contract in order to create requests for the
 * Victorlink network
 */
contract VictorlinkClientVRF {
    using Victorlink for Victorlink.Request;
    using SafeMath for uint256;

    uint256 internal constant LINK = 10**18;
    uint256 private constant AMOUNT_OVERRIDE = 0;
    address private constant SENDER_OVERRIDE = address(0);
    uint256 private constant ARGS_VERSION = 1;

    IVictorMid internal victorMid;
    IVRC20 internal token;
    IVictorlinkRequestVRF private oracle;

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
     * @notice Encodes the request to be sent to the vrfCoordinator contract
     * @dev The Victorlink node expects values to be in order for the request to be picked up. Order of types
     * will be validated in the VRFCoordinator contract.
     * @param _req The initialized Victorlink Request
     * @return The bytes payload for the `transferAndCall` method
     */
    function encodeVRFRequest(Victorlink.Request memory _req)
        internal
        view
        returns (bytes memory)
    {
        return
            abi.encodeWithSelector(
                oracle.vrfRequest.selector,
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
}
