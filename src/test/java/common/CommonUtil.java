package common;

import java.util.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.ThreadLocalRandom;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.unix4j.Unix4j;
import org.unix4j.line.Line;
import org.unix4j.unix.Grep;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.restassured.response.Response;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.text.DateFormat;

@SuppressWarnings("all")
public class CommonUtil {
	private static String labelNumber;
	private static int randomlabelNumber;
	private static String alphaNumber32;
	private static String alphaNumber64Epoch;
	private static String labelText;
	private static String applicationType;
	private static String testRailID;
	private static String randomlabelText;
	private static int columnIndex;
	private static String apiUrl;
	private static String methodType;
	private static String requestParameters;
	private static String apiHeaders;
	private static String apiEndpoint;
	private static String maxHour;
	private static String basicAuth;
	private static String apiParameter;
	private static String responseHeader;
	private static int copiedCount;
	private static int randomcopiedCount;
	private static int alpha32Count;
	private static int alpha64Count;
	private static String maxDuration;
	private static String minDuration;
	private static String dbValue;
	private static Response apiResponse;
	private static String appUrl;
	public static String browserName;
	public static String apiCmdUrl;
	private static List<String> copiedValues = new ArrayList<>();
	private static List<String> globalValues = new ArrayList<>();
	private static List<String> globalRandomValues = new ArrayList<>();
	public static Map<String, String> globalUserValues = new HashMap<>();
	private static List<String> randomUsercopiedtextValues = new ArrayList<>();
	private static Map<String, String> apiResponseDictionary = new HashMap<>();
	public static Map<String, String> apiPayloadDictionary = new HashMap<>();
	private static List<String> randomcopiednumberValues = new ArrayList<>();
	private static List<String> randomAlpha32List = new ArrayList<>();
	private static List<String> randomAlpha64List = new ArrayList<>();
	private static List<String> randomcopiedtextValues = new ArrayList<>();
	private static List<String> copiedList = new ArrayList<>();
	private static String testData;
	public static String error = "";
	public static Map<String, String> dict = new HashMap<>();
	private static final Gson gson = new Gson();
	private static final String NUMBER_CONSTANT = "0123456789";
	private static final String EMPTY = "_empty_";
	private static final String ALFA_CONSTANT = "abcdefghijklmnopqrstuvwxyz";
	private static final Random RANDOM = new Random();

	static final Logger log = Logger.getLogger(CommonUtil.class);

	// To set value of LabelNamuber, copied from element
	public static void setCopiedNumber(String number) {
		labelNumber = number;
	}

	// To get value of LabelNamuber, copied from element
	public static String getCopiedNumber() {
		return labelNumber;
	}

	public static void setCopiedRandomNumber(String number) {
		if (getRandomCopiedCount() == 0) {
			randomlabelNumber = Integer.parseInt(number);
			randomcopiednumberValues.add(number);
		} else {
			randomcopiednumberValues.add(number);
		}
		globalRandomValues.add(number);
		int size = globalRandomValues.size();
		if (size - 1 == 0) {
			ExtentCucumberAdapter.addTestStepLog("@globalRanodmCopiedText --> " + globalRandomValues.get(size - 1));
		} else {
			ExtentCucumberAdapter
					.addTestStepLog("@globalRanodmCopiedText" + (size) + " ---> " + globalRandomValues.get(size - 1));
		}
		setRandomCopiedCount(getRandomCopiedCount() + 1);
	}

	public static int getCopiedRandomNumber() {
		return randomlabelNumber;
	}

	public static String getAlphaNum64CopiedText() {
		return alphaNumber64Epoch;
	}

	// To set value of LabelText variable if copied action is used once in a
	// test.else to set values in copiedValues list
	public static void setCopiedText(String text) {

		if (getCopiedCount() == 0) {
			labelText = text;
			copiedValues.add(text);
		} else {
			copiedValues.add(text);
		}
		setCopiedCount(getCopiedCount() + 1);
	}

	public static String getCopiedTextKey(String key) {
		if (dict.containsKey(key))
			return dict.get(key);
		else
			return key;
	}

	// To set value of TestRailID
	public static void setTestRailID(String type) {
		testRailID = type;
	}

	// To get value of TestRailID
	public static String getTestRailID() {
		return testRailID;
	}

	public static void setCopiedRandomText(String text) {

		if (getRandomCopiedCount() == 0) {
			randomlabelText = text;
			randomcopiedtextValues.add(text);
		} else {
			randomcopiedtextValues.add(text);
		}
		globalRandomValues.add(text);
		int size = globalRandomValues.size();
		if (size - 1 == 0) {
			ExtentCucumberAdapter.addTestStepLog("@globalRanodmCopiedText -> " + globalRandomValues.get(size - 1));
		} else {
			ExtentCucumberAdapter
					.addTestStepLog("@globalRanodmCopiedText" + (size) + " -> " + globalRandomValues.get(size - 1));
		}
		setRandomCopiedCount(getRandomCopiedCount() + 1);
	}

