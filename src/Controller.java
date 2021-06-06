import com.google.gson.GsonBuilder;
import java.security.PublicKey;
import java.security.Security;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;

public class Controller {

  public static void main(String[] args) {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    Controller controller = new Controller();

    BlockChain blockChain = new BlockChain();
    Wallet coinbase = new Wallet("Coinbase");

    // Example: Wallet 1(100.0f) -> Wallet 2(0.0f) [40.0f]
    String walletNickname1 = "yoonseop";
    String walletNickname2 = "youngkyeong";
    Wallet a = WalletUtils.getInstance().getWallet(walletNickname1);
    Wallet b = WalletUtils.getInstance().getWallet(walletNickname2);

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
          controller.getBalance(cmd);
          break;
        case SEND:
          String sender = cmd.getOptionValue("from");
          String recipient = cmd.getOptionValue("to");
          String value = cmd.getOptionValue("amount");

          if (sender.isEmpty() || recipient.isEmpty() || value.isEmpty()) {
            controller.help();
          }

          controller.sendFunds(sender, recipient, Float.valueOf(value));
          break;
        case CREATE_WALLET:
          String nickname = cmd.getOptionValue("nickname");
          controller.createWallet(nickname);
          break;
        case PRINT_WALLET:
          controller.printWallets();
          break;
        case PRINT_BLOCKCHAIN:
          controller.printBlockChain(blockChain);
          break;
        default:
          controller.help();
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

//    blockChain.isChainValid();
  }

  private void printBlockChain(BlockChain blockChain) {
    String blockchainJson = new GsonBuilder().setPrettyPrinting().create()
        .toJson(blockChain.demoChain);
    System.out.println(blockchainJson);
  }

  private void sendFunds(String sender, String recipient, float value) {
    Wallet senderWallet = WalletUtils.getInstance().getWallet(sender);
    Wallet recipientWallet = WalletUtils.getInstance().getWallet(recipient);
    senderWallet.sendFunds(recipientWallet.publicKey, value);
  }

  private void createWallet(String nickname) {
    Wallet wallet = WalletUtils.getInstance().createWallet(nickname);
    System.out.println(" # Wallet created");
    System.out.println(wallet.nickname);
  }

  private void printWallets() {
    System.out.println(" # Wallet nicknames");
    for (String nickname : WalletUtils.getInstance().getNicknames()) {
      System.out.println(nickname);
    }
  }

  private void getBalance(CommandLine cmd) {
    String nickname = cmd.getOptionValue("nickname");

    if (nickname.isEmpty()) {
      help();
    }

    PublicKey selectedPublicKey = WalletUtils.getInstance()
        .findPublicKeyByNickname(nickname);

    if (selectedPublicKey == null) {
      System.out.println("Invalid public key.");
    }

    Wallet wallet = WalletUtils.getInstance().getWallet(selectedPublicKey);
    float balance = wallet.getBalance();
    System.out.println(" # [" + nickname + "] balance: " + balance);
  }

  private void help() {
    System.out.println("Usage:");
    System.out
        .println("  createwallet : Generates a new key-pair and saves it into the wallet file");
    System.out.println("  printwallets : Print all wallet nicknames");
    System.out.println("  getbalance -nickname 'nickname' : Get balance of nickname");
    System.out.println(
        "  createblockchain -address ADDRESS : Create a blockchain and send genesis block reward to ADDRESS");
    System.out.println("  printchain : Print all the blocks of the blockchain");
    System.out.println(
        "  send -from FROM -to TO -amount AMOUNT : Send AMOUNT of coins from FROM address to TO");
    System.exit(0);
  }

}
