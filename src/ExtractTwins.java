import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by rishabh on 20/12/16.
 */
public class ExtractTwins {
  static String CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();

  static int RETSTART_INIT = 0;
  /**
   * number of PMIDs returned at each call
   */
  static int RETMAX_LIMIT = 10000;

  /**
   * number of papers each paper is compared to
   */
  static int MINIMUM_CITATIONS_REQUIRED = 6;
  static int COMP_LIMIT = 10;
  static int PMIDS_LIMIT = 600;
  static int NUM_MONTHS = 12;
  static String FILE_NAME = "year_data";

  static int START_YEAR = 2005;
  static int END_YEAR = 2005;

  static boolean IS_LOADED = true;

  public static void main(String[] args) throws
      IOException {

    /**
     * arg[0] -- START_YEAR
     * arg[1] -- END_YEAR
     */
    if (args.length == 2) {
      START_YEAR = Integer.parseInt(args[0]);
      END_YEAR = Integer.parseInt(args[1]);
      IS_LOADED = false;
    }


    /**
     * Loading data year by year in different files.
     */
    System.out.println("Started loading papers..."
        + '\t' + '\t' + new Date());
    int year;
    if (!IS_LOADED) {
      for (year = END_YEAR; year >= START_YEAR; year--) {
        loadPapers(year);
        System.out.println("Year " + year + " loaded successfully!"
            + '\t' + '\t' + new Date());
      }
    }
    System.out.println("...finished loading papers"
        + '\t' + '\t' + new Date());

    for (year = END_YEAR; year >= START_YEAR; year--) {
      PrintWriter writer = new PrintWriter("output" + year + ".txt", "UTF-8");
      Scanner file = new Scanner(new File(FILE_NAME + year + ".txt"));
      System.out.println("Started finding twin-papers for " + year + "..."
          + '\t' + '\t' + new Date());
      ArrayList<Integer> pmids = new ArrayList<>();
      String url = "";
      boolean comeAgain = false;
      while (file.hasNextInt()) {
        if (!comeAgain) {
          pmids = new ArrayList<>();
          while (file.hasNextInt()) {
            int pmid = file.nextInt();
            pmids.add(pmid);
            if (pmids.size() == PMIDS_LIMIT / COMP_LIMIT) {
              break;
            }
          }
          url = createURL_similarArticles(pmids);
        } else {
          comeAgain = false;
        }
        InputStream response;
        try {
          response = getResponse(url);
        } catch (Exception e) {
          System.out.println("Exception at getResponse");
          e.printStackTrace();
          comeAgain = true;
          continue;
        }

        XMLParser xml;
        try {
          xml = new XMLParser(response);
        } catch (Exception e) {
          System.out.println("Response invalid " + pmids.get(0));
          continue;
        }

        Element root = xml.getRoot();
        if (root == null) {
          //TODO: perform informative action
          continue;
        }


        NodeList linkSet_List = root.getElementsByTagName("LinkSet");
        if (linkSet_List == null) {
          //TODO: perform informative action
          continue;
        }
        ArrayList<Integer> pmidsCitation = new ArrayList<>();
        for (int i = 0; i < pmids.size(); i++) {
          Element linkSet_i = (Element) linkSet_List.item(i);
          NodeList LinkSetDb_list_i
              = linkSet_i.getElementsByTagName("LinkSetDb");

          if (LinkSetDb_list_i.getLength() == 0) {
            continue;
          }
          Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
          NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
              ("Link");
          for (int j = 0; j < COMP_LIMIT; j++) {
            try {
              pmidsCitation.add(Integer.valueOf(link_nodes_i.item(j)
                  .getTextContent()));
            } catch (Exception e) {
              //TODO: Do something cleaner
              pmidsCitation.add(11111111);
            }
          }
        }
        url = createURL_citation(pmidsCitation);
        try {
          response = getResponse(url);
        } catch (Exception e) {
          System.out.println("Exception at getResponse");
          e.printStackTrace();
          comeAgain = true;
          continue;
        }
        try {
          xml = new XMLParser(response);
        } catch (Exception e) {
          System.out.println("Response invalid " + pmids.get(0));
          continue;
        }
        root = xml.getRoot();
        if (root == null) {
          //TODO: perform informative action
          continue;
        }

        linkSet_List = root.getElementsByTagName("LinkSet");
        if (linkSet_List == null) {
          //TODO: perform informative action
          continue;
        }
        for (int i = 0; i <= (pmidsCitation.size() - COMP_LIMIT);
             i += COMP_LIMIT) {
          Element linkSet_i = (Element) linkSet_List.item(i);
          if (linkSet_i == null) {
            //TODO: perform informative action
            continue;
          }

          NodeList LinkSetDb_list_i
              = linkSet_i.getElementsByTagName("LinkSetDb");

          if (LinkSetDb_list_i.getLength() == 0) {
            continue;
          }

          Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
          NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
              ("Link");

          if (link_nodes_i.getLength() < MINIMUM_CITATIONS_REQUIRED) {
            continue;
          }

          for (int j = i + 1; j < i + COMP_LIMIT; j++) {

            Element linkSet_j = (Element) linkSet_List.item(j);
            if (linkSet_j == null) {
              continue;
            }
            NodeList LinkSetDb_list_j = linkSet_j.getElementsByTagName
                ("LinkSetDb");
            if (LinkSetDb_list_j.getLength() == 0) {
              continue;
            }
            Element linkSetDb_element_j = (Element) LinkSetDb_list_j.item(0);
            NodeList link_nodes_j = linkSetDb_element_j.getElementsByTagName
                ("Link");

            if (link_nodes_j.getLength() < MINIMUM_CITATIONS_REQUIRED) {
              continue;
            }

            int val = compareLinkNodes(link_nodes_i, link_nodes_j);
            if (val >= 1) {
              if (val
                  >= ((link_nodes_i.getLength()
                  + link_nodes_j.getLength()) / (double) 3)) {

                writer.println(pmidsCitation.get(i) + " and " +
                    pmidsCitation.get(j)
                    + " with " + "value:" + val);
              }
            }
          }
        }
      }
      System.out.println("...finished finding twin-papers for " + year
          + '\t' + '\t' + new Date());
      writer.close();
    }
  }

