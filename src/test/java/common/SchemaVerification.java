package common;

import java.io.IOException;
import java.util.Set;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

@SuppressWarnings("all")
public class SchemaVerification {
	
	static String schemaStr = "{\n" +
		    "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
		    "  \"title\": \"User Profile Schema\",\n" +
		    "  \"type\": \"object\",\n" +
		    "  \"required\": [\"userId\", \"username\", \"email\", \"profile\", \"address\"],\n" +
		    "  \"properties\": {\n" +
		    "    \"userId\": { \"type\": \"integer\", \"minimum\": 1 },\n" +
		    "    \"username\": { \"type\": \"string\", \"minLength\": 3, \"maxLength\": 20 },\n" +
		    "    \"email\": { \"type\": \"string\", \"format\": \"email\" },\n" +
		    "    \"phoneNumbers\": {\n" +
		    "      \"type\": \"array\",\n" +
		    "      \"items\": { \"type\": \"string\", \"pattern\": \"^\\\\+?[1-9]\\\\d{1,14}$\" },\n" + // fixed escape here
		    "      \"minItems\": 1,\n" +
		    "      \"uniqueItems\": true\n" +
		    "    },\n" +
		    "    \"isActive\": { \"type\": \"boolean\" },\n" +
		    "    \"profile\": {\n" +
		    "      \"type\": \"object\",\n" +
		    "      \"properties\": {\n" +
		    "        \"bio\": { \"type\": \"string\", \"maxLength\": 500 },\n" +
		    "        \"profilePictureUrl\": { \"type\": \"string\", \"format\": \"uri\" },\n" +
		    "        \"socialLinks\": {\n" +
		    "          \"type\": \"array\",\n" +
		    "          \"items\": {\n" +
		    "            \"type\": \"object\",\n" +
		    "            \"properties\": {\n" +
		    "              \"platform\": { \"type\": \"string\", \"enum\": [\"facebook\", \"twitter\", \"linkedin\", \"instagram\"] },\n" +
		    "              \"url\": { \"type\": \"string\", \"format\": \"uri\" }\n" +
		    "            },\n" +
		    "            \"required\": [\"platform\", \"url\"]\n" +
		    "          }\n" +
		    "        }\n" +
		    "      }\n" +
		    "    },\n" +
		    "    \"address\": {\n" +
		    "      \"type\": \"object\",\n" +
		    "      \"properties\": {\n" +
		    "        \"street\": { \"type\": \"string\" },\n" +
		    "        \"city\": { \"type\": \"string\" },\n" +
		    "        \"zipCode\": { \"type\": \"string\", \"pattern\": \"^\\\\d{5}$\" },\n" + // fixed escape here
		    "        \"country\": { \"type\": \"string\" }\n" +
		    "      },\n" +
		    "      \"required\": [\"street\", \"city\", \"zipCode\", \"country\"]\n" +
		    "    },\n" +
		    "    \"preferences\": {\n" +
		    "      \"type\": \"object\",\n" +
		    "      \"properties\": {\n" +
		    "        \"language\": { \"type\": \"string\", \"enum\": [\"en\", \"es\", \"fr\", \"de\"] },\n" +
		    "        \"theme\": { \"type\": \"string\", \"enum\": [\"light\", \"dark\"] }\n" +
		    "      },\n" +
		    "      \"required\": [\"language\", \"theme\"]\n" +
		    "    }\n" +
		    "  }\n" +
		    "}";
	static String jsonStr = "{\n" +
		    "  \"userId\": 101,\n" +
		    "  \"username\": \"johndoe\",\n" +
		    "  \"email\": \"johndoe@example.com\",\n" +
		    "  \"phoneNumbers\": [\n" +
		    "    \"+1234567890\",\n" +
		    "    \"+1987654321\"\n" +
		    "  ],\n" +
		    "  \"isActive\": true,\n" +
		    "  \"profile\": {\n" +
		    "    \"bio\": \"Experienced software developer with a passion for coding and problem solving.\",\n" +
		    "    \"profilePictureUrl\": \"http://example.com/profile.jpg\",\n" +
		    "    \"socialLinks\": [\n" +
		    "      {\n" +
		    "        \"platform\": \"linkedin\",\n" +
		    "        \"url\": \"https://www.linkedin.com/in/johndoe\"\n" +
		    "      },\n" +
		    "      {\n" +
		    "        \"platform\": \"twitter\",\n" +
		    "        \"url\": \"https://twitter.com/johndoe\"\n" +
		    "      }\n" +
		    "    ]\n" +
		    "  },\n" +
		    "  \"address\": {\n" +
		    "    \"street\": \"456 Park Ave\",\n" +
		    "    \"city\": \"Springfield\",\n" +
		    "    \"zipCode\": \"12345\",\n" +
		    "    \"country\": \"USA\"\n" +
		    "  },\n" +
		    "  \"preferences\": {\n" +
		    "    \"language\": \"en\",\n" +
		    "    \"theme\": \"dark\"\n" +
		    "  }\n" +
		    "}";
	
