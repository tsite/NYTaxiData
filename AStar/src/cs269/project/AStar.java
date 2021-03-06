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
 * Uses straight line distance between lat lon points
 * Nodes are assigned to grid squares (on a 1000x1000 grid) to speed up the search for the start, end nodes.
 * The squares that start/end coordinates lie in along with the 8 surrounding squares on the grid are all searched
 * to find the nearest node on the graph.
 * 
 * Distance outputs are correctly estimating optimal distance.
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
	public static int gridLat=1000; //increase this number to increase the speed
	public static int gridLon=1000;
	public static ArrayDeque<Node>[][] grid=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon];
	public static ArrayDeque<Node>[][] gex=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon]; //slows the runtime a lot, 2-3x as slow, but necessary. can run on all four cores for 4x speed.
	public static double minlat=100,maxlat=-100,minlon=100,maxlon=-100;

	

	public static void main(String args[]) {

		System.out.println("Processing map information...");

		try {
			makeMap(new File(FILENAME));
		} catch (FileNotFoundException e) {
			System.out.println("Data file not found.");
		}

		//Node[] nodeArray = new Node[maxID + 1];
		
		for (Entry<Integer, Node> e : nodes.entrySet()) {
			//nodeArray[e.getValue().id()] = e.getValue();
			Node e2=e.getValue();
			if(e2.lat()<minlat)minlat=e2.lat();if(e2.lon()<minlon)minlon=e2.lon();
			if(e2.lat()>maxlon)maxlat=e2.lat();if(e2.lon()>maxlon)maxlon=e2.lon();
		}
		System.out.println(minlat+" "+maxlat+" "+minlon+" "+maxlon);
		for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++){grid[i][j]=new ArrayDeque<Node>();gex[i][j]=new ArrayDeque<Node>();}
		for (Entry<Integer, Node> e : nodes.entrySet()) {
		Node e2=e.getValue();
		int la=(int)(gridLat*(e2.lat()-minlat)/(1.+maxlat-minlat));
		int lo=(int)(gridLon*(e2.lon()-minlon)/(1.+maxlon-minlon));
		grid[la][lo].add(e2);
		for(int i=-1;i<2;i++)for(int j=-1;j<2;j++)if((i!=0 || j!=0) && la+i>=0 && la+i<gridLat && lo+j>=0 && lo+j<=gridLon)gex[la+i][lo+j].add(e2);
		}
		//int mx=0;
		//for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++)if(grid[i][j].size()>mx)mx=grid[i][j].size();
		//System.out.println(mx);
		//=>515 max nodes (@1000 each)! very reasonable. can increase squares to decrease nodes per square

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

		/*Scanner console = new Scanner(System.in);
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

					if (aStar(start, goal, 2*distance(start,goal)+2)) {
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
				String f = console.nextLine();*/
				String[] files = args;

				System.out.println("Processing " + files.length + " files...");

				for (int i = 0; i < files.length; ++i) {
					System.out.println("Processing file " + files[i] + "...");
					try {
						Scanner input = new Scanner(new File(files[i]));
						String name = files[i].substring(0, files[i].indexOf("."));
						PrintWriter output = new PrintWriter(new File(name + "_output.txt"));
						//input.nextLine(); DO NOT SKIP FIRST LINE AS HEADER IS REMOVED
						int counter = 0;
						long ss=System.currentTimeMillis();
						while (input.hasNextLine()) {
						//long s=System.currentTimeMillis();
							++counter;
							if (counter > 999 && counter%1000 == 0) {
								System.out.println("Processed " + counter +" lines");
								System.out.println("Time "+(System.currentTimeMillis()-ss));
								ss=System.currentTimeMillis();
							}
							
							String ride = input.nextLine();
							String[] data = ride.split(",");
							try{
							double lat1 = Double.parseDouble(/*data[11]*/data[2]);
							double lon1 = Double.parseDouble(/*data[10]*/data[1]);
							double lat2 = Double.parseDouble(/*data[13]*/data[4]);
							double lon2 = Double.parseDouble(/*data[12]*/data[3]);
							double dist = Double.parseDouble(/*data[9]*/data[0]);

							Node p = new Node(0, lat1, lon1);
							Node d = new Node(0, lat2, lon2);
							Node curP = new Node();
							Node curD = new Node();
							boolean changed1 = false;
							boolean changed2 = false;
							//Entry<Integer,Node> e : 
							int la1=(int)(gridLat*(lat1-minlat)/(1.+maxlat-minlat));
							int lo1=(int)(gridLon*(lon1-minlon)/(1.+maxlon-minlon));
							int la2=(int)(gridLat*(lat2-minlat)/(1.+maxlat-minlat));
							int lo2=(int)(gridLon*(lon2-minlon)/(1.+maxlon-minlon));
							distance=-3;
							if(la1<0 || la1>=gridLat || la2<0 || la2>=gridLat || lo1<0 || lo1>=gridLon || lo2<0 || lo2>=gridLon)
							{distance=-2;/*System.out.println("BAD LINE");output.println("MISS");*/}
							else{
							for (Node e2 : (ArrayDeque<Node>)grid[la1][lo1]) {
								if (distance(p, curP) > distance(p, e2)) {
									curP = e2;
									if (!changed1) {
										changed1 = true;
									}
								}
								}
							/*if(!changed1)*/for (Node e2 : (ArrayDeque<Node>)gex[la1][lo1]) {
								if (distance(p, curP) > distance(p, e2)) {
									curP = e2;
									if (!changed1) {
										changed1 = true;
									}
								}
								}
							for (Node e2 : (ArrayDeque<Node>)grid[la2][lo2]) {
								if (distance(d, curD) > distance(d, e2)) {
									curD = e2;
									if (!changed2) {
										changed2 = true;
									}
								}
							}/*if(!changed2)*/for (Node e2 : (ArrayDeque<Node>)gex[la2][lo2]) {
								if (distance(d, curD) > distance(d, e2)) {
									curD = e2;
									if (!changed2) {
										changed2 = true;
									}
								}
							}}
							//if(changed1 && changed2)System.out.print((System.currentTimeMillis()-s)+" "+distance(curP,curD)+" ");
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
								
								aStar(curP, curD, dist+.5);	
							} else {
								//distance = -1;
							}
							output.println(distance+"\t"+curP.id()+"\t"+curD.id());
							//output.flush();
							//System.out.println(System.currentTimeMillis()-s);
						}catch(Exception e){/*System.out.println("BAD LINE FOUND");*/output.println("-4.0\t0\t0");}} //BAD_LINE
						System.out.println(files[i] + " finished processing.");
						input.close();
						output.close();
					} catch (FileNotFoundException e) {
						System.out.println("File not found... moving to next file...");
					}
				}
				System.out.println("All files processed.");
			/*}
		} while (choice != 3);

		console.close();*/
		System.out.println("Goodbye");
	}

	public static void makeMap(File filename) throws FileNotFoundException {
		int half = 0;//double b=0;
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
				if (/*lat < edgeLat1 && lat > edgeLat2 && lon < edgeLon1 && lon > edgeLon2*/true) {
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
				//double d=distance(nodes.get(id),nodes.get(id2));
				//if(d>b && d<1.85)b=d;
				
				if (nodes.containsKey(id) && nodes.containsKey(id2)) {

					//				System.out.println(nodes.get(id).toString());
					int direction = Integer.parseInt(segs[2]);
					if (direction == 0) {
						map.get(id).add(nodes.get(id2));
						map.get(id2).add(nodes.get(id));
					} else if (direction == 1) {
						map.get(id).add(nodes.get(id2));
					} else {
					//System.out.println("abba");
						map.get(id2).add(nodes.get(id));
					}
				}
			}
		}
		//System.out.println(b);
		input.close();
	}


	public static boolean aStar (Node start, Node goal, double dist) {

		distance = -1;
		solution.clear();

		start.setGval(0);
		start.setHval(distance(start, goal));

		Node current = new Node();

		NodeRule guage = new NodeRule();

		Vector<Node> visited = new Vector<Node>();

		PriorityQueue<Node> frontier = new PriorityQueue<Node>(1, guage);
		frontier.add(start);
		//commenting out the path doesn't really same any time, but it can't hurt.
//		HashMap<Node, Node> path = new HashMap<Node, Node>();


		while (!frontier.isEmpty()) {

			current = frontier.peek();
			//			System.out.println("Current node is : " + current.toString());
			//			System.out.println("Distance to goal: " + current.hval());
			//			System.out.println("Distance traveled: " + current.gval());
			if (current.equals(goal)) {
				distance = current.gval(); //current.gval()==current.fval()
//				solution.addFirst(current);
//				while (!current.equals(start)) {
//					current = path.get(current);
//					solution.addFirst(current);
//				}
				//				solution.addFirst(start);
				return true;
			}

			visited.add(frontier.remove());

			for (Node n : map.get(current.id())) {
				if (!visited.contains(n)) {

					double gtemp = current.gval() + distance(current, n);

					if (!frontier.contains(n) || gtemp < n.gval()) {
//						path.put(n,  current);
						n.setGval(gtemp);
						n.setHval(distance(n, goal));
						if (!frontier.contains(n) && n.fval()<=dist) {
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
