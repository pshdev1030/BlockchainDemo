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

  //블록체인 무결성 검증
  public boolean isChainValid() {
    Block currentBlock;
    Block previousBlock;
    String hashTarget = StringUtil.getDifficultyString(difficulty);
    HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
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
            System.out.println("# TransactionInput (" + t + ")이 없습니다.");
            return false;
          }

          if (input.UTXO.value != tempOutput.value) {
            System.out.println("# TransactionInput (" + t + ")이 유효하지 않습니다.");
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
          System.out.println("#트랜잭션(" + t + ") Output의 송신자가 변조되어습니다.");
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
