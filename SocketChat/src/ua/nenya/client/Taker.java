package ua.nenya.client;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

public class Taker implements Runnable {

	private static final String END_NAME_LIST = "End nameList";
	private static final String SEND_ME_EXIT_NAME_LIST = "Send me exit nameList";
	private static final String HAS_LEFT = " has left";
	private static final String SEND_ME_ENTER_NAME_LIST = "Send me enter nameList";
	private static final String CAMES_NOW = " cames now";
	private static final String WRONG_LOGIN_OR_PASSWORD = "Wrong login or password.";
	private static final String NOT_OK = "NOT OK";
	private static final String OK = "OK";
	private boolean stoped;
	private PrintWriter writer;
	private BufferedReader reader;
	private JList<String> list = new JList<String>();
	private JTextArea textArea = new JTextArea();
	private JPanel cards = new JPanel(new CardLayout());
	private JScrollPane scrollPaneList = new JScrollPane(list);
	private JFrame frame = new JFrame();
	private BufferedWriter fileWriter;

	public Taker(PrintWriter writer, BufferedReader reader, JTextArea textArea, JFrame frame, JPanel cards,
			JList<String> list, JScrollPane scrollPaneList, BufferedWriter fileWriter) {
		this.writer = writer;
		this.reader = reader;
		this.textArea = textArea;
		this.frame = frame;
		this.cards = cards;
		this.list = list;
		this.scrollPaneList = scrollPaneList;
		this.fileWriter = fileWriter;
	}

	public void setStop() {
		stoped = true;
	}

	@Override
	public void run() {
		try {
			while (!stoped) {
				String s = reader.readLine();
				if (s.equals(OK)) {
					CardLayout cardLayout = (CardLayout) cards.getLayout();
					cardLayout.last(cards);
				}
				if (s.equals(NOT_OK)) {
					JOptionPane.showMessageDialog(frame, WRONG_LOGIN_OR_PASSWORD);
				}
				if (!s.equals(OK) && !s.equals(NOT_OK)) {
					fileWriter.write(s);
					fileWriter.newLine();
					textArea.append(s + "\n");
					if (s.contains(CAMES_NOW)) {
						writer.println(SEND_ME_ENTER_NAME_LIST);
						writer.flush();
						listResever(s);
					}
					if (s.contains(HAS_LEFT)) {
						writer.println(SEND_ME_EXIT_NAME_LIST);
						writer.flush();
						listResever(s);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listResever(String s) throws IOException {
		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.removeAllElements();
		while (!(s = reader.readLine()).equals(END_NAME_LIST)) {
			listModel.addElement(s);
		}
		list.setModel(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneList.setPreferredSize(new Dimension(100, 30));
		scrollPaneList.getViewport().setView(list);
	}

}
