package ua.nenya.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientProcessor implements Runnable {

	private static final String OK = "OK";
	private static final String EMPTY = "";
	private static final String EXIT = "exit";
	private static final String SEND_ME_ENTER_NAME_LIST = "Send me enter nameList";
	private static final String SEND_ME_EXIT_NAME_LIST = "Send me exit nameList";
	private static final String END_NAME_LIST = "End nameList";
	private static final String DOUBLE_DOT = ": ";
	private static final String CAMES_NOW = " cames now";
	private static final String HAS_LEFT = " has left";
	private static final String COUNT = "count";
	private static final String NOT_OK = "NOT OK";
	private static final String MD5 = "md5";
	private static final String SELECT_COUNT_FROM_USERS = "SELECT count(*) FROM users WHERE login = ? AND password = ?;";
	private static final String PASSWORD = "111111";
	private static final String LOGIN = "postgres";
	private static final String URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
	private List<String> nameList = Collections.synchronizedList(new ArrayList<String>());
	private ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
	private List<ClientProcessor> list;
	private Socket clientSocket;
	private BufferedReader reader;
	private PrintWriter writer;

	public ClientProcessor(Socket clientSocket, List<ClientProcessor> list) {
		this.clientSocket = clientSocket;
		this.list = list;
	}

	public void run() {
		String name = EMPTY;
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			try {
				reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				close();
			}
			int count = 0;
			do {
				name = reader.readLine();
				count = getCountFromExistingUser(connection, name);
			} while (count != 1);
			if (count > 0) {
				list.get(list.size() - 1).writer.println(OK);
				nameList.add(name);
				synchronized (list) {
					comeInClient(name, count);
				}
				String str = EMPTY;

				while (true) {
					str = reader.readLine();
					if (str.equals(EXIT))
						break;
					synchronized (list) {
						writeMessagesOfClient(name, str);
					}
				}

				synchronized (list) {
					leaveClient(name);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void writeMessagesOfClient(String name, String str) {
		if (str.equals(SEND_ME_ENTER_NAME_LIST)) {
			for (String iter : nameList) {
				writer.println(iter);
			}
			writer.println(END_NAME_LIST);
		} else {
			if (str.equals(SEND_ME_EXIT_NAME_LIST)) {
				for (String iter : nameList) {
					writer.println(iter);
				}
				writer.println(END_NAME_LIST);
			} else {
				for (ClientProcessor it : list) {
					it.writer.println(name + DOUBLE_DOT + str);
				}

				for (Map.Entry<String, Integer> it : map.entrySet()) {
					if (name.equals(it.getKey())) {
						map.replace(name, it.getValue() + 1);
					}
				}
			}
		}
	}

	private void comeInClient(String name, int count) {
		for (ClientProcessor it : list) {
			it.writer.println(name + CAMES_NOW);
		}
		map.putIfAbsent(name, count);
	}

	private void leaveClient(String name) {
		for (ClientProcessor it : list) {
			it.writer.println(name + HAS_LEFT);
		}

		for (int i = 0; i < nameList.size();) {
			if (name.equals(nameList.get(i))) {
				nameList.remove(i);
			} else {
				i++;
			}
		}
	}

	private int getCountFromExistingUser(Connection connection, String name) throws IOException, SQLException {

		int count;
		String pass = reader.readLine();
		String hashPass = getHash(pass);
		PreparedStatement preparedStatement = connection.prepareStatement(SELECT_COUNT_FROM_USERS);
		preparedStatement.setString(1, name);
		preparedStatement.setString(2, hashPass);
		ResultSet set = preparedStatement.executeQuery();
		set.next();
		count = set.getInt(COUNT);
		if (count != 1) {
			list.get(list.size() - 1).writer.println(NOT_OK);
		}
		return count;
	}

	public void close() {
		try {
			reader.close();
			writer.close();
			clientSocket.close();
			list.remove(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getHash(String str) {

		MessageDigest md5;
		StringBuffer hexString = new StringBuffer();

		try {
			md5 = MessageDigest.getInstance(MD5);
			md5.reset();
			md5.update(str.getBytes());
			byte messageDigest[] = md5.digest();

			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}

		} catch (NoSuchAlgorithmException e) {
			return e.toString();
		}

		return hexString.toString();
	}
}
