# terra-wallet-java
for android

## Build framework.
1. build and generate .jar.

## Usage.
1. add "./build/libs/terra-wallet-java.jar" into your project.
2. available below methods.
- TerraWalletSDK.getNewWallet()
- TerraWalletSDK.getNewWalletFromSeed(String mnemonic, int bip)
- TerraWalletSDK.sign(JSONObject message, String sequence, String account_number, String chain_id, String hexPrivateKey, String hexPublicKey)
