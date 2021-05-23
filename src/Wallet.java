import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet implements Serializable {

  private static final long serialVersionUID = 166249065006236265L;

  public PrivateKey privateKey; //트랜잭션에 사인을 하는 기능
  public PublicKey publicKey; // 입금을 받는 주소
  public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>(); //이 지갑에 저장되는 잔고 = 블록체인상에 자신의 소유로 저장되어 있는 UTXO 출력값들

  public Wallet() {
    generateKeyPair();
  }

  // Elliptic Curve KeyPair 방식을 사용하여 두 키를 생성
  private void generateKeyPair() {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");

      keyGen.initialize(ecSpec, random);
      KeyPair keyPair = keyGen.generateKeyPair();
      privateKey = keyPair.getPrivate();
      publicKey = keyPair.getPublic();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  //지갑이 갖고 있는 잔고를 리턴하면서, 지갑의 UTXO 구조체를 갱신함
  public float getBalance() {
    float total = 0;
    for (Map.Entry<String, TransactionOutput> item : BlockChain.UTXOs.entrySet()) {
      TransactionOutput UTXO = item.getValue();
      if (UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
        UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions.
        total += UTXO.value;
      }
    }
    return total;
  }

  //지갑으로부터 새 트랜잭션을 생성해냄
  public Transaction sendFunds(PublicKey _recipient, float value) {
    if (getBalance() < value) { //보낼 수 있는 양인지를 검증
      System.out.println("#잔고가 부족합니다. 트랜잭션 무시됨");
      return null;
    }
    //create array list of inputs
    ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

    float total = 0;
    for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
      TransactionOutput UTXO = item.getValue();
      total += UTXO.value;
      inputs.add(new TransactionInput(UTXO.id));
      if (total > value) {
        break; //UTXO 구조를 전부다 확인할 필요 없이 value보다 큰 것이 확인만 되면 바로 종료
      }
    }

    Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
    newTransaction.generateSignature(privateKey);

    for (TransactionInput input : inputs) {
      UTXOs.remove(input.transactionOutputId); //새 Output을 생성했으므로 존재하던 Input 삭제
    }
    return newTransaction;
  }

  public String getAddress() {
    return CryptoUtil.getStringFromKey(publicKey);
  }

}
