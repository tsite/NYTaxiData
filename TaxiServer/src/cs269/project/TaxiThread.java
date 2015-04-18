package cs269.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;
import java.util.Map.Entry;

import cs269.project.NodeRule;

public class TaxiThread extends Thread {

	private ServerSocket serv;
	private Socket sock;
	
	public static ArrayDeque<Node> solution = new ArrayDeque<Node>();
	public static double distance = 0;
	public static double time = 0;

	Vector<Double> thetas = new Vector<Double>();
	Vector<Double> means = new Vector<Double>();
	Vector<Double> sds = new Vector<Double>();
			
	public static HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	public static HashMap<Integer, Vector<Node>> map = new HashMap<Integer, Vector<Node>>();
	
	public static int maxID = 0;
	
	public static final String FILENAME = "map.tsv";
	
	public static final String THETA = "theta_calc_backup.txt";
	public static final String MEAN = "meanFeatures.txt";
	public static final String SD = "stdevFeatures.txt";
	
	public static double edgeLat1 = 40.8755576;
	public static double edgeLon1 = -73.9056675;
	public static double edgeLat2 = 40.6992052;
	public static double edgeLon2 = -74.021833;
	public static double milesPerLat = 12.185089 / (edgeLat1 - edgeLat2);
	public static double milesPerLon = 6.06893 / (edgeLon1 - edgeLon2);
	
	public static double minlat=100,maxlat=-100,minlon=100,maxlon=-100;
	public static int gridLat=1000; //increase this number to increase the speed
	public static int gridLon=1000;
	
