import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class CLI {

  private final String[] args;
  public Options options = new Options();

  public CLI(String[] args) {
    this.args = args;

    Option helpCmd = Option.builder("help").desc("Show help").build();
    Option publicKey = Option.builder("pubkey").hasArg(true).desc("Source wallet public key")
        .build();
    Option sendFrom = Option.builder("from").hasArg(true).desc("Source wallet public key").build();
    Option sendTo = Option.builder("to").hasArg(true).desc("Destination wallet public key").build();
    Option sendAmount = Option.builder("amount").hasArg(true).desc("Amount to send").build();
    Option nickname = Option.builder("nickname").hasArg().desc("Nickname of wallet").build();

    options.addOption(helpCmd);
    options.addOption(publicKey);
    options.addOption(sendFrom);
    options.addOption(sendTo);
    options.addOption(sendAmount);
    options.addOption(nickname);
  }

  public Command parse() {
    try {
      if (!validateArgs()) {
        throw new Exception("Invalid arguments.");
      }
      switch (args[0]) {
        case "createblockchain":
          return Command.CREATE_BLOCKCHAIN;
        case "getbalance":
          return Command.GET_BALANCE;
        case "send":
          return Command.SEND;
        case "createwallet":
          return Command.CREATE_WALLET;
        case "printwallets":
          return Command.PRINT_WALLET;
        case "printchain":
          return Command.PRINT_BLOCKCHAIN;
        default:
          return Command.HELP;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Command.HELP;
  }

  private boolean validateArgs() {
    return args != null && args.length >= 1;
  }


}
