package client;

import java.util.StringTokenizer;

public class ClientThread extends Thread{
	public ClientServer clientServer;
	public ClientThread(ClientServer clientServer) {
		this.clientServer=clientServer;
	}
	public void dealWithMsg(String msgReceived) {
		 // 若取得的信息为用户列表
		if(msgReceived.startsWith("/userlist ")) {
			StringTokenizer userToken=new StringTokenizer(msgReceived," ");
			int userNum=0;
			clientServer.userListPad.userList.removeAll();
			clientServer.userInputPad.userChoice.removeAllItems();
			clientServer.userInputPad.userChoice.addItem("所有用户");
			while(userToken.hasMoreTokens()) {
				String user=(String)userToken.nextToken(" ");
				if(userNum>0&&!user.startsWith("[inchess]")) {
					clientServer.userListPad.userList.add(user);
					clientServer.userInputPad.userChoice.addItem(user);
				}
				userNum++;
			}
			clientServer.userInputPad.userChoice.setSelectedIndex(0);
		}
		// 收到的信息为用户名时 
		else if(msgReceived.startsWith("/yourname ")) {
			clientServer.clientName=msgReceived.substring(10);
			clientServer.setTitle("网路小游戏 用户名： "+clientServer.clientName);
		}
		// 收到的信息为拒绝用户时 
		else if (msgReceived.equals("/reject")) {
				clientServer.pad.statusText.setText("不能加入游戏!");
				clientServer.userControlPad.cancelButton.setEnabled(false);
				clientServer.userControlPad.joinButton.setEnabled(true);
				clientServer.userControlPad.createButton.setEnabled(true);
		}
		// 收到信息为游戏匹配成功时 
		else if (msgReceived.startsWith("/peer ")) {
			clientServer.pad.chessPeerName=msgReceived.substring(6);
			clientServer.start=true;
			if(clientServer.isCreator) {
				clientServer.pad.chessColor=1;
				clientServer.pad.isMouseEnable=true;
				clientServer.userChatPad.chatTextArea.append("您是黑方\n");
				clientServer.pad.statusText.setText("黑方下...");
			}
			else if(clientServer.isParticipant) {
				clientServer.pad.chessColor=0;
				clientServer.userChatPad.chatTextArea.append("您是白方\n");
				clientServer.pad.statusText.setText("游戏加入，等待黑方。。。");
			}
		}
		// 收到信息为胜利信息 
		else if (msgReceived.equals("/youwin")) {
			clientServer.isOnChess=false;
			clientServer.pad.setWinStatus(clientServer.pad.chessColor);
			clientServer.pad.statusText.setText("己方胜利，对手退出");
			clientServer.pad.isMouseEnable=false;
		}
		
		 // 收到信息为成功创建游戏 
		else if (msgReceived.equals("/OK")) {
			clientServer.pad.statusText.setText("游戏创建等待对手");
		}
		else if(msgReceived.equals("/giveupcreategame")){
			clientServer.pad.statusText.setText("放弃创建游戏");
		}
		// 收到信息错误 
		else if (msgReceived.equals("/error")) {
			clientServer.userChatPad.chatTextArea.setText("错误，退出程序.\n");
		}
		//聊天消息
		else {
			clientServer.userChatPad.chatTextArea.append(msgReceived+"\n");
			clientServer.userChatPad.chatTextArea.setCaretPosition(clientServer.userChatPad.chatTextArea.getText().length());
		}
	}
	@Override
	public void run() {
		String message="";
		try {
			while(true) {
				message=clientServer.inputStream.readUTF();
				dealWithMsg(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
}
