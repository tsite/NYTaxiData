/**
 * Copyright (c) 2011-2013 Evolutionary Design and Optimization Group
 * 
 * Licensed under the MIT License.
 * 
 * See the "LICENSE" file for a copy of the license.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.  
 *
 */

package parsejsonmaps;

import graph.Edge;
import graph.Graph;
import graph.Coordinates;
import graph.Node;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Set;
import org.json.simple.parser.ParseException;
import roads.Road;
import roads.Segment;

/**
 *
 * @author Ignacio Arnaldo
 */
public class ParseJsonMaps {
    String filePath;
    ArrayList<Road> alRoads;
    Graph gHighways, gRailways;
    double minX, maxX, minY, maxY;
    
    /**
     * Constructor
     * @param aPath
     * @param aMinX
     * @param aMaxX
     * @param aMinY
     * @param aMaxY
     * @throws FileNotFoundException 
     */
    public ParseJsonMaps(String aPath, double aMinX, double aMaxX, double aMinY, double aMaxY) throws FileNotFoundException{
        filePath = aPath;
        alRoads = new ArrayList<>();
        minX = aMinX;
        maxX = aMaxX;
        minY = aMinY;
        maxY = aMaxY;
        gHighways = new Graph();
        gHighways.setMinX(minX);
        gHighways.setMaxX(maxX);
        gHighways.setMinY(minY);
        gHighways.setMaxY(maxY);
        gRailways = new Graph();
        gRailways.setMinX(minX);
        gRailways.setMaxX(maxX);
        gRailways.setMinY(minY);
        gRailways.setMaxY(maxY);
    }
    
    /**
     * Read Json Map
     * @throws IOException
     * @throws ParseException 
     */
    public void readJSONMap() throws IOException, ParseException{
        try (BufferedReader jsonFile = new BufferedReader(new FileReader(filePath))) {
            String line = jsonFile.readLine();
            // SKIP FIRST LINES
            while (!line.startsWith("\"features\": [")){
                line = jsonFile.readLine();
            }
            alRoads = new ArrayList<>();
            while (jsonFile.ready()){
                line = jsonFile.readLine();
                if(line.startsWith("{ \"type\": \"Feature\"")){
                    Road rAux = new Road(line);
					if("highway".equals(rAux.getRoadClass()) && getType(rAux.getType())==0) //BLUE ROADS ONLY
                    alRoads.add(rAux);
                }
				else System.err.println("BAD LINE: "+line);
            }
            jsonFile.close();
        }
    }
    
    /**
     * Generate Graph with a MAP object
     * @param roadClass 
     */
    public void generateGraph(String roadClass){
        for(Road road:alRoads){
            String roadClassAux =  road.getRoadClass();
            if(roadClassAux.equals(roadClass)){
                ArrayList<Segment> listAuxSegments = road.getSegments();
                long isOneway = road.isOneway();
                boolean isTunnel = road.isTunnel();
                boolean isBridge = road.isBridge();
                String type = road.getType();
                String name = road.getName();
				String ref=road.getRef();
                
                for(Segment seg: listAuxSegments){
                    double startSegX = seg.getStartX();
                    double startSegY = seg.getStartY();
                    double endSegX = seg.getEndX();
                    double endSegY = seg.getEndY();

                    Coordinates coordsFrom = new Coordinates(startSegX, startSegY,isTunnel,isBridge);
                    Coordinates coordsTo = new Coordinates(endSegX, endSegY,isTunnel,isBridge);
                    Edge edge = new Edge(coordsFrom,coordsTo,roadClass,type,isOneway,isTunnel,isBridge,name,ref);
                    switch (roadClass) {
                        case "railway":
                            {
                                boolean inRange = gRailways.isWithinRange(edge);
                                if(inRange) {
                                    gRailways.addNodesAndEdge(edge);
                                }
                                break;
                            }
                        case "highway":
                            {
                                boolean inRange = gHighways.isWithinRange(edge);
                                if(inRange) gHighways.addNodesAndEdge(edge);
                                break;
                            }
                        default:
                            System.err.println("Unrecognized road class in generate graph");
                    }
                    
                }
            }
        }
    }
    
    /**
     * get edge color
     * @param aType
     * @return color
     */
    private int getType(String aType){
        int blue = 0;
        int green = 1;
        int red = 2;
        int colorString = 0;
        switch (aType) {
            case "bridleway":
                colorString = green;
                break;
            case "steps":
                colorString = green;
                break;
            case "cycleway":
                colorString = green;
                break;
            case "pedestrian":
                colorString = green;
                break;
            case "footway":
                colorString = green;
                break;
            case "path":
                colorString = green;
                break;
            case "track":
                colorString = green;
                break;
            case "motorway":
                colorString = blue;
                break;
            case "motorway_link":
                colorString = blue;
                break;
            case "service":
                colorString = blue;
                break;
            case "road":
                colorString = blue;
                break;
            case "residential":
                colorString = blue;
                break;
            case "primary":
                colorString = blue;
                break;
            case "primary_link":
                colorString = blue;
                break;
            case "secondary":
                colorString = blue;
                break;
            case "secondary_link":
                colorString = blue;
                break;
            case "tertiary":
                colorString = blue;
                break;
            case "tertiary_link":
                 colorString = blue;
                break;
            case "trunk":
                colorString = blue;
                break;
            case "trunk_link":
                colorString = blue;
                break;
            case "living_street":
                colorString = blue;
                break;
			case "raceway":
                colorString = red;
                break;
            case "unclassified":
                //colorString = red;
				colorString = blue; //unclassified roads are still usable by motor vehicles
				//System.err.println("WARNING: UNCLASSIFIED ROAD FOUND");
                break;
            default:
                System.err.println("Unrecognized type " + aType);
                break;
        }
        return colorString;
    }
    
    /**
     * Print graph in TSV format (originally, Ignacio Arnaldo used gefx format)
     * @param roadClass
     */
    public void printGraph(String roadClass){
        Graph g = gHighways;
        switch (roadClass) {
            case "railway":
                g = gRailways;
                break;
            case "highway":
                g = gHighways;
                break;
            default:
                System.err.print("Error in printGraph: unrecognized road class");
        }
        Set<Coordinates> setCoordinates = g.getMap().keySet();
        for(Coordinates coords:setCoordinates){
            Node node = g.getMap().get(coords);
            long idAux = node.getNodeID();
            double xCoord = coords.getCoordX();
            double yCoord = coords.getCoordY();
			int br = coords.isBridge()?1:0; //any bridge
			int tu = coords.isTunnel()?1:0; //any tunnel
			System.out.println(idAux+"\t"+xCoord+"\t"+yCoord+"\t"+(2*br+tu));
        }        
       
        int edgeCounter = 0;
        for(Coordinates coords:setCoordinates){
            Node node = g.getMap().get(coords);
            long idFrom = node.getNodeID();
            ArrayList<Edge> listEdges = node.getEdges();
            for(Edge edge:listEdges){
                long idTo = edge.getNodeIDto();
				long dir=edge.isDirected();
				System.out.println(idFrom+"\t"+idTo+"\t"+dir+"\t"+edge.getType()+"\t"+edge.getEdgename()+"\t"+edge.getRef());
                edgeCounter++;
            }
        }
    }
    

    
    /**
     * Save text to a filepath
     * @param filepath
     * @param text
     */
    static void saveText(String filepath, String text, Boolean append) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filepath,append));
            try (PrintWriter printWriter = new PrintWriter(bw)) {
                printWriter.write(text);
                printWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("Error in saveText in ParseJsonMaps.java");
        }
    }
    
}
