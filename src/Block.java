import java.util.ArrayList;
import java.util.Date;

public class Block {

    public String hash; //본 블록의 해쉬값
    public String previousHash; //이전 블록의 해쉬값
    private String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>(); //our data will be a simple message.
    private long timeStamp;
    private int nonce;

    public Block(String previousHash){
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        String calculatedHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        merkleRoot
        );
        return calculatedHash;
    }

    public void mining(int difficulty){
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        //임시 nonce값을 삽입하며 hash 값을 계속해서 계산하다가, 앞자리수가 난이도보다 낮은 hash값이 검출되면 종료한다.
        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined! hash=" + hash);
    }

    //블럭에 트랜잭션 기록
    public boolean addTransaction(Transaction transaction) {
        //트랜잭션이 유효한지 확인, 제네시스 블록은 제외.
        if(transaction == null) return false;
        if((previousHash != "0")) {
            if((transaction.processTransaction() != true)) {
                System.out.println("트랜잭션 처리에 실패했습니다. 메소드 중지됨.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("트랜잭션이 블록에 기록되었습니다.");
        return true;
    }
}
