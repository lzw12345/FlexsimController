package com.nusinfineon.core;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.nusinfineon.exceptions.CustomException;

/**
 * Class represents the core functionalities involved in interfacing with a Microsoft Excel document for the
 * purposes of varying the batch size.
 */
public class BatchSizeCore {
    private static final String MASTER_XLSX_FILE_NAME = "temp_master_input";
    private static final String SHEET_NAME = "Product Info and Eqpt Matrix";
    private static final String MIN_BIB_COLUMN_NAME = "BIB Slot Utilization Min";
    private static final int MIN_BIB_SLOT_COLUMN_INDEX = 5;
    private final static Logger LOGGER = Logger.getLogger(BatchSizeCore.class.getName());

    private File originalInputExcelFile;
    private File tempCopyOriginalInputExcelFile;
    private ArrayList<Integer> listOfBatchSizes;
    private ArrayList<File> excelFiles;

    public BatchSizeCore(String excelFilePath, String batchSizeMinString,
                         String batchSizeMaxString, String batchSizeStepString) throws IOException {

        this.originalInputExcelFile = new File(excelFilePath);
        int minBatchSize = Integer.parseInt(batchSizeMinString);
        int maxBatchSize = Integer.parseInt(batchSizeMaxString);
        int batchStep = Integer.parseInt(batchSizeStepString);

        // Calculates the exact batch size needed and adds to an array.
        this.listOfBatchSizes = new ArrayList<Integer>();
        for (int i = minBatchSize; i <= maxBatchSize; i = batchStep + i) {
            listOfBatchSizes.add(i);
        }

    } // End of Constructor

    public void execute() throws IOException, InvalidFormatException, CustomException {

        createCopyOfInputFile();
        LOGGER.info("Successfully created a copy of main Input excel file");

        for (int batchNumber : listOfBatchSizes) {
            // Create the temporary excel file
            String fileName = "input_data_batch_size_" + batchNumber + "__";
            File singleBatchExcelFile = Files.createTempFile(fileName, ".xlsx").toFile();
            copyFileUsingStream(this.tempCopyOriginalInputExcelFile, singleBatchExcelFile);

            // Create the workbook from excel file
            Workbook workbook = WorkbookFactory.create(singleBatchExcelFile);

            // Access the necessary sheet
            Sheet sheet = workbook.getSheet(SHEET_NAME);

            // Obtain the column index matching the column name
            int columnIndex = -1;
            Row firstRow = sheet.getRow(0);
            for (int cellIndex = 0; cellIndex < firstRow.getPhysicalNumberOfCells(); cellIndex++) {
                Cell cell = firstRow.getCell(cellIndex);
                if ( (cell.getStringCellValue().trim().equals(MIN_BIB_COLUMN_NAME))) {
                    columnIndex = cellIndex;
                }
            }

            if (columnIndex == -1) {
                throw new CustomException("Column " + MIN_BIB_COLUMN_NAME + " not found in excel");
            }

            // Set the cell values for all the rows
            for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++ ) {
                Row row = sheet.getRow(rowIndex);
                Cell cell = row.getCell(columnIndex);
                cell.setCellType(CellType.NUMERIC);
                cell.setCellValue(batchNumber);
            }

            // Remember to close workbook. Else file will be corrupted
            workbook.close();
        } // End of for-loop for batch sizes
    } // End of execute method

    /**
     * Creates a copy of the master file and stores it in a temporary working directory.
     * Sample directory is: 'C:\Users\hatzi\AppData\Local\Temp\temp_master_input5569509737681448400.xlsx'.
     * @throws IOException
     */
    public void createCopyOfInputFile() throws IOException {
        Path masterTempFilePath = Files.createTempFile(MASTER_XLSX_FILE_NAME, ".xlsx");
        this.tempCopyOriginalInputExcelFile = masterTempFilePath.toFile();
        copyFileUsingStream(originalInputExcelFile, tempCopyOriginalInputExcelFile);
    }

    /**
     * Copies a file from source to destination using an input stream.
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copyFileUsingStream(File source, File destination) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }


    public String printBatchesToRun() {
        return this.listOfBatchSizes.toString();
    }

    public File getOriginalInputExcelFile() {
        return this.originalInputExcelFile;
    }

    public ArrayList<Integer> getListOfBatchSizes() {
        return listOfBatchSizes;
    }
}
