package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import server.GameServer.ServerMsgPanel;

public class ServerThread extends Thread{
	Socket clientSocket;
	Hashtable<Socket, DataOutputStream> clientDataHash=new Hashtable(50);//将客户端套接口和输出流绑定
	Hashtable<Socket, String> clientNameHash=new Hashtable(50);//将客户端套接口和客户名绑定
	Hashtable<String, String> chessPeerHash=new Hashtable(50);//将游戏创建者和游戏加入者绑定 ,要是无匹配者值为wait
	ServerMsgPanel serverMsgPanel;
	boolean isClientClosed=false;
	public ServerThread(Socket clientSocket, Hashtable clientDataHash, Hashtable clientNameHash,
			Hashtable chessPeerHash, ServerMsgPanel serverMsgPanel) {
		// TODO Auto-generated constructor stub
		this.chessPeerHash=chessPeerHash;
		this.clientDataHash=clientDataHash;
		this.clientNameHash=clientNameHash;
		this.serverMsgPanel=serverMsgPanel;
		this.clientSocket=clientSocket;
	}
//	处理客户端发来的消息
	public void dealWithMsg(String msgReceived) {
		String clientName=clientNameHash.get(clientSocket);
		String peerName;
		if(msgReceived.startsWith("/")) {
			// 收到的信息为更新用户列表 
			if(msgReceived.equals("/list")) {
				serverMsgPanel.msgTextArea.append(clientName+"的信息为更新用户列表 \n");
				Feedback(getUserList());
			}
			// 收到的信息为创建游戏
			else if(msgReceived.startsWith("/creategame [inchess]")) {
				String gameCreaterName=msgReceived.substring(msgReceived.indexOf("]")+1);
				serverMsgPanel.msgTextArea.append(gameCreaterName+"的信息为创建游戏 \n");
				synchronized (clientNameHash) {
					// 将用户端口放到用户列表中
					clientNameHash.put(clientSocket, msgReceived.substring(msgReceived.indexOf(" ")+1));
				}
				synchronized (chessPeerHash) {
					// 将主机设置为等待状态 
					chessPeerHash.put(gameCreaterName, "wait");
				}
				sendGamePeerMsg(gameCreaterName,"/OK");
				sendPublicMsg(getUserList());
			}
			// 收到的信息为放弃创建游戏
			else if(msgReceived.startsWith("/giveupcreategame ")) {
				serverMsgPanel.msgTextArea.append(clientName+"的信息为放弃创建游戏 \n");
				String createName=msgReceived.substring(msgReceived.indexOf(" ")+1);
				closeClient();
				sendGamePeerMsg(createName,"/giveupcreategame");
				
			}			
			// 收到的信息为加入游戏时 
			else if(msgReceived.startsWith("/joingame ")) {
				StringTokenizer userTokens=new StringTokenizer(msgReceived," ");
				String userToken;
				String gameCreatorName; 
				String gamePaticipantName; 
				String[]playerNames={"0","0"};
				int nameIndex=0;
				while(userTokens.hasMoreTokens()) {
					userToken=(String)userTokens.nextToken(" ");
					if(nameIndex>=1&&nameIndex<=2) {
						// 取得游戏者命
						playerNames[nameIndex-1]=userToken;
					}
					nameIndex++;
				}
				gameCreatorName=playerNames[0];
				gamePaticipantName=playerNames[1];
				serverMsgPanel.msgTextArea.append(clientName+"的信息为加入游戏 创建者"+gameCreatorName+" 加入者"+gamePaticipantName+"\n");
				if(chessPeerHash.containsKey(gameCreatorName)&&chessPeerHash.get(gameCreatorName).equals("wait")) {
					// 游戏已创建
					synchronized (clientNameHash) {
						clientNameHash.put(clientSocket, "[inchess]"+gamePaticipantName);
					}
					synchronized (chessPeerHash) {
						// 增加或修改游戏创建者与游戏加入者的名称的对应
						chessPeerHash.put(gameCreatorName, gamePaticipantName);
					}
					sendPublicMsg(getUserList());
					// 发送信息给游戏加入者
					sendGamePeerMsg(gamePaticipantName,"/peer "+"[inchess]"+gameCreatorName);
					// 发送游戏给游戏创建者
					sendGamePeerMsg(gameCreatorName,"/peer "+"[inchess]"+gamePaticipantName);
				}else {
					// 若游戏未创建则拒绝加入游戏 
					sendGamePeerMsg(gamePaticipantName,"/reject");
					closeClient();
					
				}
			}
			// 收到的信息为游戏中时 ,处理棋盘下棋同步
			else if(msgReceived.startsWith("/[inchess]")){
				int firstLocation=0,lastLocation;
				lastLocation=msgReceived.indexOf(" ",0);
				peerName=msgReceived.substring(firstLocation+1,lastLocation);
				msgReceived=msgReceived.substring(lastLocation+1);
				serverMsgPanel.msgTextArea.append(peerName+"下棋中 \n");
				if(sendGamePeerMsg(peerName,msgReceived)) {
					Feedback("/error");
				}
			}
			// 收到的信息为放弃游戏时
			else if(msgReceived.startsWith("/giveup ")) {
				serverMsgPanel.msgTextArea.append(clientName+"的信息为放弃游戏 \n");
				String chessClientName=msgReceived.substring(8);
				if(chessPeerHash.containsKey(chessClientName)&&!((String)chessPeerHash.get(chessClientName)).equals("wait")){
					// 胜利方为游戏加入者，发送胜利信息 
				   sendGamePeerMsg((String) chessPeerHash.get(chessClientName), "/youwin"); 
				    
				   synchronized (chessPeerHash) 
				   { // 删除退出游戏的用户 
				   chessPeerHash.remove(chessClientName); 
				   } 
				}
				if(chessPeerHash.containsValue(chessClientName)){
					
					sendGamePeerMsg((String)getHashKey(chessPeerHash,chessClientName),"/youwin");
					synchronized (chessPeerHash) {
						chessPeerHash.remove((String)getHashKey(chessPeerHash,chessClientName));
					}
				}	
			}
			// 收到的信息为其它命令时 
			else { 
			    int lastLocation = msgReceived.indexOf(" ", 0); 
			    if (lastLocation == -1) { 
			    	Feedback("无效命令"); 
			     return; 
			    } 
			    //目标用户聊天
			    peerName=msgReceived.substring(1, lastLocation);
			    String message=msgReceived.substring(lastLocation+1,msgReceived.length());
			    message=clientName+">"+peerName+" "+message;
			    serverMsgPanel.msgTextArea.append(clientName+"与"+peerName+"正在聊天"+"\n");
			    try {
					DataOutputStream dataOutputStream=new DataOutputStream(clientSocket.getOutputStream());
					dataOutputStream.writeUTF(message);
				} catch (IOException e) {
				}
			    sendGamePeerMsg(peerName, message);
			    serverMsgPanel.msgTextArea.setCaretPosition(serverMsgPanel.msgTextArea.getText().length());
			}
		}
		else {
			//公屏聊天
			msgReceived=clientNameHash.get(clientSocket)+">"+msgReceived;
			serverMsgPanel.msgTextArea.append(msgReceived+"\n");
			sendPublicMsg(msgReceived);
			serverMsgPanel.msgTextArea.setCaretPosition(serverMsgPanel.msgTextArea.getText().length());
		}
	}
	private void closeClient() {
		 serverMsgPanel.msgTextArea.append("用户断开连接:" + clientSocket + "\n"); 
		 synchronized (chessPeerHash) 
		 { //如果是游戏客户端主机 
		  if (chessPeerHash.containsKey(clientNameHash.get(clientSocket))) 
		  { 
		  chessPeerHash.remove((String) clientNameHash.get(clientSocket)); 
		  } 
		  if (chessPeerHash.containsValue(clientNameHash.get(clientSocket))) 
		  { 
		  chessPeerHash.put((String) getHashKey(chessPeerHash, 
		   (String) clientNameHash.get(clientSocket)), 
		   "tobeclosed"); 
		  } 
		 } 
		 synchronized (clientDataHash) 
		 { // 删除客户数据 
		  clientDataHash.remove(clientSocket); 
		 } 
		 synchronized (clientNameHash) 
		 { // 删除客户数据 
		  clientNameHash.remove(clientSocket); 
		 } 
		 sendPublicMsg(getUserList()); 
		 serverMsgPanel.statusLabel.setText("当前连接数:" + clientDataHash.size()); 
		 try
		 { 
		  clientSocket.close(); 
		 } 
		 catch (IOException exx) 
		 { 
		  exx.printStackTrace(); 
		 } 
		 isClientClosed = true; 
	}
	public void sendPublicMsg(String publicMsg) {
		synchronized (clientDataHash) {
			for(Enumeration enu=clientDataHash.elements();enu.hasMoreElements();) {
				DataOutputStream outputStream=(DataOutputStream)enu.nextElement();
				try {
					outputStream.writeUTF(publicMsg);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private boolean sendGamePeerMsg(String gamePeerTarget, String gamePeerMsg) {
		for(Enumeration enu=clientDataHash.keys();enu.hasMoreElements();) {
			Socket userClient=(Socket)enu.nextElement();
			if(gamePeerTarget.equals((String)clientNameHash.get(userClient))
					&&!gamePeerTarget.equals((String)clientNameHash.get(clientSocket))) {
				synchronized (clientDataHash) {
					DataOutputStream dataOutputStream=(DataOutputStream)clientDataHash.get(userClient);
					try {
						dataOutputStream.writeUTF(gamePeerMsg);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return false;
			}
		}
		return true;
	}
	// 发送反馈信息给连接到主机的人 
	public void Feedback(String feedBackMsg) {
		synchronized (clientDataHash){ 
		  DataOutputStream outputData = (DataOutputStream) clientDataHash.get(clientSocket); 
		  try
		  { 
		  outputData.writeUTF(feedBackMsg); 
		  } 
		  catch (Exception eb) 
		  { 
		  eb.printStackTrace(); 
		  } 
		} 
	}
	// 取得用户列表 
	public String getUserList() {
		String userList="/userlist";
		for(Enumeration enu=clientNameHash.elements();enu.hasMoreElements();) {
			userList=userList+" "+(String)enu.nextElement();
		}
		return userList;
	}
	// 根据value值从Hashtable中取得相应的key 
	 public Object getHashKey(Hashtable targetHash, Object hashValue) 
	 { 
		 Object hashKey; 
		 for (Enumeration enu = targetHash.keys(); enu.hasMoreElements();) 
		 { 
		  hashKey = (Object) enu.nextElement(); 
		  if (hashValue.equals((Object) targetHash.get(hashKey))) 
			  return hashKey; 
		 } 
		 return null; 
	 }
	 public void sendInitMsg() 
	 { 
		 sendPublicMsg(getUserList()); 
		 Feedback("/yourname " + (String) clientNameHash.get(clientSocket)); 
		 Feedback("Java 五子棋客户端"); 
	 } 
	public void run() {
		DataInputStream inputStream;
		synchronized (clientDataHash) {
			serverMsgPanel.statusLabel.setText("当前连接数："+clientDataHash.size());
		}
		try {
			inputStream=new DataInputStream(clientSocket.getInputStream());
			sendInitMsg();
			while(true) {
				String message=inputStream.readUTF();
				dealWithMsg(message);
				if(isClientClosed)return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if(!isClientClosed) {
				closeClient();
				isClientClosed=true;
			}
		}
		
		
	}

}
