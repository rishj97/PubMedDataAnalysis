import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by rishabh on 20/12/16.
 */
public class ExtractTwins {
  static String charset = java.nio.charset.StandardCharsets.UTF_8.name();

  static int retstart_init = 0;
  /**
   * number of PMIDs returned at each call
   */
  static int retmax_limit = 10000;

  /**
   * number of papers each paper is compared to
   */
  static int comp_limit = 10;
  static int pmids_limit = 600;

  static String file_name = "year_data";

  static int START_YEAR = 2005;
  static int END_YEAR = 2005;

  static boolean IS_LOADED = true;
  static boolean LOAD_ONLY = false;

  public static void main(String[] args) throws
      IOException, ParserConfigurationException, SAXException, TransformerException {

    /**
     * arg[0] -- START_YEAR
     * arg[1] -- END_YEAR
     */
    if (args.length == 2) {
      START_YEAR = Integer.parseInt(args[0]);
      END_YEAR = Integer.parseInt(args[1]);
      IS_LOADED = false;
    }


    /**
     * Loading data year by year in different files.
     */
    System.out.println("Started loading papers..."
        + '\t' + '\t' + new Date());
    int year;
    if (!IS_LOADED) {
      for (year = END_YEAR; year >= START_YEAR; year--) {
        loadPapers(year);
        System.out.println("Year " + year + " loaded successfully!"
            + '\t' + '\t' + new Date());
      }
    }
    System.out.println("...finished loading papers"
        + '\t' + '\t' + new Date());

    if (LOAD_ONLY) {
      System.exit(0);
    }

    for (year = END_YEAR; year >= START_YEAR; year--) {
      Scanner file = new Scanner(new File(file_name + year + ".txt"));
      while (file.hasNextInt()) {
        ArrayList<Integer> pmids = new ArrayList<>();
        int pmidsLength = 0;
        while (file.hasNextInt()) {
          int pmid = file.nextInt();
          pmids.add(pmid);
          pmidsLength++;
          if (pmidsLength == pmids_limit / comp_limit) {
            break;
          }
        }
        String url = createURL_similarArticles(pmids);
        InputStream response = null;
        try {
          response = getResponse(url);
        } catch (Exception e) {
          //TODO:Do Something
        }

        XMLParser xml;
        try {
          xml = new XMLParser(response);
        } catch (Exception e) {
          System.out.println("Response invalid " + pmids.get(0));
          continue;
        }

        Element root = xml.getRoot();
        if (root == null) {
          //TODO: perform informative action
          continue;
        }


        NodeList linkSet_List = root.getElementsByTagName("LinkSet");
        if (linkSet_List == null) {
          //TODO: perform informative action
          continue;
        }
        ArrayList<Integer> pmidsCitation = new ArrayList<>();
        for (int i = 0; i < pmids.size(); i++) {
          Element linkSet_i = (Element) linkSet_List.item(0);
          NodeList LinkSetDb_list_i
              = linkSet_i.getElementsByTagName("LinkSetDb");

          if (LinkSetDb_list_i.getLength() == 0) {
            continue;
          }
          Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
          NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
              ("Link");
          for (int j = 0; j < comp_limit; j++) {
            try {
              pmidsCitation.add(Integer.valueOf(link_nodes_i.item(j)
                  .getTextContent()));
            } catch (Exception e) {
              pmidsCitation.add(11111111);
            }
          }
        }
        url = createURL_citation(pmids);
        response = null;
        try {
          response = getResponse(url);
        } catch (Exception e) {
          //TODO: Do Something
        }
        try {
          xml = new XMLParser(response);
        } catch (Exception e) {
          System.out.println("Response invalid " + pmids.get(0));
          continue;
        }
        root = xml.getRoot();
        if (root == null) {
          //TODO: perform informative action
          continue;
        }

        linkSet_List = root.getElementsByTagName("LinkSet");
        if (linkSet_List == null) {
          //TODO: perform informative action
          continue;
        }

        for (int i = 0; i <= (pmidsCitation.size() - comp_limit);
             i += comp_limit) {
          Element linkSet_i = (Element) linkSet_List.item(i);
          if (linkSet_i == null) {
            //TODO: perform informative action
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


          for (int j = i + 1; j < i + comp_limit; j++) {

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
                  + link_nodes_j.getLength()) / (double) 3)) {

                System.out.println(pmidsCitation.get(i) + " and " +
                    pmidsCitation.get(j)
                    + " with " + "value:" + val);

              }
            }


          }
        }

      }
    }


    System.exit(0);
    for (year = END_YEAR; year >= START_YEAR; year--)

    {
      System.out.println("Started loading citation data for " + year + "..."
          + '\t' + '\t' + new Date());

      loadCitationPapers(year);

      System.out.println("Year " + year + " citation loaded successfully!"
          + '\t' + '\t' + new Date());
    }

  }

  private static InputStream getResponse(String url) throws IOException {
    URLConnection connection = new URL(url).openConnection();
    connection.setRequestProperty("Accept-Charset", charset);
    return connection.getInputStream();
  }

  private static String createURL_similarArticles(ArrayList<Integer> pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?";
    for (int pmid : pmids) {
      url += "&id=" + pmid;
    }
    url += "&linkname=pubmed_pubmed";
    return url;
  }

  private static void loadCitationPapers(int year)
      throws FileNotFoundException {

    int numPapers = 0;
    boolean openNewFile;
    int fileNumber = 1;

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    Document doc;
    Element rootElement;

    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    // root elements
    doc = docBuilder.newDocument();
    rootElement = doc.createElement("CitationData");
    doc.appendChild(rootElement);

    openNewFile = false;

    String url_citations_i = null;
    Scanner file = new Scanner(new File(file_name + year + ".txt"));
    Integer callNumber = 0;
    boolean reRequest = false;
    while (file.hasNextInt()) {
      if (openNewFile) {
        openNewFile = false;

        //close previous file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
          transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
          e.printStackTrace();
        }
        DOMSource source = new DOMSource(doc);
        String name = "year" + year + "Citation" + fileNumber + ".xml";
        System.out.println("Closing file " + name + " ..."
            + '\t' + '\t' + new Date());
        System.out.println("Number of papers loaded = " + numPapers);
        fileNumber++;
        StreamResult result = new StreamResult(new File(name));

//       Output to console for testing
//       StreamResult result = new StreamResult(System.out);

        try {
          transformer.transform(source, result);
        } catch (TransformerException e) {
          e.printStackTrace();
        }


        //create new file
        System.out.println("Opening new file..."
            + '\t' + '\t' + new Date());
        System.out.println("Number of papers loaded = " + numPapers);
        try {
          docBuilder = docFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
          e.printStackTrace();
        }

        // root elements
        doc = docBuilder.newDocument();
        rootElement = doc.createElement("CitationData");
        doc.appendChild(rootElement);
      }
      int pmidsLength = 0;
      int[] pmids = new int[pmids_limit];
      if (!reRequest) {
        while (pmidsLength < pmids_limit) {
          if (!file.hasNextInt()) {
            System.out.println("File finished with " + callNumber + " calls " +
                "and " + (fileNumber) + " files");
            System.out.println("Number of papers loaded = " + numPapers);
            break;
          }
          int integer = file.nextInt();
          pmids[pmidsLength] = integer;
          pmidsLength++;
        }
        callNumber++;
        url_citations_i = createURL_citation(pmids);
      }
      InputStream response_i = null;
      try {
        URLConnection connection = new URL(url_citations_i).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        response_i = connection.getInputStream();
        reRequest = false;

      } catch (Exception e) {
        e.printStackTrace();
        reRequest = true;
      }
      XMLParser xml;
      try {
        xml = new XMLParser(response_i);
      } catch (Exception e) {
        System.out.println("Response invalid " + pmids[0]);
        continue;
      }
      Element root = xml.getRoot();
      if (root == null) {
        //TODO: perform informative action
        continue;
      }
      NodeList linkSet_List = root.getElementsByTagName("LinkSet");
      if (linkSet_List == null) {
        //TODO: perform informative action
        continue;
      }
      for (int i = 0; i < pmidsLength; i++) {
        Element linkSet_i = (Element) linkSet_List.item(i);
        if (linkSet_i == null) {
          continue;
        }
        NodeList id_list_i
            = linkSet_i.getElementsByTagName("IdList");

        if (id_list_i.getLength() == 0) {
          continue;
        }
        int pmid = Integer.parseInt(id_list_i.item(0).getTextContent());

        NodeList LinkSetDb_list_i
            = linkSet_i.getElementsByTagName("LinkSetDb");

        if (LinkSetDb_list_i.getLength() == 0) {
          continue;
        }
        Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
        NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
            ("Link");

        if (link_nodes_i.getLength() > 5) {
          try {
            Node firstDocImportedNode = doc.importNode(linkSet_i, true);
            rootElement.appendChild(firstDocImportedNode);
            numPapers++;
          } catch (java.lang.OutOfMemoryError error) {
            System.out.println("Out of Memory Error Occurred");
            System.out.println("Number of papers loaded = " + numPapers);
            openNewFile = true;
          }
        }
      }
      if (callNumber % 300 == 0) {
        openNewFile = true;
      }
    }
    // write the content into xml file
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = null;
    try {
      transformer = transformerFactory.newTransformer();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    }
    DOMSource source = new DOMSource(doc);
    String name = "year" + year + "Citation" + fileNumber + ".xml";
    fileNumber++;
    StreamResult result = new StreamResult(new File(name));

