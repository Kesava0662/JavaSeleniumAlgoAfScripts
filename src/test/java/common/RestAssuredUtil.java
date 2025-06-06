package common;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@SuppressWarnings("all")
public class RestAssuredUtil {
	private static String apiUrl;
	private static String methodType;
	private static String requestParameters;
	private static String apiHeaders;
	private static String apiEndPoint;
	private static String basicAuth;
	private static String apiParameter;
	private static String responseHeader;
	private static String apiCmdUrl;
	private static Response apiResponse;
	private static String oAuth2;
	public static Map<String, String> apiPayloadDictionary = new HashMap<>();
	private static Map<String, String> apiResponseDictionary = new HashMap<>();
	static final Logger log = Logger.getLogger(RestAssuredUtil.class);

	private RestAssuredUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static String getApiCmdUrl() {
		return apiCmdUrl;
	}

	public static void setApiCmdUrl(String apiCmdUrl) {
		RestAssuredUtil.apiCmdUrl = apiCmdUrl;
	}

	// To set vale of APIURL for API execution
	public static void setApiUrl(String text) {
		apiUrl = text.replace("'", "");
	}

	public static String getApiUrl() {
		return apiUrl;
	}

	// To set value of MethodType for API execution
	public static void setMethodType(String text) {
		methodType = text;
	}

	public static String getMethodType() {
		return methodType;
	}

	// To set value of RequestParameters for API execution
	public static void setRequestParameters(String text) {
		requestParameters = text;
	}

	public static String getRequestParameters() {
		return requestParameters;
	}

	// To set value of APIHeaders for API execution
	public static void setAPIHeaders(String text) {
		apiHeaders = text;
	}

	public static String getAPIHeaders() {
		return apiHeaders;
	}

	// To set value of APIendpoint for API execution
	public static void setApiEndPoint(String text) {
		apiEndPoint = text;
	}

	public static String getApiEndPoint() {
		return apiEndPoint;
	}

	// To set value of Basic auth for API execution
	public static void setBasicAuth(String text) {
		basicAuth = text;
	}

	public static String getBasicAuth() {
		return basicAuth;
	}

	// To set value of APIparameter for API execution
	public static void setApiParameter(String text) {
		apiParameter = text;
	}

	public static String getApiParameter() {
		return apiParameter;
	}

	// To set value of apiResponse variable
	public static void setAPIResponse(Response text) {
		apiResponse = text;
	}

	public static Response getAPIResponse() {
		return apiResponse;
	}

	// To set value of apiResponse variable
	public static void setAPIOAuth2(String text) {
		oAuth2 = text;
	}

	public static String getAPIOAuth2() {
		return oAuth2;
	}

	// To get value of APIHeaders for API execution
	public static void setAPIResponseHeaders(String text) {
		responseHeader = text;
	}

	public static String getAPIResponseHeaders() {
		return responseHeader;
	}

	// To get value of ApiResponseDict variable ,fetch the value in dictionary
	public static String getApiResponseDict(String text) {
		return apiResponseDictionary.get(text);
	}

	public static String getApipayloadDict(String text) {

		for (Map.Entry<String, String> entry : apiPayloadDictionary.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (text.contains("$" + key)) {
				text = text.replace("$" + key, value);
			}
		}

		ExtentCucumberAdapter.addTestStepLog("Actual value: " + text);
		return text;
	}

	// To get value of apiResponseDictionary, fetch the value in dictionary
	public static String getValueFromAPiResponse(String text) {

		try {
			for (Map.Entry<String, String> entry : apiResponseDictionary.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();

				if (text.contains("@" + key)) {
					try {
						Integer.parseInt(value);
						text = text.contains("\"@" + key) ? text.replace("\"@" + key + "\"", value)
								: text.replace("@" + key, value);
					} catch (NumberFormatException e) {
						text = text.replace("@" + key, value);
					}
				}
			}
		} catch (Exception e) {
			log.info("Error processing API response: " + e.getMessage());
		}

		return text;
	}

	// To set value of ApiResponseDict variable ,store the value in dictionary
	public static void setApiResponseDict(String key, String text) {
		if ("NA".equalsIgnoreCase(text)) {
			return;
		}
		if (text.contains("--")) {
			processCheckAndGetCondition(text);
		} else {

			if (text.contains("::")) {
				storeApiResponseWithPrefix(text);
			} else {
				storeApiResponseWithoutPrefix(key, text);
			}
		}
	}

