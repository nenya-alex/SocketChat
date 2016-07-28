package ua.nenya.client;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.BoxLayout;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ShowView {

	private static final String EMPTY = "";
	private final JButton buttonJoin = new JButton("Join Chat");
	private final JButton buttonSend = new JButton("Send");
	private final JMenuItem itemFileSave = new JMenuItem("Save log...");
	private final JMenuItem itemExit = new JMenuItem("Save and Exit");
	private final JMenuItem itemExitAll = new JMenuItem("Exit");
	private final JMenuItem itemAbout = new JMenuItem("About...");
	private final static String LINE = "==========================";
	private final JTextField fieldLogin = new JTextField(20);
	private final JTextField fieldPass = new JTextField(20);
	private final static String MADE_BY = "Made by Ann&Alex 2015";
	private final static String EXIT = "exit";
	private PrintWriter writer;
	private JList<String> list = new JList<String>();
	private JTextArea textArea = new JTextArea();
	private JTextField textField = new JTextField();
	private JPanel cards = new JPanel(new CardLayout());
	private JScrollPane scrollPaneList = new JScrollPane(list);
	private JFrame frame = new JFrame();
	private BufferedWriter fileWriter;

	public ShowView(PrintWriter writer, JTextArea textArea, JList<String> list, JScrollPane scrollPaneList,
			JTextField textField, JFrame frame, JPanel cards, BufferedWriter fileWriter) {
		this.writer = writer;
		this.textArea = textArea;
		this.list = list;
		this.scrollPaneList = scrollPaneList;
		this.textField = textField;
		this.frame = frame;
		this.cards = cards;
		this.fileWriter = fileWriter;
	}

	public void createAndShowGUI() throws IOException {
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

	public void chat() {
		try {
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
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					writer.println(EXIT);
					writer.flush();
					writer.close();
					System.exit(0);
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
			textField.setText(EMPTY);
		} else {
			writer.println(text);
			writer.flush();
			textField.setText(EMPTY);
			writer.close();
			System.exit(0);
		}
	}
}
