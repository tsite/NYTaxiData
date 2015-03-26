package cs269.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;
import java.util.ArrayDeque;

import cs269.project.Node;
import cs269.project.NodeRule;

/*
 * use straight line distance between lat lon points
 * 
 * try an array to hold nodes, ordered by id#
 * 
 * break up list of nodes into squares to speed up the fit for lat lon points
 * 
 * 
 * Notes: Distance outputs are correctly estimating optimal distance.
 * 
 * Entry for line 12 in trip_data_1 correctly has a distance of 1.3 miles,
 * whereas the calculated distance is 1.8.  After looking at the map data,
 * it appears that the 65th Street Transverse through Central Park is not
 * connected to any other roads on the map.  If you find the optimal path
 * without using the transverse (using Google Maps), it comes out to about
 * 1.8 miles.  My assumption then is that the algorithm is correct but that
 * the map data is not 100% accurate.  This should account for any estimations
 * that are longer than the distance of the actual ride.  Also, the trip data
 * has distances in increments of tenths of a mile.  Some of the calculated
 * distances are longer, but truncated to the tenths place are the same as
 * the actual ride.  This makes sense given that taxi distances are measured
 * for cost based on tenths of a mile.
 * 
 *  Looking at the first 20 or so lines of output, there actually is an example
 *  of a ride that is much longer than it should be.  Line 14 of the input file
 *  has a ride distance of 2.3 miles, but the estimation is about 1.57 miles.
 *  After checking on GoogleMaps, the 1.57 mile estimate is accurate.  This looks
 *  like an example of the sort of gross overcharging that we're looking for.
 * 
 */

public class AStar {

	public static final double RAD = 3958.76; // radius of the earth in miles
	public static HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	public static HashMap<Integer, Vector<Node>> map = new HashMap<Integer, Vector<Node>>();
	public static final String FILENAME = "map.tsv";
	public static ArrayDeque<Node> solution = new ArrayDeque<Node>();
	public static double distance = 0;
	public static int maxID = 0;
	public static double edgeLat1 = 40.8755576;
	public static double edgeLon1 = -73.9056675;
	public static double edgeLat2 = 40.6992052;
	public static double edgeLon2 = -74.021833;
	public static double milesPerLat = 12.185089 / (edgeLat1 - edgeLat2);
	public static double milesPerLon = 6.06893 / (edgeLon1 - edgeLon2);

	

	public static void main(String args[]) {

		System.out.println("Processing map information...");

		try {
			makeMap(new File(FILENAME));
		} catch (FileNotFoundException e) {
			System.out.println("Data file not found.");
		}

		Node[] nodeArray = new Node[maxID + 1];
		
		for (Entry<Integer, Node> e : nodes.entrySet()) {
			nodeArray[e.getValue().id()] = e.getValue();
		}

		//		System.out.println("~~~~~~~~~~~~~~~~ ALL NODES IN LIST OF NODES: ~~~~~~~~~~~~~~~~~");
		//		for (Entry<Integer, Vector<Node>> i : map.entrySet()) {
		//			String key = i.getKey().toString();;
		//            Vector<Node> value = i.getValue();
		//            System.out.print("key: " + key + " adjacencies:  ");
		//            for (int j = 0; j < value.size(); ++j) {
		//            	System.out.print(value.elementAt(j).id() + ", ");
		//            }
		//            System.out.println("");
		//		}

		Scanner console = new Scanner(System.in);
		int choice = 0;
		do {
			System.out.print("Enter 1 to find individual paths, 2 to process files, and 3 to exit: ");
			choice = console.nextInt();
			console.nextLine();

			if (choice == 1) {
				String answer = "Y";
				do {
					System.out.println("Enter start and end nodes: ");
					System.out.print("Starting node: ");
					int s = console.nextInt();
					System.out.print("Destination node: ");
					int g = console.nextInt();
					Node start = nodes.get(s);
					System.out.println("Starting node: " + start.toString());
					Node goal = nodes.get(g);
					System.out.println("Destination node: " + goal.toString());

					if (aStar(start, goal)) {
						System.out.println("Optimal path:");
						while (!solution.isEmpty()) {
							Node cur = solution.pollFirst();
							System.out.println(cur.toString());
						}
					} else {
						System.out.println("No solution found");
					}

					System.out.print("Would you like to run a new search? (Y|N) ");
					answer = console.next();
				} while (answer.startsWith("Y"));

			}
			else {
				System.out.println("Enter names of input files separated by commas: ");
				String f = console.nextLine();
				String[] files = f.split(","); 

				System.out.println("Processing " + files.length + " files...");

				for (int i = 0; i < files.length; ++i) {
					System.out.println("Processing file " + files[i] + "...");
					try {
						Scanner input = new Scanner(new File(files[i]));
						String name = files[i].substring(0, files[i].indexOf("."));
						PrintWriter output = new PrintWriter(new File(name + "_output.txt"));
						input.nextLine();
						int counter = 0;
						while (input.hasNextLine()) {
							++counter;
							if (counter > 999 && counter%1000 == 0) {
								System.out.println("Processed " + counter +" lines");
							}
							
							String ride = input.nextLine();
							String[] data = ride.split(",");
							double lat1 = Double.parseDouble(data[11]);
							double lon1 = Double.parseDouble(data[10]);
							double lat2 = Double.parseDouble(data[13]);
							double lon2 = Double.parseDouble(data[12]);

							Node p = new Node(0, lat1, lon1);
							Node d = new Node(0, lat2, lon2);
							Node curP = new Node();
							Node curD = new Node();
							boolean changed1 = false;
							boolean changed2 = false;
							for (Entry<Integer, Node> e : nodes.entrySet()) {
								if (distance(p, curP) > distance(p, e.getValue())) {
									curP = e.getValue();
									if (!changed1) {
										changed1 = true;
									}
								}
								if (distance(d, curD) > distance(d, e.getValue())) {
									curD = e.getValue();
									if (!changed2) {
										changed2 = true;
									}
								}
							}
//							for (int j = 0; j < maxID + 1; ++j) {
//								if (nodeArray[j] != null) {
//									if (distance(p, curP) > distance(p, nodeArray[j])) {
//										curP = nodeArray[j];
//										if (!changed1) {
//											changed1 = true;
//										}
//									}
//									if (distance(d, curD) > distance(p, nodeArray[j])) {
//										curD = nodeArray[j];
//										if (!changed2) {
//											changed2 = true;
//										}
//									}
//								}
//							}
							if (changed1 && changed2) {
//								System.out.println("Start node fit: " + curP.toString() + " Goal node fit: " + curD.toString());
								
								aStar(curP, curD);	
							} else {
								distance = 0;
							}
							output.println(distance);
						}
						System.out.println(files[i] + " finished processing.");
						input.close();
						output.close();
					} catch (FileNotFoundException e) {
						System.out.println("File not found... moving to next file...");
					}
					System.out.println("All files processed.");
				}
			}
		} while (choice != 3);

		console.close();
		System.out.println("Goodbye");
	}

