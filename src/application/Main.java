package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/*
  Main class for launching the Travel Planner JavaFX Application.
 */
public class Main extends Application {

	private static String inputFilePath = "/Users/saberkhateeb/Documents/input.txt";
	private static City[] cityList; // All cities loaded from file
	private static int cityCount; // Number of cities
	private static int[][] costTable; // DP table for min cost
	private static int[][] previousCity; // Path tracking matrix
	private static String startCity; // Start point
	private static String endCity; // End point

	public static void main(String[] args) {
		readDataFromFile(); // Reads city data from file
		fillDpTable(); // Runs the DP algorithm
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Explore Your Route");

		// Background image
		Image bgImage = new Image(getClass().getResource("/travel.png").toExternalForm());
		ImageView bgView = new ImageView(bgImage);
		bgView.setFitWidth(1500);
		bgView.setFitHeight(1000);
		bgView.setPreserveRatio(false);

		// Shadow overlay
		Rectangle shadow = new Rectangle(2000, 1000);
		shadow.setFill(Color.rgb(0, 0, 0, 0.1));

		// lable hint
		Label hint = new Label("Ready to explore your route?");
		hint.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 28));
		hint.setTextFill(Color.WHITE);
		hint.setEffect(new DropShadow(3, Color.BLACK));
		hint.setPadding(new Insets(0, 0, 10, 0));

		// Button content
		Label btnLabel = new Label("Start Journey");
		btnLabel.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 30));
		btnLabel.setStyle("-fx-text-fill: #4b2e1e;");

		ImageView icon = new ImageView(new Image(getClass().getResource("/plane.png").toExternalForm()));
		icon.setFitWidth(28);
		icon.setFitHeight(28);

		HBox btnContent = new HBox(14, btnLabel, icon);
		btnContent.setAlignment(Pos.CENTER);

		// Button styling
		Button launchButton = new Button();
		launchButton.setGraphic(btnContent);
		launchButton.setStyle("-fx-background-color: rgba(252,232,195,0.95);" + "-fx-border-color: #4b2e1e;"
				+ "-fx-border-width: 2;" + "-fx-background-radius: 20;" + "-fx-border-radius: 20;"
				+ "-fx-padding: 18 40 18 40;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0.4, 2, 2);");

		// Button animation
		ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), launchButton);
		pulse.setFromX(1);
		pulse.setToX(1.06);
		pulse.setFromY(1);
		pulse.setToY(1.06);
		pulse.setAutoReverse(true);
		pulse.setCycleCount(Animation.INDEFINITE);
		pulse.play();

		// Button action
		launchButton.setOnAction(e -> showPlanningResults());

		// Container for both label and button
		VBox buttonBox = new VBox(10, hint, launchButton);
		buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
		buttonBox.setPadding(new Insets(0, 30, 50, 0));

		// Final layout
		StackPane root = new StackPane();
		root.getChildren().addAll(bgView, shadow, buttonBox);

		Scene scene = new Scene(root, 2000, 1000);
		primaryStage.setFullScreenExitHint("");
		primaryStage.setFullScreen(true);

		primaryStage.setOnCloseRequest(e -> {
			pulse.stop();
			System.exit(0);
		});

		primaryStage.setScene(scene);
		primaryStage.show();

	}

	// Reads city data, connections, and start/end cities from a file
	public static void readDataFromFile() {
		try {
			// Open the input file using a Scanner
			Scanner scanner = new Scanner(new File(inputFilePath));

			// Read the first line: total number of cities
			cityCount = Integer.parseInt(scanner.nextLine().trim());

			// Read the second line: start and end city names
			String[] startEnd = scanner.nextLine().trim().split(",");
			startCity = startEnd[0].trim(); // Starting city
			endCity = startEnd[1].trim(); // Ending city

			// Create an array to store city objects
			cityList = new City[cityCount];
			int index = 0;

			// Process each remaining line in the file (city and its connections)
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty())
					continue;

				// Split the line into parts: city name and connections
				String[] parts = line.split(", ");
				String cityName = parts[0];
				int connectionCount = parts.length - 1;

				// Temporary arrays to hold connection details
				String[] connectedNames = new String[connectionCount];
				int[] petrolCosts = new int[connectionCount];
				int[] hotelCosts = new int[connectionCount];

				// Parse each connection: [Destination, PetrolCost, HotelCost]
				for (int i = 1; i < parts.length; i++) {
					String[] info = parts[i].replace("[", "").replace("]", "").split(",");
					connectedNames[i - 1] = info[0].trim();
					petrolCosts[i - 1] = Integer.parseInt(info[1].trim());
					hotelCosts[i - 1] = Integer.parseInt(info[2].trim());
				}

				// Create Connection objects and assign them to the city
				Connection[] connections = new Connection[connectionCount];
				for (int i = 0; i < connectionCount; i++) {
					connections[i] = new Connection(connectedNames[i], petrolCosts[i], hotelCosts[i]);
				}

				// Create the City object and store it in the list
				cityList[index++] = new City(cityName, connections);
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + inputFilePath);
		}
	}

	public static void fillDpTable() {
		final int INF = Integer.MAX_VALUE; // Represents infinity for unreachable paths

		costTable = new int[cityCount][cityCount]; // Stores the minimum cost between cities
		previousCity = new int[cityCount][cityCount]; // Stores the city used to reach a certain city (for path
														// reconstruction)

		// Step 1: Initialize the cost table
		for (int i = 0; i < cityCount; i++) {
			for (int j = 0; j < cityCount; j++) {
				costTable[i][j] = (i == j) ? 0 : INF; // Cost to self is 0, others are initially INF
			}
		}

		// Step 2: Fill direct connection costs from the city data
		for (int i = 0; i < cityCount; i++) {
			City fromCity = cityList[i];
			for (Connection conn : fromCity.getConnections()) {
				int j = findCityIndex(conn.getDestination());
				if (j != -1) {
					costTable[i][j] = conn.getTotalCost(); // Petrol + Hotel
					previousCity[i][j] = i; // Save from which city we came to reach j
				}
			}
		}

		// Step 3: Apply Floyd-Warshall algorithm for all-pairs shortest path
		// Try to improve the cost from i to j via an intermediate city k
		for (int i = 0; i < cityCount; i++) {
			for (int j = 0; j < cityCount; j++) {
				if (i != j) {
					for (int k = 0; k < cityCount; k++) {
						// If path i -> k and k -> j exists
						if (k != i && k != j && costTable[i][k] != INF && costTable[k][j] != INF) {
							int newCost = costTable[i][k] + costTable[k][j];
							// Update cost if a cheaper path is found
							if (newCost < costTable[i][j]) {
								costTable[i][j] = newCost;
								previousCity[i][j] = previousCity[k][j]; // Update the previous city on the new shortest
																			// path
							}
						}
					}
				}
			}
		}
	}

	// Searches for a city by name in the cityList array and returns its index.
	// Returns -1 if the city is not found (case-insensitive comparison).
	public static int findCityIndex(String cityName) {
		for (int i = 0; i < cityList.length; i++) {
			if (cityList[i].getCityName().equalsIgnoreCase(cityName))
				return i;
		}
		return -1;
	}

	// Returns the optimal path (as a string) from the start city to the end city.
	// Uses the costTable and previousCity matrix generated by the Floyd-Warshall
	// algorithm.
	// If no path exists, returns a "No path exists" message.
	public static String getOptimalPath(String start, String end) {
		int startIdx = findCityIndex(start); // Get index of the start city
		int endIdx = findCityIndex(end); // Get index of the end city

		// If either city is invalid or there is no known path between them
		if (startIdx == -1 || endIdx == -1 || costTable[startIdx][endIdx] == Integer.MAX_VALUE) {
			return "No path exists";
		}

		// Use a StringBuilder to construct the path step by step
		StringBuilder pathBuilder = new StringBuilder();
		buildPath(startIdx, endIdx, pathBuilder); // Recursively build the path
		return pathBuilder.toString(); // Return the final path as a string
	}

	// Recursive helper method that reconstructs the path from start to end
	// using the previousCity matrix that stores the best path decisions
	private static void buildPath(int start, int end, StringBuilder pathBuilder) {
		if (start == end) {
			pathBuilder.append(cityList[start].getCityName()); // Base case: start of path
		} else {
			// Recursively go back through the path to build the correct order

			buildPath(start, previousCity[start][end], pathBuilder);
			pathBuilder.append(" -> ").append(cityList[end].getCityName());

		}
	}

	// Prints the dynamic programming cost table along with the path information
	// (previous cities)
	public static void printDpTableWithPath() {
		System.out.println("\nDynamic Programming Table with Path Info:\n");

		int colWidth = 12; // Width for formatting each column when printing

		// Print the header row (column city names)
		System.out.printf("%" + colWidth + "s", ""); // empty cell top-left
		for (int i = 0; i < cityCount; i++) {
			System.out.printf("%" + colWidth + "s", cityList[i].getCityName());
		}
		System.out.println();

		// Print each row of the cost table
		for (int i = 0; i < cityCount; i++) {
			// First column in each row (row city name)
			System.out.printf("%" + colWidth + "s", cityList[i].getCityName());

			for (int j = 0; j < cityCount; j++) {
				String cell;

				// If no path exists, display INF
				if (costTable[i][j] == Integer.MAX_VALUE) {
					cell = String.format("%s (%s)", "INF", "–");
				} else {
					// Determine the previous city name that leads to this cell
					String from = (i == j || previousCity[i][j] == 0) ? "–"
							: cityList[previousCity[i][j]].getCityName();
					cell = String.format("%d (%s)", costTable[i][j], from);
				}

				// Print the cell with proper spacing
				System.out.printf("%" + colWidth + "s", cell);
			}
			System.out.println();
		}
	}

	// Builds a styled JavaFX VBox card that displays the optimal path and its total
	// cost
	public VBox buildOptimalPathCard() {
		// Find the indices of the start and end cities
		int startIndex = findCityIndex(startCity);
		int endIndex = findCityIndex(endCity);

		// If start or end city doesn't exist, show error message
		if (startIndex == -1 || endIndex == -1) {
			Label error = new Label("Start or destination city not found.");
			error.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
			return new VBox(error);
		}

		// Create a label for the title
		Label title = new Label("Optimal Path");
		title.setStyle("-fx-text-fill: #4b2e1e;");
		title.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 36));

		// Get the optimal path string using the DP algorithm
		String pathText = getOptimalPath(startCity, endCity);
		Label pathLabel = new Label("Path: " + pathText);
		pathLabel.setWrapText(true);
		pathLabel.setStyle("-fx-text-fill: #1f1f1f; -fx-font-size: 22px; -fx-font-weight: bold;");

		// Create a money icon to display next to the cost
		Image moneyImg = new Image(getClass().getResource("/money.png").toExternalForm());
		ImageView moneyIcon = new ImageView(moneyImg);
		moneyIcon.setFitWidth(20);
		moneyIcon.setFitHeight(20);

		// Create a label showing the total cost of the path
		Label costText = new Label("Cost: " + costTable[startIndex][endIndex]);
		costText.setStyle("-fx-text-fill: #b26a00; -fx-font-size: 20px; -fx-font-weight: bold;");

		// Group the cost icon and label horizontally
		HBox costBox = new HBox(8, moneyIcon, costText);

		// Create and style the final VBox card that holds all content
		VBox card = new VBox(30, title, pathLabel, costBox);
		card.setPadding(new Insets(8));
		card.setPrefSize(1200, 230);
		card.setStyle("-fx-background-color: #fff9f0;" + "-fx-border-color: #d9ad7c;" + "-fx-border-radius: 12;"
				+ "-fx-background-radius: 12;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 1, 2);");

		return card; // Return the fully styled optimal path card
	}

	// Opens a new JavaFX window showing the full travel planning results.
	// This includes the optimal path, alternative paths, and the dynamic
	// programming table.
	// The content is loaded using buildResultsTabs() which organizes the GUI into
	// sections.
	private void showPlanningResults() {
		BorderPane content = buildResultsTabs(); // This method builds the full layout
		Scene resultScene = new Scene(content, 1000, 600);
		Stage resultStage = new Stage();
		resultStage.setTitle("Travel Plan Results");
		resultStage.setScene(resultScene);

		// Ensures the application exits completely when this window is closed
		resultStage.setOnCloseRequest(e -> {
			System.exit(0);
		});

		resultStage.show();
	}

	// Recursively generates all possible paths from the currentCity to the endCity.
	// For each valid path, it calculates the total cost and stores it as a
	// TravelPath object.
	// Returns an array of all complete paths with their corresponding costs.
	public static TravelPath[] getAllPathsWithCost(String currentCity, String[] path) {
		// Add current city to the path
		String[] newPath = new String[path.length + 1];
		for (int i = 0; i < path.length; i++) {
			newPath[i] = path[i];
		}
		newPath[path.length] = currentCity;

		// If we reached the destination, calculate cost and return this path
		if (currentCity.equals(endCity)) {
			int cost = calculatePathCost(newPath);
			return new TravelPath[] { new TravelPath(String.join(" -> ", newPath), cost) };
		}

		TravelPath[] allPaths = new TravelPath[0];

		// Search for the city object and explore its connections
		for (City city : cityList) {
			if (city.getCityName().equals(currentCity)) {
				for (String adj : city.getConnectedCityNames()) {
					if (!isCityInPath(adj, newPath)) {
						// Recursive call to extend the path
						TravelPath[] newPaths = getAllPathsWithCost(adj, newPath);
						allPaths = concatenateTravelPathArrays(allPaths, newPaths);
					}
				}
				break;
			}
		}

		return allPaths;
	}

	// Checks if a given city already exists in the current path.
	// This is used to avoid loops or revisiting the same city in a path.
	private static boolean isCityInPath(String city, String[] path) {
		for (String name : path) {
			if (name.equals(city))
				return true;
		}
		return false;
	}

	// Calculates total cost of a given city path by summing the petrol and hotel
	// costs
	// for each segment in the path from one city to the next.
	private static int calculatePathCost(String[] path) {
		int cost = 0;
		for (int i = 0; i < path.length - 1; i++) {
			City current = cityList[findCityIndex(path[i])];
			cost += current.getCostTo(path[i + 1]);
		}
		return cost;
	}

	// Combines two arrays of TravelPath objects into a single array.
	// Used to collect all possible alternative paths in a single list.
	private static TravelPath[] concatenateTravelPathArrays(TravelPath[] a, TravelPath[] b) {
		TravelPath[] result = new TravelPath[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	// Builds the full result layout in the JavaFX GUI.
	// Includes the optimal path view, alternative paths, and DP table.
	// Users can switch views using buttons in a sidebar.
	public BorderPane buildResultsTabs() {
		VBox optimalCard = buildOptimalPathCard();

		// Generate all valid alternative paths and sort them by cost
		TravelPath[] allRoutes = getAllPathsWithCost(startCity, new String[0]);
		Arrays.sort(allRoutes, Comparator.comparingInt(TravelPath::getCost));

		// Container to display alternative path cards
		VBox altList = new VBox(15);
		altList.setPadding(new Insets(10));

		Label altTitle = new Label("Alternative Paths");
		altTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 33));
		altTitle.setStyle("-fx-text-fill: #4b2e1e;");

		ScrollPane altScroll = new ScrollPane(altList);
		altScroll.setPrefSize(950, 600);
		altScroll.setStyle("-fx-background-color: transparent;");

		VBox altContainer = new VBox(20, altTitle, altScroll);
		altContainer.setPadding(new Insets(30));
		altContainer.setStyle("-fx-background-color: #fff9f0;" + "-fx-border-color: #d9ad7c;" + "-fx-border-radius: 12;"
				+ "-fx-background-radius: 12;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 1, 2);");
		altContainer.setPrefSize(1000, 700);

		// StackPane to swap between views
		StackPane contentPane = new StackPane();
		for (int i = 0; i < allRoutes.length; i++) {
			final int index = i;

			VBox routeBox = new VBox(12);
			routeBox.setPrefWidth(1200);
			routeBox.setPadding(new Insets(20, 25, 20, 25));
			routeBox.setStyle("-fx-background-color: #fff9f0; -fx-border-color: #d9ad7c; "
					+ "-fx-border-radius: 12; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, "
					+ "rgba(0,0,0,0.1), 6, 0.3, 1, 2);");

			Label title = new Label("Path #" + (index + 1));
			title.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
			title.setStyle("-fx-text-fill: #4b2e1e;");

			String fullPath = allRoutes[index].getPath();

			TextFlow pathFlow = new TextFlow();
			pathFlow.setLineSpacing(4);
			pathFlow.setPrefWidth(1000);

			String[] parts = fullPath.split(" -> ");
			for (int j = 0; j < parts.length; j++) {
				Text cityText = new Text(parts[j]);
				cityText.setFill(Color.web("#4b2e1e"));
				cityText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
				pathFlow.getChildren().add(cityText);
				if (j < parts.length - 1) {
					Text arrow = new Text(" -> ");
					arrow.setFill(Color.GRAY);
					arrow.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
					pathFlow.getChildren().add(arrow);
				}
			}

			ImageView moneyIcon = new ImageView(new Image(getClass().getResource("/money.png").toExternalForm()));
			moneyIcon.setFitWidth(18);
			moneyIcon.setFitHeight(18);

			Label costText = new Label("Cost: " + allRoutes[index].getCost());
			costText.setStyle("-fx-text-fill: #b26a00; -fx-font-size: 14px; -fx-font-weight: bold;");
			HBox costBox = new HBox(6, moneyIcon, costText);

			Button visualize = new Button("Visualize on Map");
			visualize.setStyle("-fx-background-color: #4b2e1e; -fx-text-fill: white; -fx-font-size: 13px;");
			visualize.setOnAction(ev -> {
				GraphVisualizer g = new GraphVisualizer(cityList, allRoutes[index].getPath());
				Label graphTitle = new Label("Map View - Path #" + (index + 1));
				graphTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
				graphTitle.setStyle("-fx-text-fill: #4b2e1e;");
				Button back = new Button("← Back to Alternatives");
				back.setStyle(
						"-fx-background-color: #fce8c3; -fx-text-fill: #4b2e1e; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10;");
				back.setOnAction(e2 -> contentPane.getChildren().setAll(altContainer));
				VBox mapView = new VBox(20, back, graphTitle, g);
				mapView.setPadding(new Insets(30));
				mapView.setAlignment(Pos.TOP_LEFT);
				mapView.setStyle("-fx-background-color: #f5f5f5;");
				contentPane.getChildren().setAll(mapView);
			});

			routeBox.getChildren().addAll(title, pathFlow, costBox, visualize);
			altList.getChildren().add(routeBox);
		}

		VBox sidebar = new VBox(18);
		sidebar.setPadding(new Insets(20));
		sidebar.setStyle(
				"-fx-background-color: #4b2e1e; -fx-border-color: #d9ad7c; -fx-border-width: 2px; -fx-border-radius: 10; -fx-background-radius: 10;");

		Button btn1 = new Button("Optimal Path");
		Button btn2 = new Button("Alternatives");
		Button btn3 = new Button("DP Table");

		for (Button btn : new Button[] { btn1, btn2, btn3 }) {
			btn.setMaxWidth(Double.MAX_VALUE);
			btn.setStyle(
					"-fx-background-color: #fce8c3; -fx-text-fill: #4b2e1e; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-border-radius: 10;");
		}
		sidebar.getChildren().addAll(btn1, btn2, btn3);

		VBox initialColumn = new VBox(20);
		initialColumn.setPadding(new Insets(12, 30, 30, 0));
		initialColumn.setAlignment(Pos.TOP_LEFT);
		Label initialGraphTitle = new Label("Graphical View of the Optimal Journey");
		initialGraphTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
		initialGraphTitle.setStyle("-fx-text-fill: #4b2e1e;");
		GraphVisualizer initialGraph = new GraphVisualizer(cityList, getOptimalPath(startCity, endCity));
		VBox initialGraphBox = new VBox(15, initialGraphTitle, initialGraph);
		initialGraphBox.setPadding(new Insets(20));
		initialGraphBox.setStyle("-fx-background-color: #fff9f0;"
				+ "-fx-border-color: #d9ad7c; -fx-border-radius: 8; -fx-background-radius: 8;");
		initialGraphBox.setPrefWidth(1200);
		initialColumn.getChildren().addAll(optimalCard, initialGraphBox);
		contentPane.getChildren().add(initialColumn);

		btn1.setOnAction(e -> {
			VBox column = new VBox(20);
			column.setPadding(new Insets(10, 30, 30, 0));
			column.setAlignment(Pos.TOP_LEFT);
			Label graphTitle = new Label("Graphical View of the Optimal Journey");
			graphTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
			graphTitle.setStyle("-fx-text-fill: #4b2e1e;");
			GraphVisualizer graph = new GraphVisualizer(cityList, getOptimalPath(startCity, endCity));
			VBox graphBox = new VBox(15, graphTitle, graph);
			graphBox.setPadding(new Insets(20));
			graphBox.setStyle(
					"-fx-background-color: #fff9f0; -fx-border-color: #d9ad7c; -fx-border-radius: 8; -fx-background-radius: 8;");
			graphBox.setPrefWidth(1200);
			VBox card = buildOptimalPathCard();
			column.getChildren().addAll(card, graphBox);
			contentPane.getChildren().setAll(column);
		});

		btn2.setOnAction(e -> contentPane.getChildren().setAll(altContainer));
		btn3.setOnAction(e -> contentPane.getChildren().setAll(buildDpTableCard()));

		BorderPane layout = new BorderPane();
		layout.setLeft(sidebar);
		layout.setCenter(contentPane);
		layout.setPadding(new Insets(20));
		layout.setBackground(
				new Background(new BackgroundImage(new Image(getClass().getResource("/map.png").toExternalForm()),
						BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
						new BackgroundSize(1.0, 1.0, true, true, false, true))));

		return layout;
	}

	// Builds and returns a styled VBox GUI component displaying the DP cost table
	// (in JavaFX)
	public VBox buildDpTableCard() {
		GridPane grid = new GridPane();
		grid.setHgap(5);
		grid.setVgap(5);
		grid.setPadding(new Insets(20));
		grid.setStyle("-fx-background-color: #fff9f0;");

		// Add column headers (city names)
		for (int j = 0; j < cityCount; j++) {
			Label header = new Label(cityList[j].getCityName().toUpperCase());
			header.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
			header.setTextFill(Color.web("#4b2e1e"));
			header.setStyle("-fx-background-color: #fce8c3;" + "-fx-border-color: #d9ad7c;" + "-fx-border-width: 1;"
					+ "-fx-padding: 8;" + "-fx-min-width: 70;" + "-fx-alignment: center;");
			grid.add(header, j + 1, 0); // Shifted right by 1 (first column is row header)
		}

		// Add row headers and cost values
		for (int i = 0; i < cityCount; i++) {
			Label rowLabel = new Label(cityList[i].getCityName().toUpperCase());
			rowLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
			rowLabel.setTextFill(Color.web("#4b2e1e"));
			rowLabel.setStyle("-fx-background-color: #fce8c3;" + "-fx-border-color: #d9ad7c;" + "-fx-border-width: 1;"
					+ "-fx-padding: 8;" + "-fx-min-width: 70;" + "-fx-alignment: center;");
			grid.add(rowLabel, 0, i + 1);

			for (int j = 0; j < cityCount; j++) {
				// Get value from costTable or show "INF" if no path exists
				String value = (costTable[i][j] == Integer.MAX_VALUE) ? "INF" : String.valueOf(costTable[i][j]);

				// Create label for this cell
				Label cell = new Label(value);
				cell.setFont(Font.font("monospace", 14));
				cell.setTextFill(Color.web("#2c2c2c"));
				cell.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d9d9d9;" + "-fx-border-width: 1;"
						+ "-fx-padding: 8;" + "-fx-min-width: 70;" + "-fx-alignment: center;");
				grid.add(cell, j + 1, i + 1); // Add cell to grid
			}
		}

		// Table title
		Label title = new Label("Dynamic Programming Table");
		title.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
		title.setTextFill(Color.web("#4b2e1e"));

		// Scrollable container for the grid
		ScrollPane scrollPane = new ScrollPane(grid);
		scrollPane.setFitToWidth(true);
		scrollPane.setStyle("-fx-background-color: transparent;");

		// VBox to contain the title and scrollable table
		VBox box = new VBox(20, title, scrollPane);
		box.setPadding(new Insets(30));
		box.setStyle("-fx-background-color: #fff9f0;" + "-fx-border-color: #d9ad7c;" + "-fx-border-radius: 12;"
				+ "-fx-background-radius: 12;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 6, 0.3, 1, 2);");

		return box; // Return the complete styled VBox with the DP table
	}

}
