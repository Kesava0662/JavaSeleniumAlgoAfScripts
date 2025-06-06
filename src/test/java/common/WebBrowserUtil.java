package common;

import java.util.*;
import java.io.*;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.stream.Collectors;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.apache.pdfbox.io.RandomAccessBufferedFileInputStream;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.text.PDFTextStripper;
import java.net.URL;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.UnhandledAlertException;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ComparisonChain;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;
import io.cucumber.java.Scenario;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@SuppressWarnings("all")
public class WebBrowserUtil {
	private static WebDriver driver;
	private static final int TIME_INTERVAL = Integer.parseInt(
			CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "TimeIntervalInMilliSecondsToWaitForPage"));

	private static final int MAX_DELAY = Integer.parseInt(
			CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "MaximumTimeInMilliSecondsToWaitForPage"));

	private static final int CONTROL_LOAD_TIMEOUT = Integer.parseInt(
			CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "MaximumTimeInSecondsToWaitForControl"));

	static Scenario sce;
	private static final boolean SCREENSHOT_FOR_SUCESSS = Boolean
			.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableScrenshotForSucess"));

	private static final Logger log = Logger.getLogger(WebBrowserUtil.class);

	public static void turnOffImplicitWaits() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
	}

	public static void turnOnImplicitWaits() {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
	}

	public static boolean isElementPresent(String message) {
		driver = WebBrowser.getBrowser();
		List<WebElement> webElements = driver.findElements(By.xpath("//*[contains(text(), '" + message + "')]"));

		return webElements.stream().anyMatch(element -> element.isDisplayed());
	}

	public static String getContent(String message) {
		driver = WebBrowser.getBrowser();

		List<WebElement> webElements = driver
				.findElements(By.xpath(String.format("//*[contains(text(), '%s')]", message)));

		for (WebElement element : webElements) {
			if (Boolean.TRUE.equals(element.isDisplayed())) {
				return getText(element);
			}
		}
		return webElements.isEmpty() ? "" : getText(webElements.get(0));
	}

	public static boolean verifyURL(String url) {
		try {
			driver = WebBrowser.getBrowser();
			String currentUrl = driver.getCurrentUrl().toUpperCase();

			if (currentUrl.contains(url.toUpperCase())) {
				return true;
			}

			sleep(500);

			for (String handle : driver.getWindowHandles()) {
				driver.switchTo().window(handle);
				if (driver.getCurrentUrl().toUpperCase().contains(url.toUpperCase())) {
					return true;
				}
			}
		} catch (Exception ex) {
			log.error("Error verifying URL: {}" + ex.getMessage());
		}
		return false;
	}

	public static void clickIfVisible(WebElement element) {
		int i = 0;
		while (i < 2) {
			try {
				i++;
				click(element);
				break;
			} catch (Exception ex) {
				log.error(ex.getMessage());
			}
		}
	}

	public static boolean verifyLabelDisplayed(WebElement element) {
		boolean isVerified = false;
		try {
			if (element != null) {
				isVerified = true;
			}
		} catch (Exception ex) {
			isVerified = false;
		}
		return isVerified;
	}

	public static void scrollAndEnterText(WebElement element, String text) {

		scroll(element);
		sleep(1000);

		try {
			element.clear();
			element.sendKeys(text);
			if (!text.equals(element.getAttribute("value"))) {
				element.sendKeys(Keys.CONTROL + "a");
				sleep(500);
				element.sendKeys(text);
			}
		} catch (Exception ex) {
			throw new CustomException("Scroll And EnterText unsuccessful: " + ex.getMessage(), ex);
		}
	}

	public static WebElement findElement(String locator, String identificationType) {

		driver = WebBrowser.getBrowser();
		if (locator.contains("||")) {
			String[] locatorSplit = locator.split("\\|\\|");
			locator = locatorSplit[0];
			driver = getFrame(locatorSplit[1]);
		}

		WebElement element = null;
		boolean highlightElement = WebBrowser.boolHighLightElement;
		int retryCount = 0;
		int maxRetries = 5;

		while (retryCount < maxRetries) {
			try {
				element = locateElement(locator, identificationType);
				if (highlightElement) {
					highlightElement(element);
				}
				return element;
			} catch (NoSuchElementException e) {
				sleep(2000);
				retryCount++;
				if (retryCount == maxRetries - 1) {
					throw new CustomException("Element not found: " + locator, e);
				}
			}
		}

		return element;
	}

	private static WebElement locateElement(String locator, String identificationType) {
		identificationType = identificationType.toLowerCase();
		switch (identificationType) {
		case "xpath":
			return driver.findElement(By.xpath(locator));
		case "id":
			return driver.findElement(By.id(locator));
		default:
			throw new IllegalArgumentException("Unsupported identification type: " + identificationType);
		}
	}

	private static void highlightElement(WebElement element) {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("arguments[0].style.border='2px solid red'", element);
	}

	public static WebElement findElement(String xpath, String identificationType, WebDriver frame) {

		try {
			if ("xpath".equalsIgnoreCase(identificationType)) {
				return frame.findElement(By.xpath(xpath));
			} else if ("id".equalsIgnoreCase(identificationType)) {
				return frame.findElement(By.id(xpath));
			} else {
				throw new IllegalArgumentException("Unsupported identification type: " + identificationType);
			}
		} catch (NoSuchElementException e) {
			throw new CustomException("Element not found using " + identificationType + ": " + xpath + e.getMessage());
		}
	}

	public static List<WebElement> findElements(String xpath, String identificationType) {
		driver = WebBrowser.getBrowser();
		if (xpath.contains("||")) {
			String[] xpathSplit = xpath.split("\\|\\|");
			xpath = xpathSplit[0];
			driver = getFrame(xpathSplit[1]);
		}
		List<WebElement> element = null;
		if (identificationType.equalsIgnoreCase("xpath")) {
			element = driver.findElements(By.xpath(xpath));
		} else if (identificationType.equalsIgnoreCase("id")) {
			element = driver.findElements(By.id(xpath));
		}
		return element;
	}

	public static List<WebElement> findElements(String xpath, String identificationType, WebDriver frame) {
		List<WebElement> element = null;
		if (identificationType.equalsIgnoreCase("xpath")) {
			element = frame.findElements(By.xpath(xpath));
		} else if (identificationType.equalsIgnoreCase("id")) {
			element = frame.findElements(By.id(xpath));
		}
		return element;
	}

	public static void enterText(WebElement element, String text) {

		try {
			element.click();
			element.clear();
			element.sendKeys(text);

			if (!element.getAttribute("value").equals(text)) {
				element.sendKeys(Keys.CONTROL + "a");
				sleep(500);
				element.sendKeys(text);
			}

		} catch (Exception ex) {
			throw new CustomException("Error entering text: " + ex.getMessage(), ex);
		}
	}

	public static boolean verifyEnabledDisabledOptions(String verificationType, List<WebElement> eleList, String text) {
		boolean isVerified = false;
		boolean disabled = "Disabled".equalsIgnoreCase(verificationType);
		String[] textSplit = text.split(",");
		boolean verificationStarted = false;

		for (int k = 0; k < eleList.size(); k++) {
			String eleText = eleList.get(k).getText();

			// If the element matches the text in the provided list
			if (Arrays.asList(textSplit).contains(eleText)) {
				verificationStarted = true;
				String className = eleList.get(k).getAttribute("class");
				ExtentCucumberAdapter.addTestStepLog("Element text: " + eleText + " , Element Class: " + className);

				// Check if the element is disabled
				if (className.contains("disabled") == disabled) {
					isVerified = true;
					break;
				} else {
					isVerified = false;
					ExtentCucumberAdapter.addTestStepLog(eleText + " is " + (disabled ? "enabled" : "disabled")
							+ ", expected to be " + (disabled ? "disabled" : "enabled"));
					break;
				}
			}
		}
		return isVerified;
	}

	private static void captureDesktopScreenshot(Scenario scenario) {
		try {
			Robot robot = new Robot();
			Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenImage = robot.createScreenCapture(screenRect);

			String screenshotPath = Paths
					.get(Constants.PROJECT_PATH, "src", "test", "java", "desktop_full_screenshot.png").toString();
			ImageIO.write(screenImage, "png", new File(screenshotPath));

			attachScreenshotToScenario(screenImage, scenario, "Full Desktop Screenshot");
		} catch (AWTException | IOException e) {
			log.error("Failed to capture full desktop screenshot: {}" + e.getMessage());
			scenario.log("Failed to capture full desktop screenshot: " + e.getMessage());
		}
	}

	private static void attachScreenshotToScenario(BufferedImage image, Scenario scenario, String screenshotName)
			throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", bos);
		scenario.attach(bos.toByteArray(), "image/png", screenshotName);
	}

	private static void captureScrollScreenshots(JavascriptExecutor jsExecutor, Long totalHeight, Actions actions)
			throws IOException {
		int screenHeight = driver.manage().window().getSize().height;

		for (int scrollPosition = 0; scrollPosition <= totalHeight; scrollPosition += screenHeight) {
			jsExecutor.executeScript("window.scrollTo(0," + scrollPosition + ");");
			sleep(500);

			File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			String screenshotPath = Paths
					.get(Constants.PROJECT_PATH, "src", "test", "java", "image_" + scrollPosition + ".png").toString();
			FileUtils.copyFile(screenshotFile, new File(screenshotPath));

			actions.sendKeys(Keys.PAGE_DOWN).perform();
		}
	}

	private static File[] getSortedScreenshotFiles() {
		File screenshotDir = new File(Paths.get(Constants.PROJECT_PATH, "src", "test", "java").toString());
		FileFilter fileFilter = new WildcardFileFilter("image_*.png");
		File[] files = screenshotDir.listFiles(fileFilter);

		if (files != null) {
			Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
		}

		return files != null ? files : new File[0];
	}

	private static BufferedImage mergeScreenshots(File[] files) throws IOException {
		int mergedWidth = 0;
		int mergedHeight = 0;

		for (File file : files) {
			BufferedImage img = ImageIO.read(file);
			mergedWidth = Math.max(mergedWidth, img.getWidth());
			mergedHeight += img.getHeight();
		}

		BufferedImage mergedImage = new BufferedImage(mergedWidth, mergedHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = mergedImage.createGraphics();

		int currentHeight = 0;
		for (File file : files) {
			BufferedImage img = ImageIO.read(file);
			graphics.drawImage(img, 0, currentHeight, null);
			currentHeight += img.getHeight();
		}
		graphics.dispose();

		return mergedImage;
	}

	private static byte[] compressImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		float compressionQuality = 0.6f;
		javax.imageio.ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
		javax.imageio.ImageWriteParam jpegParams = jpegWriter.getDefaultWriteParam();
		jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);

		boolean withinSizeLimit = false;
		while (!withinSizeLimit) {
			outputStream.reset();
			jpegParams.setCompressionQuality(compressionQuality);

			try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
				jpegWriter.setOutput(ios);
				jpegWriter.write(null, new javax.imageio.IIOImage(image, null, null), jpegParams);
			}

			if (outputStream.size() <= 70 * 1024 || compressionQuality <= 0.05f) {
				withinSizeLimit = true;
			} else {
				compressionQuality -= 0.05f;
			}
		}
		jpegWriter.dispose();
		return outputStream.toByteArray();
	}

	private static void mergeAndCompressScreenshots(Scenario scenario) throws IOException {
		File[] screenshotFiles = getSortedScreenshotFiles();

		if (screenshotFiles.length == 0) {
			return;
		}

		BufferedImage mergedImage = mergeScreenshots(screenshotFiles);
		byte[] compressedImageData = compressImage(mergedImage);

		if (compressedImageData != null) {
			File compressedFile = new File(
					Paths.get(Constants.PROJECT_PATH, "src", "test", "java", "compressed_image.jpg").toString());
			try (FileOutputStream fos = new FileOutputStream(compressedFile)) {
				fos.write(compressedImageData);
			}
			scenario.attach(compressedImageData, "image/jpeg", "Compressed Screenshot");
		}
	}

	private static void captureFullPageScreenshot(Scenario scenario) {
		try {
			JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
			Long totalPageHeight = (Long) jsExecutor.executeScript(
					"return Math.max(document.body.scrollHeight, document.body.offsetHeight, document.documentElement.clientHeight, "
							+ "document.documentElement.scrollHeight, document.documentElement.offsetHeight);");

			totalPageHeight = totalPageHeight < 1000 ? 1000L : totalPageHeight;

			Actions actions = new Actions(driver);
			actions.moveByOffset(100, 350).click().perform();

			captureScrollScreenshots(jsExecutor, totalPageHeight, actions);
			mergeAndCompressScreenshots(scenario);
		} catch (IOException e) {
			log.error("Error capturing full-page screenshot: {}" + e.getMessage());
			captureAndCompressScreenshot();
		}
	}

	public static void takeScrenshot(Scenario scenario) {
		boolean fullScreenshot = Boolean
				.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableFullScreenshot"));

		boolean desktopFullScreenshot = Boolean
				.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableDesktopFullScreenshot"));

		driver = WebBrowser.getBrowser();
		if (desktopFullScreenshot) {
			captureDesktopScreenshot(scenario);
		}
		if (fullScreenshot) {
			captureFullPageScreenshot(scenario);
		} else {
			captureAndCompressScreenshot();
		}
	}

	public static void captureScreenshot() {
		if (SCREENSHOT_FOR_SUCESSS && !Hooks.apiScenario) {
			captureAndCompressScreenshot();
		}
	}

	public static void takeEachStepScrenshot(Scenario scenario) {
		if (WebBrowser.boolEachstepScreenshot) {
			driver = WebBrowser.getBrowser();
			captureAndCompressScreenshot();
			log.info(scenario);
		}
	}

	public static void scroll(WebElement element) {
		try {
			driver = WebBrowser.getBrowser();
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
			sleep(800);
			Actions actions = new Actions(driver);
			actions.moveToElement(element).build().perform();
		} catch (Exception ex) {
			throw new CustomException("Scroll and click unsuccessful" + ex.getMessage());
		}
	}

	public static void refreshPage() {
		driver.navigate().refresh();
	}

	public static List<WebElement> getElementByXpath(String xpath) {
		if (xpath.contains("||")) {
			String[] xpathSplit = xpath.split("\\|\\|");
			xpath = xpathSplit[0];
			driver = getFrame(xpathSplit[1]);
		}
		return driver.findElements(By.xpath(xpath));
	}

	public static WebElement getSingleElementByXpath(String xpath) {
		if (xpath.contains("||")) {
			String[] xpathSplit = xpath.split("\\|\\|");
			xpath = xpathSplit[0];
			driver = getFrame(xpathSplit[1]);
		}
		return driver.findElement(By.xpath(xpath));
	}

	public static void selectByRandomIndex(WebElement dropDownList) {
		Select drpList = new Select(dropDownList);
		if (dropDownList.isDisplayed()) {
			int totalCount = drpList.getOptions().size();
			Random random = new Random();
			int randomNumber = random.nextInt(totalCount - 1);
			drpList.selectByIndex(randomNumber);
		} else {
			throw new CustomException("Select List not displayed");
		}
	}

	public static void selectByLastIndex(WebElement dropDownList) {
		Select drpList = new Select(dropDownList);
		if (dropDownList.isDisplayed()) {
			drpList.selectByIndex(drpList.getOptions().size() - 1);
		} else {
			throw new CustomException("Select List not displayed");
		}
	}

	public static void selectByFirstIndex(WebElement dropDownList) {
		Select drpList = new Select(dropDownList);
		if (dropDownList.isDisplayed()) {
			drpList.selectByIndex(1);
		} else {
			throw new CustomException("Select List not displayed");
		}
	}

	public static String getSelectedValue(WebElement dropDownList) {
		Select drpList = new Select(dropDownList);
		if (dropDownList.isDisplayed()) {
			return drpList.getFirstSelectedOption().getText();
		} else {
			throw new CustomException("Select List not displayed");
		}
	}

	public static void scrollAndSelectByText(WebElement dropDownList, String text) {
		scroll(dropDownList);
		Select drpList = new Select(dropDownList);
		if (dropDownList.isDisplayed()) {
			drpList.selectByVisibleText(text);
		} else {
			throw new CustomException("Select List not displayed");
		}
	}

	public static WebDriver attachPage(String pageTitle) {
		driver = WebBrowser.getBrowser();
		try {

			if (!driver.getTitle().toUpperCase().contains(pageTitle.toUpperCase())) {
				driver.switchTo().defaultContent();
				try {
					WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2 * TIME_INTERVAL / 1000));
					wait.until(ExpectedConditions.numberOfWindowsToBe(2));
				} catch (Exception ex) {
					if (driver.getWindowHandles().size() <= 2) {
						return driver;
					}
				}
				int i = 0;
				int index = MAX_DELAY / TIME_INTERVAL;
				while (i < index) {
					for (String winHandle : driver.getWindowHandles()) {
						try {
							String title = pageTitle.replace("Page", "");
							boolean isNumeric = isStringInt(title);
							if (isNumeric) {
								int windowIndex = Integer.parseInt(title);
								return driver.switchTo()
										.window((driver.getWindowHandles().toArray()[windowIndex - 1]).toString());
							}
						} catch (Exception ex) {
							log.error(ex.getMessage());
						}

						if (driver.switchTo().window(winHandle).getTitle().toUpperCase()
								.contains(pageTitle.toUpperCase())) {
							return driver.switchTo().window(winHandle);
						}
					}
					i++;
					sleep(TIME_INTERVAL);
				}
			} else {
				sleep(TIME_INTERVAL);
			}
			return driver;
		} catch (UnhandledAlertException e) {
			return driver;
		} catch (Exception ex) {
			return driver.switchTo().window((driver.getWindowHandles().toArray()[0]).toString());
		}
	}

	public static void scrollAndClick(WebElement element) {
		sleep(3 * TIME_INTERVAL);
		try {
			element.click();
		} catch (Exception ex) {
			try {
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
			} catch (Exception jsEx) {
				sleep(2 * TIME_INTERVAL);
				throw new CustomException("Scroll and click unsuccessful: " + jsEx.getMessage());
			}
		}
	}

	public static WebDriver getFrame(String propertyValue) {
		try {
			driver = WebBrowser.getBrowser();
			driver.switchTo().defaultContent();
			int i = 0;
			while (i < MAX_DELAY / TIME_INTERVAL) {
				try {
					String[] frameIndex = propertyValue.split("_");
					boolean isNumeric = isStringInt(frameIndex[0]);
					if (isNumeric) {
						int index = Integer.parseInt(frameIndex[0]);
						driver.switchTo().frame(index);
						if (frameIndex.length > 1) {
							isNumeric = isStringInt(frameIndex[1]);
							sleep(2 * TIME_INTERVAL);
							if (isNumeric) {
								index = Integer.parseInt(frameIndex[1]);
								return driver.switchTo().frame(index);
							} else {
								return driver.switchTo().frame(frameIndex[1]);
							}
						}
						return driver;
					} else {
						for (int j = 0; j < frameIndex.length; j++) {
							driver.switchTo().frame(frameIndex[j]);
							sleep(2 * TIME_INTERVAL);
						}
						return driver;
					}
				} catch (Exception e) {
					sleep(TIME_INTERVAL);
				}
				i++;
			}
			return driver;
		} catch (Exception ex) {
			throw new CustomException("Web Frame with :" + propertyValue + " not found");
		}

	}

	public static boolean isStringInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public static boolean isSorted(List<Integer> sortedList) {
		return IntStream.range(1, sortedList.size())
				.map(index -> sortedList.get(index - 1).compareTo(sortedList.get(index))).allMatch(order -> order >= 0);
	}

	public static boolean isReverseSorted(List<Integer> sortedList) {
		return IntStream.range(1, sortedList.size())
				.map(index -> sortedList.get(index - 1).compareTo(sortedList.get(index))).allMatch(order -> order >= 0);
	}

	public static boolean checked(WebElement checkBox) {
		return checkBox.isSelected();
	}

	public static void selectByText(WebElement element, String value) {
		Select drpDownLst = new Select(element);
		drpDownLst.selectByVisibleText(value);
	}

	public static boolean validationOfSortedDropdownAscending(WebElement element) {
		boolean orderStatus = false;
		Select dropdown = new Select(element);

		List<WebElement> allOptionsElement = dropdown.getOptions();
		List<String> options = new ArrayList<String>();

		for (WebElement optionElement : allOptionsElement) {
			options.add(optionElement.getText().trim());
		}
		List<String> tempList = options.stream().sorted().collect(Collectors.toList());
		String englishRules1 = ("< '\u0021' < '\u0040' < '\u0023' < '\u0024' < '\u0025' < '\u005E' "
				+ "< '\u0026' < '\u002A' < '\u0028' < '\u0029'< '\u002D' < '\u002B' < '\u0020'"
				+ " < 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9 < a < b < c < d < e < f < g < h < i < j < k < l "
				+ " < m < n < o < p < q < r < s < t < u < v < w < x < y < z < A < B < C < D < E < F "
				+ " < G < H < I < J < K < L < M < N < O < P < Q < R < S < T < U < V < W < X < Y < Z");

		try {

			RuleBasedCollator rbc = new RuleBasedCollator(englishRules1);
			rbc.setStrength(Collator.PRIMARY);
			Collections.sort(tempList, rbc);
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		orderStatus = options.equals(tempList);
		return orderStatus;

	}

	public static Comparator<String> stringAlphabeticalComparator = new Comparator<String>() {
		public int compare(String str1, String str2) {
			return ComparisonChain.start().compare(str1, str2, String.CASE_INSENSITIVE_ORDER).compare(str1, str2)
					.result();
		}
	};
	public static Comparator<String> stringReverseAlphabeticalComparator = new Comparator<String>() {
		public int compare(String str1, String str2) {
			return -1 * ComparisonChain.start().compare(str1, str2, String.CASE_INSENSITIVE_ORDER).compare(str1, str2)
					.result();
		}
	};

	public static void acceptAlert() {
		int i = 0;
		driver = WebBrowser.getBrowser();
		while (i < MAX_DELAY / TIME_INTERVAL) {
			try {
				sleep(TIME_INTERVAL);
				Alert alert = driver.switchTo().alert();
				alert.accept();
				i++;
				break;
			} catch (Exception e) {
				i++;
				sleep(TIME_INTERVAL);
			}
		}
	}

	public static void dismissAlert() {
		int i = 0;
		driver = WebBrowser.getBrowser();
		while (i < MAX_DELAY / TIME_INTERVAL) {
			try {
				sleep(TIME_INTERVAL);
				Alert alert = driver.switchTo().alert();
				alert.dismiss();
				i++;
				break;
			} catch (Exception e) {
				i++;
				sleep(TIME_INTERVAL);
			}
		}

	}

	public static void enterAlertText(String text) {
		int i = 0;
		driver = WebBrowser.getBrowser();
		while (i < MAX_DELAY / TIME_INTERVAL) {
			try {
				sleep(TIME_INTERVAL);
				Alert alert = driver.switchTo().alert();
				alert.sendKeys(text);
				alert.accept();
				i++;
				break;
			} catch (Exception e) {
				i++;
				sleep(TIME_INTERVAL);
			}
		}
	}

	public static boolean verifyAlertText(String text) {
		int i = 0;
		boolean isVerified = false;
		driver = WebBrowser.getBrowser();
		while (i < MAX_DELAY / TIME_INTERVAL) {
			try {
				sleep(TIME_INTERVAL);
				Alert alert = driver.switchTo().alert();
				isVerified = alert.getText().contains(text);
				driver.switchTo().defaultContent();
				i++;
				break;
			} catch (Exception e) {
				i++;
				driver.switchTo().defaultContent();
				sleep(TIME_INTERVAL);
			}
		}
		return isVerified;

	}

	public static void check(WebElement element) {
		if (!checked(element)) {
			click(element);
		}
	}

	public static void unCheck(WebElement element) {
		if (checked(element)) {
			click(element);
		}
	}

	public static boolean isEnabled(WebElement element) {
		return element.isEnabled();
	}

	public static String getText(WebElement element) {
		String text = "";
		text = element.getText();
		if (text == null || text.trim().isEmpty()) {
			text = element.getAttribute("value");
		}
		if (text == null || text.trim().isEmpty()) {
			text = element.getAttribute("innerHTML");
		}

		return text;
	}

	public static boolean isDisplayed(WebElement element) {
		return element.isDisplayed();
	}

	public static void clearText(WebElement element) {
		element.clear();
	}

	public static void selectEnterKey(WebElement element) {
		try {
			element.click();
			element.sendKeys(Keys.ENTER);
		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	public static String getToolTip(WebElement element) {
		String[] attributes = { "alt", "title", "data-original-title", "id", "uib-tooltip", "mattooltip" };

		for (String attr : attributes) {
			String toolTipText = element.getAttribute(attr);
			if (toolTipText != null && !toolTipText.isEmpty()) {
				return toolTipText;
			}
		}
		return getText(element);
	}

	public static boolean hasSelectedItems(WebElement element) {
		Select drpDwnLst = new Select(element);
		List<WebElement> elementsList = drpDwnLst.getAllSelectedOptions();
		if (!elementsList.get(0).getText().isEmpty()) {
			return true;
		} else {
			return false;
		}

	}

	public static String getFirstOption(WebElement element) {
		Select drpDwnLst = new Select(element);
		WebElement option = drpDwnLst.getFirstSelectedOption();
		return option.getText();
	}

	public static String isReadOnly(WebElement element) {
		return element.getAttribute("readonly");
	}

	public static void scrollAndCheck(WebElement element) {
		scrollAndClickUsingJS(element);
		sleep(1000);
		if (!checked(element)) {
			element.click();
		}
	}

	public static void rightClick(WebElement element) {
		driver = WebBrowser.getBrowser();
		Actions actions = new Actions(driver);
		actions.moveToElement(element);
		actions.contextClick(element).build().perform();
	}

	public static void doubleClick(WebElement element) {
		driver = WebBrowser.getBrowser();
		Actions actions = new Actions(driver);
		actions.moveToElement(element);
		actions.doubleClick(element).build().perform();
	}

	public static void scrollAndClearEnterText(WebElement element, String text) {
		scroll(element);
		try {
			element.clear();
			sleep(1000);
			element.sendKeys(text);

			if (!text.equals(element.getAttribute("value"))) {
				element.sendKeys(Keys.CONTROL + "a", Keys.DELETE);
				sleep(500);
				element.sendKeys(text);
			}
		} catch (Exception ex) {
			throw new CustomException("Failed to enter text after scrolling: " + ex.getMessage(), ex);
		}
	}

	public static void scrollAndUncheck(WebElement element) {
		scrollAndClickUsingJS(element);
		sleep(1000);
		if (checked(element)) {
			WebBrowserUtil.click(element);
		}
	}

	public static void scrollAndWait() {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 800)");
		sleep(TIME_INTERVAL);
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 1500)");
		sleep(3 * TIME_INTERVAL);
	}

	public static void dragAndDropHorizontally(WebElement sourceElement, int distance) {
		try {
			driver = WebBrowser.getBrowser();
			Actions action = new Actions(driver);
			action.dragAndDropBy(sourceElement, distance, 0);
			action.build().perform();
			sleep(2 * TIME_INTERVAL);
		} catch (Exception ex) {
			throw new CustomException("Drag And Drop Horizontally unsuccessful" + ex.getMessage());
		}
	}

	public static void dragAndDropHorizontally(WebElement sourceElement, WebElement targetElement) {
		try {
			driver = WebBrowser.getBrowser();
			Actions action = new Actions(driver);
			action.dragAndDrop(sourceElement, targetElement);
			action.build().perform();
			sleep(2 * TIME_INTERVAL);
		} catch (Exception ex) {
			throw new CustomException("Drag And Drop Horizontally unsuccessful" + ex.getMessage());
		}
	}

	public static void clickAndHoldAndRelease(WebElement sourceElement, WebElement targetElement) {
		try {
			driver = WebBrowser.getBrowser();
			Actions action = new Actions(driver);
			action.clickAndHold(sourceElement).moveToElement(targetElement).release(targetElement).build().perform();
			sleep(2 * TIME_INTERVAL);
		} catch (Exception ex) {
			throw new CustomException("Drag And Drop Horizontally unsuccessful" + ex.getMessage());
		}
	}

	public static void scrollDown(String param) {
		Actions actions = new Actions(driver);
		param = param.toLowerCase().replace(" ", "");

		int scrollCount = extractNumber(param);

		try {
			if (param.contains("pagedown")) {
				performScroll(actions, Keys.PAGE_DOWN, scrollCount);
			} else if (param.contains("pageup")) {
				performScroll(actions, Keys.PAGE_UP, scrollCount);
			} else if (param.contains("downarrow")) {
				performScroll(actions, Keys.ARROW_DOWN, scrollCount);
			} else if (param.contains("uparrow")) {
				performScroll(actions, Keys.ARROW_UP, scrollCount);
			} else {
				int scrollValue = Integer.parseInt(param);
				performJsScroll(scrollValue);
			}
		} catch (NumberFormatException e) {
			log.error("Invalid scroll parameter: " + param, e);
		}
	}

	private static int extractNumber(String param) {
		Matcher matcher = Pattern.compile("\\d+").matcher(param);
		return matcher.find() ? Integer.parseInt(matcher.group()) : 1;
	}

	private static void performScroll(Actions actions, Keys key, int count) {
		WebElement body = driver.findElement(By.tagName("body"));
		for (int i = 0; i < count; i++) {
			actions.moveToElement(body).sendKeys(key).perform();
			sleep(3 * TIME_INTERVAL);
		}
	}

	private static void performJsScroll(int scrollValue) {
		String script = scrollValue > 0 ? "window.scrollBy(0, 2000);" : "window.scrollBy(0, -2000);";
		for (int i = 0; i < Math.abs(scrollValue); i++) {
			((JavascriptExecutor) driver).executeScript(script);
			sleep(3 * TIME_INTERVAL);
		}
	}

	public static void scrollVerifyTooltip(String info, String mousehover) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("window.scrollTo(0, 0)");
		sleep(2000);

		for (int i = 1; i <= 10; i++) {
			js.executeScript(String.format("window.scrollTo(0, %d)", i * 100));
			sleep(TIME_INTERVAL);

			try {
				WebElement hoverEle = WebBrowserUtil.getSingleElementByXpath(info);
				if (hoverEle.isDisplayed()) {
					WebBrowserUtil.mouseHover(hoverEle);
					sleep(2000);
					WebElement eleTooltip = WebBrowserUtil.getSingleElementByXpath(mousehover);
					if (eleTooltip.isDisplayed()) {
						break;
					}
				}
			} catch (Exception e) {
				log.info(e.getMessage());
			}
		}
	}

	public static void scrollToTheElement(int numberOfTimes, WebElement element) {
		for (int i = 0; i < numberOfTimes; i++) {
			sleep(2 * TIME_INTERVAL);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", element);
			sleep(2 * TIME_INTERVAL);
		}
	}

	public static void mouseHover(WebElement element) {
		driver = WebBrowser.getBrowser();
		new Actions(driver).moveToElement(element).perform();
	}

	public static void clickBrowserBackButton() {
		driver = WebBrowser.getBrowser();
		driver.navigate().back();
	}

	public static void click(WebElement element) {
		try {
			driver = WebBrowser.getBrowser();
			try {
				element.click();
			} catch (Exception ex) {
				((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
			}
		} catch (Exception ex) {
			throw new CustomException("Click unsuccessful" + ex.getMessage());
		}
	}

	public static void scrollAndClickUsingJS(WebElement element) {
		driver = WebBrowser.getBrowser();
		Actions actions = new Actions(driver);
		try {
			if (element != null) {

				if (element.isDisplayed()) {
					WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
					wait.until(ExpectedConditions.elementToBeClickable(element));
					((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
				} else {
					actions.moveToElement(element).perform();
					actions.moveToElement(element).click().build().perform();
				}
			} else {
				throw new CustomException("Element is not present!!!");
			}
		} catch (Exception ex) {
			throw new CustomException("Scroll and click unsuccessful" + ex.getMessage());
		}
	}

	public static void scrollAndClickUsingJS(String xpath, String identifier) {
		int i = 0;
		boolean staleElement = true;
		try {

			while (i < 4 && staleElement) {
				WebElement element = WebBrowserUtil.findElement(xpath, identifier);
				try {
					Actions actions = new Actions(driver);
					actions.moveToElement(element).perform();
					((JavascriptExecutor) driver).executeScript("arguments[0].click()", element);
					staleElement = false;
				} catch (Exception e) {
					i++;
					if (i == 3) {
						throw new CustomException("Scroll and click unsuccessful" + e.getMessage());
					}
				}
			}
		} catch (Exception ex) {
			throw new CustomException("Scroll and click unsuccessful" + ex.getMessage());
		}
	}

	public static void clearAndEnterText(WebElement element, String text) {
		try {
			element.click();
			element.clear();
			element.sendKeys(text);

			if (!text.equals(element.getAttribute("value"))) {
				element.sendKeys(Keys.CONTROL + "a");
				sleep(500);
				element.sendKeys(text);
			}
		} catch (Exception ex) {
			throw new CustomException(String.format("Clear And EnterText unsuccessful: %s", ex.getMessage()));
		}
	}

	public static void waitForElementToBeHidden(String xpath) {
		try {
			driver = WebBrowser.getBrowser();
			int i = 0;
			while (i < MAX_DELAY / TIME_INTERVAL) {
				try {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
					sleep(TIME_INTERVAL);
					WebElement element = driver.findElement(By.xpath(xpath));
					if (element == null) {
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
						break;
					}
					i++;
				} catch (Exception e) {
					i++;
					sleep(TIME_INTERVAL);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
					break;
				}
			}
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage());
		}
	}

	public static void waitForElementToBeVisible(String xPath) {
		try {
			driver = WebBrowser.getBrowser();
			int i = 0;
			while (i < MAX_DELAY / TIME_INTERVAL) {
				try {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
					sleep(TIME_INTERVAL);
					WebElement element = driver.findElement(By.xpath(xPath));
					if (element != null) {
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
						break;
					}
					i++;
				} catch (Exception e) {
					i++;
					sleep(TIME_INTERVAL);
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
					break;
				}
			}
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage());
		}
	}

	public static void waitForElementToVisible(String xpath) {
		try {
			driver = WebBrowser.getBrowser();
			long currentTime = System.currentTimeMillis() / 1000;

			while ((System.currentTimeMillis() / 1000) < currentTime + 120) {
				try {
					driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
					sleep(TIME_INTERVAL);
					WebElement element = driver.findElement(By.xpath(xpath));
					if (element != null) {
						driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(CONTROL_LOAD_TIMEOUT));
						break;
					}
				} catch (Exception e) {
					sleep(TIME_INTERVAL);
				}
			}
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage());
		}
	}

	public static void uploadFile(String fileName) {
		try {
			// put path to your image in a clipboard
			StringSelection ss = new StringSelection(fileName);
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);

			// imitate mouse events like ENTER, CTRL+C, CTRL+V
			Robot robot = new Robot();
			robot.delay(4000);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.delay(50);
			robot.keyRelease(KeyEvent.VK_ENTER);
		} catch (Exception ex) {
			throw new CustomException(ex.getMessage());
		}
	}

	public static boolean validateDropdownDataSize(WebElement element) {
		Select dropdown = new Select(element);
		List<WebElement> allOptionsElement = dropdown.getOptions();
		int k = allOptionsElement.size();
		if (k == 1) {
			return true;
		} else {
			return false;
		}
	}

	public static void openCmd(String cmd) {
		try {
			Runtime.getRuntime().exec(new String[] { "cmd.exe", "/K", "Start", cmd });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static boolean validateDateFormat(String date, String format) {
		List<String> formatStrings = Arrays.asList(format);
		for (String formatString : formatStrings) {
			try {
				new SimpleDateFormat(formatString).parse(date);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	public static boolean verifyDateFormat(String xpath, String text) {
		boolean result = false;
		String[] splitText = text.split("--");
		if (text.contains("verify_list")) {
			List<WebElement> links = getElementByXpath(xpath);
			String printData = "";
			for (int i = 0; i < links.size(); i++) {
				String eleText = WebBrowserUtil.getText(links.get(i));
				if (eleText != null && !eleText.isEmpty()) {
					if (!WebBrowserUtil.validateDateFormat(eleText, splitText[1])) {
						ExtentCucumberAdapter.addTestStepLog(eleText + " is not correct date format");
						return false;
					}
				} else {
					printData += eleText + ",";
					result = true;
				}

			}
			ExtentCucumberAdapter.addTestStepLog(printData + " are in correct date format");
			return result;
		} else {
			WebElement links = getSingleElementByXpath(xpath);
			String eleText = WebBrowserUtil.getText(links);
			if (eleText == "" || eleText == null || eleText.isEmpty()) {
				ExtentCucumberAdapter.addTestStepLog("Element is null");
				result = true;
			} else if (WebBrowserUtil.validateDateFormat(eleText, splitText[1]) == false) {
				ExtentCucumberAdapter.addTestStepLog(eleText + " is not correct date format");
				return false;
			} else {
				ExtentCucumberAdapter.addTestStepLog(eleText + " is correct date format");
				result = true;
			}
			return result;
		}
	}

	public static void enterDataInExcel(String path) {
		// path="path_enter-row"
		// path="path_update-cell_idToFindRowNumber_columnNumber_valueToBeUpdated"
		String[] splitText = path.split("--");
		String excelFilePath = splitText[0];
		try {

			File file = new File(excelFilePath);
			FileInputStream inputdocument = new FileInputStream(file);
			// convert it into a POI object
			XSSFWorkbook myxlsxworkbook = new XSSFWorkbook(inputdocument);
			// Read excel sheet that needs to be updated
			XSSFSheet myworksheet = myxlsxworkbook.getSheetAt(0);
			if (path.contains("enter-row")) {
				int lastRowIndex = 0;
				if (myworksheet.getPhysicalNumberOfRows() > 0) {
					lastRowIndex = myworksheet.getLastRowNum();
					List<String> lastRowData = new ArrayList<>();
					Row row = myworksheet.getRow(lastRowIndex);
					int colNum = row.getLastCellNum();
					for (int j = 0; j < colNum; j++) {
						final Cell cell = row.getCell(j);
						if (cell != null) {
							String regex = "[0-9]+";
							Pattern p = Pattern.compile(regex);
							Matcher m = p.matcher(cell.toString());
							if (m.matches()) {
								lastRowData.add(String.valueOf(Integer.parseInt(cell.toString()) + 1));
							} else {
								lastRowData.add(cell.toString());
							}

						} else {
							lastRowData.add("");
						}

					}
					inputdocument.close();

					Row rowNew = myworksheet.createRow(lastRowIndex++);
					int cellnum = 0;
					for (String c : lastRowData) {
						Cell cell = rowNew.createCell(cellnum++);
						cell.setCellValue(c);
					}
				}
			} else if (path.contains("update-cell")) {
				String[][] excelData = null;
				int rows = myworksheet.getPhysicalNumberOfRows();
				// get number of cell from row
				int cells = myworksheet.getRow(0).getPhysicalNumberOfCells();
				excelData = new String[rows][cells];
				for (int p = 0; p < rows; p++) {
					Row row2 = myworksheet.getRow(p);
					for (int n = 0; n < cells; n++) {

						Cell cell = row2.getCell(n);
						if (cell != null) {
							excelData[p][n] = cell.toString();
						} else {
							excelData[p][n] = "";
						}
					}
				}
				int rowCount = 0;
				for (int l = 0; l < excelData.length; l++) {
					if (excelData[l][0] != null && excelData[l][0].contains(splitText[2])) {
						rowCount = l;
						break;
					}
				}
				inputdocument.close();
				Cell cellEdit = myworksheet.getRow(rowCount).getCell(Integer.parseInt(splitText[3]));
				if (cellEdit == null) {
					Row rowNew = myworksheet.getRow(rowCount);
					cellEdit = rowNew.createCell(Integer.parseInt(splitText[3]));
				}
				cellEdit.setCellValue(splitText[4]);
			}
			if (file != null) {
				FileOutputStream fileOut = new FileOutputStream(file);

				// write this workbook to an Outputstream.
				if (myxlsxworkbook != null)
					myxlsxworkbook.write(fileOut);
				fileOut.flush();
				fileOut.close();
			}
			if (myxlsxworkbook != null)
				myxlsxworkbook.close();

		} catch (Exception e) {
			throw new CustomException(e.getMessage(), e);
		}
	}

	public static void enterDataInRequiredCell(String filePath) {
		// path : path of the
		// file,sheetname,celldata-rownumber-colnumber,celldata2-rownumber2-colnumber2
		String[] splitPath = filePath.split(",");
		String path = splitPath[0];
		String sheetName = splitPath[1];

		if (splitPath.length >= 3) {
			try {
				FileInputStream fis = new FileInputStream(new File(path));
				XSSFWorkbook workbook = new XSSFWorkbook(fis);

				if (workbook.getSheetIndex(sheetName) != -1) {
					XSSFSheet sheet = workbook.getSheet(sheetName);
					for (String data : splitPath) {
						if (data.contains("-")) {
							String[] entries = data.split("-");
							String cellValue = entries[0];
							int row = Integer.parseInt(entries[1]);
							int col = Integer.parseInt(entries[2]);
							XSSFRow sheetRow = sheet.getRow(row);
							if (sheetRow == null) {
								sheetRow = sheet.createRow(row);
							}
							XSSFCell cell = sheetRow.getCell(col);
							if (cell == null) {
								cell = sheetRow.createCell(col);
							}
							cell.setCellValue(cellValue);
						}
					}
					fis.close();
					FileOutputStream fos = new FileOutputStream(new File(path));
					workbook.write(fos);
					fos.close();
				}
			} catch (IOException e) {
				log.info("Unable to enter and save to " + path);
				e.printStackTrace();
			}
		} else {
			try {
				FileInputStream fis = new FileInputStream(new File(filePath));
				XSSFWorkbook workbook = new XSSFWorkbook(fis);
				XSSFSheet sheet = workbook.getSheetAt(0);

				for (String data : splitPath) {
					if (data.contains("-")) {
						String[] entries = data.split("-");
						String cellValue = entries[0];
						int row = Integer.parseInt(entries[1]);
						int col = Integer.parseInt(entries[2]);
						XSSFRow sheetRow = sheet.getRow(row);
						if (sheetRow == null) {
							sheetRow = sheet.createRow(row);
						}
						XSSFCell cell = sheetRow.getCell(col);
						if (cell == null) {
							cell = sheetRow.createCell(col);
						}
						cell.setCellValue(cellValue);
						sheet.getRow(row).getCell(col).setCellValue(cellValue);
					}
				}

				fis.close();
				FileOutputStream fos = new FileOutputStream(new File(filePath));
				workbook.write(fos);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean verifyValueinJson(String path) {
		try {

			String[] splitedText = path.split("--");
			String[] splitKey = splitedText[1].split("\\.");
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader(splitedText[0]));
			JSONArray jsonArray = (JSONArray) jsonObject.get(splitKey[0]);
			String value = jsonArray.get(0).toString();
			HashMap<String, Object> userResponseMap = new HashMap<String, Object>();
			ObjectMapper mapper = new ObjectMapper();
			userResponseMap = mapper.readValue(value, new TypeReference<HashMap<String, Object>>() {
			});
			String finalString = userResponseMap.get(splitKey[1]).toString();
			ExtentCucumberAdapter.addTestStepLog(finalString + " length is : " + finalString.length());
			if (finalString.length() == Integer.parseInt(splitedText[2])) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			ExtentCucumberAdapter.addTestStepLog("Error : " + e);
			return false;
		}
	}

	public static boolean readPDF(String url, String pdf) {
		try {
			URL testURL = new URL(url);
			RandomAccessBufferedFileInputStream testFile = new RandomAccessBufferedFileInputStream(
					testURL.openStream());
			PDFParser testPDF = new PDFParser(testFile);
			testPDF.parse();
			String testText = new PDFTextStripper().getText(testPDF.getPDDocument());
			if (testText.contains(pdf)) {
				ExtentCucumberAdapter.addTestStepLog("PDF content : " + testText + " , Expected content : " + pdf);
				testFile.close();
				return true;
			}
		} catch (Exception e) {
			ExtentCucumberAdapter.addTestStepLog("Error : " + e);
		}
		return false;
	}

	public static boolean compareAndVerifyImage(String imageName) {
		boolean verified = false;
		driver = WebBrowser.getBrowser();
		File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		try {
			boolean enableCompareImage = Boolean
					.parseBoolean(CommonUtil.getXMLData(Constants.APPLICATION_SETTING_PATH, "EnableCompareImage"));

			String compareImages = "src/test/java/Images/Compare/";
			File file = new File(compareImages);
			file.mkdirs();

			String baseImages = "src/test/java/Images/Baseline/";
			File file1 = new File(baseImages);
			file1.mkdirs();

			if (enableCompareImage == false) {
				File dest = new File(baseImages + imageName + ".jpeg");
				FileUtils.copyFile(source, dest);
				return verified;
			} else {
				File dest = new File(compareImages + imageName + ".jpeg");
				FileUtils.copyFile(source, dest);
				BufferedImage imgA = ImageIO.read(new File(compareImages + imageName + ".jpeg"));

				DataBuffer dbA = imgA.getData().getDataBuffer();
				int sizeA = dbA.getSize();

				BufferedImage imgB = ImageIO.read(new File(baseImages + imageName + ".jpeg"));
				DataBuffer dbB = imgB.getData().getDataBuffer();
				int sizeB = dbB.getSize();
				if (sizeA == sizeB) {
					for (int i = 0; i < sizeA; i++) {
						if (dbA.getElem(i) != dbB.getElem(i)) {
							ExtentCucumberAdapter.addTestStepLog("Images are not same");
							return verified;
						}
					}
					ExtentCucumberAdapter.addTestStepLog("Images are same");
					verified = true;
				} else {
					ExtentCucumberAdapter.addTestStepLog("Images are not same");
					verified = false;
				}
			}
		} catch (Exception ex) {
			verified = false;
			log.error("Error: " + ex.getMessage());
		}
		return verified;

	}

	private static String getlatestfile(String ext) {
		File theNewestFile = null;
		try {
			String path = System.getProperty("user.dir");

			File dir = new File(path);
			FileFilter fileFilter = new WildcardFileFilter("*." + ext);
			File[] files = dir.listFiles(fileFilter);
			if (files.length > 0) {
				Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				theNewestFile = files[0];
			}
		} catch (Exception e) {
			ExtentCucumberAdapter.addTestStepLog("Error: " + e);
			throw new CustomException(e.getMessage(), e);
		}
		return theNewestFile.toString();
	}

	private static String formatDate(String data) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate currentDate = LocalDate.now();

		if ("current date".equalsIgnoreCase(data)) {
			return currentDate.format(formatter);
		}

		boolean isAddition = data.contains("+");
		boolean isSubtraction = data.contains("-");

		if (isAddition || isSubtraction) {
			return processDateAdjustment(data, currentDate, isAddition);
		}
		return "";
	}

	private static String processDateAdjustment(String data, LocalDate currentDate, boolean isAddition) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		String[] dateParts = data.split("\\+|-", 2);
		if (dateParts.length < 2) {
			return "";
		}

		String[] timeUnits = dateParts[1].trim().split(" ", 2);
		if (timeUnits.length < 2) {
			return "";
		}

		int amount;
		try {
			amount = Integer.parseInt(timeUnits[0]);
		} catch (NumberFormatException e) {
			return "";
		}

		String unit = timeUnits[1].toLowerCase();
		LocalDate adjustedDate = currentDate; // Default value

		switch (unit) {
		case "days":
			adjustedDate = isAddition ? currentDate.plusDays(amount) : currentDate.minusDays(amount);
			break;
		case "weeks":
			adjustedDate = isAddition ? currentDate.plusWeeks(amount) : currentDate.minusWeeks(amount);
			break;
		case "months":
			adjustedDate = isAddition ? currentDate.plusMonths(amount) : currentDate.minusMonths(amount);
			break;
		case "years":
			adjustedDate = isAddition ? currentDate.plusYears(amount) : currentDate.minusYears(amount);
			break;
		default:
			throw new IllegalArgumentException("Invalid time unit: " + unit);
		}

		return adjustedDate.format(formatter);
	}

// To edit CSV file with specified value for the specified header
// The values in testdata file should be passed as "sample-csv.csv,##1,Exam_Code:randomnumber_10,ExamCode_withModifier:@randomtext,##2,modality:JK,bodyRegion:Head,exam_code_short_name:Short Name,##3,exam_code_med_name:Text,dateStart:current date,dateEnd:current date+5 Years"
	public static void editCSVfile(String values) {
		String[] splitValues1 = values.split("##");
		String filePath = Paths.get(System.getProperty("user.dir"), splitValues1[0].split(",")[0]).toString();

		try {
			// Read existing CSV data
			FileReader reader = new FileReader(filePath);
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
			List<CSVRecord> records = csvParser.getRecords();
			List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());

			// Create a temporary list to hold the modified records
			List<List<String>> modifiedRecords = new ArrayList<>();
			String value = "";
			int i = 0;
			// Iterate over the records and append text to the specified column
			for (CSVRecord record : records) {
				i++;
				Map<String, String> modifications = new HashMap<>();
				for (String item : splitValues1) {
					String[] items = item.split(",");
					if (items[0].equals(String.valueOf(i))) { // if the specified row(items[0]) is same as the current
																// row (i)?
						// Create new string "items" by removing the first element
						String[] dataList = new String[items.length - 1];
						System.arraycopy(items, 1, dataList, 0, dataList.length);
						for (String Data : dataList) {
							String[] splitData = Data.split(":");
							// Modify the value as needed
							String modifiedValue = GetRequiredValue(splitData[1]);
							if (splitData[0].equals("Exam_Code"))
								CommonUtil.setCopiedText(modifiedValue);
							modifications.put(splitData[0], modifiedValue);
						}
					}
				}
				List<String> newRecord = new ArrayList<>();
				for (String header : headers) {
					if (modifications.containsKey(header))
						value = modifications.get(header);
					else
						value = record.get(header);
					newRecord.add(value);
				}
				modifiedRecords.add(newRecord); // modifiedRecords=> To get modifiedRecord
			}

			csvParser.close();
			reader.close();

			// Write the modified data back to the original file
			FileWriter writer = new FileWriter(filePath);
			CSVPrinter csvPrinter = new CSVPrinter(writer,
					CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));

			for (List<String> modifiedRecord : modifiedRecords) {
				csvPrinter.printRecord(modifiedRecord);
			}

			csvPrinter.close();
			writer.close();

		} catch (IOException e) {
			log.info("Error in editing CSV file" + e);
			throw new CustomException(e.getMessage(), e);

		}
	}

// If data contains "date" format date as required else get random number or random text or retain the data
	private static String GetRequiredValue(String data) {
		if (data.contains("date")) {
			data = WebBrowserUtil.formatDate(data);
			return data;
		} else
			return CommonUtil.getData(data);
	}

// read specified value of data in CSV file
// The value is specified in testdata file as "sample-csv.csv,##2,exam_code_short_name"	 ##2=> 2nd row excluding header row	
	public static boolean readCSVfile(String values) {
		values = CommonUtil.getData(values);
		boolean isVerified = false;
		String[] splitValues = values.split(",");
		String expectedValue = splitValues[2];

		String[] splitValues1 = values.split("##");

		String filePath = Paths.get(System.getProperty("user.dir"), splitValues1[0].split(",")[0]).toString();
		String value = "";
		try {
			// Read existing CSV data
			FileReader reader = new FileReader(filePath);
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
			List<CSVRecord> records = csvParser.getRecords();
			List<String> headers = new ArrayList<>(csvParser.getHeaderMap().keySet());

			// Create a temporary list to hold the modified records
			int i = 0;
			// Iterate over the records and append text to the specified column
			for (CSVRecord record : records) {
				i++;
				for (String item : splitValues1) {
					String[] items = item.split(",");
					if (items[0].equals(String.valueOf(i))) { // if the specified row(items[0]) is same as the current
																// row (i)?
						for (String header : headers) {
							if (header.equals(items[1])) {
								value = record.get(header);
								break;
							}
						}
					}
				}
			}
			csvParser.close();
			reader.close();
		} catch (IOException e) {
			log.info("Error in editing CSV file" + e);
			throw new CustomException(e.getMessage(), e);
		}

		isVerified = value.equals(expectedValue);
		ExtentCucumberAdapter
				.addTestStepLog("Value read from CSV file : " + value + " ;  Expected value : " + expectedValue);
		return isVerified;
	}

	public static void scrollRandomlyByNumber(String xpath, String identifier, String text) {
		int i = 0;
		boolean staleElement = true;
		driver = WebBrowser.getBrowser();
		int num = Integer.parseInt(text);
		try {
			while (i < 4 && staleElement) {
				WebElement element = WebBrowserUtil.findElement(xpath, identifier);
				try {

					JavascriptExecutor js = (JavascriptExecutor) driver;
					for (int j = 0; j < num; j++) {
						js.executeScript("arguments[0].scrollTop += 350;", element);
						sleep(1000);
					}
					staleElement = false;
				} catch (Exception e) {
					i++;
					if (i == 3) {
						throw new CustomException("Scroll unsuccessful" + e.getMessage());
					}
				}
			}
		} catch (Exception ex) {
			throw new CustomException("Scroll  unsuccessful" + ex.getMessage());
		}
	}

	public static void deleteFile(String filePath) {
		try {
			filePath = CommonUtil.getData(filePath);
			File fileToBeDeleted = new File(filePath);
			if (fileToBeDeleted.exists()) {
				if (fileToBeDeleted.delete()) {
					ExtentCucumberAdapter.addTestStepLog("Deleted the file: " + fileToBeDeleted.getName());
				} else {
					ExtentCucumberAdapter.addTestStepLog("Failed to delete the file.");
				}
			} else {
				ExtentCucumberAdapter.addTestStepLog("File does not exists.");
			}
		} catch (Exception e) {
			throw new CustomException("Failed to delete the file." + e.getMessage());
		}
	}

	public static boolean verifyPDFData(String filePath) {
		boolean isVerified = false;
		String actualText = "";
		try {
			filePath = CommonUtil.getData(filePath);
			String[] data = filePath.split(",");
			filePath = data[0];
			if (!(data[0].contains(":"))) {
				filePath = WebBrowserUtil.getlatestfile("pdf");
			}
			actualText = common.PDFUtil.getText(filePath);
			log.info("PDF data: " + actualText);
			ExtentCucumberAdapter.addTestStepLog("PDF data: " + actualText);
			String[] expectedText = data[1].split("--");
			String verifyText = "";
			if (actualText != null && !actualText.trim().isEmpty()) {
				for (int i = 0; i < expectedText.length; i++) {
					if (actualText.contains(expectedText[i])) {
						verifyText = verifyText + "true,";
						log.info(expectedText[i] + " is present in pdf data");
						ExtentCucumberAdapter.addTestStepLog(expectedText[i] + " is present in pdf data");
					} else {
						ExtentCucumberAdapter.addTestStepLog(expectedText[i] + " is NOT present in pdf data");
						verifyText = verifyText + "false,";
					}
				}
			}
			if (verifyText.contains("false")) {
				isVerified = false;
			} else {
				isVerified = true;
			}
			return isVerified;
		} catch (Exception ex) {
			return isVerified;
		}
	}

	public static void selectByCoordinates(String text) {

		driver = WebBrowser.getBrowser();
		try {
			int yaxis = 0;
			int xaxis = 0;
			String desiredPath = System.getProperty("user.dir");
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			// Define the destination file
			File destinationFile = new File("screenshot.png");

			try {
				// Copy the screenshot to the destination file
				Files.copy(screenshot.toPath(), destinationFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			String screenshotPath = destinationFile.getAbsolutePath();
			String pythonPath = desiredPath + "/detect.py";
			String[] textSplit = text.split(",");
			String imagePath = desiredPath + "\\src\\test\\java\\attachments\\" + textSplit[0];
			if (!textSplit[1].isEmpty()) {
				yaxis = Integer.parseInt(textSplit[1]);
			}
			if (!textSplit[2].isEmpty()) {
				xaxis = Integer.parseInt(textSplit[2]);
			}
			// Load the screenshot
			File file = new File(screenshotPath);
			BufferedImage screenshot1 = ImageIO.read(file);

			List<List<Integer>> resultList = GetCoordinatesByImage(pythonPath, screenshotPath, imagePath);

			List<Integer> coordinates = resultList.get(0);
			int x = coordinates.get(0) + xaxis;
			int y = coordinates.get(1) + yaxis;

			Robot robot = new Robot();
			robot.setAutoDelay(1000);

			// Move the mouse to the coordinates and click
			robot.mouseMove(x, y);

			robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
			sleep(100); // 100 milliseconds delay
			robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
			robot.delay(2000);
			destinationFile.delete();
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}

	}

	public static boolean verifyByImage(String text) {

		driver = WebBrowser.getBrowser();
		boolean isVerifed = false;
		try {
			String desiredPath = System.getProperty("user.dir");
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			// Define the destination file
			File destinationFile = new File("screenshot.png");
			try {
				// Copy the screenshot to the destination file
				Files.copy(screenshot.toPath(), destinationFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			String screenshotPath = destinationFile.getAbsolutePath();
			String pythonPath = desiredPath + "\\detect.py";
			String imagePath = desiredPath + "\\src\\test\\java\\attachments\\" + text;

			// Load the screenshot
			File file = new File(screenshotPath);
			BufferedImage screenshot1 = ImageIO.read(file);

			List<List<Integer>> resultList = GetCoordinatesByImage(pythonPath, screenshotPath, imagePath);
			destinationFile.delete();
			List<Integer> coordinates = resultList.get(0);
			int x = coordinates.get(0);
			int y = coordinates.get(1);
			if (x != 0 && y != 0) {
				isVerifed = true;
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return isVerifed;

	}

	private static List<List<Integer>> GetCoordinatesByImage(String pythonScriptPath, String sccreenshot,
			String target) {
		// Path to the Python script
		// Construct the command
		String[] command = { "python", pythonScriptPath, sccreenshot, target };
		List<List<Integer>> resultList = new ArrayList<>();
		try {
			// Start the process
			ProcessBuilder processBuilder = new ProcessBuilder(command);
			Process process = processBuilder.start();

			// Wait for the process to complete
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				StringBuilder errorOutput = new StringBuilder();
				String line;
				while ((line = errorReader.readLine()) != null) {
					errorOutput.append(line).append("\n");
				}
				System.err.println("Error output: " + errorOutput.toString());
			} else {
				String desiredPath = System.getProperty("user.dir");
				// Read the JSON file with detections
				String jsonFilePath = desiredPath + "\\detect.json";
				resultList = readDetectionsFromJson(jsonFilePath);
			}

		} catch (IOException | InterruptedException | JSONException e) {
			e.printStackTrace();
		}

		return resultList;
	}

	private static List<List<Integer>> readDetectionsFromJson(String filePath) throws IOException, JSONException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder jsonContent = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			jsonContent.append(line);
		}
		reader.close();

		// Parse the JSON content
		JSONArray jsonArray = new JSONArray(jsonContent.toString());
		List<List<Integer>> detections = new ArrayList<>();
		log.info(jsonArray.length());
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONArray innerArray = jsonArray.getJSONArray(i);
			List<Integer> detection = new ArrayList<>();
			for (int j = 0; j < innerArray.length(); j++) {
				detection.add((int) innerArray.getDouble(j));
			}
			detections.add(detection);
		}
		return detections;
	}

	private static void captureAndCompressScreenshot() {
		driver = WebBrowser.getBrowser();
		try {
			// Take the screenshot and read it as a BufferedImage
			File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			BufferedImage image = ImageIO.read(screenshotFile);

			// Get the original image size
			long originalFileSize = screenshotFile.length(); // in bytes
			// If the image size is already below the threshold, skip compression
			if (originalFileSize <= 70 * 1024) { // If <= 70 KB
				sce.attach(java.nio.file.Files.readAllBytes(screenshotFile.toPath()), "image/jpeg", "image");
				return;
			}

			// Initialize compression settings
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			float compressionQuality = 0.6f; // Starting compression quality
			javax.imageio.ImageWriter jpegWriter = ImageIO.getImageWritersByFormatName("jpg").next();
			javax.imageio.ImageWriteParam jpegParams = jpegWriter.getDefaultWriteParam();
			jpegParams.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);

			byte[] bestCompressedScreenshot = null;
			long bestFileSize = originalFileSize;
			boolean withinRange = false;

			// Compression loop
			while (!withinRange) {
				outputStream.reset();
				jpegParams.setCompressionQuality(compressionQuality);

				try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
					jpegWriter.setOutput(ios);
					jpegWriter.write(null, new javax.imageio.IIOImage(image, null, null), jpegParams);
				}

				long compressedFileSize = outputStream.size();
				// Store the best compressed version
				if (compressedFileSize < bestFileSize) {
					bestCompressedScreenshot = outputStream.toByteArray();
					bestFileSize = compressedFileSize;
				}

				// Stop if within target size
				if (compressedFileSize <= 70 * 1024) {
					withinRange = true;
				} else if (compressionQuality > 0.05f) {
					compressionQuality -= 0.03f;
				} else {
					break;
				}
			}
			jpegWriter.dispose();

			// Attach the best available image
			if (bestCompressedScreenshot != null && bestFileSize <= originalFileSize) {
				sce.attach(bestCompressedScreenshot, "image/jpeg", "image");
			} else {
				sce.attach(java.nio.file.Files.readAllBytes(screenshotFile.toPath()), "image/jpeg", "image");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void sleep(int seconds) {
		try {
			Thread.sleep(seconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}