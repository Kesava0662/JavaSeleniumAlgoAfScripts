package common;

import java.util.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.DocumentException;
import com.itextpdf.text.PageSize;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.FileOutputStream;
import java.io.IOException;
import io.cucumber.java.*;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CustomReportGenerator {
	private static final String CUSTOMREPORT = "CustomReport";
	public static final Path htmlfile = Paths.get(Constants.PROJECT_PATH, CUSTOMREPORT, "report.html");
	public static final Path pdffile = Paths.get(Constants.PROJECT_PATH, CUSTOMREPORT, "output.pdf");

	private static List<String[]> scenarioResults = new ArrayList<>();
	static final Logger log = Logger.getLogger(CustomReportGenerator.class);

	private CustomReportGenerator() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static void addScenarioResult(String[] result) {
		scenarioResults.add(result);
	}

	public static List<String[]> getaddScenarioResult() {
		return scenarioResults;
	}

	public static String getTestCaseNumber(Scenario scenario) {
		return scenario.getSourceTagNames().stream().filter(tag -> tag.startsWith("@test")).findFirst()
				.orElse("Unknown").substring(5); // Extract the test case number after "@test"
	}

	public static void generateReport() {
		try {
			// Ensure the CustomReport folder exists
			File reportDir = new File(CUSTOMREPORT);
			if (!reportDir.exists()) {
				reportDir.mkdirs();
			}

			try (FileWriter writer = new FileWriter(htmlfile.toString())) {
				writer.write("<html><head><style>");
				writer.write("table {width: 100%; border-collapse: collapse;}");
				writer.write("th, td {border: 1px solid black; padding: 8px; text-align: center;}");
				writer.write("th {background-color: #f2f2f2;}");
				writer.write("</style></head><body>");
				writer.write("<table>");
				writer.write(
						"<tr><th>Scenario Name</th><th>Scheme Name</th><th>Application Number</th><th>Output (Pass/Fail)</th></tr>");

				for (String[] row : scenarioResults) {
					writer.write("<tr>");
					for (String cell : row) {
						writer.write("<td>" + cell + "</td>");
					}
					writer.write("</tr>");
				}

				writer.write("</table>");
				writer.write("</body></html>");

				log.info("HTML report generated successfully.");
			} // FileWriter will be automatically closed here
		} catch (IOException e) {
			log.error("Error generating report: " + e.getMessage(), e);
		}
	}

	public static String getSchemaName(String name) {
		String normalizedName = name.toLowerCase().replace(" ", "");
		if (normalizedName.contains("ss")) {
			return "SS General Form 60";
		} else if (normalizedName.contains("car")) {
			return "Car Loan";
		} else if (normalizedName.contains("home")) {
			return "Home Loan";
		}
		return null;
	}

	public static void convertHtmlToPdf(String htmlFilePath, String pdfFilePath) {
		try (FileOutputStream outputStream = new FileOutputStream(pdfFilePath)) {
			// Create a new PDF document with custom page size
			Document document = new Document(PageSize.A3.rotate()); // Set page size to A3 landscape
			document.open();

			// Create an ITextRenderer object
			ITextRenderer renderer = new ITextRenderer();

			// Set the base URI for relative paths in the HTML
			renderer.setDocument(new java.io.File(htmlFilePath));
			renderer.layout();

			// Extracted method for PDF creation
			createPdf(renderer, outputStream);

			document.close();
			log.info("PDF generated successfully at " + pdfFilePath);
		} catch (Exception e) {
			log.error("Document error: " + e.getMessage());
		}
	}

	/**
	 * Extracted method to handle PDF creation.
	 */
	private static void createPdf(ITextRenderer renderer, FileOutputStream outputStream) {
		try {
			renderer.createPDF(outputStream);
		} catch (DocumentException e) {
			log.error("PDF generation error: " + e.getMessage());
		}
	}

}
