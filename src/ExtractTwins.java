import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
  static String charset = java.nio.charset.StandardCharsets.UTF_8.name();

  static int retstart_init = 0;
  /**
   * number of PMIDs returned at each call
   */
  static int retmax_limit = 10000;

  /**
   * number of papers each paper is compared to
   */
  static int comp_limit = 5;
  static int pmids_limit = 600;

  static String file_name = "year_data";

  static int START_YEAR = 2005;
  static int END_YEAR = 2005;

  static boolean IS_LOADED = true;
  static boolean LOAD_ONLY = false;

  public static void main(String[] args) throws
      IOException, ParserConfigurationException, SAXException {

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
    int i;
    if (!IS_LOADED) {
      for (i = END_YEAR; i >= START_YEAR; i--) {
        loadPapers(i);
        System.out.println("Year " + i + " loaded successfully!"
            + '\t' + '\t' + new Date());
      }
    }
    System.out.println("...finished loading papers"
        + '\t' + '\t' + new Date());

    if (LOAD_ONLY) {
      System.exit(0);
    }

    for (i = END_YEAR; i >= START_YEAR; i--) {
      System.out.println("Started loading citation data for " + i + "..."
          + '\t' + '\t' + new Date());
      ArrayList<Integer> PMIDs = new ArrayList<>();
      ArrayList<NodeList> citationData = new ArrayList<>();
      loadCitationPapers(i, PMIDs, citationData);
      System.out.println("Year " + i + " citation loaded successfully!"
          + '\t' + '\t' + new Date());
      System.out.println("CITATION SIZE = " + citationData.size());
      System.out.println("PMIDs SIZE = " + PMIDs.size());
    }
  }

  private static void loadCitationPapers(int year, ArrayList<Integer> PMIDs,
                                         ArrayList<NodeList> citationData)
      throws IOException, ParserConfigurationException, SAXException {

    String url_citations_i = null;
    Scanner file = new Scanner(new File(file_name + year + ".txt"));
    Integer callNumber = 0;
    boolean reRequest = false;
    while (file.hasNextInt()) {
      int pmidsLength = 0;
      int[] pmids = new int[pmids_limit];
      if (!reRequest) {
        while (pmidsLength < pmids_limit) {
          if (!file.hasNextInt()) {
            System.out.println("File finished with " + callNumber + " calls.");
            break;
          }
          int integer = file.nextInt();
          pmids[pmidsLength] = integer;
          pmidsLength++;
        }
        callNumber++;
        url_citations_i = createURL_citation(pmids);
      }
      InputStream response_i = null;
      try {
        URLConnection connection = new URL(url_citations_i).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        response_i = connection.getInputStream();
        reRequest = false;

      } catch (Exception e) {
        e.printStackTrace();
        reRequest = true;
      }
      XMLParser xml = null;
      try {
        xml = new XMLParser(response_i);
      } catch (Exception e) {
        System.out.println("Response invalid " + pmids[0]);
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
      for (int i = 0; i < pmidsLength; i++) {
        Element linkSet_i = (Element) linkSet_List.item(i);
        if (linkSet_i == null) {
          continue;
        }
        NodeList id_list_i
            = linkSet_i.getElementsByTagName("IdList");

        if (id_list_i.getLength() == 0) {
          continue;
        }
        int pmid = Integer.parseInt(id_list_i.item(0).getTextContent());

        NodeList LinkSetDb_list_i
            = linkSet_i.getElementsByTagName("LinkSetDb");

        if (LinkSetDb_list_i.getLength() == 0) {
          continue;
        }
        Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
        NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
            ("Link");

        if (link_nodes_i.getLength() > 5) {
          PMIDs.add(pmid);
          citationData.add(link_nodes_i);
        }
      }
    }
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

  private static String createURL_citation(int[] pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin";
    for (int i = 0; i < pmids.length; i++) {
      url = url + "&id=" + pmids[i];
    }
    return url;
  }

  private static void loadPapers(int year) throws IOException,
      ParserConfigurationException, SAXException {

    FileWriter file_writer = new FileWriter(file_name + year + ".txt");
    PrintWriter print_writer = new PrintWriter(file_writer, true);


    int flag = 1;
    long retstart = retstart_init;
    while (flag == 1) {
      String url = createURL_esearch(year, retstart);
      retstart += retmax_limit;

      URLConnection connection = new URL(url).openConnection();
      connection.setRequestProperty("Accept-Charset", charset);
      InputStream response = connection.getInputStream();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


      // use the factory to create a documentBuilder
      DocumentBuilder builder = factory.newDocumentBuilder();

      // create a new document from input stream
      Document doc;
      try {
        doc = builder.parse(response);
      } catch (Exception e) {
        System.out.println("Exception occurred while loading papers for " +
            "year " + year + "!!" + '\t' + '\t' + new Date());
        e.printStackTrace();
        retstart -= retmax_limit;
        continue;
      }

      // get the first element
      Element root = doc.getDocumentElement();

      // get all child nodes
      NodeList idList_List = root.getElementsByTagName("IdList");
      Element idList_element = (Element) idList_List.item(0);
      NodeList ids_nodes;
      try {
        ids_nodes = idList_element.getElementsByTagName("Id");
      } catch (Exception e) {
        System.out.println("Exception occurred while loading papers for " +
            "year " + year + "!!" + '\t' + '\t' + new Date());
        System.out.println(e);
        retstart -= retmax_limit;
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
    print_writer.close();
  }

  private static String createURL_esearch(int year, long ret_start) {

    String str = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=" + year +
        "[pdat]&retmax=" + retmax_limit + "&retstart=" +
        ret_start;
    return str;
  }
}
