package client;

import java.awt.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*; 
  
import javax.swing.JFrame;
import javax.swing.JTextField;

import chess.Pad;
import user.UserChatPanel;
import user.UserControlPanel;
import user.UserInputPanel;
import user.UserListPanel;

public class ClientServer extends JFrame implements ActionListener,KeyListener{
	Socket clientSocket;
	DataInputStream inputStream;
	DataOutputStream outputStream;
	String clientName=null;
	String host=null;
	final int PORT=2021;
//	用户功能判断
	boolean isOnChat=false;
	boolean isOnChess=false;
	boolean start=false;
	// 游戏是否进行中
	boolean isGameConnected=false;
	// 是否为游戏创建者
	boolean isCreator=false;
	// 是否为游戏加入者 
	boolean isParticipant = false; 
	 // 用户列表区 
	UserListPanel userListPad = new UserListPanel(); 
	 // 用户聊天区 
	UserChatPanel userChatPad = new UserChatPanel(); 
	 // 用户操作区 
	UserControlPanel userControlPad = new UserControlPanel(); 
	 // 用户输入区 
	UserInputPanel userInputPad = new UserInputPanel(); 
	 // 下棋区 
	Pad pad = new Pad(userChatPad); 
	 // 面板区 
	Panel southPanel = new Panel(); 
	Panel northPanel = new Panel(); 
	Panel centerPanel = new Panel(); 
	Panel eastPanel = new Panel();
	public ClientServer() {
		super("网络小游戏客户端");
		setLayout(new BorderLayout());
		host=userControlPad.ipInputted.getText();
		pad.host=userControlPad.ipInputted.getText();
		
		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(userListPad,BorderLayout.NORTH);
		eastPanel.add(userChatPad,BorderLayout.CENTER);
		add(eastPanel,BorderLayout.WEST);
		
		centerPanel.add(pad,BorderLayout.CENTER);
		centerPanel.add(userInputPad,BorderLayout.SOUTH);
		centerPanel.setBackground(Color.LIGHT_GRAY);
		add(centerPanel,BorderLayout.CENTER);
		
		userInputPad.contentInputted.addKeyListener(this);
		userControlPad.connectButton.addActionListener(this);
		userControlPad.createButton.addActionListener(this);
		userControlPad.cancelButton.addActionListener(this);
		userControlPad.joinButton.addActionListener(this);
		userControlPad.exitButton.addActionListener(this);
		userControlPad.createButton.setEnabled(false);
		userControlPad.joinButton.setEnabled(false);
		userControlPad.cancelButton.setEnabled(false);
		southPanel.add(userControlPad,BorderLayout.CENTER);
		southPanel.setBackground(Color.LIGHT_GRAY);;
		add(southPanel,BorderLayout.SOUTH);
		
		addWindowListener(new WindowAdapter() { 
			public void windowClosing(WindowEvent e) {
				if (isOnChat) { // 聊天中 
					try{ // 关闭客户端套接口 
						clientSocket.close(); 
					} 
					catch (Exception ed){} 
				} 
				if (isOnChess || isGameConnected) { // 下棋中 
					try{ // 关闭下棋端口 
						pad.chessSocket.close(); 
					} 
					catch (Exception ee){} 
				} 
				System.exit(0); 
			} 
		}); 
		pack();
		setSize(670,560);
		setVisible(true);
		setResizable(false);
		this.validate();
	}
	int num=1;
	// 按指定的IP地址和端口连接到服务器 
	public boolean connectToServer(String serverIP,int serverPort)throws Exception {
		try {
			System.out.print(serverIP);
			clientSocket =new Socket(serverIP,serverPort);
			inputStream=new DataInputStream(clientSocket.getInputStream());
			outputStream=new DataOutputStream(clientSocket.getOutputStream());
			ClientThread clientThread=new ClientThread(this);
			clientThread.setName("客户线程"+num++);
			clientThread.start();
			isOnChat=true;
			return true;
		} catch (Exception e) {
			userChatPad.chatTextArea.setText("不能连接\n");
			e.printStackTrace();
		}
		return false;
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		JTextField inputwords=(JTextField)e.getSource();
		if(e.getKeyCode()==KeyEvent.VK_ENTER) {
			if(userInputPad.userChoice.getSelectedItem().equals("所有用户")) {
				try {
					outputStream.writeUTF(inputwords.getText());
					inputwords.setText("");
				} catch (Exception e2) {
					userChatPad.chatTextArea.setText("不能连接到服务器!\n");
					userListPad.userList.removeAll();
					userInputPad.userChoice.removeAll();
					inputwords.setText("");
					userControlPad.connectButton.setEnabled(true);
				}
			}
			// 给指定人发信息
			else {
				try {
					outputStream.writeUTF("/"+userInputPad.userChoice.getSelectedItem()
											+" "+inputwords.getText());
					inputwords.setText("");
				} catch (IOException e1) {
					userChatPad.chatTextArea.setText("不能连接到服务器!\n");
					userListPad.userList.removeAll();
					inputwords.setText("");
					userControlPad.connectButton.setEnabled(true);
				}
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// 连接到主机按钮单击事件  
		if(e.getSource()==userControlPad.connectButton) {
			host=pad.host=userControlPad.ipInputted.getText();
			try {
				if(connectToServer(host, PORT)) {
					userChatPad.chatTextArea.setText("连接到服务器");
					userControlPad.connectButton.setEnabled(false);
					userControlPad.createButton.setEnabled(true);
					userControlPad.joinButton.setEnabled(true);
					pad.statusText.setText("连接成功，请等待!");
				}
			} catch (Exception e2) {
				userChatPad.chatTextArea.setText("连接失败");
				e2.printStackTrace();
			}
		}
		// 创建游戏按钮单击事件
		else if(e.getSource()==userControlPad.createButton) {
			try {
				if(!isGameConnected&&pad.connectServer(pad.host, pad.PORT)) {
					isGameConnected=true;
				}
				isOnChess=true;
				isCreator=true;
				userControlPad.createButton.setEnabled(false); 
				userControlPad.joinButton.setEnabled(false); 
				userControlPad.cancelButton.setEnabled(true); 
				pad.padThread.sendMessage("/creategame "+"[inchess]"+clientName);
			} catch (Exception e2) {
				isOnChess=false;
				isCreator=false;
				userControlPad.createButton.setEnabled(true); 
				userControlPad.joinButton.setEnabled(true); 
				userControlPad.cancelButton.setEnabled(false); 
				userChatPad.chatTextArea.setText("棋盘线程不能连接，创建游戏失败: \n");
				e2.getStackTrace();
			}
		}
		// 加入游戏按钮单击事件 
		else if(e.getSource()==userControlPad.joinButton) {
			String selectedUser=(String)userListPad.userList.getSelectedItem();
			if(selectedUser==null||selectedUser.startsWith("[inchess]")||selectedUser.equals(clientName)) {
				pad.statusText.setText("必须选择一个用户!不能选择自己和游戏中用户");
			}else {
				try {
					if(!isGameConnected&&pad.connectServer(pad.host, pad.PORT)) {
						isGameConnected=true;
					}
					isOnChess = true; 
					isCreator=false;
					isParticipant = true; 
					userControlPad.createButton.setEnabled(false);
					userControlPad.joinButton.setEnabled(false);
					userControlPad.cancelButton.setEnabled(true);
					pad.padThread.sendMessage("/joingame "+selectedUser+" "+clientName);
					
				} catch (Exception e2) {
					isOnChess = false; 
					isParticipant = false; 
					isGameConnected=false;
					userControlPad.createButton.setEnabled(true);
					userControlPad.joinButton.setEnabled(true);
					userControlPad.cancelButton.setEnabled(false);
					userChatPad.chatTextArea.setText("不能连接: \n"+e2);
				}
			}
		}
		// 放弃游戏按钮单击事件
		else if(e.getSource()==userControlPad.cancelButton) {
			if(!start) {
				pad.padThread.sendMessage("/giveupcreategame "+clientName);
			}
			else if(isOnChess) {
				pad.padThread.sendMessage("/giveup "+clientName);
				if(pad.chessColor==0)
					pad.setWinStatus(1);
				else pad.setWinStatus(0);
				pad.statusText.setText("请创建或加入游戏!"); 
			}
			pad.clearPad();
			isCreator=false;
			isOnChess=false;
			isGameConnected=isParticipant=false;
			userControlPad.createButton.setEnabled(true); 
			userControlPad.joinButton.setEnabled(true); 
			userControlPad.cancelButton.setEnabled(false); 
			
		}
		//退出游戏按钮单击事件
		else if(e.getSource()==userControlPad.exitButton) {
			try {
				if(isOnChat) {
					clientSocket.close();
				}else if(isOnChess||isGameConnected) {
					pad.chessSocket.close();
				}
			} catch (Exception e2) {
				System.exit(0);
			}
			System.exit(0);
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		ClientServer cs=new ClientServer();
	}
}
