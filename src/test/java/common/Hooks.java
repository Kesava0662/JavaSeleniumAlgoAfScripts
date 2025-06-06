package common;

import java.io.*;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.SoftAssert;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import JiraUtility.JiraServiceProvider;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.java.*;
import ScreenRecorder.ScreenRecorderUtil;
import java.lang.reflect.Field;
import java.util.stream.Collectors;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.PickleStepTestStep;

@SuppressWarnings("all")
public class Hooks {
	public static WebDriver driver;
	private static int passCount = 0;
	private static int failCount = 0;
	private static int skipCount = 0;
	private int currentStepDefIndex = 0;
	private static List<PickleStepTestStep> stepDefs;
	private static boolean scenarioName = true;
	public static Set<Cookie> cookies = null;
	public static boolean cookiesAdded;
	public static boolean apiScenario = false;
	private static final Logger log = Logger.getLogger(Hooks.class);
	public static boolean closeBrowser = true;
	public static boolean flag = true;
	public static SoftAssert softAssertions = new SoftAssert();
	private static List<Map<String, Object>> tstSteps = new ArrayList<>();
	private static List<String> tagsExecuted = new ArrayList<>();

	private static final boolean enableVideoCaptureForSuccess = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableVideoCaptureForSuccess"));

	private static final boolean enableVideoCaptureForFailure = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableVideoCaptureForFailure"));

