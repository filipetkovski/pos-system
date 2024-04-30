package system.pos.spring.utility;

import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import system.pos.spring.model.Order;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelDownloader {
    public static void downloadExcelFile(Workbook workbook, String downloadName) throws FileNotFoundException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(downloadName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx"));
        fileChooser.setTitle("Save Excel File");

        FileOutputStream fileOut = new FileOutputStream(fileChooser.showSaveDialog(null));

        try {
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
