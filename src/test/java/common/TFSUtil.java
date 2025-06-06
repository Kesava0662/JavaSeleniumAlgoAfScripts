
package common;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import java.util.*;
import static io.restassured.RestAssured.given;

@SuppressWarnings("all")
public class TFSUtil {

	private static final String AUTHORIZATION = "Authorization";
	private static final String APPLICATION_JSON = "application/json";
	private static final String TFS_AUTH = "TFSAUTH";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String TFS_COLLECTION = "TPC_Region16";
	private static final String CONST_OUTCOME = "outcome";
	private static final String CONST_HOST = "https://";
	private static final String CONST_TEST_RUN_ID = "testRunId";
	private static final String CONST_RESULT_ID = "testResultId";
	private static final String CONST_POINT_ID = "testPointId";

	public static String yourTfsUrl;
	public static String tfsServer;
	public static String tfsProject;
	public static String stestSuiteId = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "TestSuiteId");
	public static String authKey = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, TFS_AUTH);
	public static int testSuiteId;
	public static String url = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "AUTH_HOST_URI");
	static final Logger log = Logger.getLogger(TFSUtil.class);
	private Response adsQueryResp;
	private String reqIdsCsv;
	private String testIdsCsv;
	private String adsFolderId;
	private List<String> uniqTestList;
	private List<String> nodeArray;

	public void setAdsResponse(Response response) {
		this.adsQueryResp = response;
	}

	public Response getAdsResponse() {
		return adsQueryResp;
	}

	public void setReqIdsCsv(String reqIdsCsv) {
		this.reqIdsCsv = reqIdsCsv;
	}

	public String getReqIdsCsv() {
		return reqIdsCsv;
	}

	public void setTestIdsCsv(String testIds) {
		this.testIdsCsv = testIds;
	}

	public String getTestIdsCsv() {
		return testIdsCsv;
	}

	public void setAdsFolderId(String folderId) {
		this.adsFolderId = folderId;
	}

	public String getAdsFolderId() {
		return adsFolderId;
	}

	public void setUniqTestList(List<String> testList) {
		this.uniqTestList = new ArrayList<>(testList);
	}

	public List<String> getUniqTestList() {
		return uniqTestList;
	}

	public void setNodeArray(List<String> nodeArrayList) {
		this.nodeArray = nodeArrayList;
	}

	public List<String> getNodeArray() {
		return nodeArray;
	}

	private static int getTestSuiteId() {
		try {
			testSuiteId = Integer.parseInt(stestSuiteId);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage());
		}
		return testSuiteId;

	}

	public int[] getTestPointId(int testCaseId) {
		int[] testIds = new int[2];
		int tstSuiteId = getTestSuiteId();

		RestAssured.baseURI = String.format("https://%s.com", tfsServer);

		String requestBody = String.format("{ \"PointsFilter\": { \"TestcaseIds\": [%d] } }", testCaseId);

		Response response = given().header(CONTENT_TYPE, APPLICATION_JSON).header(AUTHORIZATION, authKey)
				.body(requestBody)
				.post(String.format("/tfs/TPC_Region16/%s/_apis/test/points?api-version=6.0-preview.2", tfsProject))
				.then().extract().response();

		if (response.getStatusCode() != 200) {
			throw new IllegalStateException("API request failed with status: " + response.getStatusCode());
		}

		int len = response.jsonPath().getInt("points.size()");
		for (int i = 0; i < len; i++) {
			String suitePath = String.format("points[%d].suite.id", i);
			if (response.jsonPath().getInt(suitePath) == tstSuiteId) {
				testIds[0] = response.jsonPath().getInt(String.format("points[%d].id", i));
				testIds[1] = response.jsonPath().getInt(String.format("points[%d].testPlan.id", i));
				break;
			}
		}
		return testIds;
	}

	public int getTestRunId(int testCaseId, int testPointId, int testPlanId) {
		RestAssured.baseURI = CONST_HOST + tfsServer + ".com";

		String requestBody = String.format(
				"{\"runModel\":\"{\\\"title\\\":\\\"Automated Test Results\\\",\\\"iteration\\\":\\\"%s\\\",\\\"state\\\":2,\\\"testPlanId\\\":%d}\","
						+ "\"testResultCreationRequestModels\":\"[{\\\"testCaseId\\\":%d,\\\"testPointId\\\":%d}]\"}",
				tfsProject, testPlanId, testCaseId, testPointId);

		Response response = given().header(CONTENT_TYPE, APPLICATION_JSON).header(AUTHORIZATION, authKey)
				.body(requestBody)
				.post("/tfs/TPC_Region16/6c85a2d3-7c45-4990-b6b1-aa92429d388a/_api/_testrun/Create?__v=5").then()
				.extract().response();

		String sTestRunId = response.jsonPath().getString("testResultCreationResponseModels.id.testRunId");
		if (sTestRunId == null || sTestRunId.isEmpty()) {
			throw new IllegalStateException("Test Run ID not found in response.");
		}

		sTestRunId = sTestRunId.replaceAll("[\\[\\]]", ""); // Remove square brackets if present
		return Integer.parseInt(sTestRunId);
	}

	public List<Map<String, Object>> getTestPointIdList(String testCaseIds) {
		int length;
		int testPoint;
		int testPlan;
		List<Map<String, Object>> testList = new ArrayList<>();

		RestAssured.baseURI = String.format("https://%s.com", tfsServer);

		String requestBody = String.format("{ \"PointsFilter\": { \"TestcaseIds\": [%s] } }", testCaseIds);

		Response response = given().header(CONTENT_TYPE, APPLICATION_JSON).header(AUTHORIZATION, authKey)
				.body(requestBody)
				.post(String.format("/tfs/TPC_Region16/%s/_apis/test/points?api-version=6.0-preview.2", tfsProject))
				.then().extract().response();

		if (response.getStatusCode() != 200) {
			throw new IllegalStateException("API request failed with status: " + response.getStatusCode());
		}

		length = response.jsonPath().getInt("points.size()");
		for (int j = 0; j < length; j++) {
			Map<String, Object> testIds = new HashMap<>();
			String suitePath = String.format("points[%d].suite.id", j);

			if (response.jsonPath().getInt(suitePath) == Integer.parseInt(stestSuiteId)) {
				testPoint = response.jsonPath().getInt(String.format("points[%d].id", j));
				testPlan = response.jsonPath().getInt(String.format("points[%d].testPlan.id", j));
				testIds.put(CONST_POINT_ID, testPoint);
				testIds.put("testPlan", testPlan);
				testList.add(testIds);
			}
		}
		return testList;
	}

	public List<Map<String, Object>> getTestResultIds(List<Map<String, Object>> testList) {
		int len;
		int testRunId;
		int testResultId;
		int tstSuiteId = getTestSuiteId();
		List<Map<String, Object>> testResultsList = new ArrayList<>();
		Map<String, Object> testPlan = testList.get(0);

		int testPlanId = (int) testPlan.get("testPlan");
		RestAssured.baseURI = CONST_HOST + tfsServer + ".com";

		Response response = given().header(CONTENT_TYPE, APPLICATION_JSON).header(AUTHORIZATION, authKey).header(
				"accept",
				"application/json;api-version=6.0-preview.2;excludeUrls=true;enumsAsNumbers=true;msDateFormat=true;noArrayWrap=true")
				.body(bulkGenerateRunId(testList)).patch("/tfs/TPC_Region16/" + tfsProject + "/_apis/testplan/Plans/"
						+ testPlanId + "/Suites/" + tstSuiteId + "/TestPoint")
				.then().log().all().extract().response();
		JSONObject resultJson;
		JSONArray jsonArray = new JSONArray(response.body().asString());
		len = jsonArray.length();
		for (int i = 0; i < len; i++) {
			Map<String, Object> testResultsMap = new HashMap<>();
			resultJson = jsonArray.getJSONObject(i).getJSONObject("results");
			testRunId = resultJson.getInt("lastTestRunId");
			testResultId = resultJson.getInt("lastResultId");
			testResultsMap.put(CONST_TEST_RUN_ID, testRunId);
			testResultsMap.put(CONST_RESULT_ID, testResultId);
			testResultsList.add(testResultsMap);
		}
		log.info("Test Result List is : " + testResultsList);
		return testResultsList;
	}

	public void bulkUpdateTestResults(List<Map<String, Object>> testResults, String version) {
		log.info("Inside Bulk upload method");
		log.info("Version is :" + version);
		RestAssured.baseURI = CONST_HOST + tfsServer + ".com";

		Response response = given().header(CONTENT_TYPE, APPLICATION_JSON).header(AUTHORIZATION, authKey)
				.body(bulkUploadResultsToTFS(testResults, version))
				.post("/tfs/TPC_Region16/6c85a2d3-7c45-4990-b6b1-aa92429d388a/_api/_testresult/Update?__v=5").then()
				.log().all().extract().response();
		log.info("response: " + response.getBody().asString());

	}

	public void updateResultsToTFS(int testCaseId, int testPointId, int testRunId,
			List<Map<String, Object>> testSteps) {

		JSONObject payload = new JSONObject();
		payload.put("testCaseId", testCaseId);
		payload.put(CONST_POINT_ID, testPointId);
		payload.put(CONST_TEST_RUN_ID, testRunId);
		payload.put("testCaseStatus", "Passed");
		payload.put("comments", "comments to be added");
		payload.put("testSteps", testSteps);

		String requestBody = payload.toString();
		String lurl = String.format(
				"https://%s.com/TestRunUpdateService.svc/UpdateTest/Json?server=%s&collection=%s&project=%s", tfsServer,
				tfsServer, TFS_COLLECTION, tfsProject);

		RestAssured.baseURI = CONST_HOST + tfsServer + ".com";
		Response response = given().header(AUTHORIZATION, authKey).body(requestBody).post(lurl).then().extract()
				.response();

		log.info("Response from TFS: " + response.asString());
	}

	private static String bulkGenerateRunId(List<Map<String, Object>> testList) {

		List<JSONObject> testIds = new ArrayList<>();

		for (Map<String, Object> testPlan : testList) {
			JSONObject payload = new JSONObject();
			payload.put("id", testPlan.get(CONST_POINT_ID));
			payload.put("results", new JSONObject().put(CONST_OUTCOME, 2));
			testIds.add(payload);
		}

		log.info("Payload for Bulk Run Ids: " + testIds);
		return new JSONArray(testIds).toString();
	}

	private static String bulkUploadResultsToTFS(List<Map<String, Object>> testList, String version) {
		if (testList.isEmpty()) {
			log.warn("Test list is empty. No results to upload.");
			return "{}";
		}

		String testRunId = String.valueOf(testList.get(0).get(CONST_TEST_RUN_ID));
		StringBuilder resultsBuilder = new StringBuilder("[");

		for (Map<String, Object> testResult : testList) {
			String testResultId = String.valueOf(testResult.get(CONST_RESULT_ID));

			JSONObject testCaseResult = new JSONObject().put(CONST_OUTCOME, 2).put("state", 5);

			JSONArray actionResults = new JSONArray().put(new JSONObject().put(CONST_OUTCOME, 2)
					.put("comment", "Tested in Version: " + version).put("iterationId", 1));

			JSONObject resultPayload = new JSONObject().put("testCaseResult", testCaseResult)
					.put("actionResults", actionResults).put("actionResultDeletes", new JSONArray())
					.put("parameters", new JSONArray()).put("parameterDeletes", new JSONArray())
					.put(CONST_TEST_RUN_ID, testRunId).put(CONST_RESULT_ID, testResultId);

			if (resultsBuilder.length() > 1) {
				resultsBuilder.append(",");
			}
			resultsBuilder.append(resultPayload.toString());
		}

		resultsBuilder.append("]");

		JSONObject payload = new JSONObject().put("updateRequests", new JSONArray(resultsBuilder.toString()));

		log.info("Payload for Bulk Update: " + payload);
		return payload.toString();
	}

}
