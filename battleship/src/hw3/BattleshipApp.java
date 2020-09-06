package hw3;

/**
 * 
 * @author Samuel Hong
 * @version <b>1.0</b> rev.0
 *
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import javax.swing.*;

public class BattleshipApp implements Runnable {

	private String ip = "localhost";
	private int port;
	private Scanner scanner = new Scanner(System.in);
	private JFrame frame;
	private Thread thread;
	
	private Socket socket;
	private DataOutputStream dos;
	private DataInputStream dis;

	private Board board; //Your board
	private Board oboard; //Opponent board
	private int shipstatus = 0; //Int to decide which ship to place down
	private int turn = 0; //If the game has started
	private boolean yourTurn = false; //if its your turn
	private boolean accepted = false; //if game has connected with another player
	private boolean gameStart = false; //it game has started
	
	private JButton[][] buttons1; //2d array of buttons (grid of opposing board)
	private JButton[][] buttons2; //2d array of buttons (grid of your board)
	private JTextField ship; //Tells you which ship to put down
	private JButton done = new JButton("Ready"); //Button to tell other player you are ready
	private JTextField status; //Tells whether you connected with another player
	
	//ship heads and tails in the format "11"
	private String battleshiphead;
	private String battleshiptail;
	private String carrierhead;
	private String carriertail;
	private String destroyerhead;
	private String destroyertail;
	private String submarinehead;
	private String submarinetail;
	private String patrolboathead;
	private String patrolboattail;
	
	//color of the different ships
	
	private Color[] shipcolors = {Color.RED, Color.BLUE, Color.YELLOW, Color.GREEN, Color.ORANGE};
	
	private ServerSocket serverSocket;
	
	/**
	 * 
	 * @param port to connect to
	 */
	
	public BattleshipApp(int port) {
		System.out.print("Do you want to Connect to the remote system or Wait for an incoming connection (enter C or W)? ");
		String choice = scanner.nextLine();
		this.port = port;
		if (choice.toLowerCase().equals("c")) {
			status = new JTextField("Connected");
		}
		else if(choice.toLowerCase().equals("w")) {
			status = new JTextField("Waiting for player");
		}
		System.out.println("What is your name: ");
		String name = scanner.nextLine();
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800,1000);
		
		ship = new JTextField("Battleship Head"); //Tells you to place the first battleship head
		
		int player = 2;
		
		if(!connect()) { //If you cannot find someone else, then you have to start a server and you are player 1
			initializeServer();
			player = 1;
		}
		else {
			accepted = true;
		}
		JPanel p1 = new JPanel(); //panel to place buttons1
		JPanel p2 = new JPanel(); //panel to place buttons2
		buttons1 = new JButton[10][10]; //opponent board
		buttons2 = new JButton[10][10]; //your board
		
		//Create a grid of buttons of color white 
		for(int i = 0; i<10; i++) {
			for(int j = 0; j<10; j++) {
				buttons1[i][j] = new JButton();
				buttons1[i][j].setPreferredSize(new Dimension(40,40));
				buttons1[i][j].setBackground(Color.WHITE);
				buttons1[i][j].setOpaque(true);
				buttons1[i][j].addActionListener(new Shoot());
				p1.add(buttons1[i][j]);
			}
		}
		for(int i = 0; i<10; i++) {
			for(int j = 0; j<10; j++) {
				buttons2[i][j] = new JButton();
				buttons2[i][j].setPreferredSize(new Dimension(40,40));
				buttons2[i][j].setBackground(Color.WHITE);
				buttons2[i][j].setOpaque(true);
				buttons2[i][j].addActionListener(new PlaceShip());
				p2.add(buttons2[i][j]);
			}
		}
		
		board = new Board("P1"); //Your board
		board.setName(name);
		frame.setTitle("Battleship: " + board.getName() +" (PLAYER" + player+")");
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		p1.setLayout(new GridLayout(10,10));
		p1.setAlignmentY(Component.LEFT_ALIGNMENT);
		p1.setPreferredSize(new Dimension(400,400));
		p1.setMaximumSize(new Dimension(400,400));
		p1.setBorder(BorderFactory.createTitledBorder("opponent board"));
		p2.setLayout(new GridLayout(10,10));
		p2.setAlignmentY(Component.RIGHT_ALIGNMENT);
		p2.setPreferredSize(new Dimension(400,400));
		p2.setMaximumSize(new Dimension(400,400));
		p2.setBorder(BorderFactory.createTitledBorder("your board"));
		frame.getContentPane().add(p1);
		frame.getContentPane().add(p2);
		frame.setVisible(true);
		frame.add(ship);
		frame.add(status);
		done.setAlignmentX(Component.RIGHT_ALIGNMENT);
		done.addActionListener(new Ready());
		frame.add(done);
		
		thread = new Thread(this, "Battleship");
		thread.start();
		
	}
	/**
	 * starts battleship simulation
	 */
	public void run() {
		while(true) {
			turn();
			
			if(!accepted) { //if no one else has connected, wait for a server request
				listenForServerRequest();
			}
		}
	}
	/**
	 * If it is your turn, you wait for other player to send a coordinate, then an "X" marks where they hit on your board
	 * In between each turn, check if someone has won
	 * If game has not started yet, read a string of all ship heads and tails to create oboard
	 * Will not be visible to the player
	 */
	private void turn() {
		if((!yourTurn || yourTurn) && accepted && turn > 0) {
			String temp = "";
			try {
				temp = dis.readUTF();//Reads in a coordinate of the other player firing
				yourTurn = true;
				board.hit(buttons2[Character.getNumericValue(temp.charAt(0))][Character.getNumericValue(temp.charAt(1))]);
				if(board.aim(temp)) {
					if(board.checkwin()) {
						System.out.println(board.getPlayer() + " LOSES"); //If you lose, system exits
						System.exit(0);
					}
				}
				
			}
			catch(IOException e) {
				System.out.println(e);
			}
			
		}
		else if((!yourTurn || yourTurn) && accepted && turn == 0) {
			String temp = "";
			try {
				temp = dis.readUTF(); //Reads in a string of coordinates to create oboard (opponent board)
				oboard = new Board("P2");
				oboard.setBattleship(temp.substring(0,2), temp.substring(2,4));
				oboard.setCarrier(temp.substring(4,6), temp.substring(6,8));
				oboard.setDestroyer(temp.substring(8,10), temp.substring(10,12));
				oboard.setSubmarine(temp.substring(12,14), temp.substring(14,16));
				oboard.setPatrolboat(temp.substring(16,18), temp.substring(18,20));
				turn++;
			}
			catch(IOException e){
				System.out.println(e);
			}
		}
	}
	/**
	 * Starts a serversocket using this.port, and since you start server, you are player 1
	 */
	public void initializeServer() {
		try {
			serverSocket = new ServerSocket(this.port);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		yourTurn = true;
	}
	
	/**
	 * wait for someone else to join the game
	 */
	
	private void listenForServerRequest() {
		Socket socket = null;
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			status.setText("P2 connected");
			accepted = true;
		}
		catch(IOException e) {
			System.out.println(e);
		}
	}
	/**
	 * 
	 * @return whether or not you are connected to another player
	 */
	private boolean connect() {
		try {
			socket = new Socket(ip, port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
		}
		catch(IOException e) {
			System.out.println(e);
			return false;
		}
		System.out.println("Successful connect");
		return true;
	}
	/**
	 * 
	 * @author samuelhong
	 * On your board grids, you get to choose where to place the ship heads and tails
	 * If the size of the boat does not fit, then return and try to place in a more suitable location
	 *
	 */
	private class PlaceShip implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(shipstatus > 9) {
				return;
			}
			if(e.getSource() instanceof JButton && !gameStart) {
				((JButton) e.getSource()).setBackground(shipcolors[shipstatus/2]);
				String temp = "";
				for(int i = 0; i<10; i++) {
					for(int j = 0; j<10; j++) {
						if(buttons2[i][j] == e.getSource()) {
							temp = Integer.toString(i) + Integer.toString(j); //Checks which button user is pressing on
							break;
						}
						
					}
				}
				//Based off shipstatus, user places head or tail of a ship, and giving it a different color
				//Once a ship tail is placed, the entire ship from head to tail is colored and the ship is placed in 
				//their board class
				//If User places a tail incorrectly, a message is sent and they must try again
				//Head must be ahead of the tail
				if(shipstatus == 0) {
					battleshiphead = temp;
					ship.setText("Battleship Tail (3 units away)");
				}
				else if(shipstatus == 1) {
					if(!sameLine(temp, battleshiphead) || !correctLength(temp, battleshiphead, 3)) {
						System.out.println("MUST BE 3 UNITS AWAY");
						((JButton) e.getSource()).setBackground(Color.WHITE);
						return;
					}
					
					battleshiptail = temp;
					ship.setText("Carrier Head");
					for(int i = Character.getNumericValue(battleshiphead.charAt(0)); i< Character.getNumericValue(temp.charAt(0))+1; i++) {
						for(int j = Character.getNumericValue(battleshiphead.charAt(1)); j< Character.getNumericValue(temp.charAt(1))+1; j++) {
							buttons2[i][j].setBackground(shipcolors[shipstatus/2]);
						}
					}
				}
				else if(shipstatus == 2) {
					carrierhead = temp;
					ship.setText("Carrier Tail (4 units away)");
				}
				else if(shipstatus == 3) {
					if(!sameLine(temp, carrierhead)  || !correctLength(temp, carrierhead, 4)) {
						System.out.println("MUST BE 4 UNITS AWAY");
						((JButton) e.getSource()).setBackground(Color.WHITE);
						return;
					}
					carriertail = temp;
					ship.setText("Destroyer Head");
					for(int i = Character.getNumericValue(carrierhead.charAt(0)); i< Character.getNumericValue(temp.charAt(0))+1; i++) {
						for(int j = Character.getNumericValue(carrierhead.charAt(1)); j< Character.getNumericValue(temp.charAt(1))+1; j++) {
							buttons2[i][j].setBackground(shipcolors[shipstatus/2]);
						}
					}

				}
				else if(shipstatus == 4) {
					destroyerhead = temp;
					ship.setText("Destroyer Tail (2 units away)");
				}
				else if(shipstatus == 5) {
					if(!sameLine(temp, destroyerhead) || !correctLength(temp, destroyerhead, 2)) {
						System.out.println("MUST BE 2 UNITS AWAY");
						((JButton) e.getSource()).setBackground(Color.WHITE);
						return;
					}
					destroyertail = temp;
					ship.setText("Submarine Head");
					for(int i = Character.getNumericValue(destroyerhead.charAt(0)); i< Character.getNumericValue(temp.charAt(0))+1; i++) {
						for(int j = Character.getNumericValue(destroyerhead.charAt(1)); j< Character.getNumericValue(temp.charAt(1))+1; j++) {
							buttons2[i][j].setBackground(shipcolors[shipstatus/2]);
						}
					}
				}
				else if(shipstatus == 6) {
					submarinehead = temp;
					ship.setText("Submarine Tail (2 units away)");
				}
				else if(shipstatus == 7) {
					if(!sameLine(temp, submarinehead)  || !correctLength(temp, submarinehead, 2)) {
						System.out.println("MUST BE 2 UNITS AWAY");
						((JButton) e.getSource()).setBackground(Color.WHITE);
						return;
					}
					submarinetail = temp;
					ship.setText("Patrol Boat Head");
					for(int i = Character.getNumericValue(submarinehead.charAt(0)); i< Character.getNumericValue(temp.charAt(0))+1; i++) {
						for(int j = Character.getNumericValue(submarinehead.charAt(1)); j< Character.getNumericValue(temp.charAt(1))+1; j++) {
							buttons2[i][j].setBackground(shipcolors[shipstatus/2]);
						}
					}
				}
				else if(shipstatus == 8) {
					patrolboathead = temp;
					ship.setText("Patrol Boat Tail (1 unit away)");
				}
				else if(shipstatus == 9) {
					if(!sameLine(temp, patrolboathead)  || !correctLength(temp, patrolboathead, 1)) {
						System.out.println("MUST BE 1 UNIT AWAY");
						((JButton) e.getSource()).setBackground(Color.WHITE);
						return;
					}
					patrolboattail = temp;
					ship.setText("Finished Placing Ships (PRESS READY)");
					for(int i = Character.getNumericValue(patrolboathead.charAt(0)); i< Character.getNumericValue(temp.charAt(0))+1; i++) {
						for(int j = Character.getNumericValue(patrolboathead.charAt(1)); j< Character.getNumericValue(temp.charAt(1))+1; j++) {
							buttons2[i][j].setBackground(shipcolors[shipstatus/2]);
						}
					}
				}
				
				shipstatus++;
			}
		}
	}
	/**
	 * 
	 * @author samuelhong
	 * User writes to the other player a coordinate that they would like to aim at
	 * If hits a ship, then marks with an "X"
	 * If misses, then marks with a "O"
	 */
	private class Shoot implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(yourTurn && accepted) {
				String temp = "";
				for(int i = 0; i<10; i++) {
					for(int j = 0; j<10; j++) {
						if(buttons1[i][j] == e.getSource()) {
							temp = Integer.toString(i) + Integer.toString(j);
							break;
						}
					}
				}
				try {
					dos.writeUTF(temp);
					dos.flush();
					if(turn > 0) {
						if(oboard.aim(temp)) {	//If hits a ship on opponent board- mark "X"
							oboard.hit(buttons1[Character.getNumericValue(temp.charAt(0))][Character.getNumericValue(temp.charAt(1))]);
							if(oboard.checkwin()) {
								System.out.println(oboard.getPlayer() +" WINS");
								System.exit(0);
							}
						}
						else { //If does not hit a ship on opponent board- mark "O"
							oboard.miss(buttons1[Character.getNumericValue(temp.charAt(0))][Character.getNumericValue(temp.charAt(1))]);
						}
					}
					yourTurn = false; 
					
				}
				catch(IOException f) {
					System.out.println(f);
				}

				
			}
		}
	}
	
	/**
	 * 
	 * @author samuelhong
	 *When the user is done placing ships, writes a string of coordinates to the opponent
	 */
	
	private class Ready implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(shipstatus > 9) {
				board.setBattleship(battleshiphead, battleshiptail);
				board.setCarrier(carrierhead, carriertail);
				board.setDestroyer(destroyerhead, destroyertail);
				board.setSubmarine(submarinehead, submarinetail);
				board.setPatrolboat(patrolboathead, patrolboattail);
				try {
					dos.writeUTF(battleshiphead+battleshiptail+carrierhead+carriertail+destroyerhead+destroyertail+submarinehead+submarinetail+patrolboathead+patrolboattail);
				}
				catch(IOException f) {
					System.out.println(f);
				}
			}
		}
	}
	/**
	 * 
	 * @param a ship head
	 * @param b ship tail
	 * @return whether or not the ship head and tail are in the same line
	 */
	public boolean sameLine(String a, String b) {
		return((Character.getNumericValue(a.charAt(0)) == Character.getNumericValue(b.charAt(0))) || Character.getNumericValue(a.charAt(1)) == Character.getNumericValue(b.charAt(1)));
	}
	/**
	 * 
	 * @param a ship head
	 * @param b ship tail
	 * @param l length of the ship
	 * @return whether or not ship tail is l units away from ship head
	 */
	public boolean correctLength(String a, String b, int l) {
		int temp = Character.getNumericValue(a.charAt(0)) + Character.getNumericValue(a.charAt(1));
		int temp2 = Character.getNumericValue(b.charAt(0)) + Character.getNumericValue(b.charAt(1));
		return (temp-temp2) == l;
	}
	
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter port: ");
		int port = scanner.nextInt();
		BattleshipApp game = new BattleshipApp(port);
	}
}	