	public static String getCopiedRandomText() {
		return randomlabelText;
	}

	// To get value of columnIndex, copied from element
	public static int getColumnIndex() {
		return columnIndex;
	}

	// To set value of columnIndex, copied from element
	public static void setColumnIndex(int number) {
		columnIndex = number;
	}

	// To set vale of APIURL for API execution
	public static void setAPIURL(String text) {
		apiUrl = text.replace("'", "");
	}

	// To get value of APIURL for API execution
	public static String getAPIURL() {
		return apiUrl;
	}

	// To set value of MethodType for API execution
	public static void setMethodType(String text) {
		methodType = text;
	}

	// To get value of MethodType for API execution
	public static String getMethodType() {
		return methodType;
	}

	// To set value of RequestParameters for API execution
	public static void setRequestParameters(String text) {
		requestParameters = text;
	}

	// To get value of RequestParameters for API execution
	public static String getRequestParameters() {
		return requestParameters;
	}

	// To set value of APIHeaders for API execution
	public static void setAPIHeaders(String text) {
		apiHeaders = text;
	}

	// To get value of APIHeaders for API execution
	public static String getAPIResponseHeaders() {
		return responseHeader;
	}

	public static void setAPIResponseHeaders(String text) {
		responseHeader = text;
	}

	// To get value of APIHeaders for API execution
	public static String getAPIHeaders() {
		return apiHeaders;
	}

	// To set value of APIendpoint for API execution
	public static void setAPIEndpoint(String text) {
		apiEndpoint = text;
	}

	// To get value of APIendpoint for API execution
	public static String getAPIEndpoint() {
		return apiEndpoint;
	}

	// To set value of APIparameter for API execution
	public static void setAPIParameter(String text) {
		apiParameter = text;
	}

	public static void setAPIResponse(Response text) {
		apiResponse = text;
	}

	public static Response getAPIResponse() {
		return apiResponse;
	}

	// To set value of Basic auth for API execution
	public static String getBasicAuth() {
		return basicAuth;
	}

	// To set value of Basic auth for API execution
	public static void setBasicAuth(String text) {
		basicAuth = text;
	}

	// To get value of APIparameter for API execution
	public static String getAPIParameter() {
		return apiParameter;
	}

	public static void setTestData(String text) {
		testData = text;
	}

	public static String getTestData() {
		return testData;
	}

	// To generate random number of 5 digits
	public static String getRandomNumberString() {
		int number = RANDOM.nextInt(100000);
		return String.format("%05d", number);
	}

	// To set value of applicationType
	public static void setApplicationType(String type) {
		applicationType = type;
	}

	// To get value of applicationType
	public static String getApplicationType() {
		return applicationType;
	}

	public static String getMaxhour() {

		return maxHour;
	}

	public static void setMaxhour(String text) {
		maxHour = text;
	}

	// To set value of copiedCount variable
	public static void setCopiedCount(int number) {
		copiedCount = number;
	}

	public static void setRandomCopiedCount(int number) {
		randomcopiedCount = number;
	}

	// To get value of copiedCount, copied from element's size
	public static int getCopiedCount() {
		return copiedCount;
	}

	public static int getRandomCopiedCount() {
		return randomcopiedCount;
	}

	public static void setAlpha32Count(int number) {
		alpha32Count = number;
	}

	public static int getAlpha32Count() {
		return alpha32Count;
	}

	public static void setAlpha64Count(int number) {
		alpha64Count = number;
	}

	public static int getAlpha64Count() {
		return alpha64Count;
	}

	public static void setMaximumDuration(String text) {
		maxDuration = text;
	}

	public static String getMaximumDuration() {
		return maxDuration;
	}

	public static void setMinimumDuration(String text) {
		minDuration = text;
	}

	public static String getMinimumDuration() {
		return minDuration;
	}

	// To set value of LabelText variable if copied action is used once in a test
	// case, else to set values in copiedValues list
	public static void setCopiedTextKey(String key, String text) {

		dict.put(key, text);

	}

	public static void setGlobalValues(String text) {

		globalValues.add(text);

	}

	public static void setDBValue(String text) {
		dbValue = text;
	}

	public static String getDBValue() {
		return dbValue;
	}

	public static void setCopiedList(List text) {
		copiedList = text;
	}

	public static List getCopiedList() {
		return copiedList;
	}

	public static String getAppUrl() {
		return appUrl;
	}

	public static void setAppUrl(String appUrl) {
		CommonUtil.appUrl = appUrl;
	}

	// To get value by index of copiedValues list by passing index number
	public static String getCopiedCountText(String text) {
		return copiedValues.get(Integer.parseInt(text) - 1);
	}