	private static void storeApiResponseWithPrefix(String text) {
		String[] actualKey = text.split("::");
		String[] splitText = actualKey[1].split(",");

		ExtentCucumberAdapter.addTestStepLog("Storing Values : ");

		for (String field : splitText) {
			String value = extractValueFromResponse(field);
			value = sanitizeValue(value);
			String formattedKey = actualKey[0] + "::" + field;

			ExtentCucumberAdapter.addTestStepLog(formattedKey + " : " + value);
			apiResponseDictionary.put(formattedKey, value);
		}
	}

	private static void storeApiResponseWithoutPrefix(String key, String text) {
		String[] splitText = text.split(",");

		ExtentCucumberAdapter.addTestStepLog("Storing Values : ");

		for (String field : splitText) {
			String value = extractValueFromResponse(field);
			value = sanitizeValue(value);
			String formattedKey = key + "." + field;

			ExtentCucumberAdapter.addTestStepLog(formattedKey + " : " + value);
			apiResponseDictionary.put(formattedKey, value);
		}
	}

	private static String extractValueFromResponse(String field) {
		try {
			XmlPath xmlPath = new XmlPath(getAPIResponse().asString());
			return xmlPath.getString(field);
		} catch (Exception e) {
			return getAPIResponse().jsonPath().getString(field);
		}
	}

	private static String sanitizeValue(String value) {
		return value.contains(",") ? value.replace(",", "#") : value;
	}

	private static void processCheckAndGetCondition(String text) {
		String[] verifyStr = text.split("and");

		if (!verifyStr[0].contains("check --") || !verifyStr[0].contains("[]")) {
			return;
		}

		String sanitizedStr = verifyStr[0].replace("{", "").replace("check --", "").trim();
		String[] str = sanitizedStr.split("\\.");
		int size = getAPIResponse().jsonPath().getList(str[0].replace("[]", "")).size();

		String[] checkStrParts = sanitizedStr.split("=");
		if (checkStrParts.length < 2) {
			return;
		}

		String checkStrBool = checkStrParts[1];
		String[] checkStrKey = checkStrParts[0].contains("[]") ? checkStrParts[0].split("\\[\\]")
				: new String[] { checkStrParts[0], "" };

		String sanitizedGetStr = verifyStr[1].replace("get --", "").replace("}", "").trim();
		String[] getStrKey = sanitizedGetStr.split("\\[\\]");

		for (int i = 0; i < size - 1; i++) {
			String checkKey = checkStrKey[0] + "[" + i + "]" + checkStrKey[1];
			String responseValue = getAPIResponse().jsonPath().getString(checkKey);

			if (responseValue.equalsIgnoreCase(checkStrBool)) {
				String getKey = getStrKey[0] + "[" + i + "]" + getStrKey[1];
				apiResponseDictionary.put("sku" + i, getAPIResponse().jsonPath().getString(getKey));
			}
		}
	}

