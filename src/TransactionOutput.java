import java.security.PublicKey;

//다음 트랜잭션의 Input이 되어줌.
public class TransactionOutput {

  public String id;
  public PublicKey recipient; // 잔고를 넘겨 받는 대상
  public float value; //the amount of coins they own
  public String parentTransactionId; //이 트랜잭션Output을 생성한 트랜잭션의 ID

  //생성자
  public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
    this.recipient = recipient;
    this.value = value;
    this.parentTransactionId = parentTransactionId;
    this.id = CryptoUtil.applySha256(
        CryptoUtil.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
    System.out.println(CryptoUtil.getStringFromKey(recipient) + ": " + value);
  }

  //트랜잭션의 잔고가 누구의 것인지 참+거짓을 판단
  public boolean isMine(PublicKey publicKey) {
    return (publicKey == recipient);
  }

}