	static String schemaStr1 = "{\n" +
			"  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
			"  \"title\": \"User Schema\",\n" +
			"  \"type\": \"object\",\n" +
			"  \"required\": [\"id\", \"name\", \"email\"],\n" +
			"  \"properties\": {\n" +
			"    \"id\": {\n" +
			"      \"type\": \"integer\"\n" +
			"    },\n" +
			"    \"name\": {\n" +
			"      \"type\": \"string\",\n" +
			"      \"minLength\": 2\n" +
			"    },\n" +
			"    \"email\": {\n" +
			"      \"type\": \"string\",\n" +
			"      \"format\": \"email\"\n" +
			"    },\n" +
			"    \"age\": {\n" +
			"      \"type\": \"integer\",\n" +
			"      \"minimum\": 18,\n" +
			"      \"maximum\": 100\n" +
			"    },\n" +
			"    \"address\": {\n" +
			"      \"type\": \"object\",\n" +
			"      \"properties\": {\n" +
			"        \"street\": { \"type\": \"string\" },\n" +
			"        \"city\": { \"type\": \"string\" },\n" +
			"        \"zipCode\": { \"type\": \"string\" }\n" +
			"      }\n" +
			"    },\n" +
			"    \"phoneNumbers\": {\n" +
			"      \"type\": \"array\",\n" +
			"      \"items\": {\n" +
			"        \"type\": \"string\"\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}";
	static String jsonStr1 = "{\n" +
			"  \"id\": 1,\n" +
			"  \"name\": \"Aditya\",\n" +
			"  \"email\": \"alice.johnson@example.com\",\n" +
			"  \"age\": 17,\n" +
			"  \"address\": {\n" +
			"    \"street\": \"123 Maple Street\",\n" +
			"    \"city\": \"Springfield\",\n" +
			"    \"zipCode\": \"12345\"\n" +
			"  },\n" +
			"  \"phoneNumbers\": [\n" +
			"    \"555-1234\",\n" +
			"    \"555-5678\"\n" +
			"  ]\n" +
			"}";
	
	/*static String schemaStr1 = "{\n" +
			"  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
			"  \"title\": \"User Schema\",\n" +
			"  \"type\": \"object\",\n" +
			"  \"required\": [\"id\", \"name\", \"email\"],\n" +
			"  \"properties\": {\n" +
			"    \"id\": {\n" +
			"      \"type\": \"integer\"\n" +
			"    },\n" +
			"    \"name\": {\n" +
			"      \"type\": \"string\",\n" +
			"      \"minLength\": 2\n" +
			"    },\n" +
			"    \"email\": {\n" +
			"      \"type\": \"string\",\n" +
			"      \"format\": \"email\"\n" +
			"    },\n" +
			"    \"age\": {\n" +
			"      \"type\": \"integer\",\n" +
			"      \"minimum\": 18,\n" +
			"      \"maximum\": 100\n" +
			"    },\n" +
			"    \"address\": {\n" +
			"      \"type\": \"object\",\n" +
			"      \"properties\": {\n" +
			"        \"street\": { \"type\": \"string\" },\n" +
			"        \"city\": { \"type\": \"string\" },\n" +
			"        \"zipCode\": { \"type\": \"string\" }\n" +
			"      }\n" +
			"    },\n" +
			"    \"phoneNumbers\": {\n" +
			"      \"type\": \"array\",\n" +
			"      \"items\": {\n" +
			"        \"type\": \"string\"\n" +
			"      }\n" +
			"    }\n" +
			"  }\n" +
			"}";
	static String jsonStr1 = "{\n" +
			"  \"id\": 1,\n" +
			"  \"name\": \"Aditya\",\n" +
			"  \"email\": \"alice.johnson@example.com\",\n" +
			"  \"age\": 19,\n" +
			"  \"address\": {\n" +
			"    \"street\": \"123 Maple Street\",\n" +
			"    \"city\": \"Springfield\",\n" +
			"    \"zipCode\": \"12345\"\n" +
			"  },\n" +
			"  \"phoneNumbers\": [\n" +
			"    \"555-1234\",\n" +
			"    \"555-5678\"\n" +
			"  ]\n" +
			"}";*/
	
