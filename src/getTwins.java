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

public class getTwins {

  static int retstart_init = 0;
  static int retmax_limit = 10000;
  static String file_name = "year_data";
  static int START_YEAR = 1996;
  static int END_YEAR = 1996;
  static boolean isLoaded = true;


  //TODO: ensure URL connection is estalished only once.

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    int i;
    //Loading data year by year in different files.
    if (!isLoaded) {
      for (i = END_YEAR; i >= START_YEAR; i--) {
        loadPapers(i);
      }
    }

    //TODO:Going through each file and finding twins.
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


    // use the factory to create a documentBuilder
    DocumentBuilder builder = factory.newDocumentBuilder();


    for (int year = END_YEAR; year >= START_YEAR; year--) {

      RandomAccessFile file = new RandomAccessFile(file_name + year + ".txt",
          "r");
      int count = 0;

      while (true) {


        int PMID_i = Integer.parseInt(file.readLine());
        //System.out.println(count);
        count++;
        long file_pointer = file.getFilePointer();

        String url_citations_i = createURL_citation(PMID_i);

        URLConnection connection = new URL(url_citations_i).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response_i = connection.getInputStream();

        // create a new document from input stream
        Document doc = builder.parse(response_i);


        // get the first element
        Element root = doc.getDocumentElement();
        if (root == null) {
          continue;
        }
        NodeList linkSet_List = root.getElementsByTagName("LinkSet");
        if (linkSet_List == null) {
          continue;
        }
        Element linkSet_i = (Element) linkSet_List.item(0);
        if (linkSet_i == null) {
          continue;
        }
        NodeList LinkSetDb_list_i = linkSet_i.getElementsByTagName("LinkSetDb");
        if (LinkSetDb_list_i.getLength() == 0) {
          continue;
        }
        Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
        NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
            ("Link");

        if (link_nodes_i.getLength() <= 5) {
          continue;
        }

        for (int j = 1; j <= 6; j++) {


          int PMID_j = Integer.parseInt(file.readLine());
          String url_citations_j = createURL_citation(PMID_j);


          connection = new URL(url_citations_j).openConnection();
          connection.setRequestProperty("Accept-Charset", charset);
          //TODO :Exception Handling when connection lost.
          InputStream response_j = connection.getInputStream();
          doc = builder.parse(response_j);
          root = doc.getDocumentElement();

          linkSet_List = root.getElementsByTagName("LinkSet");
          if (linkSet_List.getLength() == 0) {
            continue;
          }
          Element linkSet_j = (Element) linkSet_List.item(0);
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
            System.out.println(PMID_i + "and" + PMID_j + " with value:" + val);
          }


          //TODO: Compare pmid_i and pmid_j
        }
        file.seek(file_pointer);

      }


//      System.out.println(ob);


    }


  }

  private static int compareLinkNodes(NodeList link_nodes_i, NodeList link_nodes_j) {
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
    String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin";
    for (int i = 0; i < pmids.length; i++) {
      url = url + "&id=" + pmids[i];
    }
    url = url + "&tool=my_tool&email=my_email@example.com";

    return url;
  }

  private static void loadPapers(int year) throws IOException, ParserConfigurationException, SAXException {

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    FileWriter file_writer = new FileWriter(file_name + year + ".txt");
    PrintWriter print_writer = new PrintWriter(file_writer, true);


    int flag = 1;
    int retstart = retstart_init;
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
      Document doc = builder.parse(response);


      // get the first element
      Element root = doc.getDocumentElement();

      // get all child nodes
      NodeList idList_List = root.getElementsByTagName("IdList");
      Element idList_element = (Element) idList_List.item(0);
      NodeList ids_nodes = idList_element.getElementsByTagName("Id");
      if (ids_nodes.getLength() == 0) {
        flag = 0;
      }
      for (int j = 0; j < ids_nodes.getLength(); j++) {
        Element e = (Element) ids_nodes.item(j);

//          if(e.getTextContent().equals("8942774")) {
//            System.out.println("its there");
//            flag=0;
//
//          }

        print_writer.printf("%s" + "%n", e.getTextContent());
      }

    }
    print_writer.close();
  }

  private static String createURL_esearch(int year, int ret_start) {

    String str = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=" + year +
        "[pdat]&retmax=" + retmax_limit + "&retstart=" +
        ret_start;
    return str;
  }

}