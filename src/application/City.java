package application;

/*
  Represents a city with a name and a list of direct connections to other
  cities.
 */
public class City {

	private String cityName; // Name of the city
	private Connection[] connections; // All connections from this city

	// Constructor to initialize the city and its connections
	public City(String cityName, Connection[] connections) {
		this.cityName = cityName;
		this.connections = connections;
	}

	// Returns the name of the city
	public String getCityName() {
		return cityName;
	}

	// Returns number of direct connections
	public int getNumberOfConnections() {
		return connections.length;
	}

	// Returns array of all connection objects
	public Connection[] getConnections() {
		return connections;
	}

	// Returns an array of destination city names
	public String[] getConnectedCityNames() {
		String[] names = new String[connections.length];
		for (int i = 0; i < connections.length; i++) {
			names[i] = connections[i].getDestination();
		}
		return names;
	}

	// Returns true if this city connects directly to a given city
	public boolean hasConnectionTo(String target) {
		for (int i = 0; i < connections.length; i++) {
			if (connections[i].getDestination().equalsIgnoreCase(target)) {
				return true;
			}
		}
		return false;
	}

	// Returns the total cost to reach a given city, or -1 if not connected
	public int getCostTo(String target) {
		for (int i = 0; i < connections.length; i++) {
			if (connections[i].getDestination().equalsIgnoreCase(target)) {
				return connections[i].getTotalCost();
			}
		}
		return -1;
	}

	// String representation of the city and its connections
	public String toString() {
		String result = "City: " + cityName + "\nConnections:\n";
		for (int i = 0; i < connections.length; i++) {
			result += "  -> " + connections[i].toString() + "\n";
		}
		return result;
	}
}
