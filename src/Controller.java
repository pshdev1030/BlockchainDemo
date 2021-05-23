import com.google.gson.GsonBuilder;
import java.security.PublicKey;
import java.security.Security;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

public class Controller {

  public static void main(String[] args) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    Controller c = new Controller();

    BlockChain blockChain = new BlockChain();
    Wallet coinbase = new Wallet();

    // Example: Wallet 1(100.0f) -> Wallet 2(0.0f) [40.0f]
    String walletPubKey1 = "6b:15:1e:e5:61:62:df:35:80:be:85:ea:ab:78:0b:78:db:77:d6:03";
    String walletPubKey2 = "93:e8:b0:5c:20:e7:5c:42:d6:f8:3f:59:8d:99:5a:e6:81:47:23:46";
    Wallet a = WalletUtils.getInstance().getWallet(walletPubKey1);
    Wallet b = WalletUtils.getInstance().getWallet(walletPubKey2);

    blockChain.genesisTransaction = new Transaction(coinbase.publicKey, a.publicKey, 100f, null);
    blockChain.genesisTransaction.generateSignature(coinbase.privateKey);   // 최초 트랜잭션은 수동으로 서명
    blockChain.genesisTransaction.transactionId = "0"; // 최초 트랜잭션은 수동으로 ID를 설정
    blockChain.genesisTransaction.outputs.add(
        new TransactionOutput(blockChain.genesisTransaction.recipient,
            blockChain.genesisTransaction.value,
            blockChain.genesisTransaction.transactionId)); // 수동으로 OUTPUT에 추가
    BlockChain.UTXOs.put(blockChain.genesisTransaction.outputs.get(0).id,
        blockChain.genesisTransaction.outputs.get(0)); //블록체인의 장부에 저장

    Block genesis = new Block("0");
    genesis.addTransaction(blockChain.genesisTransaction);
    blockChain.addBlock(genesis);

    Block block1 = new Block(genesis.hash);

    System.out.println("\n지갑A 잔고: " + a.getBalance());
    System.out.println("\n지갑A에서 지갑B로 40 전송..");
    block1.addTransaction(a.sendFunds(b.publicKey, 40f));
    blockChain.addBlock(block1);
    System.out.println("\n지갑A 잔고: " + a.getBalance());
    System.out.println("지갑B 잔고: " + b.getBalance());

    CLI cli = new CLI(args);
    Command command = cli.parse();

    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(cli.options, args);

      switch (command) {
        case CREATE_BLOCKCHAIN:

          break;
        case GET_BALANCE:
          c.getBalance(cmd);
          break;
        case SEND:
          String sender = cmd.getOptionValue("from");
          String recipient = cmd.getOptionValue("to");
          String value = cmd.getOptionValue("amount");

          if (sender.isEmpty() || recipient.isEmpty() || value.isEmpty()) {
            c.help();
          }

          c.sendFunds(sender, recipient, Float.valueOf(value));
          break;
        case CREATE_WALLET:
          c.createWallet();
          break;
        case PRINT_PUBLIC_KEYS:
          c.printPublicKeys();
          break;
        case PRINT_BLOCKCHAIN:
          break;
        default:
          c.help();
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

    blockChain.isChainValid();
    String blockchainJson = new GsonBuilder().setPrettyPrinting().create()
        .toJson(blockChain.demoChain);
    System.out.println(blockchainJson);
  }

  private void sendFunds(String sender, String recipient, Float value) {
    PublicKey senderPublicKey = WalletUtils.getInstance().findPublicKeyByString(sender);
    PublicKey recipientPublicKey = WalletUtils.getInstance().findPublicKeyByString(recipient);
  }

  private void createWallet() {
    Wallet wallet = WalletUtils.getInstance().createWallet();
    System.out.println(" # 지갑 생성");
    System.out.println(wallet.publicKey);
  }

  private void printPublicKeys() {
    System.out.println(" # Wallet Public Keys");
    for (PublicKey publicKey : WalletUtils.getInstance().getPublicKeys()) {
      System.out.println(publicKey);
    }
  }

  private void getBalance(CommandLine cmd) {
    String getBalancePublicKey = cmd.getOptionValue("pubkey");

    if (getBalancePublicKey.isEmpty()) {
      help();
    }

    PublicKey selectedPublicKey = WalletUtils.getInstance()
        .findPublicKeyByString(getBalancePublicKey);

    if (selectedPublicKey == null) {
      System.out.println("Invalid public key.");
    }

    Wallet wallet = WalletUtils.getInstance().getWallet(selectedPublicKey);
    float balance = wallet.getBalance();
    System.out.println(" # 잔고: " + balance);
  }

  private void help() {
    System.out.println("Usage:");
    System.out
        .println("  createwallet : Generates a new key-pair and saves it into the wallet file");
    System.out.println("  printpubkeys : Print all wallet public keys");
    System.out.println("  getbalance -pubkey 'pubkey' : Get balance of public key");
    System.out.println(
        "  createblockchain -address ADDRESS : Create a blockchain and send genesis block reward to ADDRESS");
    System.out.println("  printchain : Print all the blocks of the blockchain");
    System.out.println(
        "  send -from FROM -to TO -amount AMOUNT : Send AMOUNT of coins from FROM address to TO");
    System.exit(0);
  }

}
