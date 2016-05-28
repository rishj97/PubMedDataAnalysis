import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class getYear {


  //TODO: ensure URL connection is estalished only once.


  static String URLConnection;
  String charset = java.nio.charset.StandardCharsets.UTF_8.name();


  public static void main(String[] args) throws IOException {
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    String url = createURL("esearch", "pubmed", "term=2011[pdat]&retmax=10");


    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response = connection.getInputStream();

//    try (Scanner scanner = new Scanner(response)) {
//      String responseBody = scanner.useDelimiter("\\A").next();
//      System.out.println(responseBody);
//    }


    url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin&id=10557283&tool=my_tool&email=my_email@example.com";


    connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    response = connection.getInputStream();

//    try (Scanner scanner = new Scanner(response)) {
//      String responseBody = scanner.useDelimiter("\\A").next();
//      System.out.println(responseBody);
//    }

    // create a new DocumentBuilderFactory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      // use the factory to create a documentBuilder
      DocumentBuilder builder = factory.newDocumentBuilder();

      // create a new document from input stream
      Document doc = builder.parse(response);

      // get the first element
      Element root = doc.getDocumentElement();

      System.out.println(root);

      // get all child nodes
      NodeList nodes = root.getChildNodes();
      System.out.println(nodes.getLength());
      System.out.println(nodes.item(0));
      System.out.println(nodes.item(1));
      System.out.println(nodes.item(2));

      // print the text content of each child
      for (int i = 0; i < nodes.getLength(); i++) {
        System.out.println("" + nodes.item(i).getTextContent());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
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