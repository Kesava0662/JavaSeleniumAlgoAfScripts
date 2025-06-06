package common;

import java.sql.*;
import java.util.*;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;

public class MySqlServerUtil {

	private Connection conn = null;
	static final Logger log = Logger.getLogger(MySqlServerUtil.class);

	public MySqlServerUtil() {
	}

	public MySqlServerUtil(Connection conn) {
		this.conn = conn;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public void closeConnection() {
		try {
			if (this.conn != null && !this.conn.isClosed()) {
				this.conn.close();
				log.info("Database connection closed successfully.");
			}
		} catch (SQLException e) {
			log.error("Error closing connection: " + e.getMessage(), e);
		}
	}

	public void createConnection() {
		try {
			String dbsqlURL = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbURL");
			String dbsqlUsername = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbUsername");
			String dbsqlPassword = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbPassword");

			log.info("Connecting to DB: " + dbsqlURL + " with user: " + dbsqlUsername + "Password :" + dbsqlPassword);

			if (this.conn == null || this.conn.isClosed()) {
				setConnection();
			} else {
				log.info("Already connected to DB.");
			}
		} catch (Exception ex) {
			throw new CustomException("DBSettings.xml file does not exist or has incorrect configuration", ex);
		}
	}

	public void setConnection() {
		String url = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbURL");
		String user = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbUsername");
		String password = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MySql_DbPassword");

		Properties props = new Properties();
		props.setProperty("user", user.trim());
		props.setProperty("password", password.trim());
		props.setProperty("ssl", "true");

		try {
			conn = DriverManager.getConnection(url.trim(), props);
			conn.setAutoCommit(true);
			log.info("Successfully connected to MySQL database.");
		} catch (SQLException e) {
			log.error("Database connection error: " + e.getMessage(), e);
		}
	}

	public void update(String query) {
		try {
			createConnection();
			try (Statement statement = conn.createStatement()) {
				int rowsAffected = statement.executeUpdate(query.trim());
				log.info("Query executed successfully. Rows affected: " + rowsAffected);
			}
		} catch (SQLException e) {
			throw new CustomException("Error executing update query", e);
		} finally {
			closeConnection();
		}
	}

	public List<HashMap<String, Object>> select(String query) {
		List<HashMap<String, Object>> resultList = new ArrayList<>();
		try {
			createConnection();
			try (PreparedStatement pstmt = conn.prepareStatement(query.trim()); ResultSet rs = pstmt.executeQuery()) {
				resultList = CommonUtil.resultSetToArrayList(rs);
				log.info("Query executed successfully. Results retrieved: " + resultList.size());
			}
		} catch (SQLException e) {
			log.error("Error executing select query: " + e.getMessage(), e);
		} finally {
			closeConnection();
		}
		return resultList;
	}

	public String getSingleData(String query) {
		if (query == null || query.trim().isEmpty()) {
			throw new IllegalArgumentException("Query cannot be null or empty.");
		}

		try {
			log.info("Fetching single data for query: " + query);
			List<HashMap<String, Object>> resultList = select(query);
			if (!resultList.isEmpty()) {
				for (HashMap<String, Object> row : resultList) {
					for (Object value : row.values()) {
						if (value != null) {
							log.info("DB Value: " + value);
							ExtentCucumberAdapter.addTestStepLog("DB Value copied: " + value);
							return value.toString();
						}
					}
				}
			}
		} catch (Exception ex) {
			log.error("Error fetching single data: " + ex.getMessage(), ex);
		}
		return "";
	}

	public boolean verifyDbData(String queryDetails) throws Exception {
		if (queryDetails == null || queryDetails.isEmpty()) {
			throw new CustomException("Query parameter cannot be null or empty.");
		}

		String[] details = queryDetails.split("--");
		if (details.length != 2) {
			throw new CustomException("Invalid query format. Expected format: query--compareValue");
		}

		String query = details[0].trim();
		String compareValue = details[1].replaceAll("[\\[\\]]", "").trim();

		try {
			List<HashMap<String, Object>> dataList = select(query);
			for (HashMap<String, Object> row : dataList) {
				for (Object value : row.values()) {
					if (value != null && value.toString().contains(compareValue)) {
						log.info("DB Output matches expected value.");
						return true;
					}
				}
			}
		} catch (Exception e) {
			log.error("Error verifying DB data: " + e.getMessage(), e);
			throw new CustomException("Unexpected error occurred", e);
		}
		return false;
	}

	public static boolean validateResultSetRecords(ResultSet rs, String columnName, String expectedValue) {
		boolean isValid = false;
		try {
			while (rs.next()) {
				String actualValue = rs.getString(columnName);
				log.info("Comparing DB value: " + actualValue + " with expected: " + expectedValue);
				if (actualValue != null && actualValue.equals(expectedValue)) {
					isValid = true;
					break;
				}
			}
		} catch (SQLException e) {
			log.error("Error validating ResultSet records: " + e.getMessage(), e);
		}
		return isValid;
	}
}
