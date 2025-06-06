package common;

import java.sql.*;
import java.util.*;
import java.util.Properties;
import org.apache.log4j.Logger;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;

@SuppressWarnings("all")
public class SqlServerUtil {

    private Connection connection;
    private ResultSet results;
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static final Logger log = Logger.getLogger(SqlServerUtil.class);

    public SqlServerUtil() {
    }

    public SqlServerUtil(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    private void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void createConnection() {
        try {
            log.info("Fetching database credentials...");
            dbUrl = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "Sql_DbURL");
            dbUsername = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "Sql_DbUsername");
            dbPassword = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "Sql_DbPassword");

            log.info("DB Details: URL - " + dbUrl + " | Username - " + dbUsername);
        } catch (Exception ex) {
            throw new CustomException("DBSettings.xml file does not exist", ex);
        }

        if (this.connection == null) {
            setConnection();
        }
    }

    private void setConnection() {
        Properties properties = new Properties();
        properties.setProperty("user", dbUsername.trim());
        properties.setProperty("password", dbPassword.trim());
        properties.setProperty("ssl", "true");

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            setConnection(DriverManager.getConnection(dbUrl.trim(), properties));
            this.connection.setAutoCommit(true);
        } catch (ClassNotFoundException ex) {
            log.error("JDBC Driver not found: ", ex);
        } catch (SQLException ex) {
            log.error("Database connection error: ", ex);
        }
    }

    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            log.error("Error closing database connection: ", ex);
        }
    }

    public void update(String query) {
        try {
            log.info("Executing query: " + query);
            createConnection();

            if (this.connection != null && !this.connection.isClosed()) {
                try (Statement statement = this.connection.createStatement()) {
                    statement.executeUpdate(query.trim());
                }
            }
        } catch (SQLException ex) {
            throw new CustomException("SQL Execution Error", ex);
        } finally {
            closeConnection();
        }
    }

    public void select(String query) {
        try {
            log.info("Executing select query: " + query);
            createConnection();
            String sqlQuery = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, query);

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQuery)) {
                this.results = preparedStatement.executeQuery();
                List<HashMap<String, Object>> resultList = CommonUtil.resultSetToArrayList(results);

                log.info("Query Result Size: " + resultList.size());
                for (HashMap<String, Object> row : resultList) {
                    for (Object value : row.values()) {
                        log.info(value.toString());
                    }
                }
            }
        } catch (SQLException ex) {
            log.error("SQL Select Error: ", ex);
        } finally {
            closeConnection();
        }
    }

    public List<HashMap<String, Object>> getData(String query) {
        List<HashMap<String, Object>> resultList = new ArrayList<>();
        try {
            log.info("Fetching data for query: " + query);
            setConnection();

            try (Statement statement = this.connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                resultList = CommonUtil.resultSetToArrayList(resultSet);
                log.info("Fetched " + resultList.size() + " records.");
            }
        } catch (SQLException ex) {
            log.error("Data Fetch Error: ", ex);
        } finally {
            closeConnection();
        }
        return resultList;
    }

    public String getSingleData(String query) {
        try {
            setConnection();
            List<HashMap<String, Object>> resultList = getData(query);

            for (HashMap<String, Object> row : resultList) {
                for (Object value : row.values()) {
                    ExtentCucumberAdapter.addTestStepLog("DB Value retrieved: " + value);
                    return value.toString();
                }
            }
        } catch (Exception ex) {
            log.error("Error fetching single value: ", ex);
        } finally {
            closeConnection();
        }
        return "";
    }

    public boolean verifyDbData(String paramQuery) {
        boolean status = false;
        try {
            log.info("Verifying DB data...");
            String[] queryDetails = paramQuery.split("--");
            if (queryDetails.length != 2) {
                ExtentCucumberAdapter.addTestStepLog("Invalid query format.");
                throw new CustomException("Invalid parameter.");
            }

            String query = queryDetails[0];
            String expectedValue = queryDetails[1].replace("[", "").replace("]", "");

            log.info("Expected Value: " + expectedValue);
            List<HashMap<String, Object>> resultList = getData(query);
            log.info("DB Output: " + resultList);

            for (HashMap<String, Object> row : resultList) {
                for (Object value : row.values()) {
                    if (value != null && value.toString().contains(expectedValue)) {
                        ExtentCucumberAdapter.addTestStepLog("DB Output: " + resultList + ", Expected: " + expectedValue);
                        status = true;
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            ExtentCucumberAdapter.addTestStepLog("Error: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return status;
    }

    public static boolean validateResultSetRecords(ResultSet resultSet, String comparedValue, int columnIndex) {
        try {
            Object columnObject = resultSet.getObject(columnIndex);
            if (columnObject == null) {
                return false;
            }
            String columnValue = columnObject.toString();
            log.info("Comparing Result Set Value: " + columnValue);
            return comparedValue.equalsIgnoreCase(columnValue);
        } catch (SQLException ex) {
            log.error("Error validating result set: ", ex);
        }
        return false;
    }
}

