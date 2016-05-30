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

    for (int year = END_YEAR; year >= START_YEAR; year--) {

      RandomAccessFile file = new RandomAccessFile(file_name + year + ".txt",
          "r");

      while (true) {


        int PMID_i = Integer.parseInt(file.readLine());
        System.out.println(PMID_i);
        long file_pointer = file.getFilePointer();

        for (int j=1; j <= 100; j++) {
        int PMID_j = Integer.parseInt(file.readLine());
          //TODO: Compare pmid_i and pmid_j
        }

        file.seek(file_pointer);
      }


//      System.out.println(ob);


    }


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