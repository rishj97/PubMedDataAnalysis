import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by rishabh on 04/09/16.
 */
public class ExcelFileReader {
  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


    // use the factory to create a documentBuilder
    DocumentBuilder builder = factory.newDocumentBuilder();
    FileInputStream file = new FileInputStream(new File("Table3_cocitation Jaccard index.xlsx"));

    //Create Workbook instance holding reference to .xlsx file
    XSSFWorkbook workbook = new XSSFWorkbook(file);

    //Get first/desired sheet from the workbook
    XSSFSheet sheet_input = workbook.getSheetAt(0);
    CellAddress add = new CellAddress(2, 2);
    Iterator<Row> rowIterator = sheet_input.rowIterator();
    rowIterator.next();
    Row row;
    Cell cell;
    int rowCount = 0;
    int counter = 0, minYear = 9999, maxYear = 0,minIndex = -1, maxIndex = -1;
    while(rowIterator.hasNext()) {
      row = rowIterator.next();
      cell = row.getCell(0);
      String PairID = cell.getStringCellValue();String y1y2 = "";
      int firstIndex = PairID.indexOf(',');
      int lastIndex = PairID.lastIndexOf(',');
      int y1 = Integer.parseInt(PairID.substring(firstIndex+2, firstIndex+6));
      int y2 = Integer.parseInt(PairID.substring(lastIndex-4, lastIndex));
      if(!(y1==y2)) {
        counter++;
      }
      if(Math.min(y1,y2)<minYear) {
        minYear = Math.min(y1,y2);
        minIndex = rowCount;
      }
      if(Math.max(y1,y2)>maxYear) {
        maxYear = Math.max(y1,y2);
        maxIndex = rowCount;
      }

//      System.exit(0);

    rowCount++;
    }
    System.out.println("counter: "+counter);
    System.out.println("Ratio: "+counter/(double)2321);
    System.out.println("min: "+minYear+" at "+minIndex);
    System.out.println("max: "+maxYear+" at "+maxIndex);
  }
}
