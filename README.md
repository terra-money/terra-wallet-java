# terra-wallet-java
for android

## Build framework.
1. build and generate "./build/libs/terra-wallet-java.jar"

## Usage. (Android Studio)
1. File - Project Structure - Module - '+' click.
2. 'Import .JAR/.AAR Package' click.
3. Browse 'terra-wallet-java.jar'.
4. add below implementation in Build.gradle(app) dependencies.

~~~gradle
implementation project(":terra-wallet-java")
implementation 'org.web3j:crypto:4.5.17'
implementation 'com.google.code.gson:gson:2.8.6''
~~~

5. See below example.

~~~java
import money.terra.terrawallet.TerraWalletSDK;
import org.json.JSONObject;

public class Example {
    public void createWallet() {
        String[] wallet = TerraWalletSDK.getNewWallet();

        String privateKey = wallet[0];
        // ex) 99b555956f56a2889c78594cfac8d8aa6d0a6e75bd3ccfefb5248b6b83d8096c, 32bytes

        String publicKey = wallet[1];
        // ex) 0352105a7248e226cbb913aad4d5997cf03db9e6caf03dd9a1d168442325d4ff1f, 33bytes

        String terraAddress = wallet[2];
        // ex) terra14aqr0fwhsh334qpeu39wuzdt9hkw2pwvwnyvh6

        String mnemonic = wallet[3];
        // ex) police head unfair frozen animal sketch peace budget orange foot fault quantum caution make reject fruit minimum east stuff leisure seminar ocean credit ridge, 24 words
    }

    public void loadWallet(String mnemonicWords) {
        String[] wallet = TerraWalletSDK.getNewWalletFromSeed(mnemonicWords, 330);
        // default bip is 330

        String privateKey = wallet[0];
        // ex) 99b555956f56a2889c78594cfac8d8aa6d0a6e75bd3ccfefb5248b6b83d8096c, 32bytes
        // if mnemonic is wrong, Return value will be ""

        String publicKey = wallet[1];
        // ex) 0352105a7248e226cbb913aad4d5997cf03db9e6caf03dd9a1d168442325d4ff1f, 33bytes
        // if mnemonic is wrong, Return value will be ""

        String terraAddress = wallet[2];
        // ex) terra14aqr0fwhsh334qpeu39wuzdt9hkw2pwvwnyvh6
        // if mnemonic is wrong, Return value will be ""
    }

    public String signMessage(JSONObject tx,
                                   String sequence,
                                   String accountNumber,
                                   String chainId,
                                   String hexPrivateKey,
                                   String hexPublicKey) throws Exception {

        // Parameter Info (for Testnet, https://soju-lcd.terra.dev)

        // tx
        // transfer ex) "{\"msg\":[{\"type\":\"bank/MsgSend\",\"value\":{\"from_address\":\"terra14aqr0fwhsh334qpeu39wuzdt9hkw2pwvwnyvh6\",\"to_address\":\"terra1y56xnxa2aaxtuc3rpntgxx0qchyzy2wp7dqgy3\",\"amount\":[{\"denom\":\"uluna\",\"amount\":\"50\"}]}}],\"fee\":{\"amount\":[{\"denom\":\"uluna\",\"amount\":\"50\"}],\"gas\":\"200000\"},\"signatures\":null,\"memo\":\"memo\"}"

        // sequence, accountNumber
        // https://soju-lcd.terra.dev/auth/accounts/{YOUR ADDRESS}

        // chainId (ex: 'soju-0013')
        // https://soju-lcd.terra.dev/blocks/latest

        // hexPrivateKey, ex) 99b555956f56a2889c78594cfac8d8aa6d0a6e75bd3ccfefb5248b6b83d8096c
        // hexPublicKey,  ex) 0352105a7248e226cbb913aad4d5997cf03db9e6caf03dd9a1d168442325d4ff1f

        try {
            String requestBody = TerraWalletSDK.sign(tx, sequence, accountNumber, chainId, hexPrivateKey, hexPublicKey).toString();
            return requestBody;

            // you can send a 'requestBody' to
            // 'https://soju-lcd.terra.dev/txs' POST
        }catch(Exception e) {
            return "";
        }
    }
}

~~~

## License
MIT
