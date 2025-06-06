package common;

import java.io.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class AutoHealUtil {
	// getters/setters
	private static String xpath;
	private static String xpathKey;
	private static String target;

	private static final Logger log = Logger.getLogger(AutoHealUtil.class);

	// Private constructor to prevent instantiation
	private AutoHealUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	// Getter and Setter for xpath
	public static String getXpath() {
		return xpath;
	}

	public static void setXpath(String value) {
		xpath = value;
	}

	// Getter and Setter for xpathKey
	public static String getXpathKey() {
		return xpathKey;
	}

	public static void setXpathKey(String value) {
		xpathKey = value;
	}

	// Getter and Setter for target
	public static String getTarget() {
		return target;
	}

	public static void setTarget(String value) {
		target = value;
	}

	public static void updtaeXML(String uidTag) {
		try {
			String workingDirectory;
			// here, we assign the name of the OS, according to Java, to a variable...
			String os = (System.getProperty("os.name")).toUpperCase();
			// to determine what the workingDirectory is.
			// if it is some version of Windows
			if (os.contains("WIN")) {
				// it is simply the location of the "AppData" folder
				workingDirectory = System.getenv("LOCALAPPDATA");
			}
			else if (os.contains("LINUX")) {
				// it is simply the location of the "AppData" folder
				workingDirectory = "/tmp";
			}
			// Otherwise, we assume Linux or Mac
			else {
				// in either case, we would start in the user's home directory
				workingDirectory = System.getProperty("user.home");
				// if we are on a Mac, we are not done, we look for "Application Support"
				workingDirectory += "/Library/Application Support";
			}
			String autoHealPath = Paths.get(workingDirectory, "AlgoAF", "AutoHeal", "web_" + uidTag).toString();

			createDirectory(autoHealPath);

			String xmlFile = Paths.get(autoHealPath, "AFConfig.xml").toString();
			String htmlFile = Paths.get(autoHealPath, "WebPage.html").toString();

			String path = System.getProperty("user.dir");
			String objectRepositoryPath = Paths.get(path, "src", "test", "java", "ObjectRepository.yml").toString();

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("Configuration");
			doc.appendChild(rootElement);

			Element autType = doc.createElement("AutomationType");
			autType.appendChild(doc.createTextNode("Web"));
			rootElement.appendChild(autType);
			
			Element langFW = doc.createElement("Language_Framework");
			langFW.appendChild(doc.createTextNode("Java_Selenium"));
			rootElement.appendChild(langFW);

			Element autoHeal = doc.createElement("AutoHeal");
			autoHeal.appendChild(doc.createTextNode("True"));
			rootElement.appendChild(autoHeal);

			Element projectPath = doc.createElement("ObjectRepositoryFile");
			projectPath.appendChild(doc.createTextNode(objectRepositoryPath));
			rootElement.appendChild(projectPath);

			Element xPathKey = doc.createElement("XPathKey");
			xPathKey.appendChild(doc.createTextNode(xpathKey));
			rootElement.appendChild(xPathKey);

			Element xPath = doc.createElement("XPath");
			xPath.appendChild(doc.createTextNode(xpath));
			rootElement.appendChild(xPath);

			Element target = doc.createElement("Target");
			target.appendChild(doc.createTextNode(AutoHealUtil.target));
			rootElement.appendChild(target);

			Element xPathUpdatedStatus = doc.createElement("XPathUpdatedStatus");
			xPathUpdatedStatus.appendChild(doc.createTextNode("False"));
			rootElement.appendChild(xPathUpdatedStatus);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""); // Disallow DOCTYPE DTDs
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, ""); // Disallow external
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(xmlFile));
			transformer.transform(source, result);
			log.info("XML File saved!");

			saveHtmlPageSource(htmlFile);

		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	public static void saveConfigDeatils(String uidTag) {
		try {
			String autoHealingString = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "AutoHealing");
			boolean autoHealing = Boolean.parseBoolean(autoHealingString);
			if (autoHealing) {
				updtaeXML(uidTag);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

	private static void createDirectory(String path) {
		try {
			Files.createDirectories(Paths.get(path));
		} catch (Exception ex) {
			log.error("Error creating directory: " + ex.getMessage());
		}
	}

	public static void saveHtmlPageSource(String htmlFile) {
	    try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(htmlFile), StandardCharsets.UTF_8);
	         BufferedWriter out = new BufferedWriter(writer)) {

	        WebDriver driver = WebBrowser.getBrowser();
	        out.write(driver.getPageSource());
	        log.info("HTML data saved in UTF-8 format!");

	    } catch (IOException e) {
	        log.error("Error saving HTML file: " + e.getMessage());
	    }
	}

}