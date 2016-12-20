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
 * Created by rishabh on 20/12/16.
 */
public class CheckTwin {
  static String charset = java.nio.charset.StandardCharsets.UTF_8.name();

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    Scanner sc = new Scanner(System.in);
    int pmid1 = sc.nextInt();
    int pmid2 = sc.nextInt();
    boolean commonAuthor = checkCommonAuthors(new int[]{pmid1, pmid2});

    if (!commonAuthor) {
      boolean citationsMatch = checkCitationsMatch(new int[]{pmid1, pmid2});
      if (citationsMatch) {
        System.out.println("It's a Twin");
      } else {
        System.out.println("Citation problem dude");
      }
    } else {
      System.out.println("Author problems dude");
    }

  }

  private static boolean checkCommonAuthors(int[] pmidPair) throws IOException, ParserConfigurationException, SAXException {
    String url = createURL_paperInfo(pmidPair);
    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response = connection.getInputStream();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(response);

    Element root = doc.getDocumentElement();
    if (root == null) {
      System.out.println("Null object returned for author analysis");
      return false;
    }

    NodeList documentSummarySet_list = root.getElementsByTagName
        ("DocumentSummarySet");
    if (documentSummarySet_list == null) {
      System.out.println("Null object returned for author analysis");
      return false;
    }

    Element documentSummarySet_element = (Element) documentSummarySet_list
        .item
            (0);
    if (documentSummarySet_element == null) {
      System.out.println("Null object returned for author analysis");
      return false;
    }
    NodeList documentSummaries_list = documentSummarySet_element
        .getElementsByTagName
            ("DocumentSummary");

    Element doc1 = (Element) documentSummaries_list.item(0);
    Element doc2 = (Element) documentSummaries_list.item(1);

    NodeList authors1 = ((Element) doc1.getElementsByTagName
        ("Authors").item(0)).getElementsByTagName("Author");

    NodeList authors2 = ((Element) doc2.getElementsByTagName
        ("Authors").item(0)).getElementsByTagName("Author");

    int authors1Length = authors1.getLength();
    int authors2Length = authors2.getLength();
    boolean noAuthorCommon = true;

    int i, j;
    for (i = 0; i < authors1Length; i++) {
      for (j = 0; j < authors2Length; j++) {
        if (authors1.item(i).getTextContent().equals(authors2.item(j)
            .getTextContent())) {
          noAuthorCommon = false;
          break;
        }
      }
      if (!noAuthorCommon) {
        break;
      }
    }

    return !noAuthorCommon;
  }
  private static String createURL_paperInfo(int[] pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary" +
        ".fcgi?db=pubmed&id=";
    int i = 0;
    for (; i < pmids.length - 1; i++) {
      url = url + pmids[i] + ",";
    }
    url = url + pmids[i] + "&version=2.0";

    return url;
  }

  private static boolean checkCitationsMatch(int[] pmidPair) throws
      IOException, ParserConfigurationException, SAXException {
    //get citations list for both
    String url1 = createURL_citation(pmidPair);
    URLConnection connection = new URL(url1).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    InputStream response_i = connection.getInputStream();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(response_i);
    Element root = doc.getDocumentElement();

    NodeList linkSet_List = root.getElementsByTagName("LinkSet");
    if (linkSet_List == null) {
      return false;
    }

    Element linkSet_i = (Element) linkSet_List.item(0);
    if (linkSet_i == null) {
      return false;
    }

    NodeList LinkSetDb_list_i
        = linkSet_i.getElementsByTagName("LinkSetDb");

    if (LinkSetDb_list_i.getLength() == 0) {
      return false;
    }

    Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
    NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
        ("Link");

    if (link_nodes_i.getLength() <= 5) {
      return false;
    }


    Element linkSet_j = (Element) linkSet_List.item(1);
    if (linkSet_j == null) {
      return false;
    }
    NodeList LinkSetDb_list_j = linkSet_j.getElementsByTagName
        ("LinkSetDb");
    if (LinkSetDb_list_j.getLength() == 0) {
      return false;
    }
    Element linkSetDb_element_j = (Element) LinkSetDb_list_j.item(0);
    NodeList link_nodes_j = linkSetDb_element_j.getElementsByTagName
        ("Link");

    if (link_nodes_j.getLength() <= 5) {
      return false;
    }

    double val = compareLinkNodes(link_nodes_i, link_nodes_j);
    if (val >= 1) {
      if (val
          >= ((link_nodes_i.getLength()
          + link_nodes_j.getLength()) /3)) {

        System.out.println(val);
        return true;

      }
    }
    return false;
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
}
