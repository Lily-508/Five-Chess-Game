package user;

import java.awt.BorderLayout;
import java.awt.List;
import java.awt.Panel;

import javax.swing.JPanel;

/**
 * 
	用户列表Panel，最多10个用户
 *
 */
public class UserListPanel extends JPanel {
	public List userList=new List(10);
	public UserListPanel() {
		setLayout(new BorderLayout());
		for(int i=0;i<10;i++) {
			userList.add(i+"."+"无用户");
		}
		add(userList,BorderLayout.CENTER);
	}

}
