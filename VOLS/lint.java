//Pranav Batra
//Find the distribution of traffic volume by hour of day and day of week.
//Took slightly over an hour (3638893 seconds) to run.
//Counters that did not record traffic on a hourly basis were excluded from the tally.
import java.io.*;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

	public class lint {
	public static void main(String args[]) {try{
	long t=new Date().getTime();
	int n=1000000;
	boolean rcid[]=new boolean[n];
	long hist[][]=new long[7][24];
	long hcount[][]=new long[7][24];
	for(int i=0;i<n;i++)rcid[i]=false;
	Calendar c = Calendar.getInstance();
	SimpleDateFormat parser = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	//parser.setLenient(false);

	for(int i=1;i<12;i++){
	BufferedReader br = new BufferedReader(new FileReader("VOL_20"+String.format("%02d",i)+".csv"));
    String line=br.readLine(); //skip first line
    while ((line = br.readLine()) != null) {
	String[] tokens=line.split(",");
	c.setTimeInMillis(parser.parse(tokens[1]).getTime());
	int dy=c.get(Calendar.DAY_OF_WEEK)-1;
	int hr=c.get(Calendar.HOUR_OF_DAY);
	int min=c.get(Calendar.MINUTE);
	if(min!=0)rcid[Integer.parseInt(tokens[0])]=true;
    }
	br.close();
	br=new BufferedReader(new FileReader("VOL_20"+String.format("%02d",i)+".csv"));
	br.readLine();
	while ((line = br.readLine()) != null) {
	String[] tokens=line.split(",");
	c.setTimeInMillis(parser.parse(tokens[1]).getTime());
	int dy=c.get(Calendar.DAY_OF_WEEK)-1;
	int hr=c.get(Calendar.HOUR_OF_DAY);
	int min=c.get(Calendar.MINUTE);
	if(rcid[Integer.parseInt(tokens[0])]==false){hist[dy][hr]+=Integer.parseInt(tokens[4]);hcount[dy][hr]++;} //HOURLY COUNTS ONLY.
	}
	System.out.println("file "+Integer.toString(i)+"processed "+Long.toString(new Date().getTime()-t));}
	PrintWriter output = new PrintWriter(new File("hist.txt"));
	for(int i=0;i<7;i++)for(int j=0;j<24;j++)output.println(hist[i][j]);
	output.close();
	output = new PrintWriter(new File("hcount.txt"));
	for(int i=0;i<7;i++)for(int j=0;j<24;j++)output.println(hcount[i][j]);
	output.close();
	output = new PrintWriter(new File("rcid.txt")); //is the RC_ID a hourly counter or not. Most are hourly, some are every 5 minutes, etc.
	for(int i=0;i<n;i++)output.println(rcid[i]?1:0);
	output.close();
}catch(Exception e){System.err.println("ERROR");System.err.println(e.toString());}}}