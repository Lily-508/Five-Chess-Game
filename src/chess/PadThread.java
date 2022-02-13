package chess;

import java.util.StringTokenizer;

public class PadThread extends Thread{
	Pad currPad;
	public PadThread(Pad currPad) {
		this.currPad=currPad;
	}
	public void dealWithMsg(String msgReceived) {
		if(msgReceived.startsWith("/chess ")) {
			StringTokenizer userMsgToken=new StringTokenizer(msgReceived," ");
			// ��ʾ������Ϣ�����顢0����Ϊ��x���ꣻ1����λ��y���ꣻ2����λ��������ɫ 
			  String[] chessInfo = { "-1", "-1", "0" }; 
			  int i=0;
			  String chessInfoToken;
			  while(userMsgToken.hasMoreTokens()) {
				  chessInfoToken=(String)userMsgToken.nextToken(" ");
				  if(i>=1&&i<=3) {
					  chessInfo[i-1]=chessInfoToken;
				  }
				  i++;
			  }
			  currPad.paintNetPad(Integer.parseInt(chessInfo[0]), Integer.parseInt(chessInfo[1]), Integer.parseInt(chessInfo[2]));
		}else if(msgReceived.startsWith("/yourname ")) {
			currPad.chessSelfName=msgReceived.substring(10);
		}else if(msgReceived.equals("/error")){
			currPad.statusText.setText("�û������ڣ������¼���");
		}
	}
	public void sendMessage(String sendMessage) {
		try {
			currPad.outputStream.writeUTF(sendMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void run() {
		String msgReceived="";
		try {
			while(true) {
				msgReceived=currPad.inputStream.readUTF();
				dealWithMsg(msgReceived);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	

}
