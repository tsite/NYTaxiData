package cs269.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import java.util.Map.Entry;

import cs269.project.Node;
import cs269.project.TaxiThread;


public class TaxiServer {

	public static final int PORT = 2000;

	public static HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	public static HashMap<Integer, Vector<Node>> map = new HashMap<Integer, Vector<Node>>();
	public static final String FILENAME = "map.tsv";
	public static ArrayDeque<Node> solution = new ArrayDeque<Node>();
	public static int maxID = 0;
	public static int gridLat=1000; //increase this number to increase the speed
	public static int gridLon=1000;
	public static ArrayDeque<Node>[][] grid=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon];
	public static ArrayDeque<Node>[][] gex=(ArrayDeque<Node>[][])new ArrayDeque[gridLat][gridLon];
	public static double minlat=100,maxlat=-100,minlon=100,maxlon=-100;
	
	public static boolean loop = true;

	
	public static void main (String args[]) {
		
		try {
//			makeMap(new File(FILENAME));	
//			
//			for (Entry<Integer, Node> e : nodes.entrySet()) {
//				//nodeArray[e.getValue().id()] = e.getValue();
//				Node e2=e.getValue();
//				if(e2.lat()<minlat)minlat=e2.lat();if(e2.lon()<minlon)minlon=e2.lon();
//				if(e2.lat()>maxlon)maxlat=e2.lat();if(e2.lon()>maxlon)maxlon=e2.lon();
//			}
//			System.out.println(minlat+" "+maxlat+" "+minlon+" "+maxlon);
//			for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++){grid[i][j]=new ArrayDeque<Node>();gex[i][j]=new ArrayDeque<Node>();}
//			System.out.println("debug1");
//			for (Entry<Integer, Node> e : nodes.entrySet()) {
//			Node e2=e.getValue();
//			int la=(int)(gridLat*(e2.lat()-minlat)/(1.+maxlat-minlat));
//			int lo=(int)(gridLon*(e2.lon()-minlon)/(1.+maxlon-minlon));
//			grid[la][lo].add(e2);
//			for(int i=-1;i<2;i++)for(int j=-1;j<2;j++)if((i!=0 || j!=0) && la+i>=0 && la+i<gridLat && lo+j>=0 && lo+j<=gridLon)gex[la+i][lo+j].add(e2);
//			}
//			System.out.println("debug2");
//			int mx=0;
//			for(int i=0;i<gridLat;i++)for(int j=0;j<gridLon;j++)if(grid[i][j].size()>mx)mx=grid[i][j].size();
//			System.out.println(mx);
			
			ServerSocket serv = new ServerSocket(PORT);
			serv.setSoTimeout(1000);
			
			System.out.println("Listening on port: " + PORT);
			
			System.out.println("Accepting connections...");
			Vector<TaxiThread> threads = new Vector<TaxiThread>();
			
			while (loop) {
				try {
					Socket s = serv.accept();
					TaxiThread tt = new TaxiThread(serv, s);//, grid, gex);
					threads.add(tt);
					tt.start();
				} catch (SocketTimeoutException ignore) {}
			}
		
			for (TaxiThread t : threads) {
				try {
					t.join();
				} catch (InterruptedException ignored) {}
			}
			
			System.out.println("Shutting down server.");
		
		
		} catch (FileNotFoundException e) {
			System.out.println("File " + FILENAME + " not found.");
			//TODO: deal with this for client
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
	
}
