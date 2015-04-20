//Pranav Batra
//V1.0

import java.io.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

	public class lint {
	public static double edgeLat1 = 40.8755576;
	public static double edgeLon1 = -73.9056675;
	public static double edgeLat2 = 40.6992052;
	public static double edgeLon2 = -74.021833;
	public static double milesPerLat = 12.185089 / (edgeLat1 - edgeLat2);
	public static double milesPerLon = 6.06893 / (edgeLon1 - edgeLon2);

	public static double distance(double lat1,double lon1,double lat2,double lon2) {
		double xs = (lon2 - lon1) * milesPerLon;
		double ys = (lat2 - lat1) * milesPerLat;
		xs = xs * xs;
		ys = ys * ys;
		return Math.sqrt(xs + ys);
	}
	
	public static void main(String args[]) {try{
	long t=new Date().getTime(); //start time
	long ero=0; //various statistics & error couts
	long err1=0;
	long err2=0;
	long err3=0;
	long err4=0;
	long err5=0;
	long err6=0;
	long err7=0;
	long errt=0;
	long spe=0;
	long sper=0;
	long sper1=0;
	long sper2=0;
	long sper3=0;
	long sper4=0;
	double merr=0; //mean square error (distance)
	double mer=0; //mean error (distance)
	//use the mean square error & the mean error to calculate the standard deviation.
	int merc=0; //mean square error count (the number of lines used)
	PrintWriter er = new PrintWriter(new File("goodline.txt"));
	int n=15000; //step size = 1 second, rounded. slightly over 4 hours covered.
	int step=100; //max distance => 150 miles. binned to .01 miles.
	int ostep=100; //max overcharge => 150 miles. binned to .01 miles.
	//Might be interesting to visualize the dependence of overcharge on distance (goc). A trie can be used to store the sparse matrix
	//necessary for the visualization.
	int mstep=10000; //max error: 1.5 miles. binned to .0001 miles.
	double maxlon=-71.790277;
	double minlon=-79.765001;
	double maxlat=45.011668;
	double minlat=40.494443;
	//as expected, new york is rectangularly shaped.
	//using http://answers.google.com/answers/threadview?id=149284 for lat,long coordinate boundaries.
	double maxlonr=-100; //actual lat, long boundaries of data
	double minlonr=100;
	double maxlatr=-100;
	double minlatr=100;
	int hist[]=new int[n]; //histogram of time durations, really
	int d3[]=new int[n]; //histogram of geographic distances
	int oc[]=new int[n]; //histogram of overcharge distance
	int merd[]=new int[n]; //histogram of distance from start, end grid points
	//int goc[][]=new int[n][n]; //goc
	double nlat[]=new double[1200000];
	double nlon[]=new double[1200000];
	int gstep=100; //up to 150 mph
	int grate[]=new int[n]; //rate
	int dtr[]=new int[n]; //true distance
	
	int gsize=100;//can merge squares to reduce grid size. around 13 miles per square. 
	double grid[][][][]=new double[gsize][gsize][7][24]; //rates gridded over distance, time
	int gridd[][][][]=new int[gsize][gsize][7][24]; //associated counts
	int ggridd[][]=new int[gsize][gsize]; //counts gridded over distance only
	int tgrid[][]=new int[7][24]; //counts gridded over time only
	
	Calendar c = Calendar.getInstance();
	SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //date format
	parser.setLenient(false);
	
	BufferedReader map = new BufferedReader(new FileReader("map.tsv"));
	String[] mlin;
	while ((mlin = map.readLine().split("\t")).length == 4 ) //node id lookup
	{
	nlat[Integer.parseInt(mlin[0])]=Double.parseDouble(mlin[2]);
	nlon[Integer.parseInt(mlin[0])]=Double.parseDouble(mlin[1]);
	}
	map.close();
	System.out.println("MAP PROCESSED");
	
	int ln=0;
	for(int i=0;i<n;i++){hist[i]=0;d3[i]=0;oc[i]=0;merd[i]=0;grate[i]=0;dtr[i]=0;/*op[i]=0;*/} //initialization
	for(int i=1;i<13;i++){ //just first for now.
	BufferedReader br = new BufferedReader(new FileReader("trip_data_"+Integer.toString(i)+".csv")); //trip data, csv
	BufferedReader tr = new BufferedReader(new FileReader("trip_data_"+Integer.toString(i)+"_position_output.txt")); //A* distance, tsv
    String line=br.readLine(); //skip first line of trip data (header)
	String l2; //don't skip first line of A* distance (no header)
    while ((line = br.readLine()) != null) {l2=tr.readLine(); ln++; //line counter
	String[] tokens=line.split(",");
	String[] tok2=l2.split("\t");
	double tok=Double.parseDouble(tok2[0]);
	if(tok==0)sper++; //distribution of errors for A* search (see AStar.java for the meaning of each error code)
	else if(tok==-1)sper1++;
	else if(tok==-2)sper2++;
	else if(tok==-3)sper3++;
	else if(tok==-4)sper4++;
	else spe++;
	try{
	err1++; //missing_field count
	int time=Integer.parseInt(tokens[8]);
	double dist=Double.parseDouble(tokens[9]);
	double lat1=Double.parseDouble(tokens[11]);
	double lon1=Double.parseDouble(tokens[10]);
	double lon2=Double.parseDouble(tokens[12]);
	double lat2=Double.parseDouble(tokens[13]);

	long s = parser.parse(tokens[5]).getTime();
	long e = parser.parse(tokens[6]).getTime();
	err1--;
	
	c.setTimeInMillis((s+e)/2); //time midpoint
	int d2=(int)Math.floor(step*distance(lat1,lon1,lat2,lon2)); //geographic distance
	
	if(lat1==0 || lat2==0 || lon1==0 || lon2==0 || time==0 || dist==0){err2++;continue;} //0_in_field
	if(d2==0){err6++;continue;} //geographic_distance_0
	
	if(lat1>maxlat || lat2>maxlat || lon1>maxlon || lon2>maxlon || lat1<minlat || lat2<minlat || lon1<minlon || lon2<minlon){err5++;continue;}

	if(d2>step*dist){err3++;continue;} //mistyped coordinates
	
	int ival=(int)((e-s)/1000); //time interval (end time-start time)
	if(ival!=time){err4++;continue;} //mistyped_interval or start,end times
	
	double rate=dist*3600.0/time; //rate = actual distance traveled / time duration
	if(rate>100){err7++;continue;} //arbitrary cutoff at 100 miles/hour (taxis should not be going faster than this...)
	
	if(step*dist>=n){System.out.println("B555");System.err.println(Integer.toString(i)+" "+Integer.toString(ln)+" "+Double.toString(dist));throw new Exception("B555");} //these errors only appears in input file 7, interestingly enough... (see error.log for more info).
	if(time>=n){System.err.println(Integer.toString(i)+" "+Integer.toString(ln)+" time too big");throw new Exception("ttb");}
	else if(time<0){System.err.println(Integer.toString(i)+" "+Integer.toString(ln)+" time too small");throw new Exception("tts");}
	
	er.println(Integer.toString(i)+"\t"+Integer.toString(ln)); //list of valid lines (taxi trips)
	
	hist[time]++; //time duration histogram
	grate[(int)Math.floor(gstep*rate)]++; //rate histogram
	dtr[(int)(step*dist)]++; //distance traveled histogram
	
	int dy=c.get(Calendar.DAY_OF_WEEK)-1; //day of week (0-6)
	int hr=c.get(Calendar.HOUR_OF_DAY); //hour of day (0-23)
	int mll=(int)Math.floor(gsize*((lat1+lat2)/2-minlat)/(maxlat-minlat)); //midpoint latitude
	int mlo=(int)Math.floor(gsize*((lon1+lon2)/2-minlon)/(maxlon-minlon)); //midpoint longitude
	
	int cc=gridd[mll][mlo][dy][hr]++; //count associated with rates (gridded over location, time)
	grid[mll][mlo][dy][hr]+=(rate-grid[mll][mlo][dy][hr])/(cc+1); //rate (gridded over location, time)
	//numerically stable method of calculating the mean; recommended in Knuth, The Art of Computer Programming (according to stackoverflow)
	tgrid[dy][hr]++; //counts gridded by time
	ggridd[mll][mlo]++; //counts gridded by location
	d3[d2]++; //geographic distance histogram
	
	//actual coordinate boundaries
	if(lat1>maxlatr)maxlatr=lat1;
	if(lat2>maxlatr)maxlatr=lat2;
	if(lon1>maxlonr)maxlonr=lon1;
	if(lon2>minlonr)maxlonr=lon2;
	if(lat1<minlatr)minlatr=lat1;
	if(lat2<minlatr)minlatr=lat2;
	if(lon1<minlonr)minlonr=lon1;
	if(lon2<minlonr)minlonr=lon2;
	
	if(tok>0){ //if the A* search found a shortest path, calculate overcharge statistics
	int toks=Integer.parseInt(tok2[1]); //start_node
	int toke=Integer.parseInt(tok2[2]); //end_node
	double error=distance(nlat[toks],nlon[toks],lat1,lon1)+distance(nlat[toke],nlon[toke],lat2,lon2); //distance from grid points
	merc++;
	merr+=(error*error-merr)/merc; //mean square error, numerically stable calculation
	mer+=(error-mer)/merc; //mean error, numerically stable calculation
	int medd=(int)Math.floor(mstep*error); //error histogram
	if(medd>=n || medd<0){System.out.println("EEE "+Double.toString(error));throw new Error("DONE");} //the data passes this assertion
	merd[medd]++;
	int overchg = (int)Math.floor(ostep*(dist-tok)+1000)/*+n*/; //up to 10 miles under.
	if(overchg>=n){System.err.println("EOC");throw new Error("EXCESSIVE OVERCHARGE");} //the data passes these assertions
	if(overchg<0){System.err.println("EUC");throw new Error("EXCESSIVE UNDERCHARGE");}
	oc[overchg]++;
	//goc[(int)(step*dist)][overchg]++;
	}
	else ero++; //no overcharge path found
	}catch(Exception e){errt++;} //exception handling
    }System.out.println("file "+Integer.toString(i)+"processed "+Long.toString(new Date().getTime()-t));}
	
	//Print out all the data
	PrintWriter output = new PrintWriter(new File("timehist.txt"));
	for(int i=0;i<n;i++)output.println(hist[i]);
	output.close();
	output = new PrintWriter(new File("overchg.txt"));
	for(int i=0;i<n;i++)output.println(oc[i]);
	output.close();
	output = new PrintWriter(new File("dhist.txt"));
	for(int i=0;i<n;i++)output.println(d3[i]);
	output.close();
	/*output = new PrintWriter(new File("goc.txt"));
	for(int i=0;i<n;i++)for(int j=0;j<n;j++)output.println(goc[i][j]);
	output.close();*/
	output = new PrintWriter(new File("merd.txt"));	
	for(int i=0;i<n;i++)output.println(merd[i]);
	output.close();
	output = new PrintWriter(new File("grate.txt"));	
	for(int i=0;i<n;i++)output.println(grate[i]);
	output.close();
	output = new PrintWriter(new File("dtr.txt"));	
	for(int i=0;i<n;i++)output.println(dtr[i]);
	output.close();
	output = new PrintWriter(new File("grid.txt"));
	for(int i=0;i<gsize;i++)for(int j=0;j<gsize;j++)for(int k=0;k<7;k++)for(int l=0;l<24;l++)output.println(grid[i][j][k][l]);
	output.close();
	output = new PrintWriter(new File("gridd.txt"));
	for(int i=0;i<gsize;i++)for(int j=0;j<gsize;j++)for(int k=0;k<7;k++)for(int l=0;l<24;l++)output.println(gridd[i][j][k][l]);
	output.close();
	output = new PrintWriter(new File("tgrid.txt"));
	for(int k=0;k<7;k++)for(int l=0;l<24;l++)output.println(tgrid[k][l]);
	output.close();
	output = new PrintWriter(new File("ggridd.txt"));
	for(int i=0;i<gsize;i++)for(int j=0;j<gsize;j++)output.println(ggridd[i][j]);
	output.close();
	output = new PrintWriter(new File("stats.txt")); //generic stats
	output.println(mer);
	output.println(merr);
	output.println(merc);
	output.println(ero);
	output.println(err1);
	output.println(err2);
	output.println(err3);
	output.println(err4);
	output.println(err5);
	output.println(err6);
	output.println(err7);
	output.println(errt);
	output.println(minlatr);
	output.println(maxlatr);
	output.println(minlonr);
	output.println(maxlonr);
	output.println(spe);
	output.println(sper);
	output.println(sper1);
	output.println(sper2);
	output.println(sper3);
	output.println(sper4);
	output.println(new Date().getTime()-t);
	output.close();
	System.out.println("err1: "+Double.toString(err1)+",err2: "+Double.toString(err2)+", err3:"+Double.toString(err3)+", errr4:"+Double.toString(err4)+", errt:"+Double.toString(errt));
}catch(Exception e){System.err.println("ERROR");System.err.println(e.toString());}}} //no fatal errors encountered.