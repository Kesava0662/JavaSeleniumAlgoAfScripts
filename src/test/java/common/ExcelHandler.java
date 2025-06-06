package common;

import java.io.*;
import java.util.*;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;

@SuppressWarnings("all")
public class ExcelHandler {

	private HSSFSheet sheet;
	private String excelFileName;
	private HSSFWorkbook wb;
	private String sheetName;
	private int rowno;
	private int colno;
	private HSSFRow row;
	private HSSFCell cell;
	private static final Logger log = Logger.getLogger(ExcelHandler.class);

	public static File getTheNewestFile(String filePath, String ext) {
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			log.error("Invalid directory: " + filePath);
			return null;
		}

		FileFilter fileFilter = new WildcardFileFilter("*." + ext);
		File[] files = dir.listFiles(fileFilter);

		if (files == null || files.length == 0) {
			log.error("No files found with extension: " + ext);
			return null;
		}

		Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		return files[0];
	}

	public static long getCSVRowsCount(String fileName) {
		long rowCount = 0;
		Path filePath = Paths.get(fileName);

		if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			log.error("File does not exist or is not a regular file: " + fileName);
			return 0;
		}

		try (BufferedReader reader = Files.newBufferedReader(filePath)) {
			while (reader.readLine() != null) {
				rowCount++;
			}
			log.info("Total rows: " + rowCount);
		} catch (IOException e) {
			log.error("Error reading file: " + fileName, e);
		}

		// Delete file using Files.delete() for better error messages
		try {
			Files.delete(filePath);
			log.info("File deleted successfully: " + fileName);
		} catch (IOException e) {
			log.error("Failed to delete file: " + fileName, e);
		}

		return rowCount;
	}

	public static int getRecordsCount(String filename) {
		File file = new File(filename);
		if (!file.exists() || !file.isFile()) {
			log.error("File does not exist or is not a valid file: " + filename);
			return 0;
		}

		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			byte[] buffer = new byte[1024];
			int count = 0;
			int readChars;
			boolean hasContent = false;

			while ((readChars = is.read(buffer)) != -1) {
				hasContent = true;
				for (int i = 0; i < readChars; i++) {
					if (buffer[i] == '\n') {
						count++;
					}
				}
			}
			// If there's content but no newline, count it as one record
			return hasContent && count == 0 ? 1 : count;
		} catch (IOException e) {
			log.error("Error reading file: " + filename + " - " + e.getMessage());
			return 0;
		}
	}

	public static String readData(String filename) {
		List<String> data = new ArrayList<>();
		Path filePath = Paths.get(filename);

		if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			log.error("File does not exist or is not a valid file: {}" + filename);
			return "";
		}

		try (BufferedReader br = Files.newBufferedReader(filePath)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(",");
				for (String token : tokens) {
					data.add(token.trim());
				}
			}
		} catch (IOException e) {
			log.error("Error reading file:" + filename + e);
		}

		// Delete file after reading (optional)
		try {
			if (Files.deleteIfExists(filePath)) {
				log.info("File deleted successfully: {}" + filename);
			}
		} catch (IOException e) {
			log.error("Failed to delete file: {}" + filename + e);
		}

		return String.join("  ", data); // Maintain the original spacing format
	}

	/**
	 * Appends new elements in an existing sheet from the last row
	 */
	public void appendInSheet(String file, String sheetName, String[][] strings) {
		String excelName = file + ".xls";

		try (FileInputStream fis = new FileInputStream(excelName);
				HSSFWorkbook lwb = new HSSFWorkbook(fis);
				FileOutputStream fileOut = new FileOutputStream(excelName)) {

			HSSFSheet lsheet = lwb.getSheet(sheetName);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet " + sheetName + " not found in the file.");
			}

			int rowNum = lsheet.getPhysicalNumberOfRows();

			for (String[] rowData : strings) {
				HSSFRow lrow = lsheet.createRow(rowNum++);
				if (rowData != null) {
					int colNum = 0;
					for (String cellData : rowData) {
						HSSFCell cell1 = lrow.createCell(colNum++);
						cell1.setCellValue(cellData);
					}
				}
			}

			wb.write(fileOut);
		} catch (IOException e) {
			log.error("Error while appending data to sheet: " + e.getMessage());
		}
	}

	/**
	 * used to read data from an excel sheet
	 */
	public String[][] readArrXLSFile(String filename, String sheetname) throws IOException {
		FileInputStream inputStream = new FileInputStream(filename);
		HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
		HSSFSheet lsheet = workbook.getSheet(sheetname);

		int rows = lsheet.getPhysicalNumberOfRows();
		String[][] value = new String[rows][];

		for (int r = 0; r < rows; r++) {
			HSSFRow lrow = lsheet.getRow(r);
			if (lrow != null) {
				int cells = lrow.getPhysicalNumberOfCells();
				value[r] = new String[cells];

				for (int c = 0; c < cells; c++) {
					HSSFCell lcell = lrow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK); // Avoid NPE

					switch (lcell.getCellTypeEnum()) {
					case FORMULA:
						value[r][c] = lcell.getCellFormula();
						break;
					case NUMERIC:
						value[r][c] = String.valueOf(lcell.getNumericCellValue());
						break;
					case STRING:
						value[r][c] = lcell.getStringCellValue();
						break;
					case BLANK:
						value[r][c] = "";
						break;
					case ERROR:
						value[r][c] = Byte.toString(lcell.getErrorCellValue());
						break;
					default:
						value[r][c] = "";
					}
				}
			}
		}

		workbook.close();
		inputStream.close();
		return value;
	}

	public void writeXLSFile() throws IOException {

		writeXLSOpen();
		writeXLSCFHeadings();
		writeXLSClose();
	}

	/**
	 * used to print the entire control mapping (as is)
	 */
	public void writeXLSFile(String file, String[][] data) throws IOException {

		writeXLSOpen(file);
		writeXLSCFHeadings();
		writeArr2XLS(data);
		writeXLSClose();
	}

	/**
	 * To make control mappings form json for profiler
	 */
	public void writeXLSFile(String file, String sheetName, String[][] data) throws IOException {
		writeXLSOpen(file, sheetName);
		writeArr2XLS(data);
		writeXLSClose();
	}

	/**
	 * To make control mappings form json for profiler
	 */
	public void writeXLSFile(String file, String sheetName, ArrayList<String[]> data) throws IOException {
		writeXLSOpen(file, sheetName);
		writeArr2XLS(data);
		writeXLSClose();
	}

	/**
	 * used to initialize to print the control mapping
	 */
	public void writeCtrlMapXLSFileinit(String file, String[][] data) {
		writeXLSOpen(file);
		writeXLSCFHeadings();
	}

	/**
	 * used to open or make an excel file for writing
	 */
	public void writeXLSOpen() {
		excelFileName = "Data1.xls";
		sheetName = "sheet1";
		wb = new HSSFWorkbook();
		sheet = wb.createSheet(sheetName);
	}

	/**
	 * used to open or make an excel file for writing
	 */
	public void writeXLSOpen(String file) {
		excelFileName = file + ".xls";
		sheetName = "sheet1";
		wb = new HSSFWorkbook();
		sheet = wb.createSheet(sheetName);
	}

	/**
	 * used to open or make an excel file for writing
	 */
	public void writeXLSOpen(String file, String sheetname) {
		excelFileName = file;
		sheetName = sheetname;
		wb = new HSSFWorkbook();
		sheet = wb.createSheet(sheetName);
	}

	/**
	 * used to make heading row of controls mapping file
	 */
	public void writeXLSCFHeadings() {
		row = sheet.createRow(rowno++);
		row.createCell(0).setCellValue("GWT");
		row.createCell(1).setCellValue("Control Name");
		row.createCell(2).setCellValue("Control Type");
		row.createCell(3).setCellValue("Control Identification Type");
		row.createCell(4).setCellValue("Control Id");
		row.createCell(5).setCellValue("Parent Control");
		row.createCell(6).setCellValue("PageName");
		row.createCell(7).setCellValue("Action");
		row.createCell(8).setCellValue("ActionMethods");

	}

	/**
	 * used to print a 2d array into an excel sheet
	 */
	public void writeArr2XLS(String[][] data) {
		for (int r = 0; r < data.length; r++) {
			HSSFRow lrow = sheet.createRow(r);
			if (data[r] != null) {
				for (int c = 0; c < data[r].length; c++) {
					HSSFCell lcell = lrow.createCell(c);
					lcell.setCellValue(data[r][c]);
				}
			}
		}
	}

	/**
	 * used to print a 2d array into an excel sheet
	 */
	public void writeArr2XLS(ArrayList<String[]> data) {

		for (int r = 0; r < data.size(); r++) {
			HSSFRow lrow = sheet.createRow(r);
			if (data.get(r) != null) {
				for (int c = 0; c < data.get(r).length; c++) {
					HSSFCell lcell = lrow.createCell(c);
					lcell.setCellValue(data.get(r)[c]);
				}
			}
		}
	}

	/**
	 * used to closed the excel file
	 */
	public void writeXLSClose() throws IOException {
	    if (excelFileName != null && wb != null) {
	        try (FileOutputStream fileOut = new FileOutputStream(excelFileName)) {
	            wb.write(fileOut);
	            fileOut.flush(); 
	        }
	        wb.close();
	    }
	}

	public void nextRow(String[] strings) {
		HSSFRow lrow = sheet.createRow(rowno++);
		int col = 0;
		for (String string : strings) {
			lrow.createCell(col++).setCellValue(string);
		}
	}

	public void emptyRow() {
		rowno++;
	}

	public void newRow(String name) {
		row = sheet.createRow(rowno++);
		colno = 0;
		cell = row.createCell(colno);
		cell.setCellValue(name);

	}

	public void newCell(String value) {
		cell = row.createCell(++colno);
		cell.setCellValue(value);

	}

	public void inNewSheet(String sheetName, String[][] strings) {
		HSSFSheet lsheet = wb.createSheet(sheetName);
		int r = 0;
		int c = 0;
		for (String[] strings2 : strings) {
			HSSFRow lrow = lsheet.createRow(r++);
			c = 0;
			if (strings2 != null) {
				for (String string : strings2) {
					HSSFCell lcell = lrow.createCell(c++);
					lcell.setCellValue(string);
				}
			}
		}
	}

}