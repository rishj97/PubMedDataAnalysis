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
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by rishabh on 20/12/16.
 */
public class ExtractTwins {

  static int retstart_init = 0;
  /**
   * number of PMIDs returned at each call
   */
  static int retmax_limit = 10000;

  /**
   * number of papers each paper is compared to
   */
  static int comp_limit = 5;
  static int pmids_limit = 650;

  static String file_name = "year_data";

  static int START_YEAR = 2005;
  static int END_YEAR = 2005;

  static boolean IS_LOADED = false;
  static boolean LOAD_ONLY = false;

  public static void main(String[] args) throws
      IOException, ParserConfigurationException, SAXException {
    int i = 0;
    /**
     * arg[0] -- START_YEAR
     * arg[1] -- END_YEAR
     * arg[2] -- LOAD_ONLY bool
     */
    if (args.length == 3) {
      START_YEAR = Integer.parseInt(args[0]);
      END_YEAR = Integer.parseInt(args[1]);
      IS_LOADED = false;
    }


    /**
     * Loading data year by year in different files.
     */
    System.out.println("Started loading papers..."
        + '\t' + '\t' + new Date());
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

    HashMap<Integer, InputStream> citationHashMap = new HashMap<>();
    System.out.println("Started loading citation data..."
        + '\t' + '\t' + new Date());
    if (!IS_LOADED) {
      for (i = END_YEAR; i >= START_YEAR; i--) {
        citationHashMap = loadCitationPapers(i);
        System.out.println("Year " + i + " citation loaded successfully!"
            + '\t' + '\t' + new Date());
      }
    }
    System.out.println("...finished loading citation data"
        + '\t' + '\t' + new Date());


    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    System.out.println("Started finding twin-papers..."
        + '\t' + '\t' + new Date());

    DocumentBuilder builder = factory.newDocumentBuilder();

    for (int year = END_YEAR; year >= START_YEAR; year--) {
      boolean connectionEstablished = true;
      long file_pointer = 0;
      int[] pmids = new int[pmids_limit];
      RandomAccessFile file = new RandomAccessFile(file_name + year + ".txt",
          "r");
      int end_of_file = 0;
      int count;
      int null_counter = 0;
      FileWriter file_writer = new FileWriter(file_name + year + "output.txt");
      PrintWriter print_writer = new PrintWriter(file_writer, true);

      while (true) {

        try {
          String url_citations_i;
          if (connectionEstablished) {
            count = 0;

            while (count < pmids_limit) {
              if (count == (pmids_limit - comp_limit)) {
                file_pointer = file.getFilePointer();
              }
              int integer;
              String str = file.readLine();
              if (str == null) {
                end_of_file = 1;
                break;
              }
              integer = Integer.parseInt(str);
              pmids[count] = integer;
              count++;
            }

          }
          url_citations_i = createURL_citation(pmids);

          System.out.println("Called at " + new Date());

          URLConnection connection = new URL(url_citations_i).openConnection();
          connection.setRequestProperty("Accept-Charset", charset);
          InputStream response_i = connection.getInputStream();

          System.out.println("Received at " + new Date());

          if (!connectionEstablished) {
            System.out.println("***Connection re-established successfully!***"
                + '\t' + '\t' + new Date());
            connectionEstablished = true;
          }

          Document doc = builder.parse(response_i);
          Element root = doc.getDocumentElement();
          if (root == null) {
            null_counter++;
            continue;
          }

          NodeList linkSet_List = root.getElementsByTagName("LinkSet");
          if (linkSet_List == null) {
            null_counter++;
            continue;
          }

          for (i = 0; i < (pmids.length - comp_limit); i++) {
            Element linkSet_i = (Element) linkSet_List.item(i);
            if (linkSet_i == null) {
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

            if (link_nodes_i.getLength() <= 5) {
              continue;
            }


            for (int j = i + 1; j < i + 1 + comp_limit; j++) {

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

              if (link_nodes_j.getLength() <= 5) {
                continue;
              }

              int val = compareLinkNodes(link_nodes_i, link_nodes_j);
              if (val >= 1) {
                if (val
                    >= ((link_nodes_i.getLength()
                    + link_nodes_j.getLength()) / 3)) {

                  print_writer.println(pmids[i] + " and " + pmids[j]
                      + " with " + "value:" + val);

                }
              }


            }
          }
          if (end_of_file == 1) {
            break;
          }
          file.seek(file_pointer);

        } catch (UnknownHostException e) {

          if (connectionEstablished) {
            System.out.println("!!!NETWORK CONNECTION LOST!!!"
                + '\t' + '\t' + new Date());
            connectionEstablished = false;

          }

        } catch (Exception e) {


          System.out.println(e);
          System.out.println(year + " PMID=" + pmids[1] + " i=" + i);
          System.out.println("Null Counter= " + null_counter
              + '\t' + '\t' + new Date());

        }
      }
      System.out.println("Year " + year + " Null counter = " + null_counter);
      System.out.println("...finished finding twin-papers for this year!"
          + '\t' + '\t' + new Date());
    }
    System.out.println("...all twin-papers found successfully!!"
        + '\t' + '\t' + new Date());
  }

  private static HashMap<Integer, InputStream> loadCitationPapers(int year) throws IOException {
    HashMap<Integer, InputStream> citationHashMap = new HashMap<>();
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    String url_citations_i;
    Scanner file = new Scanner(file_name + year + ".txt");
    Integer callNumber = 0;
    while (file.hasNextLine()) {
      int count = 0;
      int[] pmids = new int[pmids_limit];
      while (count < pmids_limit) {
        int integer;
        if(!file.hasNextLine()) {
          break;
        }
        String str = file.nextLine();
        if (str == null) {
          break;
        }
        integer = Integer.parseInt(str);
        pmids[count] = integer;
        count++;
      }
      callNumber++;

      url_citations_i = createURL_citation(pmids);


      URLConnection connection = new URL(url_citations_i).openConnection();
      connection.setRequestProperty("Accept-Charset", charset);
      InputStream response_i = connection.getInputStream();

      citationHashMap.put(callNumber, response_i);
    }


    return citationHashMap;
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

  private static String createURL_citation(int pmid) {
    String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin&id=" + pmid +
        "&tool" +
        "=my_tool&email=my_email@example.com";

    return url;
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

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

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
