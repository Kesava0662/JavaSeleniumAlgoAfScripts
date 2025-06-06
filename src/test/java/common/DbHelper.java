package common;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class DbHelper {

	private static int iterations = 10;
	static final Logger log = Logger.getLogger(DbHelper.class);
	private static final String NULL_CONSTANT = "N/A";

	private DbHelper() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	// To get data from postgresql or redshift db and store in list
	@SuppressWarnings("rawtypes")
	public static List getData(String queryParam) {
		List resultList = null;
		String[] dbDetails = queryParam.split("_");
		if (dbDetails.length > 1) {
			if (dbDetails[0].toLowerCase().contains("RedShift".toLowerCase())) {
				RedShiftUtil redShiftUtil = new RedShiftUtil();
				resultList = redShiftUtil.getData(queryParam);
			} else if (dbDetails[0].toLowerCase().contains("PostgreSql".toLowerCase())) {
				PostgreSQLUtil postgreSQLUtil = new PostgreSQLUtil();
				resultList = postgreSQLUtil.getData(queryParam);
			}
		}
		return resultList;
	}

	public static String queryCopiedText(String query) {
		String constCopiedText = "@copied_text";
		if (query.contains(constCopiedText)) {
			for (int i = 1; i <= iterations; i++) {
				String value = constCopiedText + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getCopiedCountText(Integer.toString(i)));
				}
			}
			if (query.contains(constCopiedText)) {
				query = query.replace(constCopiedText, CommonUtil.getCopiedText());
			}
			if (query.contains(NULL_CONSTANT)) {
				query = query.replace(String.format("'%s'", NULL_CONSTANT), "null");

			}
		}
		return query;
	}

	public static String alphaNumeric32CopiedText(String query) {
		String alpaNumConstant = "@alphaNumeric32CopiedText";
		if (query.contains(alpaNumConstant)) {
			for (int i = 1; i <= iterations; i++) {
				String value = alpaNumConstant + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getAlphaNum32CopiedCountText(Integer.toString(i)));
				}
			}
			if (query.contains(alpaNumConstant)) {
				query = query.replace(alpaNumConstant, CommonUtil.getAlphaNum32CopiedText());
			}
			if (query.contains(NULL_CONSTANT)) {
				query = query.replace(String.format("'%s'", NULL_CONSTANT), "null");
			}
		}
		return query;
	}

	public static String alphaNumeric64CopiedText(String query) {
		String alpaNumConstant64 = "@alphaNumeric64CopiedText";
		if (query.contains(alpaNumConstant64)) {
			for (int i = 1; i <= iterations; i++) {
				String value = alpaNumConstant64 + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getAlphaNum64CopiedCountText(Integer.toString(i)));
				}
			}
			if (query.contains(alpaNumConstant64)) {
				query = query.replace(alpaNumConstant64, CommonUtil.getAlphaNum64CopiedText());
			}
			if (query.contains(NULL_CONSTANT)) {
				query = query.replace(String.format("'%s'", NULL_CONSTANT), "null");
			}
		}
		return query;
	}

	public static String globalRandomText(String query) {
		String globalRmCopiedText = "@globalRanodmCopiedText";
		if (query.contains(globalRmCopiedText)) {

			String regex = "@globalRanodmCopiedText(\\d+)"; // Match @globalRanodmCopiedText followed by digits
			// Use Pattern and Matcher for dynamic replacement
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(query);
			while (matcher.find()) {
				String matchedText = matcher.group(); // Full match, e.g., @globalRanodmCopiedText12
				String numberPart = matcher.group(1); // Extract the number part, e.g., 12
				// Replace the matched text with the global random text
				query = query.replace(matchedText, CommonUtil.getGlobalRandomText(numberPart));
			}
			if (query.contains(globalRmCopiedText)) {
				query = query.replace(globalRmCopiedText, CommonUtil.getGlobalRandomText("1"));
			}
		} else if (query.contains("@")) {
			for (String key : CommonUtil.globalUserValues.keySet()) {
				if (query.contains(key)) {
					query = query.replace("@" + key, CommonUtil.globalUserValues.get(key));
				}
			}
		}
		return query;
	}

	public static String replaceGlobalText(String query) {

		if (query.contains("@global_text")) {
			for (int i = 1; i <= iterations; i++) {
				String value = "@global_text" + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getGlobalText(Integer.toString(i)));
				}
			}
		}
		return query;
	}

	public static String textRandomCopiedText(String query) {
		String verifyRmCopiedText = "@verifyRandomCopiedText";
		if (query.contains(verifyRmCopiedText)) {
			for (int i = 1; i <= iterations; i++) {
				String value = verifyRmCopiedText + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getRandomCopiedCountText(Integer.toString(i)));
				}
			}

			if (query.contains(verifyRmCopiedText)) {
				query = query.replace(verifyRmCopiedText, CommonUtil.getCopiedRandomText());
			}
		}
		return query;
	}

	public static String textRandomCopiedNumber(String query) {
		String verifyRmCopiedNumber = "@verifyRandomCopiedNumber";
		if (query.contains(verifyRmCopiedNumber)) {
			for (int i = 1; i <= iterations; i++) {
				String value = verifyRmCopiedNumber + i;
				if (query.contains(value)) {
					query = query.replace(value, CommonUtil.getRandomCopiedCountNumber(Integer.toString(i)));
				}
			}
			if (query.contains(verifyRmCopiedNumber)) {
				query = query.replace(verifyRmCopiedNumber, String.valueOf(CommonUtil.getCopiedRandomNumber()));
			}
		}
		return query;
	}

}
