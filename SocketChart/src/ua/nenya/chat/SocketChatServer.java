package ua.nenya.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SocketChatServer {

	static List<ClientProcessor> list = Collections.synchronizedList(new ArrayList<ClientProcessor>());

	static List<String> nameList = Collections.synchronizedList(new ArrayList<String>());

	static ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();

	private static ServerSocket serverSocket;

	public static void main(String[] args) {
		try {
			serverSocket = new ServerSocket(20444);
			while (true) {
				Socket socket = serverSocket.accept();

				Executor executor = Executors.newFixedThreadPool(10);
				ClientProcessor client = new ClientProcessor(socket);
				list.add(client);
				executor.execute(client);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	static void closeAll() {
		try {
			serverSocket.close();
			synchronized (list) {
				for (ClientProcessor it : list) {
					it.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static class ClientProcessor implements Runnable {
		private static final String PASSWORD = "111111";
		private static final String LOGIN = "postgres";
		private static final String URL = "jdbc:postgresql://127.0.0.1:5432/postgres";
		Socket clientSocket;
		String name = "";
		String pass = "";
		int count = 0;
		BufferedReader reader;
		PrintWriter writer;
		Connection connection;
		Statement st;

		public ClientProcessor(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {
				connection = DriverManager.getConnection(URL, LOGIN, PASSWORD);
				st = connection.createStatement();
			} catch (SQLException e1) {
				e1.printStackTrace();
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
					pass = reader.readLine();
					String hashPass = getHash(pass);
					String select = "SELECT count(*) FROM users WHERE login = '" + name + "' AND password = '"
							+ hashPass + "';";
					ResultSet set = st.executeQuery(select);
					set.next();
					count = set.getInt("count");
					if (count != 1) {
						list.get(list.size() - 1).writer.println("NOT OK");
					}
				} while (count != 1);
				if (count > 0) {
					list.get(list.size() - 1).writer.println("OK");
					nameList.add(name);
					synchronized (list) {
						for (ClientProcessor it : list) {
							it.writer.println(name + " cames now");
						}
						map.putIfAbsent(name, count);
					}
					String str = "";

					while (true) {
						str = reader.readLine();
						if (str.equals("exit"))
							break;
						synchronized (list) {
							if (str.equals("Send me enter nameList")) {
								for (String iter : nameList) {
									writer.println(iter);
								}
								writer.println("End nameList");
							} else {
								if (str.equals("Send me exit nameList")) {
									for (String iter : nameList) {
										writer.println(iter);
									}
									writer.println("End nameList");
								} else {
									for (ClientProcessor it : list) {
										it.writer.println(name + ": " + str);
									}
									System.out.println(name + ": " + str);

									for (Map.Entry<String, Integer> it : map.entrySet()) {
										if (name.equals(it.getKey())) {
											map.replace(name, it.getValue() + 1);
										}
									}
								}
							}
						}
					}

					synchronized (list) {
						for (ClientProcessor it : list) {
							it.writer.println(name + " has left");
						}

						for (int i = 0; i < nameList.size();) {
							if (name.equals(nameList.get(i))) {
								nameList.remove(i);
							} else {
								i++;
							}
						}

						for (Map.Entry<String, Integer> it : map.entrySet()) {
							if (name.equals(it.getKey())) {
								System.out.println(it.getKey() + " has written " + it.getValue() + " messenger(s)!");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				close();
			}
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

		public String getHash(String str) {

			MessageDigest md5;
			StringBuffer hexString = new StringBuffer();

			try {
				md5 = MessageDigest.getInstance("md5");
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
}
