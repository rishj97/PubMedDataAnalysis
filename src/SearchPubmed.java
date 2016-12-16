import com.algosome.eutils.EntrezSearch;

import java.io.IOException;

/**
 * Created by rishabh on 15/12/16.
 */
public class SearchPubmed {
  public static void main(String[] args) throws IOException {
    EntrezSearch search = new EntrezSearch();
    search.setDefaultParameters();
    search.setDatabase(EntrezSearch.DB_PUBMED);
    search.setDateType(EntrezSearch.FIELD_PDAT);
    search.setMinDate("1958/01/01");
    search.setMaxDate("1958/01/31");
    search.setESearchOutput("years_stats.txt");
    search.doQuery();

//    EntrezFetch fetch = new EntrezFetch();
//    fetch.setDatabase("pubmed");
//    fetch.setDateType("pdat");
//    fetch.setMinDate("1958/01/01");
//    fetch.setMaxDate("1958/01/31");
//    fetch.setESearchOutput("years_stats.txt");
//    fetch.doQuery();

  }
}
