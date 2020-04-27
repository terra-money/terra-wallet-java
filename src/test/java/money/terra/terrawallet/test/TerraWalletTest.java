package money.terra.terrawallet.test;

import money.terra.terrawallet.TerraWalletSDK;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

public class TerraWalletTest {
    @Test
    public void walletGenerateAndRecoverTest() throws Exception {
        final int count = 100;

        ArrayList<String[]> wallets = new ArrayList();
        // generate
        for(int i=0; i<count; i++) {
            String[] wallet = TerraWalletSDK.getNewWallet();

            String privateKey = wallet[0];
            String publicKey = wallet[1];
            String publicKey64 = wallet[2];
            String address = wallet[3];
            String mnemonic = wallet[4];

            Assert.assertEquals("generated["+i+"], privateKey is wrong.", 64, privateKey.length());
            Assert.assertEquals("generated["+i+"], publicKey is wrong.", 66, publicKey.length());
            Assert.assertEquals("generated["+i+"], mnemonic is wrong.", 24, mnemonic.split(" ").length);

            //success generating.
            wallets.add(wallet);
        }



        // recover from generated above.

        Assert.assertEquals("items generated not enough.", wallets.size(), count);

        for(int i=0; i<wallets.size(); i++) {

            String mnemonic = wallets.get(i)[4];
            String[] wallet = TerraWalletSDK.getNewWalletFromSeed(mnemonic,330);

            String privateKey = wallet[0];
            String publicKey = wallet[1];   //compressed public key.
            String publicKey64 = wallet[2];   //'un'compressed public key.
            String address = wallet[3];

            //check privateKey(recovered and generated is equal)
            Assert.assertEquals("recovered["+i+"], privateKey is wrong.", wallets.get(i)[0], privateKey);
            //check publicKey(recovered and generated is equal)
            Assert.assertEquals("recovered["+i+"], publicKey is wrong.", wallets.get(i)[1], publicKey);
            //check address(recovered and generated is equal)
            Assert.assertEquals("recovered["+i+"], address is wrong.", wallets.get(i)[3], address);
        }

    }
//
//    @Test
//    public void transferTest() {
//        String hexPrivateKey = "99b555956f56a2889c78594cfac8d8aa6d0a6e75bd3ccfefb5248b6b83d8096c";
//        String hexPublicKey = "0352105a7248e226cbb913aad4d5997cf03db9e6caf03dd9a1d168442325d4ff1f";
//        String address = "terra14aqr0fwhsh334qpeu39wuzdt9hkw2pwvwnyvh6";
//        String chainId = "soju-0013";
//
//
//        String toAddress = "terra1y56xnxa2aaxtuc3rpntgxx0qchyzy2wp7dqgy3";
//        String transferBalance = "1000000"; //1Luna
//
//        getAccountInfo(address) { (luna, accountNumber, sequence) in
//
//            if let lunaValue = Int(luna), lunaValue > 1000 {
//
//            } else {
//                Assert.error("not enough luna balance.");
//                return;
//            }
//
//            if accountNumber == "" {
//                Assert.error("account number is not valid");
//                return;
//            }
//
//            if sequence == "" {
//                Assert.error("sequence is not valid");
//                return;
//            }
//
//
//            self.makeTransferMessage(from: address, to: toAddress, accountNumber: accountNumber, chainId: chainId, sequence: sequence, transferBalance: transferBalance) { message in
//
//                if message.isEmpty {
//                    Assert.error("makeTransferMessage is wrong.");
//                }
//
//                let signed = TerraWalletSDK.sign(message, sequence: sequence, account_number: accountNumber, chain_id: chainId, hexPrivateKey: hexPrivateKey, hexPublicKey: hexPublicKey)
//
//                guard let jsonData = try? JSONSerialization.data(withJSONObject: signed, options: []) else {
//                    Assert.error("signed message is wrong.");
//                    return
//                }
//
//                self.broadcast(message: jsonData) { (success) in
//                    if !success {
//                        Assert.error("broadcast failed.");
//                    } else {
//                        print("TRANSFER SUCCESS.")
//                    }
//                    expectation.fulfill()
//                }
//            }
//        }
//
//        // Wait until the expectation is fulfilled, with a timeout of 10 seconds.
//        wait(for: [expectation], timeout: 30.0)
//    }
//
//    @Test
//    public void marketSwapTest() {
//        String hexPrivateKey = "99b555956f56a2889c78594cfac8d8aa6d0a6e75bd3ccfefb5248b6b83d8096c";
//        String hexPublicKey = "0352105a7248e226cbb913aad4d5997cf03db9e6caf03dd9a1d168442325d4ff1f";
//        String address = "terra14aqr0fwhsh334qpeu39wuzdt9hkw2pwvwnyvh6";
//        String chainId = "soju-0013";
//
//
//        String toCurrency = "usdr"; //SDT
//        String swapBalance = "1000000"; //1Luna
//
//        self.getAccountInfo(address: address) { (luna, accountNumber, sequence) in
//            if let lunaValue = Int(luna), lunaValue > 1000 {
//
//            } else {
//                Assert.error("not enough luna balance.");
//                return;
//            }
//
//            if accountNumber == "" {
//                Assert.error("account number is not valid");
//                return;
//            }
//
//            if sequence == "" {
//                Assert.error("sequence is not valid");
//                return;
//            }
//
//
//            self.makeMarketSwapMessage(from: address, toCurrency: toCurrency, accountNumber: accountNumber, chainId: chainId, sequence: sequence, swapBalance: swapBalance) { message in
//
//                if message.isEmpty {
//                    Assert.error("makeMarketSwapMessage is wrong.");
//                }
//
//                let signed = TerraWalletSDK.sign(message, sequence: sequence, account_number: accountNumber, chain_id: chainId, hexPrivateKey: hexPrivateKey, hexPublicKey: hexPublicKey)
//
//                guard let jsonData = try? JSONSerialization.data(withJSONObject: signed, options: []) else {
//                    Assert.error("signed message is wrong.");
//                    return
//                }
//
//                self.broadcast(message: jsonData) { (success) in
//                    if !success {
//                        Assert.error("broadcast failed.");
//                    } else {
//                        print("MARKET SWAP SUCCESS.")
//                    }
//                    expectation.fulfill()
//                }
//            }
//        }
//
//        // Wait until the expectation is fulfilled, with a timeout of 10 seconds.
//        wait(for: [expectation], timeout: 30.0)
//    }
//
//    private void getAccountInfo(String address, callback: @escaping (String, String, String)->()) {
//
//        dataTask("/auth/accounts/" + address, "GET") { (data, response, error) in
//            String luna = "";
//            String accountNumber = "";
//            String sequence = "";
//
//            if let data = data,
//            let json = try? JSONSerialization.jsonObject(with: data, options: []),
//            let dic = json as? [String:Any] {
//
//                let result = dic["result"] as? [String: Any]
//                let value = result?["value"] as? [String: Any]
//
//                if let coins = value?["coins"] as? [[String: Any]] {
//                    for coin in coins {
//                        if let denom = coin["denom"] as? String, denom == "uluna" {
//                            luna = (coin["amount"] as? String) ?? ""
//                        }
//                    }
//
//                }
//
//                accountNumber = (value?["account_number"] as? String) ?? ""
//                sequence = (value?["sequence"] as? String) ?? ""
//            }
//
//            callback(luna, accountNumber, sequence)
//        }
//    }
//
//    private void makeTransferMessage(String from, String to, String accountNumber, String chainId, String sequence, String transferBalance, callback: @escaping ([String:Any])->()) {
//
//        String requestJson = "{\"base_req\": {\"from\": \"" + from +
//                "\",\"memo\": \"Sent via terra-wallet-ios xctest\",\"chain_id\": \"" + chainId +
//                "\",\"account_number\": \"" + accountNumber +
//                "\",\"sequence\": \"" + sequence +
//                "\",\"gas\": \"200000\",\"gas_adjustment\": \"1.2\",\"fees\": [{\"denom\": \"uluna\",\"amount\": \"50\"}],\"simulate\": false},\"coins\": [{\"denom\": \"uluna\",\"amount\": \"" + transferBalance + "\"}]}";
//
//        dataTask("/bank/accounts/" + to + "/transfers", requestJson.data(using: .utf8)) { (data, response, error) in
//            var message = Dictionary<String, Any>()
//
//            if let data = data,
//            let json = try? JSONSerialization.jsonObject(with: data, options: []),
//            let dic = json as? [String:Any],
//            let msg = dic["value"] as? [String : Any] {
//                message = msg
//            }
//
//            callback(message)
//        }
//    }
//
//    private void makeMarketSwapMessage(String from, String toCurrency, String accountNumber, String chainId, String sequence, String swapBalance, callback: @escaping ([String:Any])->()) {
//
//        String requestJson = "{\"base_req\": {\"from\": \"" + from +
//                "\",\"memo\": \"Sent via terra-wallet-ios xctest\",\"chain_id\": \"" + chainId +
//                "\",\"account_number\": \"" + accountNumber +
//                "\",\"sequence\": \"" + sequence +
//                "\",\"gas\": \"200000\",\"gas_adjustment\": \"1.2\",\"fees\": [{\"denom\": \"uluna\",\"amount\": \"50\"}],\"simulate\": false},\"offer_coin\": {\"denom\": \"uluna\",\"amount\": \"" + swapBalance +
//                "\"},\"ask_denom\": \"" + toCurrency + "\"}";
//
//
//        dataTask("/market/swap", requestJson.data(using: .utf8)) { (data, response, error) in
//            var message = Dictionary<String, Any>()
//
//            if let data = data,
//            let json = try? JSONSerialization.jsonObject(with: data, options: []),
//            let dic = json as? [String:Any],
//            let msg = dic["value"] as? [String : Any] {
//                message = msg
//            }
//
//            callback(message)
//        }
//    }
//
//    private void broadcast(byte[] message, callback:@escaping(Bool)->()) {
//        dataTask("/txs", message) { (data, response, error) in
//            if let response = response as? HTTPURLResponse, response.statusCode == 200 {
//                callback(true)
//            } else {
//                if let data = data {
//                    print(String(data: data, encoding:.utf8))
//                }
//                callback(false)
//            }
//        }
//    }
//
//    private void dataTask(String url, String httpMethod, byte[] data, callback:@escaping (Data?, URLResponse?, Error?)->()) {
//        let url = URL(string: "https://soju-lcd.terra.dev\(url)")!
//
//                var request = URLRequest(url: url)
//        request.httpMethod = httpMethod
//        request.allHTTPHeaderFields = ["Content-Type" : "application/json"]
//        request.httpBody = data
//
//        URLSession.shared.dataTask(with: request) { (data, response, error) in
//            callback(data, response, error)
//        }.resume()
//    }
}
