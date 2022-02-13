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
	// 黑棋x,y轴坐标位数组
	public int chessBlack_XPOS[]=new int [200];
	public int chessBlack_YPOS[]=new int [200];
	// 白棋x、y轴坐标位数组
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
	public TextField statusText=new TextField("请连接服务器!");
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
	// 连接到主机 
	public boolean connectServer(String serverIP,int serverPort)throws Exception {
		try {
			if(chessSocket!=null)return true;
			else {
				chessSocket=new Socket(serverIP,serverPort);
				inputStream=new DataInputStream(chessSocket.getInputStream());
				outputStream=new DataOutputStream(chessSocket.getOutputStream());
				padThread.setName("棋盘线程"+num++);
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
		statusText.setText("清空棋盘");
		add(statusText);
		// 将黑棋的位置设置到零点
		for(int i=0;i<=chessBlackCount;i++) {
			chessBlack_XPOS[i]=0;
			chessBlack_YPOS[i]=0;
		}
		// 将白棋的位置设置到零点 
		for(int i=0;i<=chessBlackCount;i++) {
			chessWhite_XPOS[i]=0;
			chessWhite_YPOS[i]=0;
		}
		// 清空棋盘上的黑棋数 
		 chessBlackCount = 0; 
		 // 清空棋盘上的白棋数 
		 chessWhiteCount = 0; 
	}
	 // 设定胜利时的棋盘状态 
	public void setWinStatus(int winChessColor) {
		removeAll();
		add(statusText);
		// 将黑棋的位置设置到零点
		for(int i=0;i<=chessBlackCount;i++) {
			chessBlack_XPOS[i]=0;
			chessBlack_YPOS[i]=0;
		}
		// 将白棋的位置设置到零点 
		for(int i=0;i<=chessBlackCount;i++) {
			chessWhite_XPOS[i]=0;
			chessWhite_YPOS[i]=0;
		}
		// 清空棋盘上的黑棋数 
		 chessBlackCount = 0; 
		 // 清空棋盘上的白棋数 
		 chessWhiteCount = 0; 
		// 黑棋胜	
		if(winChessColor==1) {
			chessBlackWinCount++;
			statusText.setText("黑棋胜利，黑：白 "+chessBlackWinCount+":"+chessWhiteWinCount+"游戏重启，等待白方。。。\n");
			userChatPad.chatTextArea.append("黑棋胜利，黑：白 "+chessBlackWinCount+":"+chessWhiteWinCount+"游戏重启，等待白方。。。\n");
		}
		else if(winChessColor==0) {
			chessWhiteWinCount++;
			statusText.setText("白棋胜利，黑：白 "+chessBlackWinCount+":"+chessWhiteWinCount+"游戏重启，等待黑方。。。\n");
			userChatPad.chatTextArea.append("黑棋胜利，黑：白 "+chessBlackWinCount+":"+chessWhiteWinCount+"游戏重启，等待白方。。。\n");
		}
	}
	
	// 取得指定棋子的位置
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
	
	// 判断当前状态是否为胜利状态
	public boolean checkWinStatus(int x,int y,int chessColor) {
		int chessLinkedCount=1;// 连接棋子数
		int chessLinkedCompare=1;// 用于比较是否要继续遍历一个棋子的相邻网格
		int chesstoCompareIndex=0;// 要比较的棋子在数组中的索引位置
		int closeGrid=1;// 相邻网格的位置
		//右左，上下，右上左下，右下左上
		int pos[][]= {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,-1},{1,-1},{-1,1}};
		if(chessColor==1) {
			//8个方向
			for(int i=0;i<8;i++) {
				// 遍历相邻4个网格
				for(closeGrid=1;closeGrid<=4;closeGrid++) {
					// 遍历棋盘上所有黑棋子 
					for(chesstoCompareIndex=0;chesstoCompareIndex<=chessBlackCount;chesstoCompareIndex++) {
						if(((x+closeGrid*pos[i][0])*20==chessBlack_XPOS[chesstoCompareIndex])
								&&(y+closeGrid*pos[i][1])*20==chessBlack_YPOS[chesstoCompareIndex]) {
							// 判断当前下的棋子的右边4个棋子是否都为黑棋
							chessLinkedCount++;
							if(chessLinkedCount==5)return true;
							else break;
						}
					}
				//中途没有找到黑棋直接退出方向
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
			//8个方向
			for(int i=0;i<8;i++) {
				// 遍历相邻4个网格
				for(closeGrid=1;closeGrid<=4;closeGrid++) {
					// 遍历棋盘上所有白棋子 
					for(chesstoCompareIndex=0;chesstoCompareIndex<=chessWhiteCount;chesstoCompareIndex++) {
						if(((x+closeGrid*pos[i][0])*20==chessWhite_XPOS[chesstoCompareIndex])
								&&(y+closeGrid*pos[i][1])*20==chessWhite_YPOS[chesstoCompareIndex]) {
							// 判断当前下的棋子的右边4个棋子是否都为白棋
							chessLinkedCount=chessLinkedCount+1;
							if(chessLinkedCount==5)return true;
							else break;
						}
					}
//				中途没有找到白棋直接退出右方向
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
	
	// 画棋盘 
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
	// 画棋子
	public void paintPoint(int x,int y,int chessColor) {
		System.out.println(" 画棋子");
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
					statusText.setText("黑 （第"+chessBlackCount+"步）坐标 "+x+" "+y+",轮到白方");
				}else {
					setWinStatus(1);
				}
			}else if(chessColor==0) {
				this.add(white);
				white.setBounds(x*20-7, y*20-7, 16, 16);
				if(!isWinned) {
					statusText.setText("白 （第"+chessBlackCount+"步）坐标 "+x+" "+y+",轮到黑方");
				}else {
					setWinStatus(0);
				}
			}
			isMouseEnable=false;
		}
	}
	// 画网络棋盘 
	public void paintNetPad(int x,int y,int chessColor) {
		PointBlack black=new PointBlack(this);
		PointWhite white=new PointWhite(this);
		setLocation(x, y, chessColor);
		System.out.println(" 画网络棋盘");
		isWinned=checkWinStatus(x, y, chessColor);
		isMouseEnable=true;
		if(chessColor==1) {
			this.add(black);
			black.setBounds(x*20-7, y*20-7, 16, 16);
			if(!isWinned) {
				statusText.setText("黑 （第"+chessBlackCount+"步）坐标 "+x+" "+y+",轮到白方");
				
			}else {
				padThread.sendMessage("/"+chessPeerName+" /victory "+chessColor);
				setWinStatus(1);
			}
		}else if(chessColor==0) {
			this.add(white);
			white.setBounds(x*20-7, y*20-7, 16, 16);
			if(!isWinned) {
				statusText.setText("白（第"+chessBlackCount+"步）坐标 "+x+" "+y+",轮到黑方");
			}else {
				padThread.sendMessage("/"+chessPeerName+" /victory "+chessColor);
				setWinStatus(0);
			}
		}
	}
	// 捕获下棋事件 
	public void mousePressed(MouseEvent e) {
		if(e.getModifiers()==InputEvent.BUTTON1_MASK) {
			chessX_POS=(int)e.getX();
			chessY_POS=(int)e.getY();
			int a=(chessX_POS+10)/20;
			int b=(chessY_POS+10)/20;
			if(chessX_POS/20<2||chessY_POS/20<2||chessX_POS/20>19||chessY_POS/20>19) 
			{System.out.println(chessX_POS+" "+chessY_POS+"下棋位置不正确");}
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
