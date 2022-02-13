package chess;

import java.awt.*; 
import java.awt.event.*; 
import java.io.*; 
import java.net.*;

import user.UserChatPanel; 

public class Pad extends Panel implements MouseListener,ActionListener{
	public boolean isMouseEnable=false;
	public boolean isWinned=false;
	public boolean isPlaying=false;
	public int chessX_POS=-1;
	public int chessY_POS=-1;
	public int chessColor=1;
	// ����x,y������λ����
	public int chessBlack_XPOS[]=new int [200];
	public int chessBlack_YPOS[]=new int [200];
	// ����x��y������λ����
	public int chessWhite_XPOS[]=new int [200];
	public int chessWhite_YPOS[]=new int [200];
	public int chessBlackCount=0;
	public int chessWhiteCount=0;
	public int chessBlackWinCount=0;
	public int chessWhiteWinCount=0;

	public Socket chessSocket=null;
	public DataInputStream inputStream;
	public DataOutputStream outputStream;
	public String chessSelfName=null;
	public String chessPeerName=null;
	public String host=null;
	public final int PORT=2021;
	public TextField statusText=new TextField("�����ӷ�����!");
	UserChatPanel userChatPad;
	public PadThread padThread=new PadThread(this);
	
	public Pad(UserChatPanel userChatPad) {
		this.userChatPad=userChatPad;
		setSize(440,440);
		setLayout(null);
		setBackground(new Color(205, 133, 63));
		addMouseListener(this);
		add(statusText); 
		statusText.setBounds(40, 5, 360, 24);
		statusText.setEditable(false);
	}
	int num=1;
	// ���ӵ����� 
	public boolean connectServer(String serverIP,int serverPort)throws Exception {
		try {
			if(chessSocket!=null)return true;
			else {
				chessSocket=new Socket(serverIP,serverPort);
				inputStream=new DataInputStream(chessSocket.getInputStream());
				outputStream=new DataOutputStream(chessSocket.getOutputStream());
				padThread.setName("�����߳�"+num++);
				padThread.start();
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			statusText.setText("connect failed\n");
		}
		return false;
	}
	public void clearPad() {
		removeAll();
		statusText.setText("�������");
		add(statusText);
		// �������λ�����õ����
		for(int i=0;i<=chessBlackCount;i++) {
			chessBlack_XPOS[i]=0;
			chessBlack_YPOS[i]=0;
		}
		// �������λ�����õ���� 
		for(int i=0;i<=chessBlackCount;i++) {
			chessWhite_XPOS[i]=0;
			chessWhite_YPOS[i]=0;
		}
		// ��������ϵĺ����� 
		 chessBlackCount = 0; 
		 // ��������ϵİ����� 
		 chessWhiteCount = 0; 
	}
	 // �趨ʤ��ʱ������״̬ 
	public void setWinStatus(int winChessColor) {
		removeAll();
		add(statusText);
		// �������λ�����õ����
		for(int i=0;i<=chessBlackCount;i++) {
			chessBlack_XPOS[i]=0;
			chessBlack_YPOS[i]=0;
		}
		// �������λ�����õ���� 
		for(int i=0;i<=chessBlackCount;i++) {
			chessWhite_XPOS[i]=0;
			chessWhite_YPOS[i]=0;
		}
		// ��������ϵĺ����� 
		 chessBlackCount = 0; 
		 // ��������ϵİ����� 
		 chessWhiteCount = 0; 
		// ����ʤ	
		if(winChessColor==1) {
			chessBlackWinCount++;
			statusText.setText("����ʤ�����ڣ��� "+chessBlackWinCount+":"+chessWhiteWinCount+"��Ϸ�������ȴ��׷�������\n");
			userChatPad.chatTextArea.append("����ʤ�����ڣ��� "+chessBlackWinCount+":"+chessWhiteWinCount+"��Ϸ�������ȴ��׷�������\n");
		}
		else if(winChessColor==0) {
			chessWhiteWinCount++;
			statusText.setText("����ʤ�����ڣ��� "+chessBlackWinCount+":"+chessWhiteWinCount+"��Ϸ�������ȴ��ڷ�������\n");
			userChatPad.chatTextArea.append("����ʤ�����ڣ��� "+chessBlackWinCount+":"+chessWhiteWinCount+"��Ϸ�������ȴ��׷�������\n");
		}
	}
	
	// ȡ��ָ�����ӵ�λ��
	public void setLocation(int x,int y,int chessColor) {
		if(chessColor==1) {
			chessBlack_XPOS[chessBlackCount]=x*20;
			chessBlack_YPOS[chessBlackCount]=y*20;
			chessBlackCount++;
		}else if(chessColor==0) {
			chessWhite_XPOS[chessWhiteCount]=x*20;
			chessWhite_YPOS[chessWhiteCount]=y*20;
			chessWhiteCount++;
		}
	}
	
	// �жϵ�ǰ״̬�Ƿ�Ϊʤ��״̬
	public boolean checkWinStatus(int x,int y,int chessColor) {
		int chessLinkedCount=1;// ����������
		int chessLinkedCompare=1;// ���ڱȽ��Ƿ�Ҫ��������һ�����ӵ���������
		int chesstoCompareIndex=0;// Ҫ�Ƚϵ������������е�����λ��
		int closeGrid=1;// ���������λ��
		//�������£��������£���������
		int pos[][]= {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1},{1,-1},{-1,1}};
		if(chessColor==1) {
			//8������
			for(int i=0;i<8;i++) {
				// ��������4������
				for(closeGrid=1;closeGrid<=4;closeGrid++) {
					// �������������к����� 
					for(chesstoCompareIndex=0;chesstoCompareIndex<=chessBlackCount;chesstoCompareIndex++) {
						if(((x+closeGrid*pos[i][0])*20==chessBlack_XPOS[chesstoCompareIndex])
								&&(y+closeGrid*pos[i][1])*20==chessBlack_YPOS[chesstoCompareIndex]) {
							// �жϵ�ǰ�µ����ӵ��ұ�4�������Ƿ�Ϊ����
							chessLinkedCount++;
							if(chessLinkedCount==5)return true;
							else break;
						}
					}
				//��;û���ҵ�����ֱ���˳�����
					if(chessLinkedCount==chessLinkedCompare+1) {
						chessLinkedCompare++;
					}else break;
				}
				if((i+1)%2==0) {
					chessLinkedCount=1;
					chessLinkedCompare=1;
				}
			}
		}
		else if(chessColor==0) {
			//8������
			for(int i=0;i<8;i++) {
				// ��������4������
				for(closeGrid=1;closeGrid<=4;closeGrid++) {
					// �������������а����� 
					for(chesstoCompareIndex=0;chesstoCompareIndex<=chessWhiteCount;chesstoCompareIndex++) {
						if(((x+closeGrid*pos[i][0])*20==chessWhite_XPOS[chesstoCompareIndex])
								&&(y+closeGrid*pos[i][1])*20==chessWhite_YPOS[chesstoCompareIndex]) {
							// �жϵ�ǰ�µ����ӵ��ұ�4�������Ƿ�Ϊ����
							chessLinkedCount=chessLinkedCount+1;
							if(chessLinkedCount==5)return true;
							else break;
						}
					}
//				��;û���ҵ�����ֱ���˳��ҷ���
					if(chessLinkedCount==chessLinkedCompare+1) {
						chessLinkedCompare++;
					}else break;
				}
				if((i+1)%2==0) {
					chessLinkedCount=1;
					chessLinkedCompare=1;
				}
			}
		}
		return false;
	}
	