	public static ArrayDeque<Node>[][] grid=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon];
	public static ArrayDeque<Node>[][] gex=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon];
	
	public TaxiThread(ServerSocket serv, Socket sock) {//, ArrayDeque<Node>[][] grid, ArrayDeque<Node>[][] gex) {
		this.serv = serv;
		this.sock = sock;
//		this.grid = grid;
//		this.gex = gex;
	}
	
	@Override
	public void run() {
		
		System.out.println("New connection from " + sock.getInetAddress() + " - new thread started with id " + + this.getId());
		
		BufferedReader in = null;
		PrintWriter out = null;
		
		try {
			
			Scanner theta = new Scanner(new File(THETA));
			
			while (theta.hasNextLine()) {
				thetas.add(Double.parseDouble(theta.nextLine()));
			}
			
			Scanner mean = new Scanner(new File(MEAN));
			
			while (mean.hasNextLine()) {
				means.add(Double.parseDouble(mean.nextLine()));
			}
			
			Scanner sd = new Scanner(new File(SD));
			
			while (sd.hasNextLine()) {
				sds.add(Double.parseDouble(sd.nextLine()));
			}
			
			
			
			makeMap(new File(FILENAME));	
			
			for (Entry<Integer, Node> e : nodes.entrySet()) {
				//nodeArray[e.getValue().id()] = e.getValue();
				Node e2=e.getValue();
				if(e2.lat()<minlat)minlat=e2.lat();if(e2.lon()<minlon)minlon=e2.lon();
				if(e2.lat()>maxlon)maxlat=e2.lat();if(e2.lon()>maxlon)maxlon=e2.lon();
			}
			System.out.println(minlat+" "+maxlat+" "+minlon+" "+maxlon);
			for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++){grid[i][j]=new ArrayDeque<Node>();gex[i][j]=new ArrayDeque<Node>();}
			System.out.println("debug1");
			for (Entry<Integer, Node> e : nodes.entrySet()) {
			Node e2=e.getValue();
			int la=(int)(gridLat*(e2.lat()-minlat)/(1.+maxlat-minlat));
			int lo=(int)(gridLon*(e2.lon()-minlon)/(1.+maxlon-minlon));
			grid[la][lo].add(e2);
			for(int i=-1;i<2;i++)for(int j=-1;j<2;j++)if((i!=0 || j!=0) && la+i>=0 && la+i<gridLat && lo+j>=0 && lo+j<=gridLon)gex[la+i][lo+j].add(e2);
			}
			System.out.println("debug2");
			int mx=0;
			for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++)if(grid[i][j].size()>mx)mx=grid[i][j].size();
			System.out.println(mx);
			
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
		
			out.println("FINISHED_SETUP");
			
			String next;
			while ((next = in.readLine()) != null) {
				System.out.println("Incoming message from " + sock.getInetAddress() + ": " + next);
				if (next == "FINISHED") {
					break;
				}
				else {
					// TODO: figure out how to get this data from client
					String[] coords = next.split(",");
					double lat1 = Double.parseDouble(coords[0]);
					double lon1 = Double.parseDouble(coords[1]);
					double lat2 = Double.parseDouble(coords[2]);
					double lon2 = Double.parseDouble(coords[3]);
					
					System.out.println("Lat1 = " + lat1 + ", Lon2 = " + lon1 + ", Lat2 = " + lat2 + ", Lon2 = " + lon2); 
					
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
					{distance=-2;/*System.out.println("BAAD LINE");output.println("MISS");*/}
					else{
					for (Node e2 : (ArrayDeque<Node>)grid[la1][lo1]) {
						System.out.println("checking");
						if (distance(p, curP) > distance(p, e2)) {
							curP = e2;
							if (!changed1) {
								changed1 = true;
							}
						}
						}
					if(!changed1)for (Node e2 : (ArrayDeque<Node>)gex[la1][lo1]) {
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
					}if(!changed2)for (Node e2 : (ArrayDeque<Node>)gex[la2][lo2]) {
						if (distance(d, curD) > distance(d, e2)) {
							curD = e2;
							if (!changed2) {
								changed2 = true;
							}
						}
					}}
					
					System.out.println("Node1: " + curP.toString());
					System.out.println("Node2: " + curD.toString());
					
					if (changed1 && changed2) {
						if (aStar(curP, curD, 2*distance(curP,curD)+2)) {
							System.out.println("Optimal path is " + distance + " miles.");
						
							
							int day = dayOfWeek();
							int time = timeInSecs();
							double strDistance = distance(curP, curD);
							double startLon = curP.lon();
							double startLat = curP.lat(); 
							double endLon = curD.lon();
							double endLat = curD.lat(); 
							double comLon = (endLon - startLon);
							double comLat = (endLat - startLat);
							double midLon = (0.5 * (endLon + startLon));
							double midLat = (0.5 * (endLat + startLat));
							
							Vector<Double> features = new Vector<Double>();
							for (int i = 1; i < 10; ++i) {
								features.add(Math.pow(day, i));
								features.add(Math.pow(time, i));
								features.add(Math.pow(strDistance, i));
								features.add(Math.pow(comLon, i));
								features.add(Math.pow(comLat, i));
								features.add(Math.pow(midLon, i));
								features.add(Math.pow(midLat, i));
								features.add(Math.pow(startLon, i));
								features.add(Math.pow(startLat, i));
								features.add(Math.pow(endLon, i));
								features.add(Math.pow(endLat, i));
							}
							
							Vector<Double> scaled = new Vector<Double>();
							scaled.add(1.0);
							System.out.println(features.size());
							System.out.println(sds.size());
							System.out.println(means.size());
							for (int i = 0; i < features.size(); ++i) {
								scaled.add((features.elementAt(i) - means.elementAt(i)) / sds.elementAt(i));
							}
							
							time = 0;
							for (int i = 0; i < scaled.size(); ++i) {
								time += (thetas.elementAt(i) * scaled.elementAt(i));
							}
							
							out.println(distance + "," + time);
							
						}
						else {
							System.out.println("No solution found");
							out.println("ERROR: No solution found...");
						}
					} else {
						System.out.println("Coordinates not found on map");
						out.println("ERROR: Coordinates outside of map...");
					}
				}
			}
		
		} catch (IOException e) {

			// handle read/write errors
			e.printStackTrace();

		} finally {
			System.out.println("Closing thread with thread id = " + this.getId());
			if(out != null) {
				// close the writer, it would implicitly close the socket
				out.close();
			} else {
				// silently close socket
				try{ sock.close(); } catch (IOException ignored) {};
			}			
		}
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
				distance = current.gval();
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
	
	public static void makeMap(File filename) throws FileNotFoundException {
		int half = 0;
		Scanner input = new Scanner(filename);
		while (input.hasNextLine()) {
			String line = input.nextLine();
			String[] segs = line.split("\t");

			int id = Integer.parseInt(segs[0]);
			if (segs.length == 4) {
				if (id > maxID) {
					maxID = id;
				}
				double lon = Double.parseDouble(segs[1]);
				double lat = Double.parseDouble(segs[2]);
				
				nodes.put(id, new Node(id, lat, lon));
				map.put(id, new Vector<Node>());
			}
			else {
				if (half == 0) {
					System.out.println("Processed all nodes - now creating adjacency list");
					++half;
				}
				int id2 = Integer.parseInt(segs[1]);
				
				if (nodes.containsKey(id) && nodes.containsKey(id2)) {
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
	
	public static int timeInSecs() {
		Date now = new Date();
		SimpleDateFormat fHour = new SimpleDateFormat("H");
		SimpleDateFormat fMinute = new SimpleDateFormat("m");
		SimpleDateFormat fSecond = new SimpleDateFormat("s");
		int hour = Integer.parseInt(fHour.format(now));
		int minute = Integer.parseInt(fMinute.format(now));
		int second = Integer.parseInt(fSecond.format(now));
		int time = (hour * 3600) + (minute * 60) + second;
		return time;
	}
	
	public static int dayOfWeek() {
		Date now = new Date();
		SimpleDateFormat fDay = new SimpleDateFormat("E");
		int day = 0;
		String dayOfWeek = fDay.format(now);
		if (dayOfWeek == "Sunday") {
			day = 0;
		} else if (dayOfWeek == "Monday") {
			day = 1;
		} else if (dayOfWeek == "Tuesday") {
			day = 2;
		} else if (dayOfWeek == "Wednesday") {
			day = 3;
		} else if (dayOfWeek == "Thursday") {
			day = 4;
		} else if (dayOfWeek == "Friday") {
			day = 5;
		} else if (dayOfWeek == "Saturday") {
			day = 6;
		}
		return day;
	}
	
	
	
	public static double distance(Node n1, Node n2) {
		double xs = (n2.lon() - n1.lon()) * milesPerLon;
		double ys = (n2.lat() - n1.lat()) * milesPerLat;
		xs = xs * xs;
		ys = ys * ys;
		return Math.sqrt(xs + ys);
	}
	
	
}
