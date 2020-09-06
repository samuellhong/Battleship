package hw3;

import javax.swing.JButton;

/**
 * 
 * @author Samuel Hong
 * @version <b>1.0</b> rev.0
 *
 */

public class Board {
	
	private int width = 10; //width of the board
	private int length = 10; //length of the board
	private int[][] b; //board, initially all 0s
	private String player = ""; //player name
	private Battleship battleship;
	private Carrier carrier;
	private Destroyer destroyer;
	private Submarine submarine;
	private Patrolboat patrolboat;
	
	/**
	 * 
	 * @param name player name
	 * 
	 */
	
	public Board(String name) {
		b = new int[10][10];
		player = name;
	}
	
	/**
	 * 
	 * @return player number
	 */
	
	public String getPlayer() {
		return this.player;
	}
	/**
	 * 
	 * @return width
	 */
	public int getWidth() {
		return this.width;
	}
	/**
	 * 
	 * @return length
	 */
	public int getLength() {
		return this.length;
	}
	/**
	 * 
	 * @return player name
	 */
	public String getName() {
		return this.player;
	}
	/**
	 * 
	 * @param name sets this.name = name
	 */
	public void setName(String name) {
		this.player = name;
	}
	/**
	 * 
	 * @param h ship head
	 * @param t ship tail
	 * creates a new battleship 
	 */
	public void setBattleship(String h, String t) {
		battleship = new Battleship(h, t);
	}
	/**
	 * 
	 * @param h ship head
	 * @param t ship tail
	 * creates a new carrier
	 */
	public void setCarrier(String h, String t) {
		carrier = new Carrier(h, t);
	}
	/**
	 * 
	 * @param h ship head
	 * @param t ship tail
	 * creates a new destroyer
	 */
	public void setDestroyer(String h, String t) {
		destroyer = new Destroyer(h, t);
	}
	/**
	 * 
	 * @param h ship head
	 * @param t ship tail
	 * creates a new submarine
	 */
	public void setSubmarine(String h, String t) {
		submarine = new Submarine(h, t);
	}
	/**
	 * 
	 * @param h ship head
	 * @param t ship tail
	 * creates a new patrolboat
	 */
	public void setPatrolboat(String h, String t) {
		patrolboat = new Patrolboat(h, t);
	}
	/**
	 * 
	 * @param temp button that text set to "X"
	 */
	public void hit(JButton temp) {
		temp.setText("X");
	}
	/**
	 * 
	 * @param temp button that text set to "O"
	 */
	public void miss(JButton temp) {
		temp.setText("O");
	}
	/**
	 * 
	 * @param h coordinate of place aimed at
	 * @return true if you hit a ship
	 * Goes through all the ships and sees if they hit a ship, if so, then that ship is hit and checks to see if they sunk that ship
	 */
	public boolean aim(String h) {
		for(int i = 0; i<4; i++) {
			if(battleship.getBody()[i].equals(h)) {
				int temp = (Character.getNumericValue(h.charAt(0))+Character.getNumericValue(h.charAt(1))) - (Character.getNumericValue(battleship.getHead().charAt(0))+Character.getNumericValue(battleship.getHead().charAt(1)));
				battleship.hit(temp);
				if(battleship.sunk()) {
					System.out.println("Sunk Battleship");
				}
				return true;
			}
		}
		for(int i = 0; i<5; i++) {
			if(carrier.getBody()[i].equals(h)) {
				int temp = (Character.getNumericValue(h.charAt(0))+Character.getNumericValue(h.charAt(1))) - (Character.getNumericValue(carrier.getHead().charAt(0))+Character.getNumericValue(carrier.getHead().charAt(1)));
				carrier.hit(temp);
				if(carrier.sunk()) {
					System.out.println("Sunk Carrier");
				}
				return true;
			}
		}
		for(int i = 0; i<3; i++) {
			if(destroyer.getBody()[i].equals(h)) {
				int temp = (Character.getNumericValue(h.charAt(0))+Character.getNumericValue(h.charAt(1))) - (Character.getNumericValue(destroyer.getHead().charAt(0))+Character.getNumericValue(destroyer.getHead().charAt(1)));
				destroyer.hit(temp);
				if(destroyer.sunk()) {
					System.out.println("Sunk Destroyer");
				}
				return true;
			}
		}
		for(int i = 0; i<3; i++) {
			if(submarine.getBody()[i].equals(h)) {
				int temp = (Character.getNumericValue(h.charAt(0))+Character.getNumericValue(h.charAt(1))) - (Character.getNumericValue(submarine.getHead().charAt(0))+Character.getNumericValue(submarine.getHead().charAt(1)));
				submarine.hit(temp);
				if(submarine.sunk()) {
					System.out.println("Sunk Submarine");
				}
				return true;
			}
		}
		for(int i = 0; i<2; i++) {
			if(patrolboat.getBody()[i].equals(h)) {
				int temp = (Character.getNumericValue(h.charAt(0))+Character.getNumericValue(h.charAt(1))) - (Character.getNumericValue(patrolboat.getHead().charAt(0))+Character.getNumericValue(patrolboat.getHead().charAt(1)));
				patrolboat.hit(temp);
				if(patrolboat.sunk()) {
					System.out.println("Sunk Patrolboat");
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean checkwin() {
		return (battleship.sunk() && carrier.sunk() && destroyer.sunk() && submarine.sunk() && patrolboat.sunk());
	}
}
