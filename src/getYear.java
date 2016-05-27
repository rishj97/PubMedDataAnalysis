import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class getYear {
  public static void main(String[] args) throws IOException {
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    URLConnection connection = new URL("http://eutils.ncbi.nlm.nih" +
        ".gov/entrez/eutils/esearch" +
        ".fcgi?db=pubmed&term=2011[pdat]&retmax=1000").openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response = connection.getInputStream();

    /*try (Scanner scanner = new Scanner(response)) {
      String responseBody = scanner.useDelimiter("\\A").next();
      System.out.println(responseBody);
    }*/


    // create a new DocumentBuilderFactory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    try {
      // use the factory to create a documentbuilder
      DocumentBuilder builder = factory.newDocumentBuilder();

      // create a new document from input stream
      Document doc = builder.parse(response);

      // get the first element
      Element element = doc.getDocumentElement();

      // get all child nodes
      NodeList nodes = element.getChildNodes();

      // print the text content of each child
      for (int i = 0; i < nodes.getLength(); i++) {
        System.out.println("" + nodes.item(i).getTextContent());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}