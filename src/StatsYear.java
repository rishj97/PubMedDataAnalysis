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

/**
 * Created by rishabh on 16/10/16.
 */
public class StatsYear {
  static String file_name = "years_stats";
  static int START_YEAR = 1958;
  static int END_YEAR = 2015;

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    int i;

    if (args.length == 2) {
      START_YEAR = Integer.parseInt(args[0]);
      END_YEAR = Integer.parseInt(args[1]);
    }

    String charset = java.nio.charset.StandardCharsets.UTF_8.name();

    FileWriter file_writer = new FileWriter(file_name + ".txt");
    PrintWriter print_writer = new PrintWriter(file_writer, true);

    print_writer.println("YEAR" + '\t' + "NUMBER OF PAPERS CONSIDERED");

    for (i = END_YEAR; i >= START_YEAR; i--) {
      String url = createURL_esearch_count(i);
      URLConnection connection = new URL(url).openConnection();
      connection.setRequestProperty("Accept-Charset", charset);
      InputStream response = connection.getInputStream();

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      Document doc = builder.parse(response);

      Element root = doc.getDocumentElement();

      NodeList count_list = root.getElementsByTagName("Count");
      Element count_element = (Element) count_list.item(0);
      int count_year = Integer.parseInt(count_element.getTextContent());
      print_writer.println(i + "\t" + count_year);

    }

    print_writer.close();
  }

  private static String createURL_esearch_count(int year) {

    String str = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=" + year +
        "[pdat]";
    return str;
  }
}
