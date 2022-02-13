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
	Hashtable<Socket, DataOutputStream> clientDataHash=new Hashtable(50);//���ͻ����׽ӿں��������
	Hashtable<Socket, String> clientNameHash=new Hashtable(50);//���ͻ����׽ӿںͿͻ�����
	Hashtable<String, String> chessPeerHash=new Hashtable(50);//����Ϸ�����ߺ���Ϸ�����߰� ,Ҫ����ƥ����ֵΪwait
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
//	����ͻ��˷�������Ϣ
	public void dealWithMsg(String msgReceived) {
		String clientName=clientNameHash.get(clientSocket);
		String peerName;
		if(msgReceived.startsWith("/")) {
			// �յ�����ϢΪ�����û��б� 
			if(msgReceived.equals("/list")) {
				serverMsgPanel.msgTextArea.append(clientName+"����ϢΪ�����û��б� \n");
				Feedback(getUserList());
			}
			// �յ�����ϢΪ������Ϸ
			else if(msgReceived.startsWith("/creategame [inchess]")) {
				String gameCreaterName=msgReceived.substring(msgReceived.indexOf("]")+1);
				serverMsgPanel.msgTextArea.append(gameCreaterName+"����ϢΪ������Ϸ \n");
				synchronized (clientNameHash) {
					// ���û��˿ڷŵ��û��б���
					clientNameHash.put(clientSocket, msgReceived.substring(msgReceived.indexOf(" ")+1));
				}
				synchronized (chessPeerHash) {
					// ����������Ϊ�ȴ�״̬ 
					chessPeerHash.put(gameCreaterName, "wait");
				}
				sendGamePeerMsg(gameCreaterName,"/OK");
				sendPublicMsg(getUserList());
			}
			// �յ�����ϢΪ����������Ϸ
			else if(msgReceived.startsWith("/giveupcreategame ")) {
				serverMsgPanel.msgTextArea.append(clientName+"����ϢΪ����������Ϸ \n");
				String createName=msgReceived.substring(msgReceived.indexOf(" ")+1);
				closeClient();
				sendGamePeerMsg(createName,"/giveupcreategame");
				
			}			
			// �յ�����ϢΪ������Ϸʱ 
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
						// ȡ����Ϸ����
						playerNames[nameIndex-1]=userToken;
					}
					nameIndex++;
				}
				gameCreatorName=playerNames[0];
				gamePaticipantName=playerNames[1];
				serverMsgPanel.msgTextArea.append(clientName+"����ϢΪ������Ϸ ������"+gameCreatorName+" ������"+gamePaticipantName+"\n");
				if(chessPeerHash.containsKey(gameCreatorName)&&chessPeerHash.get(gameCreatorName).equals("wait")) {
					// ��Ϸ�Ѵ���
					synchronized (clientNameHash) {
						clientNameHash.put(clientSocket, "[inchess]"+gamePaticipantName);
					}
					synchronized (chessPeerHash) {
						// ���ӻ��޸���Ϸ����������Ϸ�����ߵ����ƵĶ�Ӧ
						chessPeerHash.put(gameCreatorName, gamePaticipantName);
					}
					sendPublicMsg(getUserList());
					// ������Ϣ����Ϸ������
					sendGamePeerMsg(gamePaticipantName,"/peer "+"[inchess]"+gameCreatorName);
					// ������Ϸ����Ϸ������
					sendGamePeerMsg(gameCreatorName,"/peer "+"[inchess]"+gamePaticipantName);
				}else {
					// ����Ϸδ������ܾ�������Ϸ 
					sendGamePeerMsg(gamePaticipantName,"/reject");
					closeClient();
					
				}
			}
			// �յ�����ϢΪ��Ϸ��ʱ ,������������ͬ��
			else if(msgReceived.startsWith("/[inchess]")){
				int firstLocation=0,lastLocation;
				lastLocation=msgReceived.indexOf(" ",0);
				peerName=msgReceived.substring(firstLocation+1,lastLocation);
				msgReceived=msgReceived.substring(lastLocation+1);
				serverMsgPanel.msgTextArea.append(peerName+"������ \n");
				if(sendGamePeerMsg(peerName,msgReceived)) {
					Feedback("/error");
				}
			}
			// �յ�����ϢΪ������Ϸʱ
			else if(msgReceived.startsWith("/giveup ")) {
				serverMsgPanel.msgTextArea.append(clientName+"����ϢΪ������Ϸ \n");
				String chessClientName=msgReceived.substring(8);
				if(chessPeerHash.containsKey(chessClientName)&&!((String)chessPeerHash.get(chessClientName)).equals("wait")){
					// ʤ����Ϊ��Ϸ�����ߣ�����ʤ����Ϣ 
				   sendGamePeerMsg((String) chessPeerHash.get(chessClientName), "/youwin"); 
				    
				   synchronized (chessPeerHash) 
				   { // ɾ���˳���Ϸ���û� 
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
			// �յ�����ϢΪ��������ʱ 
			else { 
			    int lastLocation = msgReceived.indexOf(" ", 0); 
			    if (lastLocation == -1) { 
			    	Feedback("��Ч����"); 
			     return; 
			    } 
			    //Ŀ���û�����
			    peerName=msgReceived.substring(1, lastLocation);
			    String message=msgReceived.substring(lastLocation+1,msgReceived.length());
			    message=clientName+">"+peerName+" "+message;
			    serverMsgPanel.msgTextArea.append(clientName+"��"+peerName+"��������"+"\n");
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
			//��������
			msgReceived=clientNameHash.get(clientSocket)+">"+msgReceived;
			serverMsgPanel.msgTextArea.append(msgReceived+"\n");
			sendPublicMsg(msgReceived);
			serverMsgPanel.msgTextArea.setCaretPosition(serverMsgPanel.msgTextArea.getText().length());
		}
	}
	private void closeClient() {
		 serverMsgPanel.msgTextArea.append("�û��Ͽ�����:" + clientSocket + "\n"); 
		 synchronized (chessPeerHash) 
		 { //�������Ϸ�ͻ������� 
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
		 { // ɾ���ͻ����� 
		  clientDataHash.remove(clientSocket); 
		 } 
		 synchronized (clientNameHash) 
		 { // ɾ���ͻ����� 
		  clientNameHash.remove(clientSocket); 
		 } 
		 sendPublicMsg(getUserList()); 
		 serverMsgPanel.statusLabel.setText("��ǰ������:" + clientDataHash.size()); 
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
	// ���ͷ�����Ϣ�����ӵ��������� 
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
	// ȡ���û��б� 
	public String getUserList() {
		String userList="/userlist";
		for(Enumeration enu=clientNameHash.elements();enu.hasMoreElements();) {
			userList=userList+" "+(String)enu.nextElement();
		}
		return userList;
	}
	// ����valueֵ��Hashtable��ȡ����Ӧ��key 
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
		 Feedback("Java ������ͻ���"); 
	 } 
	public void run() {
		DataInputStream inputStream;
		synchronized (clientDataHash) {
			serverMsgPanel.statusLabel.setText("��ǰ��������"+clientDataHash.size());
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