	static String schemaStr2 = "{\n" +
			"  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
			"  \"type\": \"object\",\n" +
			"  \"properties\": {\n" +
			"    \"data\": {\n" +
			"      \"type\": \"object\",\n" +
			"      \"properties\": {\n" +
			"        \"token\": { \"type\": \"string\" },\n" +
			"        \"needIpLocation\": { \"type\": \"boolean\" },\n" +
			"        \"user\": {\n" +
			"          \"type\": \"object\",\n" +
			"          \"properties\": {\n" +
			"            \"id\": { \"type\": \"integer\" },\n" +
			"            \"firstName\": { \"type\": \"string\" },\n" +
			"            \"lastName\": { \"type\": \"string\" },\n" +
			"            \"email\": { \"type\": \"string\", \"format\": \"email\" },\n" +
			"            \"aclRoleId\": { \"type\": \"integer\" },\n" +
			"            \"timezoneId\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"addressLine1\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"addressLine2\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"stateId\": { \"type\": \"integer\" },\n" +
			"            \"cityId\": { \"type\": \"integer\" },\n" +
			"            \"postCode\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"phoneId\": { \"type\": \"integer\" },\n" +
			"            \"avatarFileId\": { \"type\": [\"integer\", \"null\"] },\n" +
			"            \"signatureFileId\": { \"type\": [\"integer\", \"null\"] },\n" +
			"            \"sex\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"dob\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"status\": { \"type\": \"string\" },\n" +
			"            \"deactivated\": { \"type\": \"boolean\" },\n" +
			"            \"accountCreated\": { \"type\": \"string\", \"format\": \"date-time\" },\n" +
			"            \"homeLink\": { \"type\": \"string\" },\n" +
			"            \"addressDescription\": { \"type\": \"string\" },\n" +
			"            \"companyName\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"accountArchived\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"passwordExpiration\": { \"type\": \"string\", \"format\": \"date-time\" },\n" +
			"            \"questionAttempts\": { \"type\": \"integer\" },\n" +
			"            \"lockingTime\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"isSecurityQuestionsEnabled\": { \"type\": \"boolean\" },\n" +
			"            \"lockingReason\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"loginDelayedAt\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"loginAttempts\": { \"type\": \"integer\" },\n" +
			"            \"unlockedLogin\": { \"type\": \"boolean\" },\n" +
			"            \"type\": { \"type\": \"string\" },\n" +
			"            \"deletedRoleName\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"fullNameForSearch\": { \"type\": \"string\" },\n" +
			"            \"twoFaAttempts\": { \"type\": \"integer\" },\n" +
			"            \"isTwoFaEnabled\": { \"type\": \"boolean\" },\n" +
			"            \"font\": { \"type\": [\"string\", \"null\"] },\n" +
			"            \"queueDepth\": { \"type\": \"integer\" },\n" +
			"            \"usersSettings\": {\n" +
			"              \"type\": \"array\",\n" +
			"              \"items\": {\n" +
			"                \"type\": \"object\",\n" +
			"                \"properties\": {\n" +
			"                  \"id\": { \"type\": [\"integer\", \"null\"] },\n" +
			"                  \"userId\": { \"type\": [\"integer\", \"null\"] },\n" +
			"                  \"key\": { \"type\": \"string\" },\n" +
			"                  \"value\": { \"type\": [\"boolean\", \"string\"] },\n" +
			"                  \"timeToRefresh\": { \"type\": [\"string\", \"null\"] },\n" +
			"                  \"imageViewer\": { \"type\": [\"string\", \"null\"] },\n" +
			"                  \"savantSignButton\": { \"type\": [\"string\", \"null\"] },\n" +
			"                  \"updatedAt\": { \"type\": [\"string\", \"null\"], \"format\": \"date-time\" },\n" +
			"                  \"deletedAt\": { \"type\": [\"string\", \"null\"] },\n" +
			"                  \"deleted_at\": { \"type\": [\"string\", \"null\"] },\n" +
			"                  \"expireTime\": { \"type\": [\"integer\", \"null\"] }\n" +
			"                },\n" +
			"                \"required\": [\"key\", \"value\"]\n" +
			"              }\n" +
			"            },\n" +
			"            \"facilities\": { \"type\": \"array\" },\n" +
			"            \"facilityGroups\": { \"type\": \"array\" },\n" +
			"            \"aclRole\": {\n" +
			"              \"type\": \"object\",\n" +
			"              \"properties\": {\n" +
			"                \"id\": { \"type\": \"integer\" },\n" +
			"                \"facilityGroupId\": { \"type\": [\"integer\", \"null\"] },\n" +
			"                \"name\": { \"type\": \"string\" },\n" +
			"                \"type\": { \"type\": \"string\" },\n" +
			"                \"description\": { \"type\": \"string\" },\n" +
			"                \"default\": { \"type\": \"boolean\" },\n" +
			"                \"signature\": { \"type\": \"string\" },\n" +
			"                \"alias\": { \"type\": \"string\" },\n" +
			"                \"facility_group_id\": { \"type\": [\"integer\", \"null\"] }\n" +
			"              }\n" +
			"            },\n" +
			"            \"userPhone\": {\n" +
			"              \"type\": \"object\",\n" +
			"              \"properties\": {\n" +
			"                \"countryCode\": { \"type\": \"string\" },\n" +
			"                \"number\": { \"type\": \"string\" }\n" +
			"              }\n" +
			"            },\n" +
			"            \"usersExamListSorting\": {\n" +
			"              \"type\": \"object\",\n" +
			"              \"properties\": {\n" +
			"                \"data\": {\n" +
			"                  \"type\": \"array\",\n" +
			"                  \"items\": {\n" +
			"                    \"type\": \"object\",\n" +
			"                    \"properties\": {\n" +
			"                      \"title\": { \"type\": \"string\" },\n" +
			"                      \"value\": { \"type\": \"string\" },\n" +
			"                      \"isActive\": { \"type\": \"boolean\" },\n" +
			"                      \"position\": { \"type\": \"integer\" }\n" +
			"                    }\n" +
			"                  }\n" +
			"                }\n" +
			"              }\n" +
			"            },\n" +
			"            \"data\": {\n" +
			"              \"type\": \"object\",\n" +
			"              \"properties\": {\n" +
			"                \"isTrigramUserCreated\": { \"type\": \"boolean\" }\n" +
			"              }\n" +
			"            },\n" +
			"            \"hotKeyCustomTemplateId\": { \"type\": [\"integer\", \"null\"] },\n" +
			"            \"timeToLogout\": { \"type\": \"integer\" }\n" +
			"          }\n" +
			"        }\n" +
			"      }\n" +
			"    }\n" +
			"  },\n" +
			"  \"required\": [\"data\"]\n" +
			"}";
	static String jsonStr2 = "{\n" +
			"  \\\"data\\\": {\n" +
			"    \\\"token\\\": \\\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ODc0NSwiZW1haWwiOiJhZG1pbmFsZ28wMUB5b3BtYWlsLmNvbSIsInBhc3NlZFZlcmlmaWNhdGlvbiI6ZmFsc2UsImlwIjoiNDMuMjA0LjEwNi41MCIsImV4cCI6MTc0NTU3OTQwMywiaWF0IjoxNzQ1NTc1ODAzfQ.SToVXfjsrEjUOc0QnfmS7vItB7Gqu_S4ffLbc-9a80M\\\",\n" +
			"    \\\"needIpLocation\\\": false,\n" +
			"    \\\"user\\\": {\n" +
			"      \\\"id\\\": 8745,\n" +
			"      \\\"firstName\\\": \\\"Admin\\\",\n" +
			"      \\\"lastName\\\": \\\"Algo\\\",\n" +
			"      \\\"email\\\": \\\"adminalgo01@yopmail.com\\\",\n" +
			"      \\\"aclRoleId\\\": 1,\n" +
			"      \\\"timezoneId\\\": null,\n" +
			"      \\\"addressLine1\\\": null,\n" +
			"      \\\"addressLine2\\\": null,\n" +
			"      \\\"stateId\\\": 36,\n" +
			"      \\\"cityId\\\": 9619,\n" +
			"      \\\"postCode\\\": null,\n" +
			"      \\\"phoneId\\\": 17773,\n" +
			"      \\\"avatarFileId\\\": null,\n" +
			"      \\\"signatureFileId\\\": 21694,\n" +
			"      \\\"sex\\\": null,\n" +
			"      \\\"dob\\\": null,\n" +
			"      \\\"status\\\": \\\"confirmed\\\",\n" +
			"      \\\"deactivated\\\": false,\n" +
			"      \\\"accountCreated\\\": \\\"2025-02-13T15:06:46.339Z\\\",\n" +
			"      \\\"homeLink\\\": \\\"/profile/settings\\\",\n" +
			"      \\\"addressDescription\\\": \\\"New York, NY, USA\\\",\n" +
			"      \\\"companyName\\\": null,\n" +
			"      \\\"accountArchived\\\": null,\n" +
			"      \\\"passwordExpiration\\\": \\\"2025-06-20T09:33:36.000Z\\\",\n" +
			"      \\\"questionAttempts\\\": 0,\n" +
			"      \\\"lockingTime\\\": null,\n" +
			"      \\\"isSecurityQuestionsEnabled\\\": false,\n" +
			"      \\\"lockingReason\\\": null,\n" +
			"      \\\"loginDelayedAt\\\": null,\n" +
			"      \\\"loginAttempts\\\": 0,\n" +
			"      \\\"unlockedLogin\\\": false,\n" +
			"      \\\"type\\\": \\\"standard\\\",\n" +
			"      \\\"deletedRoleName\\\": null,\n" +
			"      \\\"fullNameForSearch\\\": \\\"algo admin algo, admin, algo\\\",\n" +
			"      \\\"twoFaAttempts\\\": 0,\n" +
			"      \\\"isTwoFaEnabled\\\": false,\n" +
			"      \\\"font\\\": null,\n" +
			"      \\\"queueDepth\\\": 15,\n" +
			"      \\\"usersSettings\\\": [ ... ],\n" + // Truncated to avoid massive output
			"      \\\"facilities\\\": [],\n" +
			"      \\\"facilityGroups\\\": [],\n" +
			"      \\\"aclRole\\\": {\n" +
			"        \\\"id\\\": 1,\n" +
			"        \\\"facilityGroupId\\\": null,\n" +
			"        \\\"name\\\": \\\"administrator\\\",\n" +
			"        \\\"type\\\": \\\"global\\\",\n" +
			"        \\\"description\\\": \\\"\\\",\n" +
			"        \\\"default\\\": false,\n" +
			"        \\\"signature\\\": \\\"disabled\\\",\n" +
			"        \\\"alias\\\": \\\"administrator\\\",\n" +
			"        \\\"facility_group_id\\\": null\n" +
			"      },\n" +
			"      \\\"usersIpRanges\\\": null,\n" +
			"      \\\"userPhone\\\": {\n" +
			"        \\\"countryCode\\\": \\\"1\\\",\n" +
			"        \\\"number\\\": \\\"2345678908\\\"\n" +
			"      },\n" +
			"      \\\"usersExamListSorting\\\": {\n" +
			"        \\\"data\\\": [ ... ]\n" +
			"      },\n" +
			"      \\\"data\\\": {\n" +
			"        \\\"isTrigramUserCreated\\\": false\n" +
			"      },\n" +
			"      \\\"hotKeyCustomTemplateId\\\": null,\n" +
			"      \\\"timeToLogout\\\": 1800000\n" +
			"    }\n" +
			"  }\n" +
			"}";
	
	
	static String jsonStr3 = "\"{\"data\":{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ODc0NSwiZW1haWwiOiJhZG1pbmFsZ28wMUB5b3BtYWlsLmNvbSIsInBhc3NlZFZlcmlmaWNhdGlvbiI6ZmFsc2UsImlwIjoiNDMuMjA0LjEwNi41MCIsImV4cCI6MTc0NTU3OTQwMywiaWF0IjoxNzQ1NTc1ODAzfQ.SToVXfjsrEjUOc0QnfmS7vItB7Gqu_S4ffLbc-9a80M\",\"needIpLocation\":false,\"user\":{\"id\":8745,\"firstName\":\"Admin\",\"lastName\":\"Algo\",\"email\":\"adminalgo01@yopmail.com\",\"aclRoleId\":1,\"timezoneId\":null,\"addressLine1\":null,\"addressLine2\":null,\"stateId\":36,\"cityId\":9619,\"postCode\":null,\"phoneId\":17773,\"avatarFileId\":null,\"signatureFileId\":21694,\"sex\":null,\"dob\":null,\"status\":\"confirmed\",\"deactivated\":false,\"accountCreated\":\"2025-02-13T15:06:46.339Z\",\"homeLink\":\"/profile/settings\",\"addressDescription\":\"New York, NY, USA\",\"companyName\":null,\"accountArchived\":null,\"passwordExpiration\":\"2025-06-20T09:33:36.000Z\",\"questionAttempts\":0,\"lockingTime\":null,\"isSecurityQuestionsEnabled\":false,\"lockingReason\":null,\"loginDelayedAt\":null,\"loginAttempts\":0,\"unlockedLogin\":false,\"type\":\"standard\",\"deletedRoleName\":null,\"fullNameForSearch\":\"algo admin algo, admin, algo\",\"twoFaAttempts\":0,\"isTwoFaEnabled\":false,\"font\":null,\"queueDepth\":15,\"usersSettings\":[{\"id\":96835,\"userId\":8745,\"key\":\"email_notification\",\"value\":true,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96836,\"userId\":8745,\"key\":\"sms_notification\",\"value\":false,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96837,\"userId\":8745,\"key\":\"push_notification\",\"value\":false,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96839,\"userId\":8745,\"key\":\"authenticator_enabled\",\"value\":false,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96840,\"userId\":8745,\"key\":\"default_template\",\"value\":true,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96841,\"userId\":8745,\"key\":\"order_details_orientation\",\"value\":true,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96842,\"userId\":8745,\"key\":\"open_findings_new_window\",\"value\":false,\"timeToRefresh\":\"60\",\"imageViewer\":null,\"savantSignButton\":\"close\",\"updatedAt\":\"2025-02-13T15:06:46.353Z\",\"deletedAt\":null},{\"id\":96838,\"userId\":8745,\"key\":\"chat_notification\",\"value\":true,\"timeToRefresh\":null,\"imageViewer\":null,\"savantSignButton\":null,\"updatedAt\":\"2025-04-10T09:54:51.829Z\",\"deletedAt\":null},{\"key\":\"elasticEnabled\",\"value\":false},{\"key\":\"cachingEnabled\",\"value\":true,\"expireTime\":1800000}],\"facilities\":[],\"facilityGroups\":[],\"aclRole\":{\"id\":1,\"facilityGroupId\":null,\"name\":\"administrator\",\"type\":\"global\",\"description\":\"\",\"default\":false,\"signature\":\"disabled\",\"alias\":\"administrator\",\"facility_group_id\":null},\"usersIpRanges\":null,\"userPhone\":{\"countryCode\":\"1\",\"number\":\"2345678908\"},\"usersExamListSorting\":{\"data\":[{\"title\":\"Unmatched\",\"value\":\"unmatched\",\"isActive\":true,\"position\":1},{\"title\":\"To Read\",\"value\":\"inflight\",\"isActive\":true,\"position\":2},{\"title\":\"Drafted\",\"value\":\"pc\",\"isActive\":true,\"position\":3},{\"title\":\"Pending\",\"value\":\"pending\",\"isActive\":true,\"position\":4},{\"title\":\"Signed\",\"value\":\"completed\",\"isActive\":true,\"position\":5},{\"title\":\"Consult Request\",\"value\":\"consult\",\"isActive\":true,\"position\":6},{\"title\":\"Critical Results\",\"value\":\"communicate\",\"isActive\":true,\"position\":7},{\"title\":\"Support Request\",\"value\":\"support\",\"isActive\":true,\"position\":8},{\"title\":\"Billing\",\"value\":\"billing\",\"isActive\":true,\"position\":9},{\"title\":\"Delayed\",\"value\":\"delayed\",\"isActive\":true,\"position\":13}]},\"data\":{\"isTrigramUserCreated\":false},\"hotKeyCustomTemplateId\":null,\"timeToLogout\":1800000}}}";