	public static String getGlobalText(String text) {
		return globalValues.get(Integer.parseInt(text) - 1);
	}

	public static String getRandomCopiedCountText(String text) {
		return randomcopiedtextValues.get(Integer.parseInt(text) - 1);
	}

	public static String getAlphaNum32CopiedCountText(String text) {
		return randomAlpha32List.get(Integer.parseInt(text) - 1);
	}

	public static String getAlphaNum64CopiedCountText(String text) {
		return randomAlpha64List.get(Integer.parseInt(text) - 1);
	}

	public static String getAlphaNum32CopiedText() {
		return alphaNumber32;
	}

	public static String getRandomCopiedCountNumber(String text) {
		return randomcopiednumberValues.get(Integer.parseInt(text) - 1);
	}

	// To empty the copiedValues list
	public static void setCopiedCountTextNull() {
		copiedValues.clear();
		randomcopiednumberValues.clear();
		randomcopiedtextValues.clear();
	}

	public static List<String> getCopiedTextList() {
		return copiedValues;
	}

	// To get value of labelText, copied from element
	public static String getCopiedText() {
		return labelText;
	}

	public static String getGlobalRandomText(String text) {
		return globalRandomValues.get(Integer.parseInt(text) - 1);
	}

	public static String getApipayloadDict(String text) {
		for (Map.Entry<String, String> entry : apiPayloadDictionary.entrySet()) {
			text = text.replace("$" + entry.getKey(), entry.getValue());
		}
		ExtentCucumberAdapter.addTestStepLog("Actual value: " + text);
		return text;
	}

	public static String getValueFromAPiResponse(String text) {
		for (Map.Entry<String, String> entry : apiResponseDictionary.entrySet()) {
			text = text.replace("@" + entry.getKey(), entry.getValue());
		}
		return text;
	}

	public static String getApiResponseDict(String text) {
		return apiResponseDictionary.get(text);
	}

	public static void setCopiedRandomText1(String text, String varName) {
		if (getRandomCopiedCount() == 0) {
			randomlabelText = text;
			randomcopiedtextValues.add(text);
			randomUsercopiedtextValues.add(text);
		} else {
			randomcopiedtextValues.add(text);
			randomUsercopiedtextValues.add(text);
		}
		globalUserValues.put(varName, text);
		globalRandomValues.add(text);
		ExtentCucumberAdapter.addTestStepLog("@" + varName + " --> " + globalUserValues.get(varName));
		setRandomCopiedCount(getRandomCopiedCount() + 1);
	}

	public static void setCopiedRandomNumber1(String number, String varName) {
		if (getRandomCopiedCount() == 0) {
			randomlabelNumber = Integer.parseInt(number);
			randomcopiednumberValues.add(number);
		} else {
			randomcopiednumberValues.add(number);
		}
		globalUserValues.put(varName, number);
		globalRandomValues.add(number);
		ExtentCucumberAdapter.addTestStepLog("@" + varName + " --> " + globalUserValues.get(varName));
		setRandomCopiedCount(getRandomCopiedCount() + 1);
	}

	public static void setCopiedAlphaNumber32(String number) {
		if (getAlpha32Count() == 0) {
			alphaNumber32 = number;
			randomAlpha32List.add(number);
		} else {
			randomAlpha32List.add(number);
		}
		setAlpha32Count(getAlpha32Count() + 1);
	}

	public static void setCopiedAlphaNumber64(String number) {
		if (getAlpha64Count() == 0) {
			alphaNumber64Epoch = number;
			randomAlpha64List.add(number);
		} else {
			randomAlpha64List.add(number);
		}
		setAlpha64Count(getAlpha64Count() + 1);
	}

	private static String generateRandomNumber(int length) {
		StringBuilder sb = new StringBuilder();
		String numbers = NUMBER_CONSTANT;

		while (sb.length() < length) {
			int index = RANDOM.nextInt(numbers.length());
			sb.append(numbers.charAt(index));
		}
		return sb.toString();
	}

	private static String generateRandomText(int length) {
		StringBuilder sb = new StringBuilder();
		String chars = ALFA_CONSTANT;

		while (sb.length() < length) {
			int index = RANDOM.nextInt(chars.length());
			sb.append(chars.charAt(index));
		}
		return sb.toString();
	}

