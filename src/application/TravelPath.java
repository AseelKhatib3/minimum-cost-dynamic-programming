package application;

/*
 This class represents a travel path between cities,
  including the full path taken as a string and its total cost.
 */
public class TravelPath {

	private String path; // The full route as a string (for example, "Start -> B -> E -> J -> End")
	private int cost; // The total cost of that path (petrol + hotel combined)

	/*
	 * Constructor to initialize a travel path with its string path and total cost.
	 */
	public TravelPath(String path, int cost) {
		this.path = path;
		this.cost = cost;
	}

	/*
	 * Returns the travel path string.
	 */
	public String getPath() {
		return path;
	}

	/*
	 * Returns the total cost of the path.
	 */
	public int getCost() {
		return cost;
	}

	/*
	 * Returns a string representation of this TravelPath
	 */
	public String toString() {
		return "Path: " + path + ", Cost: " + cost;
	}
}