	static String schemaOutputJson = "{"
		    + "  \\\"$schema\\\": \\\"http://json-schema.org/draft-07/schema#\\\","
		    + "  \\\"type\\\": \\\"object\\\","
		    + "  \\\"properties\\\": {"
		    + "    \\\"userId\\\": { \\\"type\\\": \\\"integer\\\" },"
		    + "    \\\"username\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "    \\\"email\\\": { \\\"type\\\": \\\"string\\\", \\\"format\\\": \\\"email\\\" },"
		    + "    \\\"phoneNumbers\\\": {"
		    + "      \\\"type\\\": \\\"array\\\","
		    + "      \\\"items\\\": { \\\"type\\\": \\\"string\\\" }"
		    + "    },"
		    + "    \\\"isActive\\\": { \\\"type\\\": \\\"boolean\\\" },"
		    + "    \\\"profile\\\": {"
		    + "      \\\"type\\\": \\\"object\\\","
		    + "      \\\"properties\\\": {"
		    + "        \\\"bio\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "        \\\"profilePictureUrl\\\": { \\\"type\\\": \\\"string\\\", \\\"format\\\": \\\"uri\\\" },"
		    + "        \\\"socialLinks\\\": {"
		    + "          \\\"type\\\": \\\"array\\\","
		    + "          \\\"items\\\": {"
		    + "            \\\"type\\\": \\\"object\\\","
		    + "            \\\"properties\\\": {"
		    + "              \\\"platform\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "              \\\"url\\\": { \\\"type\\\": \\\"string\\\", \\\"format\\\": \\\"uri\\\" }"
		    + "            },"
		    + "            \\\"required\\\": [\\\"platform\\\", \\\"url\\\"]"
		    + "          }"
		    + "        }"
		    + "      },"
		    + "      \\\"required\\\": [\\\"bio\\\", \\\"profilePictureUrl\\\", \\\"socialLinks\\\"]"
		    + "    },"
		    + "    \\\"address\\\": {"
		    + "      \\\"type\\\": \\\"object\\\","
		    + "      \\\"properties\\\": {"
		    + "        \\\"street\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "        \\\"city\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "        \\\"zipCode\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "        \\\"country\\\": { \\\"type\\\": \\\"string\\\" }"
		    + "      },"
		    + "      \\\"required\\\": [\\\"street\\\", \\\"city\\\", \\\"zipCode\\\", \\\"country\\\"]"
		    + "    },"
		    + "    \\\"preferences\\\": {"
		    + "      \\\"type\\\": \\\"object\\\","
		    + "      \\\"properties\\\": {"
		    + "        \\\"language\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "        \\\"theme\\\": { \\\"type\\\": \\\"string\\\" }"
		    + "      },"
		    + "      \\\"required\\\": [\\\"language\\\", \\\"theme\\\"]"
		    + "    }"
		    + "  },"
		    + "  \\\"required\\\": ["
		    + "    \\\"userId\\\", \\\"username\\\", \\\"email\\\", \\\"phoneNumbers\\\","
		    + "    \\\"isActive\\\", \\\"profile\\\", \\\"address\\\", \\\"preferences\\\""
		    + "  ]"
		    + "}";
	
