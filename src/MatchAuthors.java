import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by rishabh on 03/07/16.
 */
public class MatchAuthors {

  private static final int PMID_ARRAY_SIZE = 1000;
  static int START_YEAR = 1995;
  static int END_YEAR = 1995;


  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    DocumentBuilderFactory factory1 = DocumentBuilderFactory.newInstance();
    DocumentBuilderFactory factory2 = DocumentBuilderFactory.newInstance();


    // use the factory to create a documentBuilder
    DocumentBuilder builder1 = factory1.newDocumentBuilder();
    DocumentBuilder builder2 = factory2.newDocumentBuilder();

    if (args.length == 2) {
      START_YEAR = Integer.parseInt(args[1]);
      END_YEAR = Integer.parseInt(args[2]);
    }

    int count = 0;
    int count_paper = 0;
    ExcelFileWriter xlsFile_pairs = new ExcelFileWriter
        ("PubMedDataPairs" + START_YEAR + "_" + END_YEAR + "pairs.txt");
    Sheet sheet_pair = xlsFile_pairs.getSheet();

    ExcelFileWriter xlsFile_paper = new ExcelFileWriter
        ("PubMedDataPairs" + START_YEAR + "_" + END_YEAR + "papers");
    Sheet sheet_paper = xlsFile_paper.getSheet();

    Row firstRow_pair = sheet_pair.createRow(count);
    firstRow_pair.createCell(1).setCellValue("PairID");
    firstRow_pair.createCell(2).setCellValue("Ref1Cited");
    firstRow_pair.createCell(3).setCellValue("Ref2Cited");
    firstRow_pair.createCell(4).setCellValue("PairCited");
    firstRow_pair.createCell(5).setCellValue("Index");
    firstRow_pair.createCell(6).setCellValue("CitationPMIDs");

    Row firstRow_paper = sheet_paper.createRow(count_paper);
    firstRow_paper.createCell(1).setCellValue("#Cited");
    firstRow_paper.createCell(2).setCellValue("RefID");
    firstRow_paper.createCell(3).setCellValue("Authors");
    firstRow_paper.createCell(4).setCellValue("Title");
    firstRow_paper.createCell(5).setCellValue("Journal");
    firstRow_paper.createCell(6).setCellValue("Volume");
    firstRow_paper.createCell(7).setCellValue("Issue");
    firstRow_paper.createCell(8).setCellValue("Pages");
    firstRow_paper.createCell(9).setCellValue("Type");
    firstRow_paper.createCell(10).setCellValue("Date");
    firstRow_paper.createCell(11).setCellValue("DOI");
    firstRow_paper.createCell(12).setCellValue("PMID");
    boolean errorOccurred = false;