	// Get vale from xml file by passing xml file path and key name
	public static String getXMLData(String filePath, String keyName) {
		String value = "";
		try {
			File file = new File(filePath);
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(file);
			value = document.getElementsByTagName(keyName).item(0).getTextContent();
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return value;
	}

	public static void setApiResponseDict(String key, String text) {
		String constSymbol = "\\[\\]";
		String checkConstatnt = "check --";
		if (text.contains("--")) {

			String[] verifystr = text.split("and");
			if (verifystr[0].contains(checkConstatnt) && verifystr[0].contains("[]")) {

				String[] str = verifystr[0].replace("\\{", "").replace(checkConstatnt, "").trim().split("\\.");
				int size = getAPIResponse().jsonPath().getList(str[0].replace(constSymbol, "")).size();

				String[] sanitizeStr = verifystr[0].replace("\\{", "").replace(checkConstatnt, "").trim().split("=");
				String checkStrbool;
				String[] checkStrkey = new String[2];

				checkStrbool = sanitizeStr[1];
				if (sanitizeStr[0].contains("[]")) {
					checkStrkey = sanitizeStr[0].split(constSymbol);
				}

				String sanitizegetStr = verifystr[1].replace("get --", "").replace("}", "").trim();
				String[] getstrkey = sanitizegetStr.split(constSymbol);

				for (int i = 0; i < size - 1; i++) {
					if (getAPIResponse().jsonPath().getString(checkStrkey[0] + "[" + i + "]" + checkStrkey[1])
							.equalsIgnoreCase(checkStrbool)) {
						apiResponseDictionary.put("sku" + i,
								getAPIResponse().jsonPath().getString(getstrkey[0] + "[" + i + "]" + getstrkey[1]));
					}
				}
			}

		} else {
			String[] splitText = text.split(",");
			for (int i = 0; i < splitText.length; i++) {
				String value = getAPIResponse().jsonPath().getString(splitText[i]);
				if (value.contains(",")) {
					value = value.replace(",", "#");
				}
				apiResponseDictionary.put(key + "." + splitText[i], value);
			}
		}
	}

	public static Map<String, String> readValuesFromJson(String filePath) {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, String> dynamicValues = new HashMap<>();

		try {
			List<Map<String, String>> list = objectMapper.readValue(new File(filePath),
					new TypeReference<List<Map<String, String>>>() {
					});
			for (Map<String, String> item : list) {
				dynamicValues.put(item.get("variable"), item.get("value"));
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return dynamicValues;
	}

	public static String replacePlaceholders(String input, Map<String, String> dynamicValues) {
		Pattern pattern = Pattern.compile("\\{\\{(.*?)\\}\\}");
		Matcher matcher = pattern.matcher(input);

		StringBuffer result = new StringBuffer();

		while (matcher.find()) {
			String placeholder = matcher.group(1);
			String replacement = dynamicValues.getOrDefault(placeholder, "");
			matcher.appendReplacement(result, replacement);
		}
		matcher.appendTail(result);

		return result.toString();
	}

	private static String getDataFromJson(String variableName) {
		try {
			if (variableName.contains("{{") && variableName.contains("}}")) {
				String envJSON = Paths.get(Constants.PROJECT_PATH, "src", "test", "java", "TestDataEnv.json")
						.toString();
				Map<String, String> dynamicValues = readValuesFromJson(envJSON);
				variableName = replacePlaceholders(variableName, dynamicValues);
				return variableName;
			}
			return variableName;
		} catch (Exception e) {
			return variableName;
		}
	}

	/**
	 * Retrieves data from a YAML file based on the provided key. If the parameter
	 * contains a specific flag, it returns one of the following: - Random number -
	 * Random text - Random email ID - Formatted date/time
	 * 
	 * @param key The key used to fetch data from the YAML file.
	 * @return The corresponding value, which may be a random number, text, email,
	 *         or formatted date/time.
	 */
	public static String getData(String variableName) {
		String randomNumber = "randomnumber_";
		String value = null;
		String temp = variableName;
		temp = System.getProperty(temp);
		if (temp != null)
			variableName = temp;
		variableName = YMLUtil.getYMLData(variableName);
		variableName = variableName.replace("_space_", " ").replace(EMPTY, "");
		variableName = DbHelper.queryCopiedText(variableName);
		variableName = DbHelper.alphaNumeric32CopiedText(variableName);
		variableName = DbHelper.alphaNumeric64CopiedText(variableName);
		variableName = DbHelper.replaceGlobalText(variableName);
		variableName = DbHelper.textRandomCopiedText(variableName);
		variableName = DbHelper.textRandomCopiedNumber(variableName);
		variableName = DbHelper.globalRandomText(variableName);
		variableName = getDataFromJson(variableName);

		variableName = getCopiedTextKey(variableName);

		try {
			if (variableName.contains("::random")) {
				Pattern randomNumberPattern = Pattern.compile("@(\\w+)::randomnumber");
				Matcher numberMatcher = randomNumberPattern.matcher(variableName);
				while (numberMatcher.find()) {
					String key = numberMatcher.group(1); // Extract the key (e.g., "MRN")
					if (!globalUserValues.containsKey(key)) {
						value = generateRandomNumber(5);
						setCopiedRandomNumber1(value, key);
						value = variableName.replace("@" + key + "::randomnumber", value);
						variableName = value;
					}
				}

				Pattern randomTextPattern = Pattern.compile("@(\\w+)::randomtext");
				Matcher textMatcher = randomTextPattern.matcher(variableName);
				while (textMatcher.find()) {
					String key = textMatcher.group(1); // Extract the key (e.g., "FirstName")
					if (!globalUserValues.containsKey(key)) {
						value = generateRandomText(7);
						setCopiedRandomText1(value, key);
						value = variableName.replace("@" + key + "::randomtext", value);
						variableName = value;
					}
				}
			}

			if (variableName.contains(randomNumber)) {
				String[] numberlist = variableName.split("_");

				if (numberlist.length == 2) {
					int length = Integer.parseInt(variableName.replace(randomNumber, ""));
					String numbers = NUMBER_CONSTANT;
					StringBuilder sb = new StringBuilder();

					while (sb.length() < length) {
						int index = RANDOM.nextInt(numbers.length());
						sb.append(numbers.charAt(index));
					}

					value = sb.toString();
					setCopiedRandomNumber(value);
				}

				if (numberlist.length == 3) {
					variableName = variableName.replace(randomNumber, "");
					int length = Integer.parseInt(numberlist[2]);
					String chars = NUMBER_CONSTANT;
					StringBuilder sb = new StringBuilder();

					while (sb.length() < length) {
						int index = RANDOM.nextInt(chars.length());
						sb.append(chars.charAt(index));
					}

					value = numberlist[0] + sb.toString();
				}
			}

			if (variableName.contains("@randomnumber")) {
				String numbers = NUMBER_CONSTANT;
				StringBuilder sb = new StringBuilder();

				while (sb.length() < 5) {
					int index = RANDOM.nextInt(numbers.length());
					sb.append(numbers.charAt(index));
				}

				value = sb.toString();
				setCopiedRandomNumber(value);
				variableName = variableName.replace("@randomnumber", value);
				value = variableName;
			}
			if (variableName.contains("@randomtext")) {
				String chars = ALFA_CONSTANT;
				StringBuilder sbString = new StringBuilder();

				while (sbString.length() < 7) {
					int index = RANDOM.nextInt(chars.length());
					sbString.append(chars.charAt(index));
				}

				value = sbString.toString();
				setCopiedRandomText(value);
				variableName = variableName.replace("@randomtext", value);
				value = variableName;
			}
			if (variableName.contains("@32DigitAplphaNum")) {
				String digitToken32 = RandomStringUtils.randomAlphanumeric(32);
				setCopiedAlphaNumber32(digitToken32);
				variableName = variableName.replace("@32DigitAplphaNum", digitToken32);
			}
			if (variableName.contains("@64DigitAplphaNumEpoch")) {
				String digitToken64 = RandomStringUtils.randomAlphanumeric(64);
				long epochTime = System.currentTimeMillis() / 1000;
				digitToken64 = digitToken64 + "." + epochTime;
				setCopiedAlphaNumber64(digitToken64);
				variableName = variableName.replace("@64DigitAplphaNumEpoch", digitToken64);
			}

			if (variableName.contains("@md5Hash")) {
				String xyz = variableName.replace(" ", "").replace(",\"hash\":\"@md5Hash\"", "");
				xyz = xyz.replace(",\"validateHash\":true", "");

				MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] messageDigest = md.digest(xyz.getBytes(StandardCharsets.UTF_8));
				BigInteger no = new BigInteger(1, messageDigest);

				StringBuilder hashText = new StringBuilder(no.toString(16));
				while (hashText.length() < 32) {
					hashText.insert(0, "0");
				}

				variableName = variableName.replace("@md5Hash", hashText.toString());
			}
			if (variableName.contains("@copiedtext")) {
				String message = CommonUtil.getCopiedText();
				value = variableName.replace("@copiedtext", message);
				variableName = value;

			}
			if (variableName.contains("@copiednumber")) {
				String message = String.valueOf(CommonUtil.getCopiedNumber());
				value = variableName.replace("@copiednumber", message);
				variableName = value;

			}
			if (variableName.contains("@datetime_utc")) {
				Instant instant = Instant.now();
				String[] sb = instant.toString().split(":");
				value = variableName.replace("@datetime_utc", sb[0]);
				variableName = value;
			}
			if (variableName.contains("@currentDirectory")) {
				value = variableName.replace("@currentDirectory", System.getProperty("user.dir"));
			}
			if (variableName.contains("@convertDate_")) {
				String[] splitText = variableName.split("_");
				SimpleDateFormat format1 = new SimpleDateFormat(splitText[2]);
				SimpleDateFormat format2 = new SimpleDateFormat(splitText[3]);
				Date date = format1.parse(splitText[1]);
				value = format2.format(date);
			}
			if (variableName.contains("@currentdate")) {
				String sb = "";
				if (variableName.contains("@currentdate_")) {
					String[] vnames = variableName.split(",");
					for (int i = 0; i < vnames.length; i++) {
						if (vnames[i].contains("@currentdate_")) {
							String[] dates = vnames[i].split("_");
							Date today = new Date();
							Date date = new Date(today.getTime() + (1000 * 60 * 60 * 24 * Integer.parseInt(dates[1])));
							sb = new SimpleDateFormat("yyyy-MM-dd").format(date);
							value = variableName.replace(vnames[i], sb);
							variableName = value;
						}

					}

				}
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				Date currentDate = new Date();
				sb = dateFormat.format(currentDate);
				value = variableName.replace("@currentdate", sb);
				variableName = value;

			}
			if (variableName.contains("@epochtime")) {
				String sb = "";
				String eTime = "@epochtime_";
				if (variableName.contains(eTime)) {
					String[] vnames = variableName.split(",");
					for (int i = 0; i < vnames.length; i++) {
						if (vnames[i].contains(eTime)) {
							String[] dates = vnames[i].split("_");
							Date today = new Date();
							Date date;
							if (dates[1].contains("'")) {
								String[] minutes = dates[1].split("'");
								date = new Date(today.getTime() + ((1000 * 60 * 60 * 24 * Integer.parseInt(minutes[0]))
										+ (1000 * 60 * Integer.parseInt(minutes[1]))));
							} else
								date = new Date(today.getTime() + (1000 * 60 * 60 * 24 * Integer.parseInt(dates[1])));

							Long epochTime = date.getTime();
							sb = Long.toString(epochTime);
							value = variableName.replace(eTime + dates[1] + "_", sb);
							variableName = value;
						}

					}

				}
				Date today = new Date();
				Date date = new Date(today.getTime());
				long epoch = date.getTime();

				value = variableName.replace("@epochtime", Long.toString(epoch));
				variableName = value;

			} else if (variableName.contains("randomdate_yyyy/dd/mm")) {
				DateFormat dateFormat = new SimpleDateFormat("yyyy/dd/MM");
				Date currentDate = new Date();
				Calendar c = Calendar.getInstance();
				c.setTime(currentDate);
				int randomNumb = ThreadLocalRandom.current().nextInt(1, 10);
				c.add(Calendar.DATE, randomNumb);
				Date currentDatePlusOne = c.getTime();
				value = dateFormat.format(currentDatePlusOne);
			} else if (variableName.contains("randomtext_")) {
				String chars = ALFA_CONSTANT;
				StringBuilder sbString = new StringBuilder();

				while (sbString.length() < 7) {
					int index = RANDOM.nextInt(chars.length());
					sbString.append(chars.charAt(index));
				}

				value = sbString.toString();
			} else if (variableName.contains("randomemail_")) {
				String rEmail = "randomemail_";
				variableName = variableName.replace("_space_", " ").replace(EMPTY, "");

				String[] emailList = variableName.split("@");
				String randomNumber1 = String.format("%05d", RANDOM.nextInt(100000)); // Generates a 5-digit number

				if (emailList.length > 1) {
					value = emailList[0].replace(rEmail, "") + randomNumber1 + "@" + emailList[1];
				} else {
					value = emailList[0].replace(rEmail, "") + randomNumber1 + "@gmail.com";
				}
			} else if (variableName.contains("dateformat_")) {
				String dateConstant = "dateformat_";

				String[] dateFormatKey = variableName.split("_");
				if (dateFormatKey.length > 2) {
					int daysNumber = Integer.parseInt(dateFormatKey[2]);
					variableName = variableName.replace(dateConstant, " ").replace(EMPTY, "");
					Date currentDate = new Date();
					Calendar c = Calendar.getInstance();
					c.setTime(currentDate);
					c.add(Calendar.DATE, daysNumber);

					value = (new SimpleDateFormat(dateFormatKey[1]).format(c.getTime())).trim();

				} else {
					variableName = variableName.replace(dateConstant, " ").replace(EMPTY, "");
					value = (new SimpleDateFormat(variableName).format(Calendar.getInstance().getTime())).trim();
				}
			} else if (variableName.contains("timeformat_")) {
				variableName = variableName.replace("timeformat_", " ").replace(EMPTY, "");
				value = (new SimpleDateFormat(variableName).format(Calendar.getInstance().getTime())).trim();
			} else {
				if (value == null) {
					value = variableName;
				}
			}
		} catch (

		Exception ex) {
			value = variableName;
		}
		ExtentCucumberAdapter.addTestStepLog("Value : " + value);
		return value;
	}

	// To verify, if parameter passed is in json format or not
	public static boolean isValidJson(String jsonInString) {
		try {
			gson.fromJson(jsonInString, Object.class);
			return true;
		} catch (com.google.gson.JsonSyntaxException ex) {
			return false;
		}
	}

	// To convert result data into list
	public static List<HashMap<String, Object>> resultSetToArrayList(ResultSet rs) {
		List<HashMap<String, Object>> list = new ArrayList<>();
		try {
			ResultSetMetaData md = rs.getMetaData();
			int columns = md.getColumnCount();

			while (rs.next()) {
				HashMap<String, Object> row = new HashMap<>();
				for (int i = 1; i <= columns; i++) {
					row.put(md.getColumnName(i), rs.getObject(i));
				}
				list.add(row);
			}
		} catch (SQLException e) {
			throw new CustomException("Error processing ResultSet", e); // Better error handling
		}
		return list;
	}

	public static boolean verifyTheDownloadedFile(String filePath) {
		sleep(5);

		if (filePath.contains("Extention_")) {
			String[] arr = filePath.split("_");
			String fileType = arr[1];
			File dir = new File(Constants.PROJECT_PATH);
			FileFilter fileFilter = new WildcardFileFilter("*.*");
			File[] files = dir.listFiles(fileFilter);

			if (files.length > 0) {
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				File theNewestFile = files[0];
				String fileExtension = CommonUtil.getFileExtension(theNewestFile);

				return fileExtension.equals(fileType);
			}
		} else {
			File dir = new File(filePath);
			FileFilter fileFilter = new WildcardFileFilter("*.*");
			File[] files = dir.listFiles(fileFilter);

			if (files.length > 0) {
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				File theNewestFile = files[0];
				log.info("Going to validate file name: " + theNewestFile.getAbsolutePath());

				long date = theNewestFile.lastModified();
				Date fileDate = new Date(date);
				Date currentDate = new Date();
				currentDate.setTime(currentDate.getTime() - 30000);

				if (fileDate.after(currentDate)) {
					log.info("Modify current date");
					return true;
				}
			}
		}
		return false;
	}

	public static String findLatestFileFromPathBasedOnType(String filePath, String fileType) {
		sleep(2);
		String path = null;
		File theNewestFile = null;
		File dir = new File(filePath);
		FileFilter fileFilter = new WildcardFileFilter("*." + fileType);
		File[] files = dir.listFiles(fileFilter);

		if (files.length > 0) {
			/** The newest file comes first **/
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			theNewestFile = files[0];
		}
		path = theNewestFile.getAbsolutePath();
		return path;
	}

	public static boolean validateContent(String content, String logFilePath) {
		String logFile = findLatestFileFromPathBasedOnType(logFilePath, "txt");
		log.info("Unzip log file path " + logFile);
		File file = new File(logFile);
		List<Line> lines = Unix4j.grep(Grep.Options.lineNumber, content, file).toLineList();
		log.info("Content " + content + " repeated " + lines.size() + " times");
		return !lines.isEmpty();
	}

	public static boolean unZipFile(String source, String destination) {
		try {
			ZipFile zipFile = new ZipFile(source);
			zipFile.extractAll(destination);
			return true;

		} catch (ZipException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean verifyFileTypeInDownloadPath(String fileNames) {
		sleep(5);
		try {
			String[] splits = fileNames.split("_");
			String filePath = splits[0];
			String fileType = splits[1];
			File theNewestFile = null;
			File dir = new File(filePath);
			FileFilter fileFilter = new WildcardFileFilter("*.*");
			File[] files = dir.listFiles(fileFilter);
			String fileExtension = null;

			if (files.length > 0) {
				/** The newest file comes first **/
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				theNewestFile = files[0];
				fileExtension = getFileExtension(theNewestFile);
			}
			long date = theNewestFile.lastModified();
			Date fileDate = new Date(date);
			Date currentDate = new Date();
			currentDate.setTime(currentDate.getTime() - 30000);
			if (fileDate.after(currentDate) && fileExtension != null && fileExtension.equalsIgnoreCase(fileType)) {
				return true;
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return false;
	}

	public static boolean verifyJsonWithApiResponse(String userInput) {
		Response res = getAPIResponse();
		ObjectMapper mapper = new ObjectMapper();
		boolean flag = false;

		if (!isValidJson(userInput)) {
			ExtentCucumberAdapter.addTestStepLog("Input JSON is invalid");
			return false;
		}

		try {
			Map<String, Object> responseMap = mapper.readValue(res.asString(),
					new TypeReference<Map<String, Object>>() {
					});
			Map<String, Object> userResponseMap = mapper.readValue(userInput, new TypeReference<Map<String, Object>>() {
			});

			responseMap.remove("_id");
			userResponseMap.remove("_id");

			if (!responseMap.keySet().equals(userResponseMap.keySet())) {
				log.info("Warning: Key sets are not equal");
			}

			if (responseMap.equals(userResponseMap)) {
				return true;
			}

			for (Map.Entry<String, Object> entry : userResponseMap.entrySet()) {
				String key = entry.getKey();
				Object userValue = entry.getValue();
				Object apiValue = responseMap.get(key);
				if (apiValue == null || userValue == null) {
					ExtentCucumberAdapter.addTestStepLog("Unable to verify key: \"" + key + "\"");
					return false;
				} else if (!responseMap.containsValue(userValue)) {
					for (Map.Entry<String, Object> responseEntry : responseMap.entrySet()) {
						Object responseValue = responseEntry.getValue();

						if (responseValue instanceof LinkedHashMap<?, ?> && responseEntry.getKey().contains(key)) {
							flag = processing(responseValue, userValue);
						}
					}
					if (!flag) {
						ExtentCucumberAdapter
								.addTestStepLog("Unable to verify: \"" + key + "\" with value: " + userValue);
						return false;
					}
				} else {
					ExtentCucumberAdapter
							.addTestStepLog("JSON key: \"" + key + "\" with value: " + userValue + " is verified");
					flag = true;
				}
			}
		} catch (Exception e) {
			ExtentCucumberAdapter.addTestStepLog("Error parsing JSON: " + e.getMessage());
		}

		return flag;
	}

	@SuppressWarnings("unchecked")
	private static boolean processing(Object mapper, Object usermapper) {
		if (!(mapper instanceof Map<?, ?>) || !(usermapper instanceof Map<?, ?>)) {
			return false;
		}

		Map<String, Object> map = (Map<String, Object>) mapper;
		Map<String, Object> userResponseMap = (Map<String, Object>) usermapper;

		Set<String> checkedKeys = new HashSet<>();
		boolean flag = false;

		for (Map.Entry<String, Object> userEntry : userResponseMap.entrySet()) {
			String userKey = userEntry.getKey();
			Object userValue = userEntry.getValue();

			if (userValue instanceof Map) {
				flag = processing(map.get(userKey), userValue);
			} else {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();

					if (userKey.contains(key) && userValue.equals(value)) {
						flag = true;
						if (checkedKeys.add(String.valueOf(userResponseMap.get(key)))) {
							log.info("checked" + key);
							break;
						}
					} else {
						flag = false;
					}
				}
			}
		}
		return flag;
	}

	private static String getFileExtension(File file) {
		String name = file.getName();
		int lastIndexOf = name.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return name.substring(lastIndexOf + 1);
	}

	public static String cleanMessage(String message) {
		if (message.contains("<") || message.contains(">")) {
			message = message.replace("<", "").replace(">", "");
		}
		return message;
	}

	@SuppressWarnings("unchecked")
	public static void storeElementInJson(String xpath, String elementHtml) {
		File file = new File(Constants.PROJECT_PATH + "/src/test/java/common/HtmlElement.json");
		JSONArray jsonArray = new JSONArray();
		boolean updated = false;
		String targetConstant = "target";

		try {
			// Load existing JSON data if file exists
			if (file.exists() && file.length() > 0) {
				try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
					JSONParser parser = new JSONParser();
					Object obj = parser.parse(reader);
					if (obj instanceof JSONArray) {
						jsonArray = (JSONArray) obj;
					}
				} catch (ParseException e) {
					log.error("Error parsing JSON file: " + e.getMessage(), e);
				}
			}

			// Normalize locator and target
			String normalizedXpath = xpath.replace("\\/\\/", "//").replace("\\/", "/");
			String normalizedElementHtml = elementHtml.replace("\\/", "/").replace("\"", "'");

			// Check if locator already exists in JSON
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject existingObject = (JSONObject) jsonArray.get(i);
				String existingLocator = (existingObject.get("locator"))!= null ? (existingObject.get("locator")).toString() : null;
				String existingTarget = (existingObject.get(targetConstant)) != null ? (existingObject.get(targetConstant)).toString() : null;

				if (existingLocator.equals(normalizedXpath)) {
					// If target is null/empty or different, update it
					if (existingTarget == null || !existingTarget.equals(normalizedElementHtml)) {
						existingObject.put(targetConstant, normalizedElementHtml);
						jsonArray.set(i, existingObject);
					}
					updated = true;
					break;
				}
			}

			// If locator does not exist, add a new entry
			if (!updated) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("locator", normalizedXpath);
				jsonObject.put(targetConstant, normalizedElementHtml);
				jsonArray.add(jsonObject);
				log.info("Stored new element data in JSON file: " + file.getAbsolutePath());
			}

			// Write the updated JSON array back to the file in pretty format
			try (FileWriter fileWriter = new FileWriter(file)) {
				Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
				fileWriter.write(gson.toJson(jsonArray));
			}

		} catch (IOException e) {
			log.error("Error writing to JSON file: " + e.getMessage(), e);
		}
	}

	public static void sleep(int seconds) {
		long milliseconds = seconds * 1000L;
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}