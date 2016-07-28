package ua.nenya.client;

import java.awt.CardLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SocketChartClient {

	private static final String LOG_FILE = "log.txt";
	private static final String HOST = "127.0.0.1";

	public static void main(String[] args) {
		final JList<String> list = new JList<String>();
		final JTextArea textArea = new JTextArea();
		final JTextField textField = new JTextField();
		final JPanel cards = new JPanel(new CardLayout());
		final JScrollPane scrollPaneList = new JScrollPane(list);

		final JFrame frame = new JFrame();
		BufferedWriter fileWriter = null;
		Socket clientSocket = null;
		PrintWriter writer = null;
		BufferedReader reader = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(LOG_FILE, true));
			clientSocket = new Socket(HOST, 20444);
			writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		final ShowView showView = new ShowView(writer, textArea, list, scrollPaneList, textField, frame, cards,
				fileWriter);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					showView.createAndShowGUI();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		showView.chat();
		Taker taker = new Taker(writer, reader, textArea, frame, cards, list, scrollPaneList, fileWriter);
		Thread thread = new Thread(taker);
		thread.start();
	}
}
