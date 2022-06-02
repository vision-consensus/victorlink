# FluxAggregator Uses

This page outlines the uses of the FluxAggregator contract for the node operators that feed data into it.

## Contract Deploy

Import all the files located in `vvm-contracts/v2.0` into Visionscan and compile the `FluxAggregator.sol`.

After compiling finished, you should type the arguments that the constructor needs.

Here is the paraphrase:

- `_wvs`: The address of the WVS token.
- `_paymentAmount`: The amount paid of WVS paid to each oracle per submission, in wei (units of 1e-9 WVS).
- `_timeout`:  the number of seconds after the previous round that are allowed to lapse before allowing an oracle to skip an unfinished round.
- `_validator`: an optional contract address for validating external validation of answers.
- `_minSubmissionValue`: an immutable check for a lower bound of what submission values are accepted from an oracle.
- `_maxSubmissionValue`: an immutable check for an upper bound of what submission values are accepted from an oracle.
- `_decimals`: represents the number of decimals to offset the answer by.
- `_description`: a short description of what is being reported.

## Node Deployment

The node service deployment is the same as before as the service is compatible with both types of Aggregators.

## Withdrawing funds

Keep in mind the oracle variable is currently your node's address rather than your oracle contract's address.

## Testnet(Vpioneer)

|Pair|Contract|
|:--|:--|
|VS-USDT||


## Mainnet
|Pair|Contract|
|:--|:--|

## Proxy

|Pair|Contract|
|:--|:--|
