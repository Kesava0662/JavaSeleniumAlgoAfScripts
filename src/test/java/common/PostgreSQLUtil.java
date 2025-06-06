package common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.log4j.Logger;

@SuppressWarnings("all")
public class PostgreSQLUtil {

	private Connection conn;
	private static String dbURL;
	private static String masterUsername;
	private static String masterUserPassword;
	private static final Logger log = Logger.getLogger(PostgreSQLUtil.class);

	public PostgreSQLUtil() {
	}

	public PostgreSQLUtil(Connection conn) {
		this.conn = conn;
	}

	public Connection getConnection() {
		return conn;
	}

	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("Error closing connection: " + e.getMessage(), e);
			}
		}
	}

	public void setConnection(String url, String user, String password) {
		Properties props = new Properties();
		props.put("user", user);
		props.put("password", password);
		try {
			conn = DriverManager.getConnection(url, props);
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			log.error("Failed to establish connection: " + e.getMessage(), e);
		}
	}

	public void update(String query) {
		try (PreparedStatement statement = conn.prepareStatement(query)) {
			statement.executeUpdate();
		} catch (SQLException e) {
			log.error("Error executing update query: " + e.getMessage(), e);
		}
	}

	public void select(String query) {
		try (PreparedStatement statement = conn.prepareStatement(query); ResultSet results = statement.executeQuery()) {

			List<HashMap<String, Object>> resultList = CommonUtil.resultSetToArrayList(results);
			log.info("Number of records retrieved: " + resultList.size());

			for (HashMap<String, Object> row : resultList) {
				for (Object mapVal : row.values()) {
					log.info(mapVal.toString());
				}
			}
		} catch (SQLException e) {
			log.error("Error executing select query: " + e.getMessage(), e);
		}
	}

	public List<HashMap<String, Object>> getData(String queryParam) {
		List<HashMap<String, Object>> resultList = new ArrayList<>();
		try {
			String query = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, queryParam);
			dbURL = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "PostgreSql_DbURL");
			masterUsername = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "PostgreSql_DbUsername");
			masterUserPassword = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "PostgreSql_DbPassword");

			setConnection(dbURL, masterUsername, masterUserPassword);

			try (PreparedStatement statement = conn.prepareStatement(query);
					ResultSet results = statement.executeQuery()) {

				resultList = CommonUtil.resultSetToArrayList(results);
			} catch (SQLException e) {
				log.error("Error executing getData query: " + e.getMessage(), e);
			}
		} catch (Exception ex) {
			throw new CustomException("DBSettings.xml file does not exist", ex);
		} finally {
			closeConnection();
		}
		return resultList;
	}
}