	static String outputJson = "{"
		    + "  \\\"$schema\\\": \\\"http://json-schema.org/draft-07/schema#\\\","
		    + "  \\\"type\\\": \\\"object\\\","
		    + "  \\\"properties\\\": {"
		    + "    \\\"code\\\": {"
		    + "      \\\"type\\\": \\\"string\\\","
		    + "      \\\"minLength\\\": 5,"
		    + "      \\\"maxLength\\\": 50"
		    + "    },"
		    + "    \\\"errors\\\": {"
		    + "      \\\"type\\\": \\\"array\\\","
		    + "      \\\"items\\\": {"
		    + "        \\\"type\\\": \\\"object\\\","
		    + "        \\\"properties\\\": {"
		    + "          \\\"actual\\\": { \\\"type\\\": \\\"object\\\" },"
		    + "          \\\"expected\\\": { \\\"type\\\": \\\"object\\\" },"
		    + "          \\\"error\\\": { \\\"type\\\": \\\"string\\\" },"
		    + "          \\\"where\\\": { \\\"type\\\": \\\"string\\\" }"
		    + "        },"
		    + "        \\\"required\\\": [\\\"actual\\\", \\\"expected\\\", \\\"error\\\", \\\"where\\\"]"
		    + "      }"
		    + "    }"
		    + "  },"
		    + "  \\\"required\\\": [\\\"code\\\", \\\"errors\\\"]"
		    + "}";


	
	/*public static void main(String[] args) {
		
		verifySchema(SchemaVerification.schemaStr1, SchemaVerification.jsonStr1);
	}*/
	
