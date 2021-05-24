
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class WalletUtils {

  private volatile static WalletUtils instance;
  private final static String WALLET_FILE = "wallet.dat";

  private WalletUtils() {
    initWalletFile();
  }

  public PublicKey findPublicKeyByString(String targetPublicKeyString) {
    for (PublicKey publicKey : getPublicKeys()) {
      String publicKeyString = publicKey.toString();
      int start = publicKeyString.indexOf("[") + 1;
      int end = publicKeyString.indexOf("]");

      if (targetPublicKeyString.equals(publicKeyString.substring(start, end))) {
        return publicKey;
      }
    }

    return null;
  }

  public synchronized static WalletUtils getInstance() {
    if (instance == null) {
      instance = new WalletUtils();
    }
    return instance;
  }

  private void initWalletFile() {
    File file = new File(WALLET_FILE);
    if (!file.exists()) {
      saveToDisk(new Wallets());
    } else {
      loadFromDisk();
    }
  }

  public Set<PublicKey> getPublicKeys() {
    Wallets wallets = loadFromDisk();
    return wallets.getPublicKeys();
  }

  public Wallet getWallet(String publicKeyString) {
    PublicKey publicKey = findPublicKeyByString(publicKeyString);
    return getWallet(publicKey);
  }

  public Wallet getWallet(PublicKey publicKey) {
    Wallets wallets = loadFromDisk();
    return wallets.getWallet(publicKey);
  }

  public Wallet createWallet() {
    Wallet wallet = new Wallet();
    Wallets wallets = loadFromDisk();
    wallets.addWallet(wallet);
    saveToDisk(wallets);
    return wallet;
  }

  private void saveToDisk(Wallets wallets) {
    try {
      if (wallets == null) {
        throw new Exception("Fail to save wallet to file.");
      }
      ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(WALLET_FILE));
      oos.writeObject(wallets);
      oos.close();
    } catch (Exception e) {
      throw new RuntimeException("Fail to save wallet to disk.");
    }
  }

  private Wallets loadFromDisk() {
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(WALLET_FILE));
      Wallets wallets = (Wallets) ois.readObject();
      ois.close();
      return wallets;
    } catch (EOFException e) {
      return new Wallets();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Fail to load wallet from disk.");
    }
  }

  public static class Wallets implements Serializable {

    private static final long serialVersionUID = -2542070981569243131L;

    private Map<PublicKey, Wallet> walletMap = new HashMap<>();

    private void addWallet(Wallet wallet) {
      try {
        walletMap.put(wallet.publicKey, wallet);
      } catch (Exception e) {
        throw new RuntimeException("Fail to add wallet.");
      }
    }

    Set<PublicKey> getPublicKeys() {
      if (walletMap == null) {
        throw new RuntimeException("Fail to get addresses.");
      }
      return walletMap.keySet();
    }

    Wallet getWallet(PublicKey publicKey) {
      Wallet wallet = walletMap.get(publicKey);
      if (wallet == null) {
        throw new RuntimeException("Fail to get wallet.");
      }
      return wallet;
    }
  }

}