	private static final boolean consoleOutput = Boolean.parseBoolean(
			CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "PrintTextInConsole").toLowerCase());

	private static final boolean storeScreenshot = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "StoreScreenshot").toLowerCase());

	private static final boolean jiraIntegration = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "JiraIntegration"));

	private static final boolean azureTFSIntegration = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "AzureTFSIntegration"));

	private static final boolean tfsResult = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "SubmitResultToTFS"));

	private static final boolean cookie = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "Cookies"));

	private static final String jiraParameters = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH,
			"JiraIntegrationParameters");

	@BeforeAll
	private static void deleteDirectoryContents() {
		String screenshotsFolderPath = Paths.get(System.getProperty("user.dir"), "screenshots").toString();
		File screenshotsFolder = new File(screenshotsFolderPath);

		if (screenshotsFolder.exists()) {
			clearDirectoryContents(screenshotsFolder);
			log.info("Existing screenshots have been cleared.");
		} else {
			log.info("No existing screenshots folder found to clear.");
		}
	}

	/**
	 * Clears all contents of the specified directory (files and subtracters).
	 */
	private static void clearDirectoryContents(File directory) {
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					clearDirectoryContents(file);
				}
				file.delete();
			}
		}
	}

	@Before
	public void init(Scenario scenario) {
		log.info("*********************************" + scenario.getSourceTagNames() + " " + scenario.getName()
				+ " Execution started *******************************");
		String node = System.getProperty("Node");
		if (node == null || node.isEmpty()) {
			node = "Node1";
		}
		String url = System.getProperty("Url");
		if (url != null && !url.isEmpty()) {
			CommonUtil.setAppUrl(url);
		}
		String apiurl = System.getProperty("apiUrl");
		if (apiurl != null && !apiurl.isEmpty()) {
			RestAssuredUtil.setApiCmdUrl(apiurl);
		}
		String browserName = System.getProperty("browserName");
		if (browserName != null && !browserName.isEmpty()) {
			CommonUtil.browserName = browserName;
		}
		YMLUtil.loadYML("src/test/java/", node);
		YMLUtil.loadObjectRepoYML("src/test/java/ObjectRepository.yml");
		String testDataFile = Constants.TESTDATA_PATH;
		String objectRepoFile = Constants.OBJECT_PATH;

		File testData = new File(testDataFile);
		if (testData.exists()) {
			YMLUtil.readObjectRepoYML(objectRepoFile, testDataFile);
		} else {
			YMLUtil.readObjectRepoYML(objectRepoFile);
		}
		YMLUtil.payloadYML("src/test/java/Payload.yml", node);
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		ExtentCucumberAdapter.addTestStepLog("" + scenario.getSourceTagNames());
		ExtentCucumberAdapter.addTestStepLog("Start Time : " + timestamp);
		if (enableVideoCaptureForSuccess || enableVideoCaptureForFailure) {
			try {
				ScreenRecorderUtil.startRecord("TestCase");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	// Method to wait for 2 minutes before executing the test case
	@Before("@waitinminutes")
	public void beforeScenario() throws Throwable {
		log.info("*********************************Scenario started*******************************");
		Thread.sleep(120000);
		CommonUtil.setCopiedCount(0);
	}

	@Before(order = 1)
	public void beforeScenarioTags(Scenario scenario) throws Throwable {
		for (String tag : scenario.getSourceTagNames()) {
			if (tag.toLowerCase().contains("api")) {
				apiScenario = true;
				flag = false;
				break;
			}
		}
	}

	@BeforeStep
	public void storeScenario(Scenario scenario) {
		if (consoleOutput && scenarioName) {
			System.out.println("\nThe Scenario Name : " + scenario.getName() + "\n");
			scenarioName = false;
		}
		WebBrowserUtil.sce = scenario;

		try {
			// Access private field 'delegate'
			Field delegateField = scenario.getClass().getDeclaredField("delegate");
			delegateField.setAccessible(true);
			TestCaseState testCaseState = (TestCaseState) delegateField.get(scenario);

			// Access private field 'testCase'
			Field testCaseField = testCaseState.getClass().getDeclaredField("testCase");
			testCaseField.setAccessible(true);
			TestCase testCase = (TestCase) testCaseField.get(testCaseState);

			// Filter out before/after hooks
			stepDefs = testCase.getTestSteps().stream().filter(PickleStepTestStep.class::isInstance)
					.map(PickleStepTestStep.class::cast).collect(Collectors.toList());

			// Retrieve the current step text safely
			Optional.ofNullable(stepDefs)
					.filter(list -> !list.isEmpty() && currentStepDefIndex >= 0 && currentStepDefIndex < list.size())
					.map(list -> list.get(currentStepDefIndex)).ifPresent(currentStepDef -> {
						String currentStep = currentStepDef.getStepText();
						log.info("Current Step: " + currentStep);
					});

		} catch (NoSuchFieldException | IllegalAccessException e) {
			log.error("Error accessing scenario details: ", e);
		}

	}

	@AfterStep
	public void addScreenshot(Scenario scenario) {
		if (consoleOutput) {
			System.out.println("the step name: " + StepListener.getStepName());
		}
		WebBrowserUtil.takeEachStepScrenshot(scenario);
		currentStepDefIndex += 1;
		if (flag) {
			driver = WebBrowser.getBrowser();
		}
		Collection<String> alltags = scenario.getSourceTagNames();
		if (storeScreenshot && StepListener.getStepName().toLowerCase().contains("verify")) {
			String testCaseName = alltags.stream().filter(tag -> tag.startsWith("@test")).findFirst()
					.map(tag -> tag.substring(1)).orElse("UnknownTest");
			String screenshotsFolderPath = Paths.get(System.getProperty("user.dir"), "screenshots").toString();
			File screenshotsFolder = new File(screenshotsFolderPath);
			if (!screenshotsFolder.exists()) {
				screenshotsFolder.mkdir();
			}
			String testCaseFolderPath = Paths.get(screenshotsFolderPath, testCaseName).toString();
			File testCaseFolder = new File(testCaseFolderPath);
			if (!testCaseFolder.exists()) {
				testCaseFolder.mkdir();
			}
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			String sanitizedStepName = StepListener.getStepName().replaceAll("[^a-zA-Z0-9_-]", "_");
			String screenshotName = "step_" + sanitizedStepName + "_" + System.currentTimeMillis() + ".png";
			File destination = new File(Paths.get(testCaseFolderPath, screenshotName).toString());
			try {
				FileUtils.copyFile(screenshot, destination);
				log.info("Screenshot saved at: " + destination.getAbsolutePath());
			} catch (IOException e) {
				log.error("Failed to save screenshot: " + e.getMessage());
			}
		}
	}

	@After(order = 1)
	public void afterScenario(Scenario scenario) {
		Assertion.assertAll();
		handleCookies(); // to handle the cookies in web page
		handleVideoRecording(scenario);// to handle video recording of the test case
		handleTestManagementIntegration(scenario); // to add test case in jira and azure if test case fail

		log.info("*********************************Scenario ended*******************************");

		ExtentCucumberAdapter.addTestStepLog("End Time: " + new Timestamp(System.currentTimeMillis()));
		CommonUtil.setCopiedCount(0);
		CommonUtil.setCopiedCountTextNull();
		softAssertions = new SoftAssert();

		boolean semiAuto = false;
		String scenarioStatusLowercase = scenario.getStatus().toString().toLowerCase();

		updateScenarioCounters(scenarioStatusLowercase);
		handleFailureCase(scenario, scenarioStatusLowercase);

		boolean isApiScenario = isApiScenario(scenario);
		if (!isApiScenario) {
			driver = WebBrowser.getBrowser();
			WebBrowserUtil.takeScrenshot(scenario);
		}

		closeBrowser = shouldCloseBrowser(scenario);

		processScenarioTags(scenario, semiAuto);

		scenarioName = true;
		softAssertions = new SoftAssert();
	}

	@After(order = 0)
	public void closeBrowser() {
		log.info("*********************************Execution ended*******************************");

		if (WebBrowser.isBrowserOpened() && closeBrowser) {
			WebBrowser.closeBrowserInstance();
		}

		File dir = new File(Constants.PROJECT_PATH + "//output//");
		File[] files = dir.listFiles();

		File lastModified = Arrays.stream(files).filter(File::isDirectory).max(Comparator.comparing(File::lastModified))
				.orElse(null);

		log.info(lastModified);

		try {
			int totalCount = passCount + failCount + skipCount;
			String json = String.format("{\"TotalTest\":%d,\"passed\":%d,\"failed\":%d,\"skipped\":%d}", totalCount,
					passCount, failCount, skipCount);

			String path = lastModified + "//Execution_status.json";
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
				writer.write(json);
			}

			String executedTagsPath = lastModified + "//ExecutedTagDetails.txt";
			Set<String> uniqueTag = new HashSet<>(tagsExecuted);
			StringBuilder test = new StringBuilder();
			StringBuilder set = new StringBuilder();
			StringBuilder otherTags = new StringBuilder();

			for (String tag : uniqueTag) {
				if (tag.contains("@test")) {
					if (!test.isEmpty())
						test.append(",");
					test.append(tag);
				} else if (tag.contains("@set")) {
					if (!set.isEmpty())
						set.append(",");
					set.append(tag);
				} else {
					if (!otherTags.isEmpty())
						otherTags.append(",");
					otherTags.append(tag);
				}
			}

			try (BufferedWriter myWriter = new BufferedWriter(new FileWriter(executedTagsPath))) {
				myWriter.write("Test Tags:\n" + test + "\n\n");
				myWriter.write("Set Tags:\n" + set + "\n\n");
				myWriter.write("Other Tags:\n" + otherTags + "\n");
			}

		} catch (IOException e) {
			log.error("Error writing execution details: ", e);
		}
	}

	private void handleCookies() {
		if (cookie && !cookiesAdded) {
			driver = WebBrowser.getBrowser();
			try {
				cookies = driver.manage().getCookies();
				cookiesAdded = true;
			} catch (Exception e) {
				log.error("Error while getting cookies: ", e);
			}
		}
	}

	private void handleVideoRecording(Scenario scenario) {

		if (!enableVideoCaptureForSuccess && !enableVideoCaptureForFailure) {
			return; // Exit early if video capture is disabled
		}

		try {
			ScreenRecorderUtil.stopRecord();
		} catch (Exception e) {
			log.error("Error while stopping screen recording: ", e);
		}

		boolean isFailure = scenario.isFailed();

		// Determine whether to delete the recording
		if ((enableVideoCaptureForSuccess && !enableVideoCaptureForFailure && isFailure)
				|| (!enableVideoCaptureForSuccess && enableVideoCaptureForFailure && !isFailure)) {
			ScreenRecorderUtil.deleteRecordedFile();
		}
	}

	private void handleTestManagementIntegration(Scenario scenario) {
		if (scenario.isFailed()) {
			if (jiraIntegration) {
				createJiraIssue(scenario);
			}
			if (azureTFSIntegration) {
				createAzureTFSIssue(scenario);
			}
		}
	}

	private void createJiraIssue(Scenario scenario) {
		try {
			String[] params = jiraParameters.split(",");
			PickleStepTestStep failedStep = stepDefs.get(currentStepDefIndex - 1);
			String failedStepText = failedStep.getStepText();

			String description = String.format("%s\nScenario failed at line %d in feature file\nFailed step: %s\n%s",
					scenario.getSourceTagNames(), scenario.getLine(), failedStepText, CommonUtil.error);

			String summary = String.format("Scenario %s failed", scenario.getName());
			summary = summary.replaceAll("[^a-zA-Z0-9 ]", "").substring(0, Math.min(summary.length(), 255));

			new JiraServiceProvider(params[0], params[1], params[2], params[3]).createJiraIssue(params[4], summary,
					description, params[5]);
		} catch (Exception e) {
			log.error("Error while creating Jira issue: ", e);
		}
	}

	private void createAzureTFSIssue(Scenario scenario) {
		try {
			PickleStepTestStep failedStep = stepDefs.get(currentStepDefIndex - 1);
			String description = String.format("%s\nScenario failed at line %d\nFailed step: %s\n%s",
					scenario.getSourceTagNames(), scenario.getLine(), failedStep.getStepText(), CommonUtil.error);
			AzureTFSUtil.createAzureTFSIssue("Scenario " + scenario.getName() + " failed", description);
		} catch (Exception e) {
			log.error("Error while creating Azure TFS issue: ", e);
		}
	}

	// Update pass, fail, and skip counts
	private void updateScenarioCounters(String scenarioStatusLowercase) {
		switch (scenarioStatusLowercase) {
		case "skipped":
			skipCount++;
			break;
		case "passed":
			passCount++;
			break;
		case "failed":
			failCount++;
			break;
		default:
			log.error("Unexpected scenario status: {}" + scenarioStatusLowercase);
		}
	}

	// Handle failure case and save config details if required
	private void handleFailureCase(Scenario scenario, String scenarioStatusLowercase) {
		if ("failed".equals(scenarioStatusLowercase)) {
			Collection<String> tagsString = scenario.getSourceTagNames();
			List<String> tagsList = new ArrayList<>(tagsString);
			if (!tagsList.isEmpty()) {
				AutoHealUtil.saveConfigDeatils(tagsList.get(0));
			}
		}
	}

	// Check if the scenario is an API scenario
	private boolean isApiScenario(Scenario scenario) {
		for (String tag : scenario.getSourceTagNames()) {
			tagsExecuted.add(tag);
			if (tag.toLowerCase().contains("api")) {
				return true;
			}
		}
		return false;
	}

	// Determine if the browser should be closed
	private boolean shouldCloseBrowser(Scenario scenario) {
		for (String tag : scenario.getSourceTagNames()) {
			if (tag.contains("usesamesession")) {
				return false;
			}
		}
		return true;
	}

	// Process scenario tags for specific conditions
	private void processScenarioTags(Scenario scenario, boolean semiAuto) {
		for (String tag : scenario.getSourceTagNames()) {
			if (shouldCloseInstance(tag)) {
				WebBrowser.closeBrowserInstance();
			}
			if (tag.contains("semiauto")) {
				semiAuto = true;
			}
			handleSemiAutoFailure(scenario, semiAuto);
			updateTFSResults(tag);
		}
	}

	// Check if the browser instance should be closed based on tags
	private boolean shouldCloseInstance(String tag) {
		return (tag.contains("set2") || tag.contains("semiauto") || tag.contains("set3") || tag.contains("set21")
				|| tag.contains("set22") || tag.contains("set23")) && closeBrowser;
	}

	// Handle semi-auto test case failures
	private void handleSemiAutoFailure(Scenario scenario, boolean semiAuto) {
		if (scenario.isFailed() && semiAuto) {
			throw new CustomException("Semi Auto test cases may fail due to OTP / Captcha.");
		}
	}

	// Update results to TFS
	private void updateTFSResults(String tag) {
		if (tfsResult && tag.contains("test")) {
			int testCaseId = 0;
			TFSUtil tfsUtil = new TFSUtil();
			int[] testPointIds = tfsUtil.getTestPointId(testCaseId);
			int testPointId = testPointIds[0];
			int testPlanId = testPointIds[1];
			int testRunId = tfsUtil.getTestRunId(testCaseId, testPointId, testPlanId);
			tfsUtil.updateResultsToTFS(testCaseId, testPointId, testRunId, tstSteps);
			tstSteps.clear();
		}
	}
}
