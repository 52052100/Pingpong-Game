
package f;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;


public class Pingpongclient {
		private JFrame frame = new JFrame("Pingpong Game");

		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		private Timer t;
		private Pingpong a;
		private int score1 = 0;
		private int score2 = 0;
		
		public Pingpongclient()  {
			try {
				
				socket = new Socket("127.0.0.1", 3333);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				a = new Pingpong(out, score1, score2); 
				frame.add(a);
				frame.addKeyListener(a);
				frame.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						out.println("GO");
						
						if(a.ballx == 390) {
							t = new Timer(10, new TimerListener(a.getBall()));
							t.start();
						}
					}
				});
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
				
			}
				
				
			
			
		}

		public static void main(String args[])  {
			while (true) {
				
				Pingpongclient client = new Pingpongclient();
				client.frame.setSize(800, 550);
				client.frame.setLocationRelativeTo(null);
				client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				client.frame.setVisible(true);
				client.frame.setResizable(true);
				
				try {
					client.play();
					if (!client.wantsToPlayAgain()) {
						break;
					}
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
			}
		}

		
		class TimerListener implements ActionListener {
			private Pingpong.Ball b;

			public TimerListener(Pingpong.Ball b) {
				this.b = b;
			}

			public void actionPerformed(ActionEvent e) {
					b.ballMove();
					out.println("Ball Move: " + b.getX() + " " + b.getY());
					if (b.getX() > 750)
						{
						out.println("Player1 scored");
						t.stop();
					
						}
					if (b.getX() < 50)
						{
						out.println("Player2 scored");
						t.stop();
						
						}
				
				frame.repaint();
					
			}

		}
		public void play()  {
			String response;
			try {
				response = in.readLine();
				if (response.startsWith("WELCOME")) {
					char mark = response.charAt(8);
					out.println("PLAYER: " + mark );
					System.out.println(response);
					frame.setTitle("Ping-Pong Game Player " + mark);
					
				}
				
				while (true) {
					response = in.readLine();
					
					if (response.startsWith("UP ")) {
						String player = response.substring(3);
						a.moveUp(player);	
					}else if (response.startsWith("DOWN ")) {
						String player = response.substring(5);
						a.moveDown(player);	
					}else if (response.equals("GO")) {
							Pingpong.message = "";
							frame.repaint();
							
					
					}else if (response.startsWith("Paddle1 Move: ")) {
						String paddle1 = response.substring(14, response.length());
						
						int py1=  Integer.parseInt(paddle1);
						a.updatePaddle1(py1);
					} else if (response.startsWith("Paddle2 Move: ")) {
						String paddle2 = response.substring(14, response.length());
						
						int py2=  Integer.parseInt(paddle2);
						a.updatePaddle2(py2);
						
						
					} else if (response.startsWith("Ball Move: ")) {
						
						String ball = response.substring(11);
						String[] s = ball.split(" ");
						int ballx=Integer.parseInt(s[0]);
						int bally=Integer.parseInt(s[1]);
						
						a.updateBall(ballx,bally);
						
						
					} else if (response.startsWith("MESSAGE")) {
						Pingpong.message = response.substring(8);
						frame.repaint();
					} else if(response.startsWith("Player1: ")) {
						score1 = Integer.parseInt(response.substring(9));
						
						a.updateScore1(score1);
						a.getBall().setX(390);
						a.getBall().setY(210);
						a.updateBall(390, 210);
						Pingpong.message = "Click your mouse to start";
						frame.repaint();
					} else if(response.startsWith("Player2: ")) {
						score2 = Integer.parseInt(response.substring(9));
						
						a.updateScore2(score2);
						a.getBall().setX(390);
						a.getBall().setY(210);
						a.updateBall(390, 210);
						Pingpong.message = "Click your mouse to start";
						frame.repaint();
					} else if (response.equals("You Win!")) {
						Pingpong.message = response;
						frame.repaint();
						break;
					}else if (response.equals("You Lose!")) {
						Pingpong.message = response;
						frame.repaint();
						break;
					}
				}
				out.println("QUIT");

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		
		private boolean wantsToPlayAgain() {
			int response = JOptionPane.showConfirmDialog(frame, "Want to try again?", "Pingpong ",
					JOptionPane.YES_NO_OPTION);
			frame.dispose();
			return response == JOptionPane.YES_OPTION;
		}

	}
class Pingpong extends JPanel implements KeyListener {
	BufferedImage fireball;
	//Image fireball= new ImageIcon("/Users/jason/eclipse-workspace/610 hw/picture/fire_ball.png").getImage();
	private Ball ball = new Ball();
	public static String message = "";
	private Font mFont = new Font("TimesRoman",Font.BOLD,20);
	private Font sFont = new Font("TimesRoman",Font.BOLD,50);
	private PrintWriter out; 
	int score1;
	int score2;
	String player1 = "";
	String player2 = "";
	int py1;
	int py2;
	int ballx;
	int bally;
	public Pingpong(PrintWriter pw, int s1, int s2) {
		out = pw;
		score1 = s1;
		score2 = s2;
		py1=200;
		py2=200;
		ballx=ball.getX();
		bally=ball.getY();
		try {
			fireball=ImageIO.read(new URL("https://orig00.deviantart.net/1fad/f/2012/146/0/b/fire_ball_png_by_dbszabo1-d515um9.png"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public int getPY1(){
		return py1;
	}
	public int getPY2(){
		return py2;
	}
	public void setPY1(int py1) {
		this.py1=py1;
	}
	public void setPY2(int py2) {
		this.py2=py2;
	}

	public Ball getBall() {
		return ball;
	}

	
	
	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_UP) {
			out.println("UP");	
		}
		if (key == KeyEvent.VK_DOWN) {
			out.println("DOWN");	
		}		
	}

	public void keyReleased(KeyEvent e) {

	}

	
	public void paint(Graphics g) {
		super.paint(g);

		g.setFont(mFont);
		g.drawString(message, 275, 500);
		g.setColor(Color.black);

		g.fillRect(50, 50, 700, 400);
	    g.setColor(Color.white);
	    g.drawLine(400,50, 400,450 );
	    g.setColor(Color.red);
		g.setFont(sFont);
		g.drawString(Integer.toString(score1), 200, 100);
		g.drawString(Integer.toString(score2), 600, 100);
		g.setColor(Color.black);
		
	   
	    
	    this.getX();
	    this.getY();
	    g.drawImage(fireball, ballx, bally, 30,30,this);
	    //g.setColor(Color.red);
	    //g.fillOval(ballx, bally, 15, 15);
	    this.getPY1();
	    this.getPY2();
	    g.setColor(Color.red);
        g.fillRect(50,py1, 15, 80);
        g.setColor(Color.GREEN);
		g.fillRect(735, py2, 15, 80);
		this.repaint();
		
		
		
	}

	
	
	public void updatePaddle1(int py1) {
		this.py1=py1;
		this.setPY1(py1);
		this.repaint();
	}
	public void updatePaddle2(int py2) {
		this.py2=py2;
		this.setPY2(py2);
		this.repaint();
	}
	

	public void updateBall(int x,int y) {
	
		ballx=x;
		bally=y;
		this.repaint();
	}
	
	public void moveUp(String mark) {
		if(mark.equals("1")) {
			this.repaint();
			py1=py1-50;
			if(py1<=50)
				py1=50;
			
			out.println("Paddle1 Move: " + py1);
			
		} else if(mark.equals("2")) {
			
			this.repaint();
			py2=py2-50;
			if(py2<=50)
				py2=50;
			
			out.println("Paddle2 Move: " + py2);
		}
	}
	
	public void moveDown(String mark) {
		if(mark.equals("1")) {
			
			this.repaint();
			py1=py1+50;
			if(py1>=370)
				py1=370;
			
			out.println("Paddle1 Move: " + py1);
		} else if(mark.equals("2")) {
			
			this.repaint();
			py2=py2+50;
			if(py2>=370)
				py2=370;
			
			out.println("Paddle2 Move: " + py2);
		}
	}
	
	
	public void updateScore1(int s1) {
		score1 = s1;
	}
	public void updateScore2(int s2) {
		score2 = s2;
	}
	
class Ball {
		
		private int x;
		private int y;
		int m = 3;
		int n = 3;

		public Ball() {
			
			
			this.x = 390;
			this.y = 210;

		}
		

		public String toString() {
			return "(" + x + ". " + y + ")";
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		
		public void ballMove() {
		
			x += m;
			y += n;
				
			if(x <= 55 && y >= getPY1() && y <= getPY1()+80 )
					m = -m;
			
			
			if(x >= 720 && y >= getPY2() && y <= getPY2()+80)
					m = -m;
			
			if ( y >= 430) {
				n = -n;
			}
			
			if ( y <= 50) {
				n = -n;
			}

		}
	}


}

class Point {
	private int x;
	private int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public String toString() {   
		return  "" + y; 
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y; 
	}

	public void setY(int y) {
		this.y = y;
	}
}




