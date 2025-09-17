package application;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/*
  This class visualizes a network of cities as a graph using JavaFX.
  Cities are represented as circles, connections as lines,
  and a specific path (optimal or alternative) is highlighted.
 */
public class GraphVisualizer extends Pane {
	private City[] cities;

	// City names in the graph
	private String[] cityNames = { "Start", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "End" };

	// Fixed coordinates for each city to control layout positioning
	private double[][] positions = { { 50, 250 }, { 150, 100 }, { 150, 250 }, { 150, 400 }, { 300, 150 }, { 300, 300 },
			{ 450, 100 }, { 450, 200 }, { 450, 300 }, { 450, 400 }, { 600, 100 }, { 600, 250 }, { 600, 400 },
			{ 750, 250 } };

	/*
	 * Constructor: Builds the visual graph based on the given city data and a
	 * specific path.
	 */
	// Inside the constructor
	public GraphVisualizer(City[] cities, String path) {
		this.cities = cities; // Fix to avoid NullPointerException
		setPrefSize(1000, 600);
		setStyle("-fx-background-color: white;");

		// Draw all connections (edges)
		for (City city : cities) {
			String from = city.getCityName();
			double[] fromPos = getCityPosition(from);
			if (fromPos == null)
				continue;

			for (String to : city.getConnectedCityNames()) {
				double[] toPos = getCityPosition(to);
				if (toPos == null)
					continue;

				Line edge = new Line(fromPos[0], fromPos[1], toPos[0], toPos[1]);
				edge.setStroke(Color.LIGHTGRAY);
				edge.setStrokeWidth(1);
				getChildren().add(edge);

				City fromCityObj = getCityByName(from);
				int cost = fromCityObj != null ? fromCityObj.getCostTo(to) : -1;

				if (cost != -1) {
					double midX = (fromPos[0] + toPos[0]) / 2;
					double midY = (fromPos[1] + toPos[1]) / 2;

					Rectangle background = new Rectangle(midX - 12, midY - 10, 24, 18);
					background.setArcWidth(8);
					background.setArcHeight(8);
					background.setFill(Color.web("#fff9f0"));
					background.setStroke(Color.LIGHTGRAY);
					background.setStrokeWidth(0.5);

					Text costLabel = new Text(String.valueOf(cost));
					costLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
					costLabel.setFill(Color.web("#4b2e1e"));
					costLabel.setX(midX - 6);
					costLabel.setY(midY + 4);

					getChildren().addAll(background, costLabel);
				}
			}
		}

		// Highlight the path
		String[] pathCities = path != null ? path.split(" -> ") : new String[0];
		for (int i = 0; i < pathCities.length - 1; i++) {
			double[] from = getCityPosition(pathCities[i]);
			double[] to = getCityPosition(pathCities[i + 1]);
			if (from == null || to == null)
				continue;

			Line highlight = new Line(from[0], from[1], to[0], to[1]);
			highlight.setStroke(Color.SADDLEBROWN);
			highlight.setStrokeWidth(3);
			getChildren().add(highlight);
		}

		// Draw nodes
		for (int i = 0; i < cityNames.length; i++) {
			String name = cityNames[i];
			double x = positions[i][0];
			double y = positions[i][1];

			Color fillColor = Color.WHITE;
			if (isInPath(name, pathCities)) {
				fillColor = name.equalsIgnoreCase("Start") || name.equalsIgnoreCase("End") ? Color.web("#c58f3b")
						: Color.web("#f3d79e");
			}

			Circle node = new Circle(x, y, 15, fillColor);
			node.setStroke(Color.SADDLEBROWN);
			getChildren().add(node);

			Text label = new Text(name);
			label.setX(x - 12);
			label.setY(y + 5);
			label.setFill(Color.BLACK);
			getChildren().add(label);
		}
	}

	/*
	 * Returns the screen coordinates of a city based on its name.
	 */
	private double[] getCityPosition(String cityName) {
		for (int i = 0; i < cityNames.length; i++) {
			if (cityNames[i].equalsIgnoreCase(cityName)) {
				return positions[i];
			}
		}
		return null;
	}

	/*
	 * Checks if a city is part of the given path.
	 * 
	 */
	private boolean isInPath(String city, String[] pathArray) {
		for (String c : pathArray) {
			if (c.equalsIgnoreCase(city)) {
				return true;
			}
		}
		return false;
	}

	private City getCityByName(String name) {
		for (City city : cities) {
			if (city.getCityName().equalsIgnoreCase(name)) {
				return city;
			}
		}
		return null;
	}

}
