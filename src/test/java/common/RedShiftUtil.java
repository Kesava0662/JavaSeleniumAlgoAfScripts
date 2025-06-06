package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public class RedShiftUtil {

	private static final Logger log = Logger.getLogger(RedShiftUtil.class);
	private static final String SSL_FACTORY = "com.amazon.redshift.ssl.NonValidatingFactory";
	private static String dbRedshiftURL;
	private static String masterRSUsername;
	private static String masterRSUserPassword;

	public List<HashMap<String, Object>> getData(String queryParams) {
		List<HashMap<String, Object>> resultsList = null;

		String query = "";
		try {
			query = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, queryParams);
			dbRedshiftURL = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbURL");
			masterRSUsername = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbUsername");
			masterRSUserPassword = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbPassword");
		} catch (Exception e) {
			throw new CustomException("DBSettings.xml file does not exist.", e);
		}

		log.info("Connecting to Redshift database...");

		try (Connection conns = DriverManager.getConnection(dbRedshiftURL, getDbProperties());
				Statement stmt = conns.createStatement();
				ResultSet rs = stmt.executeQuery(query)) {

			log.info("Executing query: " + query);
			resultsList = CommonUtil.resultSetToArrayList(rs);

		} catch (Exception e) {
			log.error("Database error: ", e);
		}

		log.info("Finished Redshift connectivity test.");
		return resultsList;
	}

	public List<HashMap<String, Object>> getExcelData(String queryParam) {
		List<HashMap<String, Object>> resultList = null;

		try {
			dbRedshiftURL = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbURL");
			masterRSUsername = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbUsername");
			masterRSUserPassword = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "RedShift_DbPassword");
		} catch (Exception e) {
			throw new CustomException("DBSettings.xml file does not exist.", e);
		}

		log.info("Connecting to Redshift database...");

		try (Connection conn = DriverManager.getConnection(dbRedshiftURL, getDbProperties());
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(queryParam)) {

			log.info("Executing query: " + queryParam);
			resultList = CommonUtil.resultSetToArrayList(rs);

		} catch (Exception e) {
			log.error("Database error: ", e);
		}

		log.info("Finished Redshift connectivity test.");
		return resultList;
	}

	private Properties getDbProperties() {
		Properties props = new Properties();
		props.setProperty("sslfactory", SSL_FACTORY);
		props.setProperty("ssl", "true");
		props.setProperty("user", masterRSUsername);
		props.setProperty("password", masterRSUserPassword);
		return props;
	}
}
