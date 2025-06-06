package common;

import java.io.*;
import java.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PDFUtil {

	private static final Logger log = Logger.getLogger(PDFUtil.class);

	private PDFUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static String getText(String file) throws IOException {
		String extension = FilenameUtils.getExtension(file);
		File pdfFile;

		if (extension != null && !extension.trim().isEmpty()) {
			pdfFile = new File(file);
		} else {
			Optional<File> newestFile = getTheNewestFile(file, "pdf");
			if (newestFile.isEmpty()) {
				log.warn("No PDF file found in directory: " + file);
				return "";
			}
			pdfFile = newestFile.get();
		}

		try (PDDocument doc = PDDocument.load(pdfFile)) {
			return new PDFTextStripper().getText(doc);
		} catch (IOException e) {
			log.error("Error reading PDF file: " + pdfFile.getAbsolutePath(), e);
			throw e;
		}
	}

	/* Get the newest file for a specific extension */
	static Optional<File> getTheNewestFile(String filePath, String ext) {
		sleep(2);
		File dir = new File(filePath);
		if (!dir.exists() || !dir.isDirectory()) {
			log.warn("Invalid directory: " + filePath);
			return Optional.empty();
		}

		FileFilter fileFilter = new WildcardFileFilter("*." + ext);
		File[] fileList = dir.listFiles(fileFilter);

		if (fileList == null || fileList.length == 0) {
			log.warn("No files found with extension: " + ext);
			return Optional.empty();
		}

		Arrays.sort(fileList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
		return Optional.of(fileList[0]);
	}

	private static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Thread sleep interrupted", e);
		}
	}
}