    for (int year = START_YEAR; year <= END_YEAR; year++) {

      System.out.println("Started year " + year + "...\t\t" + new Date());

      Scanner sc = new Scanner(new FileReader("year_outputs/year_data" + year +
          "output.txt"));


      int allPmids[] = new int[PMID_ARRAY_SIZE];
      int allPmidsIndex = 0;
      boolean isLastIteration = false;
      int iterationNum = 0;

      Document doc = null;
      Document doc_citation = null;
      while (sc.hasNext()) {
        allPmids[allPmidsIndex] = sc.nextInt();
        allPmidsIndex++;
        sc.next();
        allPmids[allPmidsIndex] = sc.nextInt();
        allPmidsIndex++;
        sc.next();
        String val = sc.next();
      }
      while(!isLastIteration) {

        int pmids[] = new int[400];
        int pmidsIndex = 0;
        while (pmidsIndex < 400) {
          if ((iterationNum * 400 + pmidsIndex) == allPmidsIndex) {
            isLastIteration = true;
            break;
          }
          pmids[pmidsIndex] = allPmids[iterationNum * 400 + pmidsIndex];
          pmidsIndex++;
        }
        iterationNum++;
        //System.out.println("k="+pmidsIndex+" "+pmids[pmidsIndex-1]);
        try {

          String url = createURL_paperInfo(pmids);
          URLConnection connection = new URL(url).openConnection();
          connection.setRequestProperty("Accept-Charset", charset);
          InputStream response = connection.getInputStream();
          doc = builder1.parse(response);


          String url_citation = createURL_citation(pmids);
          URLConnection connection2 = new URL(url_citation).openConnection();
          connection2.setRequestProperty("Accept-Charset", charset);
          InputStream response_citation = connection2.getInputStream();
          doc_citation = builder2.parse(response_citation);
        } catch (Exception e) {
          xlsFile_pairs.createFile();
          xlsFile_paper.createFile();
          System.out.println("Exception Occurred!!");
          System.out.println(e);
          System.out.println();
          year--;
          continue;
        }


        Element root = doc.getDocumentElement();
        if (root == null) {
          continue;
        }

        NodeList documentSummarySet_list = root.getElementsByTagName
            ("DocumentSummarySet");
        if (documentSummarySet_list == null) {
          continue;
        }

        Element documentSummarySet_element = (Element) documentSummarySet_list
            .item
                (0);
        if (documentSummarySet_element == null) {
          year--;
          continue;
        }
        NodeList documentSummaries_list = documentSummarySet_element
            .getElementsByTagName
                ("DocumentSummary");
        for (int pairNum = 0; pairNum < pmidsIndex - 1; pairNum += 2) {
          Element doc1 = (Element) documentSummaries_list.item(pairNum);
          String sortAuthor1 = doc1.getElementsByTagName("SortFirstAuthor")
              .item(0)
              .getTextContent();
          String source1 = doc1.getElementsByTagName("Source").item(0)
              .getTextContent();
          Element doc2 = (Element) documentSummaries_list.item(pairNum + 1);
          String sortAuthor2 = doc2.getElementsByTagName("SortFirstAuthor")
              .item(0)
              .getTextContent();
          String source2 = doc2.getElementsByTagName("Source").item(0)
              .getTextContent();

          String PairID = sortAuthor1 + ", " + year + ", " +
              "" + source1 + "||" + sortAuthor2 + ", " + year + ", " + source2;
          NodeList authors1 = ((Element) doc1.getElementsByTagName
              ("Authors").item(0)).getElementsByTagName("Author");

          NodeList authors2 = ((Element) doc2.getElementsByTagName
              ("Authors").item(0)).getElementsByTagName("Author");

          int authors1Length = authors1.getLength();
          int authors2Length = authors2.getLength();
          boolean noAuthorCommon = true;

          int i, j;
          String authors1String = "";
          String authors2String = "";

          for (i = 0; i < authors1Length; i++) {
            authors1String = authors1String + authors1.item(i).getTextContent
                () + "; ";
          }

          for (i = 0; i < authors2Length; i++) {
            authors2String = authors2String + authors2.item(i).getTextContent
                () + "; ";
          }

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

          if (noAuthorCommon) {
            count++;
            count_paper++;

            // get the first element
            Element root_citation = doc_citation.getDocumentElement();
            if (root_citation == null) {
              continue;
            }
            NodeList linkSet_List = root_citation.getElementsByTagName("LinkSet");
            if (linkSet_List == null) {
              continue;
            }

            Element linkSet_i = (Element) linkSet_List.item(pairNum);
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

            Element linkSet_j = (Element) linkSet_List.item(pairNum + 1);
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

            int len_i = link_nodes_i.getLength();
            int len_j = link_nodes_j.getLength();
            int value = 0;
            String citation_ids = "";


            for (int c1 = 0; c1 < len_i; c1++) {
              for (int c2 = 0; c2 < len_j; c2++) {
                if (link_nodes_i.item(c1).getTextContent().equals
                    (link_nodes_j.item(c2).getTextContent())) {
                  value++;
                  citation_ids = citation_ids + link_nodes_i.item(c1)
                      .getTextContent() + "||";
                  break;
                }
              }
            }
            double jaccard_index = value / (double) (len_i + len_j - value);
            //need citation_ids value len_i len_j jaccard_index PairID
            Row newRow = sheet_pair.createRow(count);
            Cell cell1 = newRow.createCell(1);
            Cell cell2 = newRow.createCell(2);
            Cell cell3 = newRow.createCell(3);
            Cell cell4 = newRow.createCell(4);
            Cell cell5 = newRow.createCell(5);
            Cell cell6 = newRow.createCell(6);

            cell1.setCellValue(PairID);
            cell2.setCellValue(len_i);
            cell3.setCellValue(len_j);
            cell4.setCellValue(value);
            cell5.setCellValue(jaccard_index);
            cell6.setCellValue(citation_ids);

            String title1 = doc1.getElementsByTagName("Title").item(0)
                .getTextContent();
            String title2 = doc2.getElementsByTagName("Title").item(0)
                .getTextContent();

            String volume1 = doc1.getElementsByTagName("Volume").item(0)
                .getTextContent();
            String volume2 = doc2.getElementsByTagName("Volume").item(0)
                .getTextContent();

            String issue1 = doc1.getElementsByTagName("Issue").item(0)
                .getTextContent();
            String issue2 = doc2.getElementsByTagName("Issue").item(0)
                .getTextContent();

            String pages1 = doc1.getElementsByTagName("Pages").item(0)
                .getTextContent();
            String pages2 = doc2.getElementsByTagName("Pages").item(0)
                .getTextContent();

            String pubType1 = doc1.getElementsByTagName("PubType").item(0)
                .getTextContent();
            String pubType2 = doc2.getElementsByTagName("PubType").item(0)
                .getTextContent();

            String pubDate1 = doc1.getElementsByTagName("PubDate").item(0)
                .getTextContent();
            String pubDate2 = doc2.getElementsByTagName("PubDate").item(0)
                .getTextContent();

            String doi1 = "null";
            String doi2 = "null";
            int c;
            NodeList articleIds1 = ((Element) doc1.getElementsByTagName
                ("ArticleIds").item(0))
                .getElementsByTagName("ArticleId");
            for (c = 0; c < articleIds1.getLength(); c++) {
              if (((Element) articleIds1.item(c)).getElementsByTagName("IdType")
                  .item(0).getTextContent().equals("doi")) {
                doi1 = ((Element) articleIds1.item(c)).getElementsByTagName
                    ("Value")
                    .item(0).getTextContent();
                break;
              }
            }

            NodeList articleIds2 = ((Element) doc2.getElementsByTagName
                ("ArticleIds").item(0))
                .getElementsByTagName("ArticleId");
            for (c = 0; c < articleIds2.getLength(); c++) {
              if (((Element) articleIds2.item(c)).getElementsByTagName("IdType")
                  .item(0).getTextContent().equals("doi")) {
                doi2 = ((Element) articleIds2.item(c)).getElementsByTagName
                    ("Value")
                    .item(0).getTextContent();
                break;
              }
            }


            newRow = sheet_paper.createRow(count_paper);
            newRow.createCell(1).setCellValue(len_i);
            newRow.createCell(2).setCellValue(sortAuthor1 + ", " + year + ", " +
                "" + source1);
            newRow.createCell(3).setCellValue(authors1String);
            newRow.createCell(4).setCellValue(title1);
            newRow.createCell(5).setCellValue(source1);
            newRow.createCell(6).setCellValue(volume1);
            newRow.createCell(7).setCellValue(issue1);
            newRow.createCell(8).setCellValue(pages1);
            newRow.createCell(9).setCellValue(pubType1);
            newRow.createCell(10).setCellValue(pubDate1);
            newRow.createCell(11).setCellValue(doi1);
            newRow.createCell(12).setCellValue(pmids[pairNum]);

            count_paper++;

            newRow = sheet_paper.createRow(count_paper);
            newRow.createCell(1).setCellValue(len_j);
            newRow.createCell(2).setCellValue(sortAuthor2 + ", " + year + ", " + source2);
            newRow.createCell(3).setCellValue(authors2String);
            newRow.createCell(4).setCellValue(title2);
            newRow.createCell(5).setCellValue(source2);
            newRow.createCell(6).setCellValue(volume2);
            newRow.createCell(7).setCellValue(issue2);
            newRow.createCell(8).setCellValue(pages2);
            newRow.createCell(9).setCellValue(pubType2);
            newRow.createCell(10).setCellValue(pubDate2);
            newRow.createCell(11).setCellValue(doi2);
            newRow.createCell(12).setCellValue(pmids[pairNum + 1]);


          }
        }
      }

        System.out.println("...year " + year + " finished\t\t" + new Date());
        xlsFile_pairs.createFile();
        xlsFile_paper.createFile();
      }

    xlsFile_pairs.createFile();
    xlsFile_paper.createFile();


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
}
