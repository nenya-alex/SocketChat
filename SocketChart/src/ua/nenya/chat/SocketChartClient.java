package ua.nenya.chat;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class SocketChartClient {

	private static final String MADE_BY = "Made by Ann&Alex 2015";

	private static final String EXIT = "exit";

	private static final String LINE = "==========================";

	private static final String HISTORY_FILE_NAME = "log.txt";

	private static final String HOST = "127.0.0.1";

	static SocketChartClient scc = new SocketChartClient();

	JList<String> list = new JList<String>();
	private Socket clientSocket;
	private BufferedReader reader;
	private BufferedWriter fileWriter;
	private PrintWriter writer;
	final JTextArea textArea = new JTextArea();
	final JTextField textField = new JTextField();
	final JPanel cards = new JPanel(new CardLayout());
	final JTextField fieldLogin = new JTextField(20);
	final JTextField fieldPass = new JTextField(20);
	final JButton buttonJoin = new JButton("Join Chat");
	final JButton buttonSend = new JButton("Send");
	final JScrollPane scrollPaneList = new JScrollPane(list);
	final JLabel label = new JLabel();
	final JMenuItem itemFileSave = new JMenuItem("Save log...");
	final JMenuItem itemExit = new JMenuItem("Save and Exit");
	final JMenuItem itemExitAll = new JMenuItem("Exit");
	final JMenuItem itemAbout = new JMenuItem("About...");
	final JFrame frame = new JFrame();

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					scc.createAndShowGUI();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		scc.chat();
		Taker taker = new Taker();
		Thread thread = new Thread(taker);
		thread.start();
	}

	private void chat() {
		try {
			clientSocket = new Socket(HOST, 20444);
			writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			fileWriter = new BufferedWriter(new FileWriter(HISTORY_FILE_NAME, true));

			fieldPass.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent arg0) {
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						try {
							join();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			});

			textField.addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent arg0) {
				}

				@Override
				public void keyReleased(KeyEvent arg0) {
				}

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						send();
					}
				}
			});

			buttonJoin.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						join();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});

			buttonSend.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					send();
				}
			});

			itemFileSave.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						fileWriter.write(new Date().toString());
						fileWriter.newLine();
						fileWriter.write(LINE);
						fileWriter.newLine();
						fileWriter.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});

			list.addListSelectionListener(new ListSelectionListener() {

				@Override
				public void valueChanged(ListSelectionEvent arg0) {
					int index = list.getSelectedIndex();
					if (index != -1) {
						String text = list.getSelectedValue().toString();
						textField.setText(text + ": ");
					}
				}
			});

			itemExit.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						fileWriter.write(new Date().toString());
						fileWriter.newLine();
						fileWriter.write(LINE);
						fileWriter.newLine();
						fileWriter.close();
						writer.println(EXIT);
						writer.flush();
						writer.close();
						System.exit(0);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});

			itemExitAll.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					writer.println(EXIT);
					writer.flush();
					writer.close();
					System.exit(0);
				}
			});

			itemAbout.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JOptionPane.showMessageDialog(itemAbout, MADE_BY);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void join() throws IOException {
		String login = fieldLogin.getText();
		String pass = fieldPass.getText();
		writer.println(login);
		writer.flush();
		writer.println(pass);
		writer.flush();
	}

	private void send() {
		String text = textField.getText();
		if (!text.equals(EXIT)) {
			writer.println(text);
			writer.flush();
			textField.setText("");
		} else {
			writer.println(text);
			writer.flush();
			textField.setText("");
			writer.close();
			System.exit(0);
		}
	}

	public static void close() {
		try {
			scc.reader.close();
			scc.writer.close();
			scc.clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static class Taker implements Runnable {

		private boolean stoped;

		public void setStop() {
			stoped = true;
		}

		@Override
		public void run() {
			try {
				while (!stoped) {
					String s = scc.reader.readLine();
					if (s.equals("OK")) {
						CardLayout cardLayout = (CardLayout) scc.cards.getLayout();
						cardLayout.last(scc.cards);
					}
					if (s.equals("NOT OK")) {
						JOptionPane.showMessageDialog(scc.frame, "Wrong login or password.");
					}
					if (!s.equals("OK") && !s.equals("NOT OK")) {
						scc.fileWriter.write(s);
						scc.fileWriter.newLine();
						scc.textArea.append(s + "\n");
						if (s.contains(" cames now")) {
							scc.writer.println("Send me enter nameList");
							scc.writer.flush();
							scc.listResever(s);
						}
						if (s.contains(" has left")) {
							scc.writer.println("Send me exit nameList");
							scc.writer.flush();
							scc.listResever(s);
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void listResever(String s) throws IOException {
		final DefaultListModel<String> listModel = new DefaultListModel<String>();
		listModel.removeAllElements();
		while (!(s = reader.readLine()).equals("End nameList")) {
			listModel.addElement(s);
		}
		list.setModel(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPaneList.setPreferredSize(new Dimension(100, 30));
		scrollPaneList.getViewport().setView(list);
	}

	private void createAndShowGUI() throws IOException {
		JPanel cardChat = new JPanel(new BorderLayout());
		final JPanel panelSouth = new JPanel();
		JScrollPane scrollPaneText = new JScrollPane(textArea);
		textArea.setLineWrap(true);
		scrollPaneText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		cardChat.add(BorderLayout.CENTER, scrollPaneText);
		list.setFont(new Font("serif", Font.BOLD, 16));

		cardChat.add(BorderLayout.EAST, scrollPaneList);
		cardChat.add(BorderLayout.SOUTH, panelSouth);

		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));
		panelSouth.add(textField);
		panelSouth.add(buttonSend);

		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuFile.add(itemFileSave);
		menuFile.add(itemExit);
		menuFile.add(itemExitAll);
		menuFile.add(itemAbout);
		menuBar.add(menuFile);
		frame.setJMenuBar(menuBar);

		final JPanel cardLogin = new JPanel();
		cardLogin.setLayout(new BoxLayout(cardLogin, BoxLayout.Y_AXIS));

		final JPanel upPanel = new JPanel();
		final JPanel midPanel = new JPanel();
		final JPanel downPanel = new JPanel();

		upPanel.setLayout(new BoxLayout(upPanel, BoxLayout.X_AXIS));
		midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.X_AXIS));
		downPanel.setLayout(new BoxLayout(downPanel, BoxLayout.X_AXIS));

		JLabel labelLog = new JLabel("Login:          ");
		fieldLogin.setMaximumSize(new Dimension(200, 20));
		upPanel.add(labelLog);
		upPanel.add(fieldLogin);
		cardLogin.add(upPanel);

		JLabel labelPass = new JLabel("Password: ");
		fieldPass.setMaximumSize(new Dimension(200, 20));
		midPanel.add(labelPass);
		midPanel.add(fieldPass);
		cardLogin.add(midPanel);

		downPanel.add(buttonJoin);
		cardLogin.add(downPanel);

		cards.add(cardLogin);
		cards.add(cardChat);

		frame.getContentPane().add(cards);

		frame.setTitle("GUI Chat Client");
		frame.setVisible(true);
		frame.setSize(700, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
