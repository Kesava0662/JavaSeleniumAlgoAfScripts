package common;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.Optional;

public class OAuth2Util {
	private static final Logger log = Logger.getLogger(OAuth2Util.class);
	private static final int SLEEP_DURATION_SECONDS = 5;
	private static final String GRANT_TYPE = "grant_type";
	private static final String CLIENT_ID = "client_id";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";

	private OAuth2Util() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static String getAccessTokenfromOAuthAPI(String text) {
		try {

			JsonParser parser = new JsonParser();
			JsonObject obj = parser.parse(text).getAsJsonObject();

			String authUrl = getJsonValue(obj, "authUrl");
			String clientId = getJsonValue(obj, "clientId");
			String callbackUrl = getJsonValue(obj, "redirect_uri");
			String scope = getJsonValue(obj, "scope");
			String accessTokenUrl = getJsonValue(obj, "accessTokenUrl");
			String clientSecret = getJsonValue(obj, "clientSecret");
			String grantType = getJsonValue(obj, GRANT_TYPE);

			if ("authorization_code".equals(grantType)) {
				return handleAuthorizationCodeFlow(obj, authUrl, clientId, callbackUrl, scope, accessTokenUrl,
						clientSecret);
			} else if ("client_credentials".equals(grantType)) {
				return handleClientCredentialsFlow(accessTokenUrl, clientId, clientSecret);
			} else {
				return handlePasswordFlow(obj, accessTokenUrl, clientId);
			}
		} catch (Exception e) {
			log.error("Error fetching OAuth2 access token", e);
			throw new CustomException(e.getMessage(), e);
		}
	}

	private static String handleAuthorizationCodeFlow(JsonObject obj, String authUrl, String clientId,
			String callbackUrl, String scope, String accessTokenUrl, String clientSecret) {
		boolean useBrowser = obj.has("useBrowser") && Boolean.parseBoolean(obj.get("useBrowser").getAsString());
		if (!useBrowser) {
			return getPasswordGrantToken(obj, accessTokenUrl, clientId, clientSecret);
		}

		WebDriver browser = WebBrowser.getBrowser();
		try {
			String authorizationUrl = authUrl + "response_type=code&client_id=" + clientId + "&redirect_uri="
					+ callbackUrl + "&scope=" + scope;
			browser.get(authorizationUrl);
			processBrowserActions(obj, browser);
			sleep(SLEEP_DURATION_SECONDS);

			String currentUrl = browser.getCurrentUrl();
			return exchangeAuthorizationCode(currentUrl, accessTokenUrl, clientId, clientSecret, callbackUrl);
		} finally {
			browser.quit();
		}
	}

	private static void processBrowserActions(JsonObject obj, WebDriver browser) {
		JsonArray userBrowsers = obj.getAsJsonArray("useBrowserData");
		userBrowsers.forEach(action -> {
			JsonObject locatorObj = action.getAsJsonObject();
			String value = locatorObj.get("value").getAsString();
			String xpath = locatorObj.get("locator").getAsString();
			sleep(2);
			WebElement element = browser.findElement(By.xpath(xpath));
			if (!value.isEmpty()) {
				element.sendKeys(value);
			} else {
				element.click();
			}
		});
	}

	private static String exchangeAuthorizationCode(String currentUrl, String accessTokenUrl, String clientId,
			String clientSecret, String callbackUrl) {
		int codeStartIndex = currentUrl.indexOf("code=") + 5;
		int codeEndIndex = currentUrl.indexOf("&", codeStartIndex);
		if (codeEndIndex == -1) {
			codeEndIndex = currentUrl.length();
		}

		String encodedAuthorizationCode = currentUrl.substring(codeStartIndex, codeEndIndex);
		String decodedAuthorizationCode = encodedAuthorizationCode.replace("%2F", "/");

		Response response = RestAssured.given().formParam("code", decodedAuthorizationCode)
				.formParam(CLIENT_ID, clientId).formParam("client_secret", clientSecret)
				.formParam("redirect_uri", callbackUrl).formParam(GRANT_TYPE, "authorization_code")
				.post(accessTokenUrl);

		return logAccessToken(response);
	}

	private static String handleClientCredentialsFlow(String accessTokenUrl, String clientId, String clientSecret) {
		Response response = RestAssured.given().baseUri(accessTokenUrl).auth().preemptive()
				.basic(clientId, clientSecret).param(GRANT_TYPE, "client_credentials").post();

		return logAccessToken(response);
	}

	private static String handlePasswordFlow(JsonObject obj, String accessTokenUrl, String clientId) {
		String username = getJsonValue(obj, USERNAME);
		String password = getJsonValue(obj, PASSWORD);

		Response response = RestAssured.given().contentType("application/x-www-form-urlencoded")
				.formParam(GRANT_TYPE, PASSWORD).formParam(CLIENT_ID, clientId).formParam(USERNAME, username)
				.formParam(PASSWORD, password).post(accessTokenUrl);

		return logAccessToken(response);
	}

	private static String getPasswordGrantToken(JsonObject obj, String accessTokenUrl, String clientId,
			String clientSecret) {
		String username = getJsonValue(obj, "auth_username");
		String password = getJsonValue(obj, "auth_password");

		Response response = RestAssured.given().formParam(GRANT_TYPE, PASSWORD).formParam(CLIENT_ID, clientId)
				.formParam("client_secret", clientSecret).formParam(USERNAME, username).formParam(PASSWORD, password)
				.post(accessTokenUrl);

		return logAccessToken(response);
	}

	private static String logAccessToken(Response response) {
		String accessToken = response.jsonPath().getString("access_token");
		ExtentCucumberAdapter.addTestStepLog("Access token is: " + accessToken);
		return accessToken;
	}

	private static String getJsonValue(JsonObject obj, String key) {
		return Optional.ofNullable(obj.has(key) ? obj.get(key).getAsString() : "").orElse("");
	}

	private static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000L);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
