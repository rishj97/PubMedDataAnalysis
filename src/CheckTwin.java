import java.util.Scanner;

/**
 * Created by rishabh on 20/12/16.
 */
public class CheckTwin {
  public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    int pmid1 = sc.nextInt();
    int pmid2 = sc.nextInt();

    boolean citationsMatch = analyseCitations(pmid1, pmid2);
    if(citationsMatch) {
      boolean commonAuthor = checkCommonAuthors(pmid1, pmid2);
      if(!commonAuthor) {
        System.out.println("It's a Twin");
        return;
      }
    }
    System.out.println("Nope");
  }

  private static boolean checkCommonAuthors(int pmid1, int pmid2) {
    return false;
  }

  private static boolean analyseCitations(int pmid1, int pmid2) {
    return false;
  }
}
