import java.security.*;
import java.util.ArrayList;

//블럭에 추가되는 거래 기록
public class Transaction {

  public String transactionId;
  public PublicKey sender;
  public PublicKey recipient;
  public float value;
  public byte[] signature;

  public ArrayList<TransactionInput> inputs; //보내는 사람이 계좌를 가지고 있는 지를 증명함
  public ArrayList<TransactionOutput> outputs = new ArrayList<>(); // 대상의 주소가 받은 금액을 보여줌 (추후 input으로 쓰임) = 지갑의 잔고가 곧 추후의 outputs가 됨

  private static int sequence = 0;

  public Transaction(PublicKey from, PublicKey to, float value,
      ArrayList<TransactionInput> inputs) {
    this.sender = from;
    this.recipient = to;
    this.value = value;
    this.inputs = inputs;
  }

  private String calculateHash() {
    sequence++;
    return CryptoUtil.applySha256(
        CryptoUtil.getStringFromKey(sender) +
            CryptoUtil.getStringFromKey(recipient) +
            value + sequence
    );
  }

  //서명 생성하기
  public void generateSignature(PrivateKey privateKey) {
    String data =
        CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(recipient) + value;
    signature = CryptoUtil.applyECDSASig(privateKey, data);
  }

  //데이터 검증
  public boolean verifySignature() {
    String data =
        CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(recipient) + value;
    return CryptoUtil.verifyECDSASig(sender, data, signature);
  }

  //트랜잭션이 생성 조건을 만족하면 True를 리턴
  public boolean processTransaction() {
    if (!verifySignature()) {
      System.out.println("#검증되지 않은 트랜잭션 서명입니다.");
      return false;
    }

    //트랜잭션 입력을 모음 (Unspent 잔고들이어야 함):
    for (TransactionInput i : inputs) {
      i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
    }

    //트랜잭션이 유효한지 확인 (한 트랜잭션의 최소량)
    if (getInputsValue() < BlockChain.minimumTransaction) {
      System.out.println("#트랜잭션의 Input 값이 조건에 미치지 못합니다 : " + getInputsValue());
      return false;
    }

    //트랜잭션 Output 생성
    float leftOver = getInputsValue() - value; //남는 잔고 계산:
    transactionId = calculateHash();
    outputs.add(new TransactionOutput(this.recipient, value,
        transactionId)); //수신자와 보낼 값을 명시하여 트랜잭션 Output 생성
    outputs.add(new TransactionOutput(this.sender, leftOver,
        transactionId)); //송신자가 갖는 잔고에서 보내는 값을 차감하여 트랜잭션 Output 생성

    //생성한 트랜잭션 Output을 블록체인 UTXO 장부에 저장
    for (TransactionOutput o : outputs) {
      BlockChain.UTXOs.put(o.id, o);
    }

    //Input으로 부터 Output이 생성되었으니 UTXO에서 기존의 Input을 삭제함
    for (TransactionInput i : inputs) {
      if (i.UTXO == null) {
        continue; //if Transaction can't be found skip it
      }
      BlockChain.UTXOs.remove(i.UTXO.id);
    }
    return true;
  }

  //트랜잭션의 Input UTXO 값들의 총합을 계산 (트랜잭션 최소 요구량을 확인하기 위함)
  public float getInputsValue() {
    float total = 0;
    for (TransactionInput i : inputs) {
      if (i.UTXO == null) {
        continue; //if Transaction can't be found skip it
      }
      total += i.UTXO.value;
    }
    return total;
  }

  //트랜잭션의 Output UTXO 값들의 총합을 계산 (거래 과정에서 변조가 있었는지 Input값과 비교하면서 확인하기 위함)
  public float getOutputsValue() {
    float total = 0;
    for (TransactionOutput o : outputs) {
      total += o.value;
    }
    return total;
  }

}
