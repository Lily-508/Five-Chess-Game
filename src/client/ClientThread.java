package client;

import java.util.StringTokenizer;

public class ClientThread extends Thread{
	public ClientServer clientServer;
	public ClientThread(ClientServer clientServer) {
		this.clientServer=clientServer;
	}
	public void dealWithMsg(String msgReceived) {
		 // ��ȡ�õ���ϢΪ�û��б�
		if(msgReceived.startsWith("/userlist ")) {
			StringTokenizer userToken=new StringTokenizer(msgReceived," ");
			int userNum=0;
			clientServer.userListPad.userList.removeAll();
			clientServer.userInputPad.userChoice.removeAllItems();
			clientServer.userInputPad.userChoice.addItem("�����û�");
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
		// �յ�����ϢΪ�û���ʱ 
		else if(msgReceived.startsWith("/yourname ")) {
			clientServer.clientName=msgReceived.substring(10);
			clientServer.setTitle("��·С��Ϸ �û����� "+clientServer.clientName);
		}
		// �յ�����ϢΪ�ܾ��û�ʱ 
		else if (msgReceived.equals("/reject")) {
				clientServer.pad.statusText.setText("���ܼ�����Ϸ!");
				clientServer.userControlPad.cancelButton.setEnabled(false);
				clientServer.userControlPad.joinButton.setEnabled(true);
				clientServer.userControlPad.createButton.setEnabled(true);
		}
		// �յ���ϢΪ��Ϸƥ��ɹ�ʱ 
		else if (msgReceived.startsWith("/peer ")) {
			clientServer.pad.chessPeerName=msgReceived.substring(6);
			clientServer.start=true;
			if(clientServer.isCreator) {
				clientServer.pad.chessColor=1;
				clientServer.pad.isMouseEnable=true;
				clientServer.userChatPad.chatTextArea.append("���Ǻڷ�\n");
				clientServer.pad.statusText.setText("�ڷ���...");
			}
			else if(clientServer.isParticipant) {
				clientServer.pad.chessColor=0;
				clientServer.userChatPad.chatTextArea.append("���ǰ׷�\n");
				clientServer.pad.statusText.setText("��Ϸ���룬�ȴ��ڷ�������");
			}
		}
		// �յ���ϢΪʤ����Ϣ 
		else if (msgReceived.equals("/youwin")) {
			clientServer.isOnChess=false;
			clientServer.pad.setWinStatus(clientServer.pad.chessColor);
			clientServer.pad.statusText.setText("����ʤ���������˳�");
			clientServer.pad.isMouseEnable=false;
		}
		
		 // �յ���ϢΪ�ɹ�������Ϸ 
		else if (msgReceived.equals("/OK")) {
			clientServer.pad.statusText.setText("��Ϸ�����ȴ�����");
		}
		else if(msgReceived.equals("/giveupcreategame")){
			clientServer.pad.statusText.setText("����������Ϸ");
		}
		// �յ���Ϣ���� 
		else if (msgReceived.equals("/error")) {
			clientServer.userChatPad.chatTextArea.setText("�����˳�����.\n");
		}
		//������Ϣ
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