	public static Boolean verifySchema(String schemaJson, String outputJson) {
		
		ObjectMapper mapper = new ObjectMapper();
		/*JsonNode schemaNode;
        JsonNode jsonNode;*/
		try {
			JsonNode schemaNode = mapper.readTree(schemaJson);
			JsonNode jsonNode = mapper.readTree(outputJson);
			JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7); // Supports multiple drafts
			JsonSchema schema = factory.getSchema(schemaNode);

			Set<ValidationMessage> errors = schema.validate(jsonNode);

			if (errors.isEmpty()) {
				ExtentCucumberAdapter
				.addTestStepLog("Verified schema of API response: is passed");
				System.out.println("✅ Schema is valid!");
				return true;
			} else {
				ExtentCucumberAdapter
				.addTestStepLog("Verified schema of API response: is failed");
				System.out.println("❌ Schema is invalid:");
				errors.forEach(err -> ExtentCucumberAdapter
						.addTestStepLog("  "+" - " + err.getMessage()));
				errors.forEach(err -> System.out.println(" - " + err.getMessage()));
				return false;
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/*public static void main(String[] args) {
        String rawResponse = "200,\"id\":@Form::82,\"name\":\"Testing Form23\",\"ifActive\": false,\"orderBasedType\": \"standard\",\"facilityId\":34,\"facilityGroupId\":57,\"isSync\": true,\"verify_schema\": \"{ \\\"userId\\\": 101, \\\"username\\\": \\\"johndoe\\\", \\\"email\\\": \\\"johndoe@example.com\\\", \\\"phoneNumbers\\\": [ \\\"+1234567890\\\", \\\"+1987654321\\\" ], \\\"isActive\\\": true, \\\"profile\\\": { \\\"bio\\\": \\\"Experienced software developer with a passion for coding and problem solving.\\\", \\\"profilePictureUrl\\\": \\\"http://example.com/profile.jpg\\\", \\\"socialLinks\\\": [ { \\\"platform\\\": \\\"linkedin\\\", \\\"url\\\": \\\"https://www.linkedin.com/in/johndoe\\\" }, { \\\"platform\\\": \\\"twitter\\\", \\\"url\\\": \\\"https://twitter.com/johndoe\\\" } ] }, \\\"address\\\": { \\\"street\\\": \\\"456 Park Ave\\\", \\\"city\\\": \\\"Springfield\\\", \\\"zipCode\\\": \\\"12345\\\", \\\"country\\\": \\\"USA\\\" }, \\\"preferences\\\": { \\\"language\\\": \\\"en\\\", \\\"theme\\\": \\\"dark\\\" } }\"";

        // Extract and clean schema SchemaVerification.schemaStr1, SchemaVerification.jsonStr1
        String pre = "200,\"id\":@Form::82,\"name\":\"Testing Form23\",\"ifActive\": false,\"orderBasedType\": \"standard\",\"facilityId\":34,\"facilityGroupId\":57,\"isSync\": true,\"verify_schema\":";
        String schemaJson = extractSchemaFromRawResponse(pre+SchemaVerification.schemaStr1);
//        String schemaJson = extractSchemaFromRawResponse(rawResponse);

        // Dummy output JSON to validate against (should match the schema structure)
        String responseJson = "{ \"userId\": \"101abc\", \"username\": \"johndoe\", \"email\": \"johndoe@example.com\", \"phoneNumbers\": [ \"+1234567890\", \"+1987654321\" ], \"isActive\": true, \"profile\": { \"bio\": \"Experienced software developer with a passion for coding and problem solving.\", \"profilePictureUrl\": \"http://example.com/profile.jpg\", \"socialLinks\": [ { \"platform\": \"linkedin\", \"url\": \"https://www.linkedin.com/in/johndoe\" }, { \"platform\": \"twitter\", \"url\": \"https://twitter.com/johndoe\" } ] }, \"address\": { \"street\": \"456 Park Ave\", \"city\": \"Springfield\", \"zipCode\": \"12345\", \"country\": \"USA\" }, \"preferences\": { \"language\": \"en\", \"theme\": \"dark\" } }";
        String responseJson1 = "{ \"username\": \"johndoe\", \"email\": \"johndoe@example.com\", \"phoneNumbers\": [ \"+1234567890\", \"+1987654321\" ], \"isActive\": true, \"profile\": { \"bio\": \"Experienced software developer with a passion for coding and problem solving.\", \"profilePictureUrl\": \"http://example.com/profile.jpg\", \"socialLinks\": [ { \"platform\": \"linkedin\", \"url\": \"https://www.linkedin.com/in/johndoe\" }, { \"platform\": \"twitter\", \"url\": \"https://twitter.com/johndoe\" } ] }, \"address\": { \"street\": \"456 Park Ave\", \"city\": \"Springfield\", \"zipCode\": \"12345\", \"country\": \"USA\" }, \"preferences\": { \"language\": \"en\", \"theme\": \"dark\" } }";

        verifySchema(SchemaVerification.schemaStr1, SchemaVerification.jsonStr1);
        verifySchema(schemaJson, SchemaVerification.jsonStr1);

        String schemaJson1 = extractSchemaFromRawResponse(pre+schemaOutputJson);
        verifySchema(schemaJson1, responseJson1);
        String schemaJson2 = extractSchemaFromRawResponse(pre+outputJson);
        String responseJson2 = "{\"code\":\"INV\",\"errors\":[{\"actual\":{},\"expected\":{},\"error\":\"Invalid token\",\"where\":\"\"}]}";
        verifySchema(schemaJson2, responseJson2);
    }*/

    public static String extractSchemaFromRawResponse(String raw) {
        String key = "\"verify_schema\":";
        int idx = raw.indexOf(key);
        if (idx == -1) {
        	idx = raw.indexOf("verify_schema:");
        	if(idx == -1)
        		throw new RuntimeException("verify_schema not found");
        }

        // Extract part after verify_schema:
        String schemaPart = raw.substring(idx + key.length()).trim();

        // Remove leading/trailing quotes
        if (schemaPart.startsWith("\"")) {
            schemaPart = schemaPart.substring(1);
        }
        if (schemaPart.endsWith("\"")) {
            schemaPart = schemaPart.substring(0, schemaPart.length() - 1);
        }

        // Unescape \" to "
        schemaPart = schemaPart.replace("\\\"", "\"");

        return schemaPart;
    }
    
    public static String extractSchemaFromRawResponse2(String raw) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode schemaNode = root.get("verify_schema");
            if (schemaNode == null || !schemaNode.isTextual()) {
                throw new RuntimeException("verify_schema not found or is not a string");
            }

            // The string inside "verify_schema" is still escaped JSON, so parse it again
            String schemaJsonEscaped = schemaNode.asText();  // This will be the escaped JSON string
            JsonNode unescapedSchemaNode = mapper.readTree(schemaJsonEscaped);  // Parse it again to JsonNode

            return unescapedSchemaNode.toString(); // Return properly formatted schema JSON
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse verify_schema from response", e);
        }
    }
    
