# 🚀 Optimal Travel Route Finder using Dynamic Programming

A project that determines the **minimum-cost travel route** between multiple cities using the **Dynamic Programming** approach (Floyd–Warshall Algorithm).  
The system efficiently calculates the **optimal and alternative paths** based on total travel cost, which includes petrol and hotel expenses.

This project elegantly visualizes algorithmic problem-solving — transforming complex path optimization into an interactive and intuitive experience.

---

## 🎯 Project Overview

This project solves the problem of finding the **minimum travel cost** from a starting city to a destination city.  
Each connection between cities has two types of costs:

- 🚗 **Petrol cost**  
- 🏨 **Hotel cost**

The algorithm computes the **optimal route** that minimizes the total travel expense using **Dynamic Programming**,  
and also displays **alternative routes** as well as the **full DP cost table** in a clear, user-friendly format.

---

## ⚙️ Features

✅ Reads city data and connections from an input file  
✅ Calculates minimum travel cost using **Dynamic Programming (Floyd–Warshall Algorithm)**  
✅ Displays **optimal and alternative routes** ranked by cost  
✅ Visualizes the **travel network graph** interactively  
✅ Generates and shows the **DP cost table** in a well-styled format  
✅ Includes **animations, gradients, and shadows** for a modern UX  

---

## 🧠 Algorithm Explanation

The project uses the **Floyd–Warshall Algorithm**, a classic Dynamic Programming technique for finding the shortest paths between all pairs of nodes in a weighted graph.

### Steps:
1. Represent all cities and connections in a **cost matrix**.  
2. Iteratively update the minimum costs using the DP relation:  
cost[i][j] = min(cost[i][j], cost[i][k] + cost[k][j])
3. Maintain a **path reconstruction matrix** to rebuild the optimal route.  

This approach ensures **O(N³)** time complexity and guarantees the globally minimal route.

---

## 🛠️ Technologies Used

- **Java** – Core logic and algorithm implementation  
- **GUI Visualization** – Custom interface and graph animations  
- **Object-Oriented Design** – Classes for City, Connection, and Path  
- **Dynamic Programming** – Optimal path cost computation  
- **Floyd–Warshall Algorithm** – Core path optimization logic  

---

## 📂 Project Structure
application/
│
├── City.java # Represents a city and its connections
├── Connection.java # Represents the cost between two cities
├── TravelPath.java # Represents a route and its total cost
├── GraphVisualizer.java # Visualizes the city network and paths
└── Main.java # Runs the app, applies DP, and manages UI logic

## 🖼️ Screenshots

### 🏠 Welcome Screen
![Screen1](./Screen1.png)
*A modern welcome screen inviting users to begin their optimal route journey.*

---

### 🗺️ Optimal Path Visualization
![Screen2](./Screen2.png)
*Displays the most efficient travel route with highlighted connections and total cost.*

---

### 🧩 Alternative Paths
![Screen3](./Screen3.png)
*Lists all alternative routes, ranked by total cost for user comparison.*

---

### 🧭 Path Graph View
![Screen4](./Screen4.png)
*Shows a visual graph of cities and their connections with edge costs.*

---

### 📊 Dynamic Programming Table
![Screen5](./Screen5.png)
*Displays the DP cost matrix showing the computed minimum travel costs.*

---

## 🚀 How to Run the Project

1. **Clone or download** this repository:
   ```bash
   git clone https://github.com/AseelKhatib3/minimum-cost-dynamic-programming.git
Open the project in your preferred IDE (Eclipse / IntelliJ).

Make sure all resource images (e.g., plane.png, map.png, money.png, etc.) are placed in your resources folder.

Prepare your input.txt file with this structure:Number of cities
StartCity, EndCity
CityA, [CityB, PetrolCost, HotelCost], [CityC, PetrolCost, HotelCost]
Run the project via the Main class.

Enjoy the full route visualization ✈️🌍
🎥 Demo

🎬 Coming soon — a short video demo showcasing the algorithm and route visualization in action.

✨ Developed by Aseel Khatib
