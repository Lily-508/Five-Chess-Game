package server;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Hashtable;
import javax.swing.*;


public class GameServer extends JFrame implements ActionListener {
	Hashtable<Socket, DataOutputStream> clientDataHash=new Hashtable(50);//将客户端套接口和输出流绑定
	Hashtable<Socket, String> clientNameHash=new Hashtable(50);//将客户端套接口和客户名绑定
	Hashtable<String, String> chessPeerHash=new Hashtable(50);//将游戏创建者和游戏加入者绑定 
	
	final int PORT=2021;
	ServerSocket serverSocket;
	JButton clearMsgButton=new JButton("清空列表");
	JButton serverStatusButton=new JButton("服务器状态");
	JButton closeSeverButton=new JButton("关闭服务器");
	ServerMsgPanel serverMsgPanel=new ServerMsgPanel();
	/*
	 * 设计服务端界面 
	 * ServerMsgPanel服务端面板类，实现连接数和连接信息实现
	 * 在GameServer无参构造器，实现框架，3个按钮以及事件连接：清空列表、服务器状态、关闭服务器
	 */
	public class ServerMsgPanel extends Panel{
		public TextArea msgTextArea=new TextArea("",20,50,TextArea.SCROLLBARS_VERTICAL_ONLY);
		public Label statusLabel=new Label("当前连接数： ",Label.LEFT);
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
		super("网络小游戏服务器");
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
			//清空服务器信息 
		}
		if(e.getSource()==serverStatusButton) {
			try {
				serverMsgPanel.msgTextArea.append("服务器信息:"
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
	 * 搭建服务端的Socket以及对客户端socket接收
	 */
	public void createServer()throws Exception{
		Socket clientSocket=null;
		int clientAccessNumber=1;
//		服务器Socket
		serverSocket=new ServerSocket(PORT);
		serverMsgPanel.msgTextArea.setText("服务器于： "+InetAddress.getLocalHost()+"启动，端口号为 "+serverSocket.getLocalPort()+"\n");
		while(true) {
//			接收客户端Socket
			try {
				serverMsgPanel.msgTextArea.append("等待用户连接\n");
				clientSocket=serverSocket.accept();
				serverMsgPanel.msgTextArea.append("已连接用户: "+clientSocket+"\n");
			} catch (Exception e) {
				System.out.println("正在等待用户连接");
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
				//接收客户端输出流
				outputStream=new DataOutputStream(clientSocket.getOutputStream());
				clientDataHash.put(clientSocket, outputStream);
				clientNameHash.put(clientSocket, ("新玩家"+clientAccessNumber));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			ServerThread thread=new ServerThread(socket,clientDataHash,clientNameHash,chessPeerHash,serverMsgPanel);
			thread.setName("服务线程"+clientAccessNumber);
			thread.start();
		}
	}
	public static void main(String[] args) {
		GameServer gameServer=new GameServer();
	}

}
