import java.security.PublicKey;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CLI {

  private String[] args;
  private Options options = new Options();

  enum Command {
    CREATE_BLOCKCHAIN,
    GET_BALANCE,
    SEND,
    CREATE_WALLET,
    PRINT_PUBLIC_KEYS,
    PRINT_BLOCKCHAIN,
    HELP
  }

  public CLI(String[] args) {
    this.args = args;

    Option helpCmd = Option.builder("help").desc("Show help").build();
    options.addOption(helpCmd);

    Option publicKey = Option.builder("pubkey").hasArg(true).desc("Source wallet public key")
        .build();
    Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet public key").build();
    Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet public key").build();
    Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();

    options.addOption(publicKey);
    options.addOption(sendFrom);
    options.addOption(sendTo);
    options.addOption(sendAmount);
  }

  public Command parse() {
    validateArgs();
    try {
      CommandLineParser parser = new DefaultParser();
      CommandLine cmd = parser.parse(options, args);
      switch (args[0]) {
        case "createblockchain":
          return Command.CREATE_BLOCKCHAIN;
        case "getbalance":
          getBalance(cmd);
          return Command.GET_BALANCE;
        case "send":
          return Command.SEND;
        case "createwallet":
          createWallet();
          return Command.CREATE_WALLET;
        case "printpubkeys":
          printPublicKeys();
          return Command.PRINT_PUBLIC_KEYS;
        case "printchain":
          return Command.PRINT_BLOCKCHAIN;
        default:
          help();
          return Command.HELP;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Command.HELP;
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

    PublicKey selectedPublicKey = null;

    Set<PublicKey> publicKeySet = WalletUtils.getInstance().getPublicKeys();
    for (PublicKey publicKey : publicKeySet) {
      String publicKeyString = publicKey.toString();
      int start = publicKeyString.indexOf("[") + 1;
      int end = publicKeyString.indexOf("]");

      if (getBalancePublicKey.equals(publicKeyString.substring(start, end))) {
        selectedPublicKey = publicKey;
        break;
      }
    }

    if (selectedPublicKey == null) {
      System.out.println("Invalid public key.");
    }

    Wallet wallet = WalletUtils.getInstance().getWallet(selectedPublicKey);
    float balance = wallet.getBalance();
    System.out.println(" # 잔고: " + balance);
  }

  private void validateArgs() {
    if (args == null || args.length < 1) {
      help();
    }
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
