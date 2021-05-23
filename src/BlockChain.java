import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {

  public ArrayList<Block> demoChain = new ArrayList<Block>();

  //UTXO = Unspent Transaction Output, 소비되지 않은 트랜잭션 출력값
  //소유자만이 암호를 해제하여 트랜잭션의 입력값으로 사용할 수 있음 (사실상 지갑은 실제로 돈을 저장하고 있는 것이 아님)
  public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
  public int difficulty = 5;
  public static float minimumTransaction = 0.1f;
  public Transaction genesisTransaction;

//  public static void main(String[] args) {
//    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
//
//    String walletPubKey1 = "6b:15:1e:e5:61:62:df:35:80:be:85:ea:ab:78:0b:78:db:77:d6:03";
//    String walletPubKey2 = "93:e8:b0:5c:20:e7:5c:42:d6:f8:3f:59:8d:99:5a:e6:81:47:23:46";
//
//    Wallet coinbase = new Wallet();
//
//    //최초 트랜잭션 생성, 지갑A에 100코인 지급 코드
//    Wallet a = WalletUtils.getInstance().getWallet(walletPubKey1);

//    genesisTransaction = new Transaction(coinbase.publicKey, a.publicKey, 100f, null);
//    genesisTransaction.generateSignature(coinbase.privateKey);   // 최초 트랜잭션은 수동으로 서명
//    genesisTransaction.transactionId = "0"; // 최초 트랜잭션은 수동으로 ID를 설정
//    genesisTransaction.outputs.add(
//        new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
//            genesisTransaction.transactionId)); // 수동으로 OUTPUT에 추가
//    UTXOs.put(genesisTransaction.outputs.get(0).id,
//        genesisTransaction.outputs.get(0)); //블록체인의 장부에 저장
//
//    System.out.println("제네시스 블록 생성 중... ");
//    Block genesis = new Block("0");
//    genesis.addTransaction(genesisTransaction);
//    addBlock(genesis);
//
//    CLI cli = new CLI(args);
//    Command cmd = cli.parse();
//
//    Block block1 = new Block(genesis.hash);
//    Wallet b = WalletUtils.getInstance().getWallet(walletPubKey2);
//
//    System.out.println("\n지갑A 잔고: " + a.getBalance());
//    System.out.println("\n지갑A에서 지갑B로 40 전송..");
//    block1.addTransaction(a.sendFunds(b.publicKey, 40f));
//    addBlock(block1);
//    System.out.println("\n지갑A 잔고: " + a.getBalance());
//    System.out.println("지갑B 잔고: " + b.getBalance());

//    switch (cmd) {
//      case CREATE_BLOCKCHAIN:
//        break;
//      case GET_BALANCE:
//        break;
//      case SEND:
//        break;
//      case CREATE_WALLET:
//        break;
//      case PRINT_PUBLIC_KEYS:
//        break;
//      case PRINT_BLOCKCHAIN:
//        break;
//      default:
//        break;
//    }

//    walletA = new Wallet();
//    walletB = new Wallet();

//

//
//    //testing

//
//    Block block2 = new Block(block1.hash);
//    System.out.println("\n지갑A에서 1000 전송 시도 (잔고 부족)...");
//    block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
//    addBlock(block2);
//    System.out.println("\n지갑A 잔고: " + walletA.getBalance());
//    System.out.println("지갑B 잔고: " + walletB.getBalance());
//
//    Block block3 = new Block(block2.hash);
//    System.out.println("\n지갑B에서 지갑A로 20 전송...");
//    block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
//    System.out.println("\n지갑A 잔고: " + walletA.getBalance());
//    System.out.println("지갑B 잔고: " + walletB.getBalance());
//
//    isChainValid();
//    String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(demoChain);
//    System.out.println(blockchainJson);
//  }

  //블록체인 무결성 검증
  public boolean isChainValid() {
    Block currentBlock;
    Block previousBlock;
    String hashTarget = StringUtil.getDifficultyString(difficulty);
    HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
    tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

    for (int i = 1; i < demoChain.size(); i++) {
      currentBlock = demoChain.get(i);
      previousBlock = demoChain.get(i - 1);

      if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
        System.out.println("#현재 블록의 해쉬값이 변조되었습니다.");
        return false;
      }

      if (!previousBlock.hash.equals(currentBlock.previousHash)) {
        System.out.println("#이전 블록의 해쉬값이 변조되었습니다.");
        return false;
      }

      if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
        System.out.println("#이 블록은 채굴된 블록이 아닙니다.");
        return false;
      }

      //loop thru blockchains transactions:
      TransactionOutput tempOutput;
      for (int t = 0; t < currentBlock.transactions.size(); t++) {
        Transaction currentTransaction = currentBlock.transactions.get(t);

        if (!currentTransaction.verifySignature()) {
          System.out.println("#트랜잭션(" + t + ")의 서명이 유효하지 않습니다.");
          return false;
        }
        if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
          System.out.println("#트랜잭션(" + t + ")의 Input과 Output의 총합이 일치하지 않습니다.");
          return false;
        }

        for (TransactionInput input : currentTransaction.inputs) {
          tempOutput = tempUTXOs.get(input.transactionOutputId);

          if (tempOutput == null) {
            System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
            return false;
          }

          if (input.UTXO.value != tempOutput.value) {
            System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
            return false;
          }

          tempUTXOs.remove(input.transactionOutputId);
        }

        for (TransactionOutput output : currentTransaction.outputs) {
          tempUTXOs.put(output.id, output);
        }

        // 연결된 블록의 정보끼리 비교
        if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
          System.out.println("#트랜잭션(" + t + ") Output의 수신자가 변조되었습니다.");
          return false;
        }
        if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
          System.out.println("#트랜잭션(" + t + ") output 'change' is not sender.");
          return false;
        }

      }
    }
    System.out.println("블록체인이 검증되었습니다.");
    return true;
  }

  public void addBlock(Block newBlock) {
    newBlock.mining(difficulty);
    demoChain.add(newBlock);
  }
}