    public static String extractSchemaFromRawResponse3(String raw) {
        String key = "\"verify_schema\":";
        int idx = raw.indexOf(key);
        if (idx == -1) {
            throw new RuntimeException("verify_schema not found");
        }

        // Get the start of the schema (after the key)
        int startIdx = idx + key.length();
        StringBuilder schemaBuilder = new StringBuilder();
        boolean inQuotes = false;
        boolean escapeNext = false;

        // Scan character by character to extract the string value of verify_schema
        for (int i = startIdx; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (!inQuotes && c == '"') {
                inQuotes = true;
                continue;
            } else if (inQuotes) {
                if (escapeNext) {
                    schemaBuilder.append(c);
                    escapeNext = false;
                } else if (c == '\\') {
                    schemaBuilder.append(c);
                    escapeNext = true;
                } else if (c == '"') {
                    break;
                } else {
                    schemaBuilder.append(c);
                }
            }
        }

        String escapedSchemaJson = schemaBuilder.toString();

        // Now unescape it and return as valid JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode parsed = mapper.readTree("\"" + escapedSchemaJson + "\""); // Wrap in quotes to parse as string
            return parsed.asText(); // This gives the unescaped schema JSON
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse verify_schema content as JSON", e);
        }
    }

    public static String extractSchemaFromRawResponse4(String raw) {
        String key = "\"verify_schema\":";
        int idx = raw.indexOf(key);
        if (idx == -1) {
            throw new RuntimeException("verify_schema not found");
        }

        int startIdx = raw.indexOf('"', idx + key.length()); // Find the opening quote of the schema string
        if (startIdx == -1) {
            throw new RuntimeException("Opening quote for verify_schema not found");
        }

        StringBuilder schemaBuilder = new StringBuilder();
        boolean escapeNext = false;

        for (int i = startIdx + 1; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (escapeNext) {
                schemaBuilder.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                schemaBuilder.append(c);
                escapeNext = true;
            } else if (c == '"') {
                break; // End of the schema string
            } else {
                schemaBuilder.append(c);
            }
        }

        String escapedSchemaJson = schemaBuilder.toString();

        try {
            // Unescape the escaped JSON string
            ObjectMapper mapper = new ObjectMapper();
            // Wrap in double quotes to parse as JSON string
            String unescaped = mapper.readValue("\"" + escapedSchemaJson + "\"", String.class);
            return unescaped;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse verify_schema content as JSON", e);
        }
    }

    public static String extractSchemaFromRawResponse5(String raw) {
        String key = "\"verify_schema\":";
        int idx = raw.indexOf(key);
        if (idx == -1) {
            throw new RuntimeException("verify_schema not found");
        }

        int startIdx = raw.indexOf('"', idx + key.length());
        if (startIdx == -1) {
            throw new RuntimeException("Opening quote for verify_schema not found");
        }

        StringBuilder schemaBuilder = new StringBuilder();
        boolean escapeNext = false;

        // Start after the first quote
        for (int i = startIdx + 1; i < raw.length(); i++) {
            char c = raw.charAt(i);

            if (escapeNext) {
                schemaBuilder.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                schemaBuilder.append(c);
                escapeNext = true;
            } else if (c == '"') {
                break; // Closing quote of the schema string
            } else {
                schemaBuilder.append(c);
            }
        }

        String escapedSchemaJson = schemaBuilder.toString();

        try {
            // Unescape the string using Jackson
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue("\"" + escapedSchemaJson + "\"", String.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to unescape verify_schema content", e);
        }
    }


}
