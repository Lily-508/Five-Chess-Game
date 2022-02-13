package user;

import java.awt.FlowLayout;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class UserInputPanel extends JPanel{
	public JTextField contentInputted=new JTextField("输入聊天内容",26);
	public JComboBox<String> userChoice=new JComboBox<>();
	public UserInputPanel() {
		// TODO Auto-generated constructor stub
		setLayout(new FlowLayout(FlowLayout.LEFT));
		contentInputted.setEditable(true);
		for(int i=0;i<50;i++) {
			userChoice.addItem(i+".无用户");
		}
		userChoice.setSize(60,24);
		add(userChoice);
		add(contentInputted);
	}
		
}
