package common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class HtmlElementReader {

	private static final Logger log = Logger.getLogger(HtmlElementReader.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	private HtmlElementReader() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static String getTargetByLocator(String locator) {
		File jsonFile = new File(Constants.HTML_ELEMENT_PATH);

		try {
			ensureFileExists(jsonFile);
			List<HtmlElement> elements = objectMapper.readValue(jsonFile, new TypeReference<List<HtmlElement>>() {
			});

			return elements.stream().filter(element -> element.getLocator().equals(locator)).map(HtmlElement::getTarget)
					.map(target -> target.replace("\"", "\\\"")).findFirst().orElse("");

		} catch (IOException e) {
			log.error("Error reading HtmlElement.json: ", e);
			return "";
		}
	}

	private static void ensureFileExists(File jsonFile) throws IOException {
		if (!jsonFile.exists()) {
			log.info("HtmlElement.json not found. Creating an empty file.");
			if (jsonFile.getParentFile() != null) {
				Files.createDirectories(jsonFile.getParentFile().toPath()); // Ensure parent directories exist
			}
			Files.createFile(jsonFile.toPath()); // Create the file
			objectMapper.writeValue(jsonFile, new ArrayList<HtmlElement>());
		}
	}

	 public static class HtmlElement {
	        private String locator;
	        private String target;

	        public String getLocator() { return locator; }
	        public void setLocator(String locator) { this.locator = locator; }
	        public String getTarget() { return target; }
	        public void setTarget(String target) { this.target = target; }
	    }
}