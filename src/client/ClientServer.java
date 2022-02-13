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
//	�û������ж�
	boolean isOnChat=false;
	boolean isOnChess=false;
	boolean start=false;
	// ��Ϸ�Ƿ������
	boolean isGameConnected=false;
	// �Ƿ�Ϊ��Ϸ������
	boolean isCreator=false;
	// �Ƿ�Ϊ��Ϸ������ 
	boolean isParticipant = false; 
	 // �û��б��� 
	UserListPanel userListPad = new UserListPanel(); 
	 // �û������� 
	UserChatPanel userChatPad = new UserChatPanel(); 
	 // �û������� 
	UserControlPanel userControlPad = new UserControlPanel(); 
	 // �û������� 
	UserInputPanel userInputPad = new UserInputPanel(); 
	 // ������ 
	Pad pad = new Pad(userChatPad); 
	 // ����� 
	Panel southPanel = new Panel(); 
	Panel northPanel = new Panel(); 
	Panel centerPanel = new Panel(); 
	Panel eastPanel = new Panel();
	public ClientServer() {
		super("����С��Ϸ�ͻ���");
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
				if (isOnChat) { // ������ 
					try{ // �رտͻ����׽ӿ� 
						clientSocket.close(); 
					} 
					catch (Exception ed){} 
				} 
				if (isOnChess || isGameConnected) { // ������ 
					try{ // �ر�����˿� 
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
	// ��ָ����IP��ַ�Ͷ˿����ӵ������� 
	public boolean connectToServer(String serverIP,int serverPort)throws Exception {
		try {
			System.out.print(serverIP);
			clientSocket =new Socket(serverIP,serverPort);
			inputStream=new DataInputStream(clientSocket.getInputStream());
			outputStream=new DataOutputStream(clientSocket.getOutputStream());
			ClientThread clientThread=new ClientThread(this);
			clientThread.setName("�ͻ��߳�"+num++);
			clientThread.start();
			isOnChat=true;
			return true;
		} catch (Exception e) {
			userChatPad.chatTextArea.setText("��������\n");
			e.printStackTrace();
		}
		return false;
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		JTextField inputwords=(JTextField)e.getSource();
		if(e.getKeyCode()==KeyEvent.VK_ENTER) {
			if(userInputPad.userChoice.getSelectedItem().equals("�����û�")) {
				try {
					outputStream.writeUTF(inputwords.getText());
					inputwords.setText("");
				} catch (Exception e2) {
					userChatPad.chatTextArea.setText("�������ӵ�������!\n");
					userListPad.userList.removeAll();
					userInputPad.userChoice.removeAll();
					inputwords.setText("");
					userControlPad.connectButton.setEnabled(true);
				}
			}
			// ��ָ���˷���Ϣ
			else {
				try {
					outputStream.writeUTF("/"+userInputPad.userChoice.getSelectedItem()
											+" "+inputwords.getText());
					inputwords.setText("");
				} catch (IOException e1) {
					userChatPad.chatTextArea.setText("�������ӵ�������!\n");
					userListPad.userList.removeAll();
					inputwords.setText("");
					userControlPad.connectButton.setEnabled(true);
				}
			}
		}
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		// ���ӵ�������ť�����¼�  
		if(e.getSource()==userControlPad.connectButton) {
			host=pad.host=userControlPad.ipInputted.getText();
			try {
				if(connectToServer(host, PORT)) {
					userChatPad.chatTextArea.setText("���ӵ�������");
					userControlPad.connectButton.setEnabled(false);
					userControlPad.createButton.setEnabled(true);
					userControlPad.joinButton.setEnabled(true);
					pad.statusText.setText("���ӳɹ�����ȴ�!");
				}
			} catch (Exception e2) {
				userChatPad.chatTextArea.setText("����ʧ��");
				e2.printStackTrace();
			}
		}
		// ������Ϸ��ť�����¼�
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
				userChatPad.chatTextArea.setText("�����̲߳������ӣ�������Ϸʧ��: \n");
				e2.getStackTrace();
			}
		}
		// ������Ϸ��ť�����¼� 
		else if(e.getSource()==userControlPad.joinButton) {
			String selectedUser=(String)userListPad.userList.getSelectedItem();
			if(selectedUser==null||selectedUser.startsWith("[inchess]")||selectedUser.equals(clientName)) {
				pad.statusText.setText("����ѡ��һ���û�!����ѡ���Լ�����Ϸ���û�");
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
					userChatPad.chatTextArea.setText("��������: \n"+e2);
				}
			}
		}
		// ������Ϸ��ť�����¼�
		else if(e.getSource()==userControlPad.cancelButton) {
			if(!start) {
				pad.padThread.sendMessage("/giveupcreategame "+clientName);
			}
			else if(isOnChess) {
				pad.padThread.sendMessage("/giveup "+clientName);
				if(pad.chessColor==0)
					pad.setWinStatus(1);
				else pad.setWinStatus(0);
				pad.statusText.setText("�봴���������Ϸ!"); 
			}
			pad.clearPad();
			isCreator=false;
			isOnChess=false;
			isGameConnected=isParticipant=false;
			userControlPad.createButton.setEnabled(true); 
			userControlPad.joinButton.setEnabled(true); 
			userControlPad.cancelButton.setEnabled(false); 
			
		}
		//�˳���Ϸ��ť�����¼�
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