  private static InputStream getResponse(String url) throws IOException {
    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", CHARSET);
    return connection.getInputStream();
  }

  private static String createURL_similarArticles(ArrayList pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?";
    for (Object pmid : pmids) {
      url += "&id=" + pmid;
    }
    url += "&linkname=pubmed_pubmed";
    return url;
  }


  private static int compareLinkNodes(NodeList link_nodes_i,
                                      NodeList link_nodes_j) {

    int len_i = link_nodes_i.getLength();
    int len_j = link_nodes_j.getLength();
    int val = 0;

    for (int c1 = 0; c1 < len_i; c1++) {
      for (int c2 = 0; c2 < len_j; c2++) {
        if (link_nodes_i.item(c1).getTextContent().equals
            (link_nodes_j.item(c2).getTextContent())) {
          val++;
          break;
        }
      }
    }
    return val;
  }

  private static String createURL_citation(ArrayList pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin";
    for (Object pmid : pmids) {
      url = url + "&id=" + pmid;
    }
    return url;
  }

  private static void loadPapers(int year) throws IOException {

    FileWriter file_writer = new FileWriter(FILE_NAME + year + ".txt");
    PrintWriter print_writer = new PrintWriter(file_writer, true);


    // loading papers month-wise because of return limit of 500000 from Pubmed
    for (int month = 1; month <= NUM_MONTHS; month++) {
      int flag = 1;
      long retStart = RETSTART_INIT;
      while (flag == 1) {
        String url = createURL_eSearch(year, month, retStart);
        retStart += RETMAX_LIMIT;

        InputStream response;
        XMLParser xmlParser;
        try {
          response = getResponse(url);
          xmlParser = new XMLParser(response);
        } catch (Exception e) {
          System.out.println("Exception occurred when getting response while " +
              "loading papers for " +
              "year " + year + "!!" + '\t' + '\t' + new Date
              ());
          e.printStackTrace();
          retStart -= RETMAX_LIMIT;
          continue;
        }

        // get the first element
        Element root = xmlParser.getRoot();

        // get all child nodes
        NodeList idList_List = root.getElementsByTagName("IdList");
        Element idList_element = (Element) idList_List.item(0);
        NodeList ids_nodes;
        try {
          ids_nodes = idList_element.getElementsByTagName("Id");
        } catch (Exception e) {
          System.out.println("Exception occurred when reading response while " +
              "loading papers for " +
              "year " + year + "!!" + '\t' + '\t' + new Date());
          System.out.println(url);
          e.printStackTrace();
          // retStart -= RETMAX_LIMIT;
          // retStart not reverted because exception probably will occur again
          continue;
        }
        if (ids_nodes.getLength() == 0) {
          flag = 0;
        }
        for (int j = 0; j < ids_nodes.getLength(); j++) {
          Element e = (Element) ids_nodes.item(j);
          print_writer.printf("%s" + "%n", e.getTextContent());
        }
      }
    }
    print_writer.close();
  }

  private static String createURL_eSearch(int year, int month, long ret_start) {

    return "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=" + year + "/" + month +
        "[pdat]&retmax=" + RETMAX_LIMIT + "&retstart=" +
        ret_start;
  }
}
