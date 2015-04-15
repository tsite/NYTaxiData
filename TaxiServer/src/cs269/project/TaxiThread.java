package cs269.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Vector;

import cs269.project.NodeRule;

public class TaxiThread extends Thread {

	ServerSocket serv;
	Socket sock;
	
	public static ArrayDeque<Node> solution = new ArrayDeque<Node>();
	public static double distance = 0;

	
	public static double edgeLat1 = 40.8755576;
	public static double edgeLon1 = -73.9056675;
	public static double edgeLat2 = 40.6992052;
	public static double edgeLon2 = -74.021833;
	public static double milesPerLat = 12.185089 / (edgeLat1 - edgeLat2);
	public static double milesPerLon = 6.06893 / (edgeLon1 - edgeLon2);
	
	public static double minlat=100,maxlat=-100,minlon=100,maxlon=-100;
	public static int gridLat = TaxiServer.gridLat; //increase this number to increase the speed
	public static int gridLon = TaxiServer.gridLon;
	
	public TaxiThread(ServerSocket serv, Socket sock) {
		this.serv = serv;
		this.sock = sock;
	}
	
	@Override
	public void run() {
		
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
		
			String next;
			while ((next = in.readLine()) != null) {
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
					for (Node e2 : (ArrayDeque<Node>)TaxiServer.grid[la1][lo1]) {
						if (distance(p, curP) > distance(p, e2)) {
							curP = e2;
							if (!changed1) {
								changed1 = true;
							}
						}
						}
					if(!changed1)for (Node e2 : (ArrayDeque<Node>)TaxiServer.gex[la1][lo1]) {
						if (distance(p, curP) > distance(p, e2)) {
							curP = e2;
							if (!changed1) {
								changed1 = true;
							}
						}
						}
					for (Node e2 : (ArrayDeque<Node>)TaxiServer.grid[la2][lo2]) {
						if (distance(d, curD) > distance(d, e2)) {
							curD = e2;
							if (!changed2) {
								changed2 = true;
							}
						}
					}if(!changed2)for (Node e2 : (ArrayDeque<Node>)TaxiServer.gex[la2][lo2]) {
						if (distance(d, curD) > distance(d, e2)) {
							curD = e2;
							if (!changed2) {
								changed2 = true;
							}
						}
					}}
					
					
					if (changed1 && changed2) {
						if (aStar(curP, curD, 2*distance(curP,curD)+2)) {
							System.out.println("Optimal path is " + distance + " miles.");
							//TODO return solution to client.
							out.print(distance);
							
//							while (!solution.isEmpty()) {
//								Node cur = solution.pollFirst();
//								System.out.println(cur.toString());
//							}
						}
						else {
							System.out.println("No solution found");
							out.print("No solution found...");
						}
					} else {
						System.out.println("Coordinates not found on map");
						out.print("Coordinates outside of map...");
					}
				}
			}
		
		} catch (IOException e) {

			// handle read/write errors
			e.printStackTrace();

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

			for (Node n : TaxiServer.map.get(current.id())) {
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
	
	
}
