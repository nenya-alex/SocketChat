package ua.nenya.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SocketChatServer {

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		List<ClientProcessor> list = Collections.synchronizedList(new ArrayList<ClientProcessor>());
		try {
			serverSocket = new ServerSocket(20444);
			while (true) {
				Socket socket = serverSocket.accept();

				Executor executor = Executors.newFixedThreadPool(10);
				ClientProcessor client = new ClientProcessor(socket, list);
				list.add(client);
				executor.execute(client);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			new SocketChatServer().closeAll(serverSocket, list);
		}
	}

	private void closeAll(ServerSocket serverSocket, List<ClientProcessor> list) {
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
}