	public static void makeMap(File filename) throws FileNotFoundException {
		int half = 0;
		Scanner input = new Scanner(filename);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			String[] segs = line.split("\t");
			//			for (int i = 0; i < segs.length; ++i) {
			//				System.out.println("New input line:");
			//				System.out.println(segs[i]);
			//			}
			//			System.out.println("");
			int id = Integer.parseInt(segs[0]);
			if (segs.length == 4) {
				if (id > maxID) {
					maxID = id;
				}
				double lon = Double.parseDouble(segs[1]);
				double lat = Double.parseDouble(segs[2]);
				if (lat < edgeLat1 && lat > edgeLat2 && lon < edgeLon1 && lon > edgeLon2) {
					nodes.put(id, new Node(id, lat, lon));
					map.put(id, new Vector<Node>());
				}
			}
			else {
				if (half == 0) {
					System.out.println("Processed all nodes - now creating adjacency list");
					++half;
				}
				int id2 = Integer.parseInt(segs[1]);
				if (nodes.containsKey(id) && nodes.containsKey(id2)) {

					//				System.out.println(nodes.get(id).toString());
					int direction = Integer.parseInt(segs[2]);
					if (direction == 0) {
						map.get(id).add(nodes.get(id2));
						map.get(id2).add(nodes.get(id));
					} else if (direction == 1) {
						map.get(id).add(nodes.get(id2));
					} else {
						map.get(id2).add(nodes.get(id));
					}
				}
			}
		}
		input.close();
	}


	public static boolean aStar (Node start, Node goal) {

		distance = 0;
		solution.clear();

		start.setGval(0);
		start.setHval(distance(start, goal));

		Node current = new Node();

		NodeRule guage = new NodeRule();

		Vector<Node> visited = new Vector<Node>();

		PriorityQueue<Node> frontier = new PriorityQueue<Node>(1, guage);
		frontier.add(start);

		HashMap<Node, Node> path = new HashMap<Node, Node>();


		while (!frontier.isEmpty()) {

			current = frontier.peek();
			//			System.out.println("Current node is : " + current.toString());
			//			System.out.println("Distance to goal: " + current.hval());
			//			System.out.println("Distance traveled: " + current.gval());
			if (current.equals(goal)) {
				distance = current.gval();
				solution.addFirst(current);
				while (!current.equals(start)) {
					current = path.get(current);
					solution.addFirst(current);
				}
				//				solution.addFirst(start);
				return true;
			}

			visited.add(frontier.remove());

			for (Node n : map.get(current.id())) {
				if (!visited.contains(n)) {

					double gtemp = current.gval() + distance(current, n);

					if (!frontier.contains(n) || gtemp < n.gval()) {
						path.put(n,  current);
						n.setGval(gtemp);
						n.setHval(distance(n, goal));
						if (!frontier.contains(n)) {
							frontier.add(n);
						}
					}	
				}
			}
		}
		return false;
	}

	
	public static double distance(Node n1, Node n2) {
		double xs = (n2.lon() - n1.lon()) * milesPerLon;
		double ys = (n2.lat() - n1.lat()) * milesPerLat;
		xs = xs * xs;
		ys = ys * ys;
		return Math.sqrt(xs + ys);
	}

	public static double latLonDist (Node n1, Node n2) {
		double root1 = Math.sin(Math.abs((Math.toRadians(n2.lat()) - Math.toRadians(n1.lat()))) / 2.0);
		root1 = root1 * root1;
		double root2 = Math.sin(Math.abs((Math.toRadians(n2.lon()) - Math.toRadians(n1.lon())))/2.0);
		root2 = root2 * root2;
		root2 = root2 * Math.cos(Math.toRadians(n1.lat())) * Math.cos(Math.toRadians(n1.lat()));
		root1 = root1 + root2;
		return 2 * RAD * Math.asin(Math.sqrt(root1));
	}

}
