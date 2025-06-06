package common;

import java.io.*;
import java.util.*;
import org.apache.log4j.Logger;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.ChannelSftp.LsEntry;

@SuppressWarnings("all")
public class SSHUtil {
	private static String host;
	private static String user;
	private static String password;
	private static int port = 22;
	private static String filePath;
	static final Logger log = Logger.getLogger(SSHUtil.class);

	private SSHUtil() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
	}

	public static void loadConnectionDeatils() {
		try {
			host = CommonUtil.getXMLData(Constants.SSH_SETTING_PATH, "SSH_Host");
			user = CommonUtil.getXMLData(Constants.SSH_SETTING_PATH, "SSH_User");
			password = CommonUtil.getXMLData(Constants.SSH_SETTING_PATH, "SSH_Password");
			ExtentCucumberAdapter.addTestStepLog(
					"************************************************" + host + "  " + user + "  " + password);
		} catch (Exception ex) {
			throw new CustomException("DBSettings.xml file does not exist");
		}
	}

	public static String getListOfFiles(String filepath) {
		filePath = filepath;
		loadConnectionDeatils();
		Session session = null;
		Channel channel = null;
		ChannelSftp channelSftp = null;
		String list = "";
		try {
			JSch jsch = new JSch();
			session = jsch.getSession(user, host, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			ExtentCucumberAdapter.addTestStepLog("Connected to the host: '" + host + "'");
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(filePath);
			Vector filelist = channelSftp.ls("*");
			for (int i = 0; i < filelist.size(); i++) {
				LsEntry entry = (LsEntry) filelist.get(i);
				list = list + entry.getFilename() + ",";
			}
		} catch (Exception ex) {
			log.error(ex.getMessage());
		} finally {
			if (session != null)
				session.disconnect();
			if (channel != null)
				channel.disconnect();
		}
		return list;
	}

	public static boolean runCommands(String commands) {
		boolean res = false;
		try {
			String[] splitValue = commands.split("---");
			if (splitValue.length > 1) {
				log.info("SPLIT@@@@@@@@@@@@@@ VALUE1*******" + splitValue[0]);
				log.info("SPLIT@@@@@@@@@@@@@@ VALUE2*******" + splitValue[1]);
				log.info("SPLIT@@@@@@@@@@@@@@ VALUE3*******" + splitValue[2]);
				log.info("SPLIT@@@@@@@@@@@@@@ VALUE3*******" + splitValue[3]);
				if (splitValue.length > 4) {
					log.info("SPLIT@@@@@@@@@@@@@@ VALUE1*******" + splitValue[4]);
				}
				Session session = setupSshSession();
				session.connect();
				Channel channel = session.openChannel("shell");// only shell
				channel.setOutputStream(System.out);
				PrintStream shellStream = new PrintStream(channel.getOutputStream()); // printStream for convenience
				channel.connect();

				String[] commands1 = splitValue[0].split("&&");

				for (String cmd : commands1) {
					if (cmd.contains("_delay_")) {
						String[] cmds = cmd.split("_delay_");
						shellStream.println(cmds[0]);
						ExtentCucumberAdapter.addTestStepLog("Executed command : " + cmds[0]);

						shellStream.flush();
						int s = Integer.parseInt(cmds[1].trim());
						sleep(s);

					} else {
						shellStream.println(cmd.trim());
						ExtentCucumberAdapter.addTestStepLog("Executed command : " + cmd.trim());
						shellStream.flush();
						sleep(2);
					}
				}

				String verifyText = DbHelper.queryCopiedText(splitValue[2]).toUpperCase().trim();
				log.info("COPIED@@@@@@@@@@@@@@ VALUE*******" + verifyText);
				String fileoutput = "";
				if (splitValue.length > 4) {
					fileoutput = getListOfFiles(splitValue[1]).toUpperCase();
				} else {
					fileoutput = getDataFromFile(splitValue[1]).toUpperCase();
				}
				log.info("FILEOUTPUT DATA_______-------" + fileoutput);
				if (splitValue[3].equals("verifydisplayed")) {
					log.info("Copied text is" + verifyText + " verified" + fileoutput.contains(verifyText.trim()));
					if (fileoutput.contains(verifyText.trim())) {
						log.info("Inside VERIFIED DISPLAYED________****true****");
						res = true;
					} else {
						log.info("Inside VERIFIED DISPLAYED________****false****");
						res = false;
					}
				} else if (splitValue[3].equals("verifynotdisplayed")) {
					if (fileoutput.contains(verifyText.toUpperCase())) {
						log.info("Inside VERIFIED NOT DISPLAYED________****false****");
						res = false;
					} else {
						log.info("Inside VERIFIED NOT DISPLAYED________****true****");
						res = true;
					}
				}

				ExtentCucumberAdapter
						.addTestStepLog("Value read from file : '" + fileoutput + "' , Copied value to be verified : '"
								+ verifyText + "' , Verification type : '" + splitValue[3] + "'");
				channel.disconnect();
				session.disconnect();
			} else {
				log.info("------------------Run only command ------------------------");

				Session session2 = setupSshSession();
				session2.connect();

				Channel channel2 = session2.openChannel("shell");// only shell
				channel2.setOutputStream(System.out);
				PrintStream shellStream2 = new PrintStream(channel2.getOutputStream()); // printStream for convenience
				channel2.connect();

				String[] commands1 = commands.split("&&");
				for (String cmd : commands1) {
					log.info("FOR_EACH---------------------------------CMD" + cmd);
					if (cmd.contains("_delay_")) {
						String[] cmds = cmd.split("_delay_");
						shellStream2.println(cmds[0]);
						ExtentCucumberAdapter.addTestStepLog("Executed command : " + cmds[0]);
						shellStream2.flush();
						int s = Integer.parseInt(cmds[1].trim());
						sleep(s);
					} else {
						shellStream2.println(cmd.trim());
						ExtentCucumberAdapter.addTestStepLog("Executed command : " + cmd.trim());
						shellStream2.flush();
						sleep(2);
					}
					channel2.disconnect();
					session2.disconnect();
				}
				res = true;
			}

		} catch (Exception e) {
			log.info("EXCEPTION BLOCK-------######********--------" + e);
		}
		return res;
	}

	public static String getDataFromFile(String filepath) {
		filePath = filepath;
		loadConnectionDeatils();
		StringBuilder list = new StringBuilder();

		try {
			JSch jsch = new JSch();
			Session session = jsch.getSession(user, host, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.connect();
			log.info("Connection established.");
			log.info("Creating SFTP Channel.");
			ExtentCucumberAdapter.addTestStepLog("Connected to the host: '" + filePath + "'");
			log.info("Connected to the host: '" + filePath + "'");

			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
			log.info("SFTP Channel created.");
			ExtentCucumberAdapter.addTestStepLog("Connected to the host: '" + host + "'");

			InputStream inputStream = sftpChannel.get(filePath);
			ExtentCucumberAdapter.addTestStepLog("Reading the data from file: '" + filePath + "'");

			try (Scanner scanner = new Scanner(new InputStreamReader(inputStream))) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					list.append(line).append("\n");
					log.info(line);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return list.toString();
	}

	public static String runCommand(String command) {
		Session session = null;
		ChannelExec channel = null;
		try {
			session = setupSshSession();
			session.connect();
			ExtentCucumberAdapter.addTestStepLog("Connected to the host: '" + host + "'");
			StringBuilder outputBuffer = new StringBuilder();
			channel = (ChannelExec) session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			InputStream commandOutput = channel.getInputStream();
			channel.connect();
			int readByte = commandOutput.read();
			ExtentCucumberAdapter.addTestStepLog("Executed the command on Remote machine");
			while (readByte != 0xffffffff) {
				outputBuffer.append((char) readByte);
				readByte = commandOutput.read();
			}
			ExtentCucumberAdapter.addTestStepLog("Command output: '" + outputBuffer.toString() + "'");
			return outputBuffer.toString();
		} catch (Exception ex) {
			closeConnection(channel, session);
			throw new RuntimeException(ex);
		} finally {
			closeConnection(channel, session);
		}
	}

	private static Session setupSshSession() {
		Session session = null;
		try {
			loadConnectionDeatils();
			session = new JSch().getSession(user, host, port);
			session.setPassword(password);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return session;
	}

	private static void closeConnection(ChannelExec channel, Session session) {
		try {
			channel.disconnect();
		} catch (Exception ignored) {
			log.error(ignored.getMessage());
		}
		session.disconnect();
	}

	private static void sleep(int seconds) {
		long milliseconds = seconds * 1000L;
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
