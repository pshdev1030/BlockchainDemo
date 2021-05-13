import java.util.Date;

public class Block {

    public String hash; //본 블록의 해쉬값
    public String previousHash; //이전 블록의 해쉬값
    private String data;
    private long timeStamp;
    private int nonce;

    public Block(String data, String previousHash){
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash(){
        String calculatedHash = StringUtil.applySha256(
                previousHash +
                        Long.toString(timeStamp) +
                        Integer.toString(nonce) +
                        data
        );
        return calculatedHash;
    }

    public void mining(int difficulty){
        String target = new String(new char[difficulty]).replace('\0', '0'); // n자리수 배열 생성
        //임시 nonce값을 삽입하며 hash 값을 계속해서 계산하다가, 앞자리수가 난이도보다 낮은 hash값이 검출되면 종료한다.
        while(!hash.substring(0, difficulty).equals(target)){
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined! hash=" + hash);
    }
}
