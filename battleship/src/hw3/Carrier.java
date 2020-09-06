package hw3;

/**
 * 
 * @author Samuel Hong
 * @version <b>1.0</b> rev.0
 *
 */

public class Carrier {
	
	private int size = 5;
	private String head; //Head of the boat in String format: 11
	private String tail; //Tail of the boat in String format: 11
	private int[] status;
	
	/**
	 * 
	 * @param head shiphead
	 * @param tail shiptail
	 * creates a ship starting at head and ending at tail
	 */
	public Carrier(String head, String tail) {
		status = new int[size];
		this.head = head;
		this.tail = tail;
	}
	/**
	 * 
	 * @return head
	 */
	public String getHead() {
		return head;
	}
	/**
	 * 
	 * @return tail
	 */
	public String getTail() {
		return tail;
	}
	/**
	 * 
	 * @return an array of coordinates of the body of the ship
	 */
	public String[] getBody() {
		String[] temp = new String[5];
		int counter = 0;
		for(int i = Character.getNumericValue(head.charAt(0)); i< Character.getNumericValue(tail.charAt(0))+1; i++) {
			for(int j = Character.getNumericValue(head.charAt(1)); j< Character.getNumericValue(tail.charAt(1))+1; j++) {
				temp[counter] = Integer.toString(i) + Integer.toString(j);
				counter++;
			}
		}
		return temp;
	}
	/**
	 * 
	 * @param p index of ship
	 * status[p] is set to hit
	 */
	public void hit(int p) {
		status[p] = 1;
	}
	/**
	 * 
	 * @return if ship has been hit in all places
	 */
	public boolean sunk() {
		for(int i = 0; i<size; i++) {
			if(status[i] == 0) {
				return false;
			}
		}
		return true;
	}
	
}

