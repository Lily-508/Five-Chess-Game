package user;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
/*
 * �û�����Panel
 */
public class UserControlPanel extends JPanel{
	public JLabel ipLabel=new JLabel("IP",JLabel.LEFT);
	public JTextField ipInputted=new JTextField("localhost",10);
	public JButton connectButton=new JButton("���ӷ�����");
	public JButton createButton=new JButton("������Ϸ");
	public JButton joinButton=new JButton("������Ϸ");
	public JButton cancelButton=new JButton("������Ϸ");
	public JButton exitButton=new JButton("�˳�����");
	
	public UserControlPanel() {
		// TODO Auto-generated constructor stub
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(Color.LIGHT_GRAY);
		add(ipLabel);
		add(ipInputted);
		add(connectButton);
		add(createButton);
		add(joinButton);
		add(cancelButton);
		add(exitButton);
	}
}
