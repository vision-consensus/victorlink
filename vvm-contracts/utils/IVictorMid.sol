// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

interface IVictorMid {

    function setToken(address tokenAddress) external ;

    function transferAndCall(address from, address to, uint tokens, bytes calldata _data) external returns (bool success) ;

    function balanceOf(address guy) external view returns (uint);

    function transferFrom(address src, address dst, uint wad) external returns (bool);

    function allowance(address src, address guy) external view returns (uint);

}