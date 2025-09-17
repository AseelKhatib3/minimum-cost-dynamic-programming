package application;

/*
 Represents a connection from one city to another. Each connection has: 
 destination city name - petrol cost to travel - hotel cost in the destination
 city
*/
public class Connection {

	private String destination;
	private int petrolCost;
	private int hotelCost;

	// Constructor to build a connection
	public Connection(String destination, int petrolCost, int hotelCost) {
		this.destination = destination;
		this.petrolCost = petrolCost;
		this.hotelCost = hotelCost;
	}

	// Returns the destination city's name
	public String getDestination() {
		return destination;
	}

	// Returns the petrol cost to reach the destination
	public int getPetrolCost() {
		return petrolCost;
	}

	// Returns the hotel cost in the destination
	public int getHotelCost() {
		return hotelCost;
	}

	// Returns total cost = petrol + hotel
	public int getTotalCost() {
		return petrolCost + hotelCost;
	}

	// String representation of the connection
	public String toString() {
		return destination + " [Petrol: " + petrolCost + ", Hotel: " + hotelCost + "]";
	}
}
