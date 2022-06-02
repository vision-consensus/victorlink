// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;
import "../utils/IVRC20.sol";
import "../utils/Ownable.sol";
import "../utils/Receiver.sol";

// ----------------------------------------------------------------------------
contract VictorMid is Ownable {

  IVRC20 private token ;

  event Transfer(address indexed from, address indexed to, uint value, bytes data);

  constructor(address _link){
    token = IVRC20(_link);
  }

  function getToken() public view returns(address){
      return address(token);
  }

  function setToken(address tokenAddress) public onlyOwner returns (bool success){
    token = IVRC20(tokenAddress);
    return true;
  }

  function transferAndCall(address from, address to, uint tokens, bytes memory _data) public validRecipient(to) returns (bool success) {
    token.transferFrom(from,to,tokens);
    emit Transfer(from, to, tokens, _data);
    if (isContract(to)) {
      contractFallback(to, tokens, _data);
    }
    return true;
  }

  function transferFrom(address from, address to, uint tokens) public validRecipient(to) returns (bool success) {
    token.transferFrom(from,to,tokens);
    return true;
  }

  function balanceOf(address guy) public view returns (uint) {
      return token.balanceOf(guy);
  }

  function allowance(address src, address guy) public view returns (uint){
      return token.allowance(src, guy);
  }

  modifier validRecipient(address _recipient) {
    require(_recipient != address(0) && _recipient != address(this));
    _;
  }

  function contractFallback(address _to, uint _value, bytes memory _data) private
  {
    Receiver receiver = Receiver(_to);
    receiver.onTokenTransfer(msg.sender, _value, _data);
  }

   function isContract(address _addr) private view returns (bool hasCode)
  {
    uint length;
    assembly { length := extcodesize(_addr) }
    return length > 0;
  }

}
