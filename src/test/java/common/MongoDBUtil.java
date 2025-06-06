package common;

import java.util.*;
import org.apache.log4j.Logger;
import org.bson.Document;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

@SuppressWarnings("all")
public class MongoDBUtil {

	private static final Logger log = Logger.getLogger(MongoDBUtil.class);
	private static String dbURL;
	private static String dbName;
	private static String query;
	private static final String CONST_DB_MSG = "MongoDB collection: ";

	private MongoDBUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
	}

	public static MongoDatabase createConnection() {
		try {
			dbURL = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MongoDB_DbURL");
			dbName = CommonUtil.getXMLData(Constants.DB_SETTING_PATH, "MongoDB_DbName");
		} catch (Exception ex) {
			throw new CustomException("DBSettings.xml file does not exist", ex);
		}

		MongoClient client = new MongoClient(new MongoClientURI(dbURL));
		return client.getDatabase(dbName);
	}

	public static boolean verifyDataInMongoDB(String queryParam, String value) {
		boolean isVerified = false;
		query = queryParam;
		try (MongoCursor<Document> iterator = createConnection().getCollection(query).find().iterator()) {
			while (iterator.hasNext()) {
				Document doc = iterator.next();
				if (doc.containsValue(value)) {
					isVerified = true;
					log.info(CONST_DB_MSG + doc);
					ExtentCucumberAdapter.addTestStepLog("Value verified in MongoDB is: " + value);
					ExtentCucumberAdapter.addTestStepLog(CONST_DB_MSG + doc);
				}
			}
		}
		return isVerified;
	}

	public static Document getMongoDBData(String collection, BasicDBObject dbObject) {
		MongoDatabase mydatabase = createConnection();
		Document document = mydatabase.getCollection(collection).find(dbObject).sort(Sorts.descending("timestamp"))
				.first();
		log.info(CONST_DB_MSG + document);
		return document;
	}

	@SuppressWarnings("deprecation")
	public static boolean compareDbValues(String dbQuery) {
		MongoDatabase mydatabase = createConnection();
		String[] queryValue = dbQuery.split("--");
		String query = DbHelper.queryCopiedText(queryValue[0]);
		String[] dbDetails = query.split("@@");
		Object object = com.mongodb.util.JSON.parse(dbDetails[1]);
		BasicDBObject dbObject = (BasicDBObject) object;

		Document document = mydatabase.getCollection(dbDetails[0]).find(dbObject).sort(Sorts.descending("timestamp"))
				.first();
		String compareValue = DbHelper.queryCopiedText(queryValue[1]).toUpperCase();

		if (document == null) {
			return "N/A".equalsIgnoreCase(compareValue) || "null".equalsIgnoreCase(compareValue);
		}
		log.info(CONST_DB_MSG + document);
		return document.containsValue(queryValue[1]);
	}

	public static boolean compareCompleteList(String collection, String field) {
		MongoDatabase mydatabase = createConnection();
		List<String> allData = new ArrayList<>();
		try (MongoCursor<Document> iterator = mydatabase.getCollection(collection).find().iterator()) {
			while (iterator.hasNext()) {
				Document doc = iterator.next();
				log.info(CONST_DB_MSG + doc.getString(field));
				allData.add(doc.getString(field));
			}
		}
		Set<String> uniqueData = new HashSet<>(allData);
		List<String> mainList = new ArrayList<>(uniqueData);
		Collections.sort(mainList);
		List<String> copiedListText = CommonUtil.getCopiedList();
		Collections.sort(copiedListText);
		return mainList.equals(copiedListText);
	}

	public static boolean isMongoDBDataNotPresent(String collection, BasicDBObject dbObject) {
		MongoDatabase mydatabase = createConnection();
		Document document = mydatabase.getCollection(collection).find(dbObject).projection(Projections.excludeId())
				.first();
		return document == null;
	}

	public static String getQuery(String queryParam) {
		try {
			return CommonUtil.getXMLData(Constants.DB_SETTING_PATH, queryParam);
		} catch (Exception ex) {
			throw new CustomException("DBSettings.xml file does not exist", ex);
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean compareDbValuesWithLogging(String dbQuery) {
		try {
			MongoDatabase mydb = createConnection();
			String[] queryVal = dbQuery.split("--");
			String query = DbHelper.queryCopiedText(queryVal[0]);
			String[] dbDetails = query.split("@@");
			Object object = com.mongodb.util.JSON.parse(dbDetails[1]);
			BasicDBObject dbObject = (BasicDBObject) object;

			FindIterable<Document> cursor = mydb.getCollection(dbDetails[0]).find(dbObject);
			for (Document doc : cursor) {
				ExtentCucumberAdapter.addTestStepLog(CONST_DB_MSG + doc);
				String value = doc.getString(dbDetails[2]);
				if (value != null && value.equalsIgnoreCase(DbHelper.queryCopiedText(queryVal[1]))) {
					ExtentCucumberAdapter.addTestStepLog("Value verified in MongoDB is: " + value);
					return true;
				}
			}
		} catch (Exception e) {
			ExtentCucumberAdapter.addTestStepLog("Error : " + e);
		}
		return false;
	}
}
