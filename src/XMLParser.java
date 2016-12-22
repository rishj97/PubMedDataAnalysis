import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rishabh on 22/12/16.
 */
public class XMLParser {
  final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
  DocumentBuilder builder;
  private Element root;

  public XMLParser(InputStream response) throws ParserConfigurationException, IOException, SAXException {
    builder = factory.newDocumentBuilder();
    Document doc = builder.parse(response);
    root = doc.getDocumentElement();
  }

  public Element getRoot() {
    return root;
  }
}
