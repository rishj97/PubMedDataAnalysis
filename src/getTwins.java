import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

public class getTwins {

  static int retstart_init = 0;
  static int retmax_limit = 10000;
  static String file_name = "year_data.txt";


  //TODO: ensure URL connection is estalished only once.

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    int i;
    int START_YEAR = 1996;
    int END_YEAR = 1996;
    for (i = END_YEAR; i >= START_YEAR; i--) {
      loadPapers(i);
    }
  }

  private static void loadPapers(int year) throws IOException, ParserConfigurationException, SAXException {

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    FileWriter file_writer = new FileWriter(file_name);
    PrintWriter print_writer = new PrintWriter( file_writer, true );





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

          if(e.getTextContent().equals("8942774")) {
            System.out.println("its there");
            flag=0;

          }

          print_writer.printf( "%s" + "%n" , e.getTextContent());
        }

    }
    print_writer.close();
  }

  private static String createURL_esearch(int year, int ret_start) {

    String str = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=" + year +
        "[pdat]&retmax="+retmax_limit+"&retstart=" +
        ret_start;
    return str;
  }

}