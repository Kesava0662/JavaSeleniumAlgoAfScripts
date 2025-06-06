package common;

public class Assertion {

	private Assertion() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	public static void isTrue(boolean result, String message) {
		CommonUtil.error = "Assertion failure in step: " + message;

		if (WebBrowser.boolEachSoftAssersion) {
			Hooks.softAssertions.assertTrue(result, message);
		} else {
			if (!result) {
				throw new AssertionError(message);
			}
		}
	}

	public static void assertAll() {
		if (WebBrowser.boolEachSoftAssersion) {
			Hooks.softAssertions.assertAll();
		}
	}
}