	// ������ 
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		for(int i=40;i<=400;i=i+20) {
			
			g.drawLine(40, i, 400, i);
			g.drawLine(i, 40, i, 400);
		}
		g.fillOval(97, 97, 6, 6);
		g.fillOval(337, 97, 6, 6);
		g.fillOval(97, 337, 6, 6);
		g.fillOval(337, 337, 6, 6);
		g.fillOval(217, 217, 6, 6);
	}
	// ������
	public void paintPoint(int x,int y,int chessColor) {
		System.out.println(" ������");
		if(isMouseEnable) {
		PointBlack black=new PointBlack(this);
		PointWhite white=new PointWhite(this);
			setLocation(x, y, chessColor);
			isWinned=checkWinStatus(x, y, chessColor);
			padThread.sendMessage("/"+chessPeerName+" /chess "+x+" "+y+" "+chessColor);
			if(chessColor==1) {	
				this.add(black);
				black.setBounds(x*20-7, y*20-7, 16, 16);
				
				if(!isWinned) {
					statusText.setText("�� ����"+chessBlackCount+"�������� "+x+" "+y+",�ֵ��׷�");
				}else {
					setWinStatus(1);
				}
			}else if(chessColor==0) {
				this.add(white);
				white.setBounds(x*20-7, y*20-7, 16, 16);
				if(!isWinned) {
					statusText.setText("�� ����"+chessBlackCount+"�������� "+x+" "+y+",�ֵ��ڷ�");
				}else {
					setWinStatus(0);
				}
			}
			isMouseEnable=false;
		}
	}
	// ���������� 
	public void paintNetPad(int x,int y,int chessColor) {
		PointBlack black=new PointBlack(this);
		PointWhite white=new PointWhite(this);
		setLocation(x, y, chessColor);
		System.out.println(" ����������");
		isWinned=checkWinStatus(x, y, chessColor);
		isMouseEnable=true;
		if(chessColor==1) {
			this.add(black);
			black.setBounds(x*20-7, y*20-7, 16, 16);
			if(!isWinned) {
				statusText.setText("�� ����"+chessBlackCount+"�������� "+x+" "+y+",�ֵ��׷�");
				
			}else {
				padThread.sendMessage("/"+chessPeerName+" /victory "+chessColor);
				setWinStatus(1);
			}
		}else if(chessColor==0) {
			this.add(white);
			white.setBounds(x*20-7, y*20-7, 16, 16);
			if(!isWinned) {
				statusText.setText("�ף���"+chessBlackCount+"�������� "+x+" "+y+",�ֵ��ڷ�");
			}else {
				padThread.sendMessage("/"+chessPeerName+" /victory "+chessColor);
				setWinStatus(0);
			}
		}
	}
	// ���������¼� 
	public void mousePressed(MouseEvent e) {
		if(e.getModifiers()==InputEvent.BUTTON1_MASK) {
			chessX_POS=(int)e.getX();
			chessY_POS=(int)e.getY();
			int a=(chessX_POS+10)/20;
			int b=(chessY_POS+10)/20;
			if(chessX_POS/20<2||chessY_POS/20<2||chessX_POS/20>19||chessY_POS/20>19) 
			{System.out.println(chessX_POS+" "+chessY_POS+"����λ�ò���ȷ");}
			else {
				for(int i=0;i<chessWhiteCount;i++) {
					if(chessWhite_XPOS[i]==a*20&&chessWhite_YPOS[i]==b*20)return ;
				}
				for(int i=0;i<chessBlackCount;i++) {
					if(chessBlack_XPOS[i]==a*20&&chessBlack_XPOS[i]==b*20)return ;
				}
				paintPoint(a, b, chessColor);
			}
		}
	}
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
