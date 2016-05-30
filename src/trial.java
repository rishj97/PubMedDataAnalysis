import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by rishabh on 29/05/16.
 */
public class trial {

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


    // use the factory to create a documentBuilder
    DocumentBuilder builder = factory.newDocumentBuilder();
    String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin&id=" + 25121335 +
        "&tool" +
        "=my_tool&email=my_email@example.com";


    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response = connection.getInputStream();

    InputStream response_i = connection.getInputStream();

    // create a new document from input stream
    Document doc = builder.parse(response_i);


    // get the first element
    Element root = doc.getDocumentElement();

    NodeList linkSet_List = root.getElementsByTagName("LinkSet");
    Element linkSet_i = (Element) linkSet_List.item(0);
    NodeList LinkSetDb_list_i = linkSet_i.getElementsByTagName("LinkSetDb");

    Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
    NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
        ("Link");
    int len_i = link_nodes_i.getLength();
    System.out.println(len_i);
    for (int c1 = 0; c1 < len_i; c1++) {
      Element e = (Element) link_nodes_i.item(c1);

      System.out.println(link_nodes_i.item(c1).getTextContent());
    }

//    String url = createURL("esearch", "pubmed", "term=2011[pdat]&retmax=1000");
//
//
//    URLConnection connection = new URL(url).openConnection();
//    connection.setRequestProperty("Accept-Charset", charset);
//    InputStream response = connection.getInputStream();

//    try (Scanner scanner = new Scanner(response)) {
//      String responseBody = scanner.useDelimiter("\\A").next();
//      System.out.println(responseBody);
//    }


//    url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
//        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin&id=10557283&tool=my_tool&email=my_email@example.com";
//
//
//    connection = new URL(url).openConnection();
//    connection.setRequestProperty("Accept-Charset", charset);
//    response = connection.getInputStream();

//    try (Scanner scanner = new Scanner(response)) {
//      String responseBody = scanner.useDelimiter("\\A").next();
//      System.out.println(responseBody);
//    }

    // create a new DocumentBuilderFactory
//    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//
//    try {
//      // use the factory to create a documentBuilder
//      DocumentBuilder builder = factory.newDocumentBuilder();
//
//      // create a new document from input stream
//      Document doc = builder.parse(response);
//
//
//      // get the first element
//      Element root = doc.getDocumentElement();
//
//      System.out.println(root.getTagName());
//
//      // get all child nodes
//      NodeList idList_List = root.getElementsByTagName("IdList");
//      Element idList_element = (Element) idList_List.item(0);
//      NodeList ids_nodes = idList_element.getElementsByTagName("Id");
//      for (int i = 0; i < ids_nodes.getLength(); i++) {
//        Element e = (Element) ids_nodes.item(i);
//        System.out.println(e.getTextContent());
//      }


//      System.out.println(nodes.getLength());
//      System.out.println(nodes.item(0));
//      System.out.println(nodes.item(1));
//      System.out.println(nodes.item(2));

      // print the text content of each child
//      for (int i = 0; i < nodes.getLength(); i++) {
//        System.out.println("" + nodes.item(i).getTextContent());
//      }
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
  }


  static String createURL(String e_type, String data_base, String appender) {
    String str = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/" + e_type + "" +
        ".fcgi?db=" + data_base + "&" + appender;
    return str;
  }


  static Scanner getCitations(String pmid) throws IOException {

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();


    String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin&" + pmid + "&tool" +
        "=my_tool&email=my_email@example.com";


    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response = connection.getInputStream();

    try (Scanner scanner = new Scanner(response)) {
      return scanner;
    }


  }

}
