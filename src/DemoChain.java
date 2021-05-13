import java.security.Security;
import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class DemoChain {
    public static ArrayList<Block> demoChain = new ArrayList<Block>();
    public static int difficulty = 5;
    public static Wallet walletA;
    public static Wallet walletB;

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        walletA = new Wallet();
        walletB = new Wallet();

        System.out.println("Private and public keys:");
        System.out.println(StringUtil.getStringFromKey(walletA.getPrivateKey()));
        System.out.println(StringUtil.getStringFromKey(walletA.publicKey));

        Transaction transaction = new Transaction(walletA.publicKey, walletB.publicKey, 5, null);
        transaction.generateSignature(walletA.getPrivateKey());

        System.out.print("Is signature verified : ");
        System.out.println(transaction.verifiySignature());

//        demoChain.add(new Block("It's the first block", "0"));
//        demoChain.get(0).mining(difficulty);
//
//        demoChain.add(new Block("It's the second block", demoChain.get(demoChain.size()-1).hash));
//        demoChain.get(1).mining(difficulty);
//
//        demoChain.add(new Block("It's the thrid block", demoChain.get(demoChain.size()-1).hash));
//        demoChain.get(2).mining(difficulty);
//
//        System.out.println("\nBlockchain is Valid:" + isChainValid());
//
//        String chainJson = new GsonBuilder().setPrettyPrinting().create().toJson(demoChain);
//        System.out.println(chainJson);
    }

    public static Boolean isChainValid(){
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        for(int i=1; i<demoChain.size(); i++){
            currentBlock = demoChain.get(i);
            previousBlock = demoChain.get(i-1);

            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("Current hashes not equal");
                return false;
            }

            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("Previous Hashes unequal");
                return false;
            }

            if(!currentBlock.hash.substring(0, difficulty).equals(hashTarget)){
                System.out.println("This block hasn't been mined");
                return false;
            }
        }
        return true;
    }
}
