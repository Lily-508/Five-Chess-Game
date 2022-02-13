package server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import javax.swing.*;


public class GameServer extends JFrame implements ActionListener {
	Hashtable<Socket, DataOutputStream> clientDataHash=new Hashtable(50);//���ͻ����׽ӿں��������
	Hashtable<Socket, String> clientNameHash=new Hashtable(50);//���ͻ����׽ӿںͿͻ�����
	Hashtable<String, String> chessPeerHash=new Hashtable(50);//����Ϸ�����ߺ���Ϸ�����߰� 
	
	final int PORT=2021;
	ServerSocket serverSocket;
	JButton clearMsgButton=new JButton("����б�");
	JButton serverStatusButton=new JButton("������״̬");
	JButton closeSeverButton=new JButton("�رշ�����");
	ServerMsgPanel serverMsgPanel=new ServerMsgPanel();
	/*
	 * ��Ʒ���˽��� 
	 * ServerMsgPanel���������࣬ʵ����������������Ϣʵ��
	 * ��GameServer�޲ι�������ʵ�ֿ�ܣ�3����ť�Լ��¼����ӣ�����б�������״̬���رշ�����
	 */
	public class ServerMsgPanel extends Panel{
		public TextArea msgTextArea=new TextArea("",20,50,TextArea.SCROLLBARS_VERTICAL_ONLY);
		public Label statusLabel=new Label("��ǰ�������� ",Label.LEFT);
		public Panel msgPanel=new Panel();
		public Panel statusPanel=new Panel();
		public ServerMsgPanel() {
			setSize(400,300);
			setBackground(Color.LIGHT_GRAY);
			setLayout(new BorderLayout());
			msgPanel.setLayout(new FlowLayout());
			msgPanel.setSize(200,200);
			statusPanel.setLayout(new BorderLayout());
			statusPanel.setSize(200, 50);
			msgPanel.add(msgTextArea);
			statusPanel.add(statusLabel,BorderLayout.WEST);
			add(msgPanel,BorderLayout.CENTER);
			add(statusPanel,BorderLayout.NORTH);
		}
	}
	public GameServer() {
		// TODO Auto-generated constructor stub
		super("����С��Ϸ������");
		setBackground(Color.LIGHT_GRAY);
		Panel buttonPanel=new Panel();
		buttonPanel.setLayout(new FlowLayout());
		buttonPanel.add(clearMsgButton);
		clearMsgButton.addActionListener(this);
		buttonPanel.add(serverStatusButton);
		serverStatusButton.addActionListener(this);
		buttonPanel.add(closeSeverButton);
		closeSeverButton.addActionListener(this);
		add(serverMsgPanel,BorderLayout.NORTH);
		add(buttonPanel,BorderLayout.SOUTH);
		pack();
		setVisible(true);
		setSize(500,430);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		validate();
		try {
			createServer();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getSource()==clearMsgButton) {
			serverMsgPanel.msgTextArea.setText("");
			//��շ�������Ϣ 
		}
		if(e.getSource()==serverStatusButton) {
			try {
				serverMsgPanel.msgTextArea.append("��������Ϣ:"
						+ InetAddress.getLocalHost() + ":"
						+ serverSocket.getLocalPort() + "\n");
			}catch (Exception ep) {
				ep.printStackTrace();
			}
		}
		if(e.getSource()==closeSeverButton) {
			System.exit(0);
		}
	}
	
	/*
	 * �����˵�Socket�Լ��Կͻ���socket����
	 */
	public void createServer()throws Exception{
		Socket clientSocket=null;
		int clientAccessNumber=1;
//		������Socket
		serverSocket=new ServerSocket(PORT);
		serverMsgPanel.msgTextArea.setText("�������ڣ� "+InetAddress.getLocalHost()+"�������˿ں�Ϊ "+serverSocket.getLocalPort()+"\n");
		while(true) {
//			���տͻ���Socket
			try {
				serverMsgPanel.msgTextArea.append("�ȴ��û�����\n");
				clientSocket=serverSocket.accept();
				serverMsgPanel.msgTextArea.append("�������û�: "+clientSocket+"\n");
			} catch (Exception e) {
				System.out.println("���ڵȴ��û�����");
			}
			if(clientSocket!=null) {
				new ServerForClientThread(clientSocket,clientAccessNumber++).start();
			}
		}
	}
	class ServerForClientThread extends Thread{
		Socket socket;
		DataOutputStream outputStream=null;
		DataInputStream inputStream=null;
		int clientAccessNumber;
		String s=null;
		public ServerForClientThread(Socket clientSocket,int clientAccessNumber) {
			this.socket=clientSocket;
			this.clientAccessNumber=clientAccessNumber;
			try {
				//���տͻ��������
				outputStream=new DataOutputStream(clientSocket.getOutputStream());
				clientDataHash.put(clientSocket, outputStream);
				clientNameHash.put(clientSocket, ("�����"+clientAccessNumber));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			ServerThread thread=new ServerThread(socket,clientDataHash,clientNameHash,chessPeerHash,serverMsgPanel);
			thread.setName("�����߳�"+clientAccessNumber);
			thread.start();
		}
	}
	public static void main(String[] args) {
		GameServer gameServer=new GameServer();
	}

}
