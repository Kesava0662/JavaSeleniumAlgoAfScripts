package common;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.v123.network.Network;
import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.devtools.v123.network.model.RequestId;
import java.util.Optional;
import org.openqa.selenium.devtools.DevTools;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import java.time.Duration;
import org.apache.log4j.Logger;
import org.openqa.selenium.Cookie;

@SuppressWarnings("all")
public class WebBrowser {
	private static WebDriver driver;
	static String parentWindowHandle;
	private static boolean isBrowserOpen = false;
	private static boolean launchNewBrowser = false;
	public static boolean boolHighLightElement = false;
	public static boolean boolEachstepScreenshot = false;
	public static boolean boolEachSoftAssersion = false;
	static List<WebDriver> webdriverList = new ArrayList<WebDriver>();
	public static String browserType = "";
	public static DevTools devTools;
	private static final Logger log = Logger.getLogger(WebBrowser.class);

	private static final boolean DIRECTORY_PATH = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "DownloadInCurrentDirectory"));

	private static final int PAGE_LOAD_TIMEOUT = Integer.parseInt(
			CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "MaximumTimeInSecondsToWaitForControl"));

	private static final boolean EACH_STEP_SCREENSHOT = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableEachStepScreenshot"));

	private static final boolean SOFT_ASSERTION = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableSoftassertion"));

	private static final boolean HIGH_LIGHT_ELEMENT = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "HighLightWebElement"));

	private static final String PATH_OF_BROWSER = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH,
			"WebdriverPath");

	private static final String LAMBDA_TEST_BUILD = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH,
			"LambdaTestBuild");

	private static final String LAMBDA_TEST_URL = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH,
			"LambdaTestURL");

	private static final String NETWORK_LOG = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "NetworkLog");

	private static final String TYPE = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "Type");

	private static final String HUB_URL = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "HubURL");

	private static final String PROFILE_PATH = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "ProfilePath");

	private static void initializeChromeDriver() {

		System.out.print("Launching Chrome");

		if ("Na".equalsIgnoreCase(PATH_OF_BROWSER)) {
			WebDriverManager.chromedriver().clearDriverCache().setup();
		} else {
			System.setProperty("webdriver.chrome.driver", PATH_OF_BROWSER + "/chromedriver.exe");
		}

		// Set browser preferences
		Map<String, Object> prefs = new HashMap<>();
		prefs.put("profile.default_content_setting_values.notifications", 2);

		if (DIRECTORY_PATH) {
			prefs.put("download.default_directory", System.getProperty("user.dir"));
		}

		ChromeOptions options = new ChromeOptions();
		configureChromeOptions(options, prefs);

		if (TYPE.toUpperCase().contains("GRID")) {
			try {
				driver = new RemoteWebDriver(new URL(HUB_URL), options);
			} catch (MalformedURLException e) {
				log.info(Level.SEVERE + "Invalid HUB URL: " + HUB_URL + e);
			}
		} else {
			driver = new ChromeDriver(options);
			driver.manage().window().maximize();
		}

	}

	private static void configureChromeOptions(ChromeOptions options, Map<String, Object> prefs) {
		options.setExperimentalOption("prefs", prefs);
		options.setCapability("goog:loggingPrefs", Collections.singletonMap(LogType.PERFORMANCE, Level.ALL));

		if (browserType.equalsIgnoreCase("HEADLESS CHROME")) {
			options.addArguments("--no-sandbox", "--headless", "disable-infobars", "--disable-extensions",
					"--disable-dev-shm-usage", "window-size=1920,1080", "--disable-gpu");
			options.addArguments(
					"user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36");
		}

		if (PROFILE_PATH != null && !PROFILE_PATH.isEmpty()) {
			options.addArguments("user-data-dir=" + PROFILE_PATH);
		}

		options.addArguments("--ignore-ssl-errors=yes", "--ignore-certificate-errors", "use-fake-ui-for-media-stream",
				"use-fake-device-for-media-stream", "--remote-allow-origins=*",
				"--disable-blink-features=AutomationControlled");

		if (browserType.equalsIgnoreCase("Kiosk Chrome")) {
			options.addArguments("--kiosk");
		}
	}

	private static void initializeFirefoxDriver() {

		System.out.print("Launching Firefox");
		if (PATH_OF_BROWSER.equalsIgnoreCase("Na")) {
			WebDriverManager.firefoxdriver().clearDriverCache().setup();
		} else {
			System.setProperty("webdriver.gecko.driver", PATH_OF_BROWSER + "/geckodriver.exe");
		}
		driver = new FirefoxDriver();
		driver.manage().window().maximize();

	}

	private static void initializeEdgeDriver() {

		System.out.print("Launching Edge");
		if (PATH_OF_BROWSER.equalsIgnoreCase("Na")) {
			WebDriverManager.edgedriver().clearDriverCache().setup();
		} else {
			System.setProperty("webdriver.edge.driver", PATH_OF_BROWSER + "/msedgedriver.exe");
		}
		driver = new EdgeDriver();
		driver.manage().window().maximize();
	}

	private static void initializeLambdaTestDriver() {
		System.out.print("Attempting connection to LambdaTest");

		RemoteWebDriver remotedriver = null;
		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setCapability("build", LAMBDA_TEST_BUILD);
		caps.setCapability("name", "");
		caps.setCapability("platform", "Windows 10");
		caps.setCapability("browserName", "Chrome");
		caps.setCapability("version", "97.0");
		caps.setCapability("network", NETWORK_LOG);

		System.out.println("Desired capabilities set successfully");

		try {
			remotedriver = new RemoteWebDriver(new URL(LAMBDA_TEST_URL), caps);
			SessionId sessionId = remotedriver.getSessionId();
			log.info("Driver session id is: " + sessionId);
		} catch (Exception e) {
			log.error("Error initializing LambdaTest driver: " + e.getMessage(), e);
		}

		driver = remotedriver;
	}

	public static WebDriver getBrowser(boolean launchBrowser) {
		if ((driver == null || launchBrowser) && !isBrowserOpen) {

			if (CommonUtil.browserName != null) {
				browserType = CommonUtil.browserName;
				log.info("browserName-----------------" + browserType);
			} else {
				browserType = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "BrowserType");
			}

			if (HIGH_LIGHT_ELEMENT) {
				boolHighLightElement = true;
			}
			if (EACH_STEP_SCREENSHOT) {
				boolEachstepScreenshot = true;
			}
			if (SOFT_ASSERTION) {
				boolEachSoftAssersion = true;
			}
			if (browserType.equals("Firefox")) {
				initializeFirefoxDriver();

			} else if (browserType.equals("Edge")) {
				initializeEdgeDriver();

			} else if (browserType.equalsIgnoreCase("Lambda")) {
				initializeLambdaTestDriver();
			} else {
				initializeChromeDriver();
			}
			webdriverList.add(driver);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
			parentWindowHandle = driver.getWindowHandle();
			isBrowserOpen = true;
		} else if (launchBrowser) {
			WebDriver driver2 = multipleBrowserInstance();
			webdriverList.add(driver2);
			driver2.manage().window().maximize();
		}
		return driver;

	}

	public static WebDriver multipleBrowserInstance() {

		if (CommonUtil.browserName != null) {
			browserType = CommonUtil.browserName;
			System.out.println("browserName-----------------" + browserType);
		} else {
			browserType = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "BrowserType");
		}

		if (HIGH_LIGHT_ELEMENT) {
			boolHighLightElement = true;
		}
		if (EACH_STEP_SCREENSHOT) {
			boolEachstepScreenshot = true;
		}
		if (SOFT_ASSERTION) {
			boolEachSoftAssersion = true;
		}
		if (browserType.equals("Firefox")) {
			initializeFirefoxDriver();
		} else if (browserType.equals("Edge")) {
			initializeEdgeDriver();
		} else if (browserType.toUpperCase().equals("Lambda".toUpperCase())) {
			initializeLambdaTestDriver();
		} else {
			initializeChromeDriver();
		}
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(PAGE_LOAD_TIMEOUT));
		parentWindowHandle = driver.getWindowHandle();
		isBrowserOpen = true;
		return driver;
	}

	public static WebDriver getBrowser() {
		return getBrowser(launchNewBrowser);
	}

	public static void closetab(int tab) {
		try {
			ArrayList<String> tabs2 = new ArrayList<String>(driver.getWindowHandles());
			driver.switchTo().window(tabs2.get(tab));
			driver.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	public static void LaunchApplication(boolean openBrowser) {
		String autUrl = "";
		if (CommonUtil.getAppUrl() != null) {
			autUrl = CommonUtil.getAppUrl();
		} else {
			autUrl = CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "URL");
		}
		if (driver == null) {
			getBrowser(openBrowser);
		}
		driver.get(autUrl);

		if (Hooks.cookiesAdded) {

			for (Cookie cookie : Hooks.cookies) {
				driver.manage().addCookie(cookie);
			}
			driver.navigate().refresh();
		}

	}

	public static void LaunchApplication(boolean openBrowser, String autUrl) {

		getBrowser(openBrowser);
		((JavascriptExecutor) driver).executeScript("window.open()");
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		for (int i = 0; i < tabs.size(); i++) {
			driver.switchTo().window(tabs.get(i));
		}
		WebDriver newDriver = webdriverList.get(webdriverList.size() - 1);
		newDriver.get(autUrl);
	}

	public static void LaunchNewInstance(boolean openBrowser, String autUrl) {
		getBrowser(openBrowser);
		WebDriver newDriver = webdriverList.get(webdriverList.size() - 1);
		newDriver.get(autUrl);
	}

	public static void LaunchApplication1(String autUrl) {
		driver.navigate().to(autUrl);
	}

	public static void openNewTab(boolean openBrowser, String autUrl) {
		((JavascriptExecutor) driver).executeScript("window.open()");
		ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
		for (int i = 0; i < tabs.size(); i++) {
			driver.switchTo().window(tabs.get(i));
		}
		WebDriver newDriver = webdriverList.get(webdriverList.size() - 1);
		newDriver.get(autUrl);
	}

	public static String getParentWindowHandle() {
		return parentWindowHandle;
	}

	public static void setCurrentBrowser(int index) {
		if (webdriverList.size() > index) {
			driver = webdriverList.get(index);
			isBrowserOpen = true;
		}
	}

	public static void closeBrowserInstance() {
		for (int counter = 0; counter < webdriverList.size(); counter++) {
			if (webdriverList.get(counter) != null) {
				webdriverList.get(counter).quit();
			}
		}

		driver = null;
		webdriverList = new ArrayList<WebDriver>();
		isBrowserOpen = false;
	}

	public static boolean isBrowserOpened() {
		return isBrowserOpen;
	}

	public static boolean DevTool(List<String> list, String url) {
		devTools = ((ChromeDriver) driver).getDevTools();
		devTools.createSession();
		devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
		Boolean flag = true;
		AtomicInteger count = new AtomicInteger(0);
		final RequestId[] id = new RequestId[3];
		List<String> responseBodies = new ArrayList<>();
		for (int retry = 0; retry < 5; retry++) {
			driver.navigate().refresh();
			WebBrowserUtil.scrollDown(String.valueOf(10));
			devTools.addListener(Network.responseReceived(), responseReceived -> {

				String responseUrl = responseReceived.getResponse().getUrl();
				log.info("Received response URL: " + responseUrl);

				if (responseUrl.equals(url)) {
					count.getAndIncrement();
					if (count.get() <= 3) {
						id[count.get() - 1] = responseReceived.getRequestId();
						log.info("ID" + count.get() + ": " + id[count.get() - 1]);
						ExtentCucumberAdapter.addTestStepLog("ID" + count.get() + ": " + id[count.get() - 1]);

					}
				}
			});

			sleep(14);

			for (int i = 0; i < id.length; i++) {
				if (id[i] != null) {
					try {
						String responseBody = devTools.send(Network.getResponseBody(id[i])).getBody();
						responseBodies.add(responseBody);
					} catch (Exception e) {
						log.error("Not able to capture response body in id[" + i + "]: " + e.getMessage());
					}
				}
			}
			if (!responseBodies.isEmpty()) {
				break;
			} else {
				log.info("Retrying to fetch response bodies...");
			}
		}
		if (!responseBodies.isEmpty()) {
			for (String responseBody : responseBodies) {
				if (responseBody.contains(list.get(list.size() - 1))) {
					for (String item : list) {
						if (responseBody.contains(item)) {
							ExtentCucumberAdapter.addTestStepLog("Yes! " + item + " it is present in the algonomy url");
						} else {
							ExtentCucumberAdapter.addTestStepLog(item + " No! it is not present in alognomy url");
							flag = false;
						}
						String[] reqStrings = responseBody.split("\\?");
						String reqString = "";
						for (String key : reqStrings) {
							if (key.contains(item)) {
								reqString = key;
								break;
							}
						}
						String[] reqStrings1 = reqString.split(",");
						String[] reqString2 = reqStrings1[0].split("&");
						for (String key : reqString2) {
							if (key.contains(item)) {
								log.info("The value is ### " + key);
								ExtentCucumberAdapter.addTestStepLog("The value is ### " + key);
								break;
							}
						}
					}
				} else {
					for (String item : list) {
						if (responseBody.contains(item)) {
							ExtentCucumberAdapter.addTestStepLog("Yes! " + item + " it is present in the algonomy url");
						} else {
							ExtentCucumberAdapter.addTestStepLog(item + " No! it is not present in alognomy url");
							flag = false;
						}
						String[] reqStrings = responseBody.split("\\?");
						String reqString = "";
						for (String key : reqStrings) {
							if (key.contains(item)) {
								reqString = key;
								break;
							}
						}
						String[] reqStrings1 = reqString.split(",");
						String[] reqString2 = reqStrings1[0].split("&");
						for (String key : reqString2) {
							if (key.contains(item)) {
								ExtentCucumberAdapter.addTestStepLog("The value is ### " + key);
								break;
							}
						}
					}
				}
			}
		} else {
			throw new CustomException("Response body is empty");
		}
		return flag;
	}

	private static void sleep(int seconds) {
		long milliseconds = seconds * 1000L;
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
