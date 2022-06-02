// SPDX-License-Identifier: MIT

pragma solidity ^0.8.0;

abstract contract Receiver {
  function onTokenTransfer(address _sender, uint _value, bytes memory _data) virtual external;
}