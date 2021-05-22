import java.util.ArrayList;
import java.util.List;

public class MerkleTree {

  public static String getRoot(List<Transaction> transactions) {
    int count = transactions.size();
    ArrayList<String> previousTreeLayer = new ArrayList<>();
    for (Transaction transaction : transactions) {
      previousTreeLayer.add(transaction.transactionId);
    }
    ArrayList<String> treeLayer = previousTreeLayer;

    while (count > 1) {
      // 해시 레이어 초기화
      treeLayer = new ArrayList<>();

      // 만약 이전 레이어의 크기가 홀수라면 마지막 데이터를 추가함
      int prevCount = previousTreeLayer.size();
      if ((prevCount & 1) == 1) {
        previousTreeLayer.add(previousTreeLayer.get(prevCount - 1));
      }

      // 2개씩 짝지어 해시하며 새로운 레이어에 추가
      for (int i = 1; i < previousTreeLayer.size(); i += 2) {
        treeLayer
            .add(CryptoUtil.applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
      }

      // count가 1이 될 때까지 위 과정을 반복
      count = treeLayer.size();
      previousTreeLayer = treeLayer;
    }

    String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    return merkleRoot;
  }

}
