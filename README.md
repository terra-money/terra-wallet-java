# terra-wallet-java
for android

## Build framework.
1. build and generate "./build/libs/terra-wallet-java.jar"

## Usage. (Android Studio)
1. File - Project Structure - Module - '+' click.
2. 'Import .JAR/.AAR Package' click.
3. Browse 'terra-wallet-java.jar'.
4. add this 'implementation project(":terra-wallet-java")' in Build.gradle(app) dependencies.
5. available below methods.
- TerraWalletSDK.getNewWallet()
- TerraWalletSDK.getNewWalletFromSeed(String mnemonic, int bip)
- TerraWalletSDK.sign(JSONObject message, String sequence, String account_number, String chain_id, String hexPrivateKey, String hexPublicKey)
