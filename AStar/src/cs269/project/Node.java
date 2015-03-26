package cs269.project;

public class Node {

	private int id;
	private double lat;
	private double lon;
	private double hval;
	private double gval;
	private double fval;

	public Node() {
		id = 0;
		lat = 0;
		lon = 0;
		hval = 0;
		gval = 0;
		fval = 0;
	}
	
	public Node(int id, double lat, double lon) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		hval = 0;
		gval = 0;
		fval = 0;
	}
	
	public Node(int id, double lat, double lon, double hval, double gval) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.hval = hval;
		this.gval = gval;
		fval = gval + hval;
	}
	
	public int id() {
		return id;
	}
	
	public void setID(int i) {
		id = i;
	}
	
	public double lat() {
		return lat;
	}
	
	public void setLat(double l) {
		lat = l;
	}
	
	public double lon() {
		return lon;
	}
	
	public void setLon(double l) {
		lon = l;
	}
	
	public double hval() {
		return hval;
	}
	
	public void setHval(double h) {
		hval = h;
		fval = hval + gval;
	}
	
	public double gval() {
		return gval;
	}
	
	public void setGval(double g) {
		gval = g;
		fval = hval + gval;
	}
	
	public double fval() {
		return fval;
	}
	
	public boolean equals(Node rhs) {
		return (this.lat == rhs.lat && this.lon == rhs.lon);
	}
	
	public String toString() {
		return ("id: " + id + "\tlat/lon: " + lat + ",  " + lon);
	}
}
