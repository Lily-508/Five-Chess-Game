package user;

import java.awt.BorderLayout;
import java.awt.List;
import java.awt.Panel;

import javax.swing.JPanel;

/**
 * 
	�û��б�Panel�����10���û�
 *
 */
public class UserListPanel extends JPanel {
	public List userList=new List(10);
	public UserListPanel() {
		setLayout(new BorderLayout());
		for(int i=0;i<10;i++) {
			userList.add(i+"."+"���û�");
		}
		add(userList,BorderLayout.CENTER);
	}

}
