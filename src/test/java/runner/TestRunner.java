package runner;

import org.junit.AfterClass;
import common.*;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/java/features", glue = { "stepdefinitions", "common" }, plugin = {
		"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", "common.StepListener" })
public class TestRunner {
	public static int retries = 0;

	@AfterClass
	public static void tearDown() {

		if (WebBrowser.isBrowserOpened()) {
			WebBrowser.closeBrowserInstance();

		}
		if (common.AddLogoToPdf.PROJECT_NAME.toLowerCase().contains("karnataka")) {
			try {
				CustomReportGenerator.generateReport();
				common.AddLogoToPdf.htmlToPdfConverter();
			} catch (Exception e) {
				System.out.println(e);
			}
			try {
				common.AddLogoToPdf.addLogoToPdf();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

}
