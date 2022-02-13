package user;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UserChatPanel extends JPanel{
	 public JTextArea chatTextArea=new JTextArea("√¸¡Ó«¯”Ú",18,20);
	 public UserChatPanel() {
		 setLayout(new BorderLayout());
		 chatTextArea.setLineWrap(true);
		 chatTextArea.setBackground(new Color(199, 237, 204));
		 JScrollPane scrollPane=new JScrollPane();
		 scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		 scrollPane.setViewportView(chatTextArea);
		 add(scrollPane,BorderLayout.CENTER);
	 }

}