//       Output to console for testing
//       StreamResult result = new StreamResult(System.out);

    try {
      transformer.transform(source, result);
    } catch (TransformerException e) {
      e.printStackTrace();
    }
    System.out.println("Number of papers loaded = " + numPapers);


  }

//  private static void loadCitationPapers(int year)
//      throws FileNotFoundException {
//
//    int numPapers = 0;
//
//    try {
//
//      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//      DocumentBuilder docBuilder = null;
//      try {
//        docBuilder = docFactory.newDocumentBuilder();
//      } catch (ParserConfigurationException e) {
//        e.printStackTrace();
//      }
//
//      // root elements
//      Document doc = docBuilder.newDocument();
//      Element rootElement = doc.createElement("CitationData");
//      doc.appendChild(rootElement);
//
//      String url_citations_i = null;
//      Scanner file = new Scanner(new File(file_name + year + ".txt"));
//      Integer callNumber = 0;
//      boolean reRequest = false;
//      while (file.hasNextInt()) {
//        int pmidsLength = 0;
//        int[] pmids = new int[pmids_limit];
//        if (!reRequest) {
//          while (pmidsLength < pmids_limit) {
//            if (!file.hasNextInt()) {
//              System.out.println("File finished with " + callNumber + " calls.");
//              break;
//            }
//            int integer = file.nextInt();
//            pmids[pmidsLength] = integer;
//            pmidsLength++;
//          }
//          callNumber++;
//          url_citations_i = createURL_citation(pmids);
//        }
//        InputStream response_i = null;
//        try {
//          URLConnection connection = new URL(url_citations_i).openConnection();
//          connection.setRequestProperty("Accept-Charset", charset);
//          response_i = connection.getInputStream();
//          reRequest = false;
//
//        } catch (Exception e) {
//          e.printStackTrace();
//          reRequest = true;
//        }
//        XMLParser xml;
//        try {
//          xml = new XMLParser(response_i);
//        } catch (Exception e) {
//          System.out.println("Response invalid " + pmids[0]);
//          continue;
//        }
//        Element root = xml.getRoot();
//        if (root == null) {
//          //TODO: perform informative action
//          continue;
//        }
//        NodeList linkSet_List = root.getElementsByTagName("LinkSet");
//        if (linkSet_List == null) {
//          //TODO: perform informative action
//          continue;
//        }
//        for (int i = 0; i < pmidsLength; i++) {
//          Element linkSet_i = (Element) linkSet_List.item(i);
//          if (linkSet_i == null) {
//            continue;
//          }
//          NodeList id_list_i
//              = linkSet_i.getElementsByTagName("IdList");
//
//          if (id_list_i.getLength() == 0) {
//            continue;
//          }
//          int pmid = Integer.parseInt(id_list_i.item(0).getTextContent());
//
//          NodeList LinkSetDb_list_i
//              = linkSet_i.getElementsByTagName("LinkSetDb");
//
//          if (LinkSetDb_list_i.getLength() == 0) {
//            continue;
//          }
//          Element linkSetDb_element_i = (Element) LinkSetDb_list_i.item(0);
//          NodeList link_nodes_i = linkSetDb_element_i.getElementsByTagName
//              ("Link");
//
//          if (link_nodes_i.getLength() > 5) {
//            Node firstDocImportedNode = doc.importNode(linkSet_i, true);
//            rootElement.appendChild(firstDocImportedNode);
//            numPapers++;
//          }
//        }
//        System.out.println("call done " + new Date());
//      }
//      // write the content into xml file
//      TransformerFactory transformerFactory = TransformerFactory.newInstance();
//      Transformer transformer = null;
//      try {
//        transformer = transformerFactory.newTransformer();
//      } catch (TransformerConfigurationException e) {
//        e.printStackTrace();
//      }
//      DOMSource source = new DOMSource(doc);
//      String name = "year" + year + "Citation.xml";
//      StreamResult result = new StreamResult(new File(name));
//
////       Output to console for testing
////       StreamResult result = new StreamResult(System.out);
//
//      try {
//        transformer.transform(source, result);
//      } catch (TransformerException e) {
//        e.printStackTrace();
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//      System.out.println("NumPapers loaded = " + numPapers);
//    }
//
//
//  }

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

  private static String createURL_citation(ArrayList pmids) {
    String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink" +
        ".fcgi?dbfrom=pubmed&linkname=pubmed_pubmed_citedin";
    for (int i = 0; i < pmids.size(); i++) {
      url = url + "&id=" + pmids.get(i);
    }
    return url;
  }

  private static void loadPapers(int year) throws IOException,
      ParserConfigurationException, SAXException {

    FileWriter file_writer = new FileWriter(file_name + year + ".txt");
    PrintWriter print_writer = new PrintWriter(file_writer, true);


    int flag = 1;
    long retstart = retstart_init;
    while (flag == 1) {
      String url = createURL_esearch(year, retstart);
      retstart += retmax_limit;

      InputStream response = null;
      XMLParser xmlParser = null;


      try {
        response = getResponse(url);
        xmlParser = new XMLParser(response);
      } catch (Exception e) {
        System.out.println("Exception occurred when getting response while " +
            "loading papers for " +
            "year " + year + "!!" + '\t' + '\t' + new Date
            ());
        e.printStackTrace();
        retstart -= retmax_limit;
        continue;
      }

      // get the first element
      Element root = xmlParser.getRoot();

      // get all child nodes
      NodeList idList_List = root.getElementsByTagName("IdList");
      Element idList_element = (Element) idList_List.item(0);
      NodeList ids_nodes;
      try {
        ids_nodes = idList_element.getElementsByTagName("Id");
      } catch (Exception e) {
        System.out.println("Exception occurred when reading response while " +
            "loading papers for " +
            "year " + year + "!!" + '\t' + '\t' + new Date());
        System.out.println(url);
        e.printStackTrace();
//        retstart -= retmax_limit;
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
