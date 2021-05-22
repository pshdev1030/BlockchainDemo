public class StringUtil {

  public static String getDifficultyString(int difficulty) {
    return new String(new char[difficulty]).replace('\0', '0');
  }

}
