package common;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import org.yaml.snakeyaml.Yaml;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

@SuppressWarnings("all")
public class YMLUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
	private static final List<Object> YML_OBJECT = new ArrayList<>();
	private static final Logger log = Logger.getLogger(YMLUtil.class);

	private static Map<String, Object> ymlData;
	private static String ymlNode = "";
	private static Object ymlObjectRepo;
	private static Object ymlPayloadObject;

	private YMLUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static String getYMLData(String key) {
		String output = key;
		try {
			Map<String, Object> map = (Map<String, Object>) YML_OBJECT.get(0);
			output = retrieveValueFromMap(map, key);
		} catch (Exception ex) {
			log.error("Error retrieving YML data for key: " + key, ex);
		}

		if (output.toLowerCase().contains("multiplefilesearch.")) {
			String[] splitKey = output.split("filesearch.");
			String mainKey = splitKey[1];

			for (int i = 1; i < YML_OBJECT.size(); i++) {
				try {
					Map<String, Object> map = (Map<String, Object>) YML_OBJECT.get(i);
					output = retrieveValueFromMap(map, mainKey);
					if (!output.equals(key)) {
						break;
					}
				} catch (Exception ex) {
					log.error("Error retrieving multiple file search data", ex);
				}
			}
		}
		return output;
	}

	public static String getYMLObjectRepositoryData(String key) {
		String output = key;
		try {
			String[] keys = key.split("[.]", 0);
			Map<String, Object> map = (Map<String, Object>) ymlObjectRepo;

			if (keys.length > 1) {
				output = retrieveNestedValue(map, keys);
			} else {
				output = retrieveValueFromMap(map, key);
			}
		} catch (Exception ex) {
			log.error("Error retrieving object repository data for key: " + key, ex);
		}

		AutoHealUtil.setXpath(output);
		AutoHealUtil.setXpathKey(key);
		try {
			String data = HtmlElementReader.getTargetByLocator(output).replace("\'", "\"");
			AutoHealUtil.setTarget(data);
		} catch (Exception ex) {
			log.error("Error setting AutoHealUtil target", ex);
		}

		return output;
	}

	public static void updateYML(String key, String value) {
		try {
			ymlData.put(key, value);
			OBJECT_MAPPER.writeValue(new File("src/test/java/TestData.yml"), ymlData);
		} catch (Exception ex) {
			log.error("Error updating YML file", ex);
		}
	}

	public static void loadYML(String path, String node) {
		File dir = new File(path);
		FileFilter fileFilter = new WildcardFileFilter("*.yml*");
		File[] files = dir.listFiles(fileFilter);

		if (files != null) {
			for (File file : files) {
				try {
					if (file.getName().contains("TestData")) {
						YamlReader reader = new YamlReader(new FileReader(file));
						YML_OBJECT.add(reader.read());
						ymlNode = node;
					}
				} catch (Exception ex) {
					log.error("Error loading YML file: " + file.getName(), ex);
				}
			}
		}
	}

	public static void loadObjectRepoYML(String file) {
		try (FileReader fileReader = new FileReader(file)) {
			YamlReader reader = new YamlReader(fileReader);
			ymlObjectRepo = reader.read();
		} catch (Exception ex) {
			log.error("Error loading object repository YML file", ex);
		}
	}

	public static void readObjectRepoYML(String... files) {
		for (String file : files) {
			String fileName = file.substring(file.lastIndexOf("/") + 1);
			try (FileInputStream inputStream = new FileInputStream(file)) {
				Yaml yaml = new Yaml();
				yaml.load(inputStream);
				log.info(fileName + " file is valid");
			} catch (Exception e) {
				log.error(fileName + " file is invalid: " + e.getMessage(), e);
			}
		}
	}

	public static void payloadYML(String file, String node) {
		try (FileReader fileReader = new FileReader(file)) {
			YamlReader reader = new YamlReader(fileReader);
			ymlPayloadObject = reader.read();
			Map<String, Object> map = (Map<String, Object>) ymlPayloadObject;

			RestAssuredUtil.apiPayloadDictionary.putAll(map.entrySet().stream().collect(
					Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString())));

			ymlNode = node;
		} catch (Exception ex) {
			log.error("Error reading payload YML file", ex);
		}
	}

	private static String retrieveValueFromMap(Map<String, Object> map, String key) {
		String output = key;
		try {
			output = Optional.ofNullable(map.get(key)).orElseGet(() -> Optional.ofNullable(map.get(ymlNode))
					.map(node -> ((Map<String, Object>) node).get(key)).orElse(key)).toString();
		} catch (Exception ex) {
			log.error("Error retrieving value from map", ex);
		}
		return output;
	}

	private static String retrieveNestedValue(Map<String, Object> map, String[] keys) {
		String output = keys[0];
		try {
			output = Optional.ofNullable(map.get(keys[0])).map(node -> ((Map<String, Object>) node).get(keys[1]))
					.orElse(keys[0]).toString();
		} catch (Exception ex) {
			log.error("Error retrieving nested value", ex);
		}
		return output;
	}
}
