import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A very simple program that writes some data to an Excel file
 * using the Apache POI library.
 * @author www.codejava.net
 *
 */
public class ExcelFileWriter {
  private String fileName;
  private Workbook workbook;
  private Sheet sheet;
  public ExcelFileWriter(String fileName) {
    this.fileName = fileName;
    workbook = new XSSFWorkbook();
    sheet = workbook.createSheet(fileName);
  }

  public void createFile() {
    try (FileOutputStream outputStream = new FileOutputStream(fileName+"" +
        ".xlsx")) {
      workbook.write(outputStream);
    } catch (IOException e) {
      System.out.println("Failure in createFile");
      e.printStackTrace();
    }
  }

  public Sheet getSheet() {
    return sheet;
  }
}