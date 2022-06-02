// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

interface IVictorlinkRequestVRF {
    function vrfRequest(
        address sender,
        uint256 payment,
        bytes32 id,
        address callbackAddress,
        bytes4 callbackFunctionId,
        uint256 nonce,
        uint256 version,
        bytes calldata data
    ) external;
}
