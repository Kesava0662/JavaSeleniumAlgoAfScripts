package common;

import java.util.*;
import java.nio.charset.StandardCharsets;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AzureTFSUtil {

	static final Logger log = Logger.getLogger(AzureTFSUtil.class);

	private AzureTFSUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	private static String organization = "";
	private static String project = "";
	private static String pat = "";

	@SuppressWarnings("deprecation")
	public static void createAzureTFSIssue(String title, String description) {
		String azureCredentials = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH,
				"AzureIntegrationTFSCredentials");

		String[] credentials = azureCredentials.split(",");
		organization = credentials[0];
		project = credentials[1];
		pat = credentials[2];

		try {
			// Check if an issue with the same title already exists
			if (issueExists(title)) {
				log.info("An issue with the title '" + title + "' already exists.");
				return;
			}

			// Set the URL for the Azure DevOps REST API endpoint to create a new work item
			URL url = new URL("https://dev.azure.com/" + organization + "/" + project
					+ "/_apis/wit/workitems/$Issue?api-version=7.0");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// Set up authentication with PAT
			String auth = ":" + pat;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
			conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

			// Set the request method and headers
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json-patch+json");
			conn.setDoOutput(true);

			// Set up JSON array for the payload
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(new JSONObject().put("op", "add").put("path", "/fields/System.Title").put("value", title));
			jsonArray.put(new JSONObject().put("op", "add").put("path", "/fields/System.Description").put("value",
					description));

			String jsonPayload = jsonArray.toString();
			log.info("Generated JSON Payload: " + jsonPayload);
			// Send the JSON payload to the server
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			// Get the response code
			int responseCode = conn.getResponseCode();
			log.info("Response Code: " + responseCode);
			if (responseCode == 200 || responseCode == 201) {
				log.info("Work item created successfully.");
			} else {
				log.info("Failed to create work item.");
			}
			conn.disconnect();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	private static boolean issueExists(String title) {
		try {
			// URL to search for work items with the specified title
			URL searchUrl = new URL(
					"https://dev.azure.com/" + organization + "/" + project + "/_apis/wit/wiql?api-version=7.0");
			HttpURLConnection conn = (HttpURLConnection) searchUrl.openConnection();

			// Set up authentication with PAT
			String auth = ":" + pat;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
			conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

			// Set the request method and headers for the WIQL query
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			// Define the WIQL query to search for work items with the specified title
			String wiqlQuery = "{\"query\": \"Select [System.Id] From WorkItems Where [System.Title] = '" + title
					+ "'\"}";

			// Send the WIQL query to the server
			try (OutputStream os = conn.getOutputStream()) {
				byte[] input = wiqlQuery.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			// Get the response code
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				// Parse the response to check if any work items were found
				String responseBody = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
				JSONObject responseJson = new JSONObject(responseBody);
				JSONArray workItems = responseJson.optJSONArray("workItems");
				return workItems != null && workItems.length() > 0;
			} else {
				log.info("Failed to search for existing issues. Response Code: " + responseCode);
			}
			conn.disconnect();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return false;
	}
}