	public static void launchAPIApplication() {
		String autUrl = "";
		if (RestAssuredUtil.apiCmdUrl != null) {
			autUrl = RestAssuredUtil.apiCmdUrl;
		} else {
			autUrl = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "APIURL");
		}
		if (autUrl == null || autUrl.isEmpty()) {
			autUrl = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "URL");
		}
		RestAssuredUtil.setApiUrl(autUrl);
	}

	private static boolean processing(Object mapper, Object usermapper) {
		HashMap<String, Object> map = new LinkedHashMap<>();
		map = (HashMap<String, Object>) mapper;
		Set<String> check = new HashSet<>();
		HashMap<String, Object> userResponseMap = new LinkedHashMap<>();
		userResponseMap = (HashMap<String, Object>) usermapper;
		boolean flag = false;
		for (String k : userResponseMap.keySet()) {
			if (!(userResponseMap.get(k) != null
					&& userResponseMap.get(k).getClass().equals(java.util.LinkedHashMap.class))) {
				for (String k1 : map.keySet()) {
					if (k/* map */.contains(k1)) {
						if (userResponseMap.get(k).equals(map.get(k1))) {
							flag = true;
							if (check.add(userResponseMap.get(k1).toString())) {
								break;
							}

						} else
							flag = false;
					}
				}
			} else {
				flag = processing(map.get(k), userResponseMap);
			}
		}
		check.clear();
		return flag;
	}

	// verify Json With ApiResponse
	public static boolean verifyJsonWithApiResponse(String userInput) {
		Response res = getAPIResponse();
		HashMap<String, Object> responseMap = new HashMap<>();
		HashMap<String, Object> userResponseMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();
		boolean flag = false;
		if (!CommonUtil.isValidJson(userInput)) {
			ExtentCucumberAdapter.addTestStepLog("Input json is invalid");
			return flag;
		}

		try {
			responseMap = mapper.readValue(res.asString(), new TypeReference<HashMap<String, Object>>() {
			});
			userResponseMap = mapper.readValue(userInput, new TypeReference<HashMap<String, Object>>() {
			});
			responseMap.remove("_id");
			userResponseMap.remove("_id");

			if (!responseMap.keySet().equals(userResponseMap.keySet())) {
				log.info("Warning: Keys set are not equal");
			}

			if (responseMap.equals(userResponseMap))
				return true;
			for (String k : userResponseMap.keySet()) {
				if (responseMap.get(k) == null && userResponseMap.get(k) == null) {
					continue;
				} else if (!(responseMap.containsValue(userResponseMap.get(k)))) {
					for (String d : responseMap.keySet()) {
						if (responseMap.get(d) != null
								&& responseMap.get(d).getClass().equals(java.util.LinkedHashMap.class)) {
							log.info("object" + responseMap.get(d));
							if (d.contains(k))
								flag = processing(responseMap.get(d), userResponseMap.get(k));
						}
					}
					if (!flag) {
						ExtentCucumberAdapter.addTestStepLog(
								"Unable to verify: " + "\"" + k + "\" with value:" + userResponseMap.get(k));
						flag = false;
					}
				} else if (responseMap.get(k) == null || userResponseMap.get(k) == null) {
					ExtentCucumberAdapter.addTestStepLog("Unable to verify: " + "\"" + k + "\"");
					flag = false;
				} else {
					ExtentCucumberAdapter.addTestStepLog(
							"JSON key: " + "\"" + k + "\" with value: " + userResponseMap.get(k) + " is verified");
					flag = true;
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
			ExtentCucumberAdapter.addTestStepLog("Input json is invalid");
		}
		return flag;
	}

	public static boolean executeApiAndVerifyResponse(String executeApiAndVerifyResponse) {
		boolean verified = false;
		int numericStatusCode;
		String content = "";
		executeApiAndVerifyResponse = CommonUtil.getData(executeApiAndVerifyResponse);
		executeApiAndVerifyResponse = RestAssuredUtil.getValueFromAPiResponse(executeApiAndVerifyResponse);
		Dictionary<String, String> parameters = new Hashtable();
		String apiURL = null, methodType = null, requestParameters = null, apiHeaders = null, apiEndPoint = null,
				apiParameter = null, basicAuth = null, oAuth = null, oAuthtoken = null;
		if (RestAssuredUtil.getApiUrl() != "") {
			apiURL = RestAssuredUtil.getApiUrl();
		}
		if (RestAssuredUtil.getMethodType() != "") {
			methodType = RestAssuredUtil.getMethodType();
		}
		String requestParams = RestAssuredUtil.getRequestParameters();
		if (requestParams != null && !requestParams.isEmpty() && !requestParams.equalsIgnoreCase("null")) {
			requestParameters = RestAssuredUtil.getRequestParameters();
		}
		if (RestAssuredUtil.getAPIHeaders() != "") {
			apiHeaders = RestAssuredUtil.getAPIHeaders();
		}
		if (RestAssuredUtil.getApiEndPoint() != "") {
			apiEndPoint = RestAssuredUtil.getApiEndPoint();
		}
		if (RestAssuredUtil.getApiParameter() != "") {
			apiParameter = RestAssuredUtil.getApiParameter();
		}
		if (RestAssuredUtil.getBasicAuth() != "") {
			basicAuth = RestAssuredUtil.getBasicAuth();
		}
		if (RestAssuredUtil.getAPIOAuth2() != "") {
			oAuth = RestAssuredUtil.getAPIOAuth2();
		}
		if (apiHeaders != null && apiHeaders.toUpperCase().contains("IMAGE/PNG")
				&& methodType.equalsIgnoreCase("GET")) {

			try {
				Response response = RestAssured.given().when().get(apiEndPoint).andReturn();
				int resCode = response.statusCode();
				ExtentCucumberAdapter.addTestStepLog("Status Code : " + resCode);
				if (resCode == 200 || resCode == 201 || resCode == 202 || resCode == 503) {
					verified = true;
					ExtentCucumberAdapter.addTestStepLog("Refer to the attached image to check output.");
				}
				byte[] image = response.getBody().asByteArray();
				String base64String = Base64.getEncoder().encodeToString(image);
				ExtentCucumberAdapter.addTestStepScreenCaptureFromPath("data:image/jpg;base64, " + base64String);

			} catch (Exception e) {
				ExtentCucumberAdapter.addTestStepLog("Error------------" + e);
				verified = false;
			}
			return verified;
		} else {
			RestAssured.useRelaxedHTTPSValidation();
			RestAssured.baseURI = apiURL;
			RequestSpecification httpRequest = RestAssured.given();
			EncoderConfig encoderconfig = new EncoderConfig();
			httpRequest.config(RestAssured.config()
					.encoderConfig(encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)));
			httpRequest.urlEncodingEnabled(false);
			String status = "Failed";
			if (basicAuth != null && !basicAuth.isEmpty()) {
				String[] basicAuthSplit = basicAuth.split(",");
				httpRequest.relaxedHTTPSValidation().auth().preemptive().basic(basicAuthSplit[0], basicAuthSplit[1]);
			}

			Response response = null;
			try {

				if (methodType.equalsIgnoreCase("POST") || methodType.equalsIgnoreCase("PUT")
						|| methodType.equalsIgnoreCase("GET") || methodType.equalsIgnoreCase("DELETE")
						|| methodType.equalsIgnoreCase("PATCH") || methodType.equalsIgnoreCase("OPTIONS")) {

					try {
						if (oAuth2 != null) {
							oAuthtoken = OAuth2Util.getAccessTokenfromOAuthAPI(oAuth2);
							httpRequest.header("Authorization", "Bearer " + oAuthtoken);
						}
						if (!apiHeaders.trim().isEmpty()) {
							String[] inputParameters = apiHeaders.split(",");
							for (String parameter : inputParameters) {
								String[] headerText = parameter.split(":");
								if (headerText.length > 1) {
									String headerValue = headerText[1];
									if (headerValue.contains("@colon")) {
										headerValue = headerValue.replace("@colon", ":");
									}
									httpRequest.header(headerText[0], headerValue);
								}
							}
						} else {
							if (!apiHeaders.contains("$") && !apiHeaders.isEmpty()) {
								httpRequest.header("content-type", apiHeaders);
							} else {
								httpRequest.header("content-type", "application/x-www-form-urlencoded");
							}
						}
					} catch (Exception e) {
						httpRequest.header("content-type", "application/x-www-form-urlencoded");
					}
					if (apiParameter != null && !"NA".equalsIgnoreCase(apiParameter)) {
						String[] paramList = apiParameter.split(",");
						for (int l = 0; l < paramList.length; l++) {
							String[] queryParamValues = paramList[l].split(":");
							httpRequest.queryParam(queryParamValues[0], queryParamValues[1]);
						}
					}
					if (requestParameters != null && !requestParameters.equalsIgnoreCase("NA")) {
						if (CommonUtil.isValidJson(requestParameters) || requestParameters.contains("[")) {
							if (apiHeaders.contains("x-www-form-urlencoded")) {
								JsonArray ob = new Gson().fromJson(requestParameters, JsonArray.class);
								ob.forEach(action -> {
									JsonObject obb = action.getAsJsonObject();
									String key = obb.get("key").getAsString();
									String value = obb.get("value").getAsString();
									httpRequest.formParam(key, value);
								});
							}
						     else if (apiHeaders.contains("multipart/form-data")) {
								JsonArray reqBody = new JsonParser().parse(requestParameters).getAsJsonArray();
								reqBody.forEach(action -> {
									JsonObject obj = action.getAsJsonObject();
									if ("file".equals(
											obj.has("type") ? obj.getAsJsonObject().get("type").getAsString() : "")) {
										String filepath = obj.get("src").getAsString();
										String key = obj.get("key").getAsString();
										httpRequest.multiPart(key, new File(filepath), "multipart/form-data");
									} else
										httpRequest.body(reqBody.getAsString());
								});
							} else
								httpRequest.body(requestParameters);
						} else if (apiHeaders.toLowerCase().contains("xml") || apiHeaders.toLowerCase().contains("html")
								|| apiHeaders.toLowerCase().contains("text")) {
							httpRequest.body(requestParameters);
						} else {
							try {
								JSONParser parser = new JSONParser();
								JSONObject json = (JSONObject) parser.parse(requestParameters);
								httpRequest.body(json.toJSONString());
							} catch (Exception e) {
								log.error(e.getMessage());
							}
						}
					} else {
						httpRequest.body("");
					}
					if (apiURL.contains(apiEndPoint)) {
						if (methodType.equalsIgnoreCase("GET")) {
							response = httpRequest.request(Method.GET);
						} else if (methodType.equalsIgnoreCase("PUT")) {
							response = httpRequest.request(Method.PUT);
						} else if (methodType.equalsIgnoreCase("DELETE")) {
							response = httpRequest.request(Method.DELETE);
						} else if (methodType.equalsIgnoreCase("PATCH")) {
							response = httpRequest.request(Method.PATCH);
						} else if (methodType.equalsIgnoreCase("OPTIONS")) {
							response = httpRequest.request(Method.OPTIONS);
						} else {
							response = httpRequest.request(Method.POST);
						}

					} else {
						if (methodType.equalsIgnoreCase("GET")) {
							response = httpRequest.request(Method.GET, apiEndPoint);
						} else if (methodType.equalsIgnoreCase("PUT")) {
							response = httpRequest.request(Method.PUT, apiEndPoint);
						} else if (methodType.equalsIgnoreCase("DELETE")) {
							response = httpRequest.request(Method.DELETE, apiEndPoint);
						} else if (methodType.equalsIgnoreCase("PATCH")) {
							response = httpRequest.request(Method.PATCH, apiEndPoint);
						} else if (methodType.equalsIgnoreCase("OPTIONS")) {
							response = httpRequest.request(Method.OPTIONS);
						} else {
							response = httpRequest.request(Method.POST, apiEndPoint);
						}

					}
				}
				numericStatusCode = response.getStatusCode();
				content = "Status code: " + numericStatusCode + "  Response: " + response.getBody().asString();
				Headers allHeaders = response.headers();
				RestAssuredUtil.setAPIResponseHeaders(allHeaders.toString());
				RestAssuredUtil.setAPIResponse(response);
			} catch (Exception e) {
				e.printStackTrace();
				numericStatusCode = 000;
				content = "Response: " + e.getMessage();
			}
			/*if(executeApiAndVerifyResponse.toUpperCase().contains("verify_schema".toUpperCase())) {
				String responseJson=response.getBody().asString();
				String schemaJson = SchemaVerification.extractSchemaFromRawResponse(executeApiAndVerifyResponse);
				//String schemaJson= executeApiAndVerifyResponse.split("verify_schema:")[1];
				
				SchemaVerification.verifySchema(schemaJson, responseJson);
			}*/
			if (executeApiAndVerifyResponse.toUpperCase().contains("verify_negative".toUpperCase())) {
				if (numericStatusCode == 400 || numericStatusCode == 500 || numericStatusCode == 401
						|| numericStatusCode == 415 || numericStatusCode == 000 || numericStatusCode == 422
						|| numericStatusCode == 404 || numericStatusCode == 403 || numericStatusCode == 503) {
					verified = true;
					status = "Passed";
					if (executeApiAndVerifyResponse.toUpperCase().contains("--")) {
						String[] splitNeagtiveValue = executeApiAndVerifyResponse.split("--");
						for (int i = 1; i < splitNeagtiveValue.length; i++) {
							if (content.contains(splitNeagtiveValue[i])) {
								verified = true;
								status = "Passed";
								ExtentCucumberAdapter
										.addTestStepLog("Verified values in API response: " + splitNeagtiveValue[i]);
							} else {
								verified = false;
								status = "Failed";
								ExtentCucumberAdapter
										.addTestStepLog(splitNeagtiveValue[i] + " is not present in API response.");
							}
						}
					}
				} else {
					verified = false;
					status = "Failed";
				}
			} else {
				if (response.getStatusLine().toUpperCase() == "OK" || numericStatusCode == 201
						|| numericStatusCode == 200 || numericStatusCode == 202 || numericStatusCode == 204
						|| numericStatusCode == 503) {
					verified = true;
					status = "Passed";
					if(executeApiAndVerifyResponse.toUpperCase().contains("verify_schema".toUpperCase())) {
						String responseJson=response.getBody().asString();
						String schemaJson = SchemaVerification.extractSchemaFromRawResponse(executeApiAndVerifyResponse);
						//String schemaJson= executeApiAndVerifyResponse.split("verify_schema:")[1];
						
						boolean statusCheck = SchemaVerification.verifySchema(schemaJson, responseJson);
						if(statusCheck)
							verified = true;
						else 
							verified = false;
					}
				} else {
					verified = false;
					status = "Failed";
				}
			}
			if (verified && !executeApiAndVerifyResponse.equalsIgnoreCase("NA")
					&& !executeApiAndVerifyResponse.toUpperCase().contains("verify_negative".toUpperCase())) {
				if (executeApiAndVerifyResponse.toUpperCase().contains("verifyCopiedList".toUpperCase())) {
					String passedString = "";
					List<String> copiedList = CommonUtil.getCopiedList();
					for (int i = 0; i < copiedList.size(); i++) {
						String verifyText = "";
						if (!executeApiAndVerifyResponse.toUpperCase()
								.contains("verifyCopiedList_nospace".toUpperCase())) {
							verifyText = (copiedList.get(i).split(" "))[0];
						} else {
							verifyText = copiedList.get(i);
						}
						if (!content.contains(verifyText)) {
							verified = false;
							ExtentCucumberAdapter
									.addTestStepLog(verifyText + " is not presnt in API response: " + content);
							break;
						}
						passedString = passedString + verifyText + ",";
						verified = true;
					}
					ExtentCucumberAdapter.addTestStepLog("Verified values in API response: " + passedString);
				} else {
					String beforeSchema = executeApiAndVerifyResponse;
					if(executeApiAndVerifyResponse.toUpperCase().contains("verify_schema".toUpperCase())) {
						
						String key = "\"verify_schema\":";

						int idx = executeApiAndVerifyResponse.indexOf(key);
						if (idx == -1) {
						    idx = executeApiAndVerifyResponse.indexOf("verify_schema:");
						}
						if (idx == -1) {
						    System.out.println("verify_schema not found");
						} else {
						    beforeSchema = executeApiAndVerifyResponse.substring(0, idx).trim();
						}
					}
					String[] splitArray = beforeSchema.split(",");
					for (int i = 0; i < splitArray.length; i++) {
						if (content.contains(splitArray[i]) == true) {
							verified = true;
							ExtentCucumberAdapter.addTestStepLog(splitArray[i] + " is verified in API output.");
						} else {
							verified = false;
							ExtentCucumberAdapter.addTestStepLog(splitArray[i] + " is not verified in API output.");
							break;
						}

					}
				}
			}
			if (apiHeaders != null && apiHeaders.contains("$")) {
				try {
					JSONParser parser = new JSONParser();
					JSONObject userObj = (JSONObject) parser.parse(content);
					String key = apiHeaders.replace("$", "");
					parameters.put(userObj.get(key).toString(), apiHeaders);
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
			}
			String printParam = content.replace("<", "&lt;");
			printParam = printParam.replace(">", "&gt;");
			ExtentCucumberAdapter.addTestStepLog(
					"Actual content: " + printParam + ", Expected content: " + executeApiAndVerifyResponse);
			RestAssuredUtil.setMethodType(null);
			RestAssuredUtil.setRequestParameters(null);
			RestAssuredUtil.setAPIHeaders(null);
			RestAssuredUtil.setApiEndPoint(null);
			RestAssuredUtil.setApiParameter(null);
			RestAssuredUtil.setBasicAuth(null);
			RestAssuredUtil.setAPIOAuth2(null);
			return verified;
		}
	}

}