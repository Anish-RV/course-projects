import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {
//    LinkedHashMap<String, Node> graph = new LinkedHashMap<>();
    private HashMap<Node, LinkedList> graph;
    private KTree tree = new KTree(null, null, null);

    public static class Node {
        String id;
        String lon;
        String lat;
        String name = null;
        String wayName = null;
        Double distance = Double.MAX_VALUE;
        Double bestdistance = Double.MAX_VALUE;
        Node parent = null;

        public Node(String id, String lon, String lat, String name) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            Node n = (Node) obj;
            return this.id.compareTo(n.id) == 0;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    /* Method will add a new node to the graph and will set its neighbors.*/
    public void addNode(Node node, Node prevnode) {
        if (this.graph.containsKey(node)) {
            LinkedList<Node> linkednode = this.graph.get(node);
            linkednode.add(prevnode);
            this.graph.replace(node, linkednode);
            if (prevnode != null) {
                linkednode = this.graph.get(prevnode);
                linkednode.add(node);
                this.graph.replace(prevnode, linkednode);
            }
        } else {
            LinkedList<Node> linkednode = new LinkedList<>();
            linkednode.add(prevnode);
            this.graph.put(node, linkednode);
            if (prevnode != null) {
                linkednode = this.graph.get(prevnode);
                linkednode.add(node);
                this.graph.replace(prevnode, linkednode);
            }
        }
    }

    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        graph = new HashMap<>();
        File inputFile = new File(dbPath);
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        this.tree.treeHelper(this); //In order to instantiate tree after being parsed.
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    public Node findNode(long id) {
        for (Node node: this.graph.keySet()) {
            if (node.id.equals(Long.toString(id))) {
                return node;
            }
        }
        return null;
    }

    public KTree getTree() {
        return tree;
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (Node node: this.graph.keySet()) {
            if (this.graph.get(node).isEmpty()) {
                this.graph.remove(node, this.graph.get(node));
            }
        }
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {
        String id = v + "";
        for (Node node: graph.keySet()) {
            if (node.id.equals(id)) {
                return Double.parseDouble(node.lon);
            }
        }
        return 0.0;
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {
        String id = v + "";
        for (Node node: graph.keySet()) {
            if (node.id.equals(id)) {
                return Double.parseDouble(node.lat);
            }
        }
        return 0.0;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        ArrayList<Long> id = new ArrayList<>();
        for (Node node: graph.keySet()) {
            id.add(Long.parseLong(node.id));
        }
        return id;
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        String id = Long.toString(v);
        LinkedList<Node> neighbor = null;
        ArrayList<Long> neighborId = new ArrayList<>();
        for (Node node: graph.keySet()) {
            if (node.id.compareTo(id) == 0) {
                neighbor = graph.get(node);
                break;
            }
        }
        for (Node node: neighbor) {
            if (node != null) {
                neighborId.add(Long.parseLong(node.id));
            }
        }
        return neighborId;
    }

    /**Takes in Node instead of id and returns list of Nodes*/
    Iterable<Node> adjacent(Node v) {
        /*LinkedList<Node> neighbor = graph.get(v);
        ArrayList<Node> neighborId = new ArrayList<>();
        if (neighbor != null && !neighbor.isEmpty()) {
            for (Node node : neighbor) {
                if (node != null) {
                    neighborId.add(node);
                }
            }
        } */
        return graph.get(v);
    }


    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    /**Uses the Nodes instead of just their ids. */
    public double distance(Node v, Node w) {
        double phi1 = Math.toRadians(Double.parseDouble(v.lat));
        double phi2 = Math.toRadians(Double.parseDouble(w.lat));
        double dphi = Math.toRadians(Double.parseDouble(w.lat) - Double.parseDouble(v.lat));
        double dlambda = Math.toRadians(Double.parseDouble(w.lon) - Double.parseDouble(v.lon));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }


    /** Calls nearestneighbour on the given tree and returns the closestNode whose id is returned
     * by closest.
     * @param lon
     * @param lat
     * @return
     */

    public long closest(double lon, double lat) {
        Double x = projectToX(lon, lat);
        Double y = projectToY(lon, lat);
        return Long.parseLong(this.tree.nearest(x, y, Double.MAX_VALUE, null, true).id);
    }
     /** Takes in a tree variable and returns a Node instead of id */
    public Node closest(double lon, double lat, boolean temp) {
        Double x = projectToX(lon, lat);
        Double y = projectToY(lon, lat);
        return this.tree.nearest(x, y, Double.MAX_VALUE, null, true);
    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    static double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /** Radius of the Earth in miles. */
    private static final int R = 3963;
    /** Latitude centered on Berkeley. */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /** Longitude centered on Berkeley. */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;

    /** new class will implement k-searching with a runtime of log(N). */
    public class KTree {
        /**inner class will help create the KTree while encapsulating how the tree is created. */
        Node item;
        KTree left;
        KTree right;
        boolean isX = true;

        public KTree(Node item, KTree left, KTree right) {
            this.item = item;
            this.left = left;
            this.right = right;
        }

        /* Had to move this outside TreeHelper class because I wasn't able to call it when
         * instatiating GraphDB after parsing.
         * @param g
         */
        public void treeHelper(GraphDB g) {
            List<Node> generalXList = new ArrayList<>(g.graph.keySet());
            g.tree = createTree(g, generalXList, true);
        }


        /** Create tree will take in a sorted list.
         * Method will find the index of where the given node occurs in the list.
         * All nodes smaller than the given node will be put as the left tree of the node.
         * All nodes greater than the given node will be put to the right of the tree.
         * Finally, method will call the KTree constructor to finish creating the tree.*/
        public KTree createTree(GraphDB g, List<Node> xList, boolean isitX) {
            KTree temptree = new KTree(null, null, null);
            int index;
            if (!xList.isEmpty()) {
                if (isitX) {
                    sortByX(xList, g);
                    index = xList.size() / 2;
                    temptree.item = xList.get(index);
                    isitX = false;
                    xList.remove(temptree.item);
                    if (!xList.isEmpty()) {
                        temptree.left = createTree(g, xList.subList(0, index), isitX);
                        temptree.right = createTree(g, xList, isitX);
                    }
                    return temptree;

                } else {
                    sortByY(xList, g);
                    index = xList.size() / 2;
                    temptree.item = xList.get(index);
                    isitX = true;
                    xList.remove(temptree.item);
                    if (!xList.isEmpty()) {
                        temptree.left = createTree(g, xList.subList(0, index), isitX);
                        temptree.right = createTree(g, xList, isitX);
                    }
                    return temptree;
                }
            }
            return null;
        }

        public void sortByY(List<Node> list, GraphDB g) {
            list.sort((node1, node2) ->
                    Double.compare(g.projectToY(Double.parseDouble(node1.lon),
                            Double.parseDouble(node1.lat)),
                            g.projectToY(Double.parseDouble(node2.lon),
                                    Double.parseDouble(node2.lat))));
        }
        public void sortByX(List<Node> list, GraphDB g) {
            list.sort((node1, node2) ->
                    Double.compare(g.projectToX(Double.parseDouble(node1.lon),
                            Double.parseDouble(node1.lat)),
                            g.projectToX(Double.parseDouble(node2.lon),
                                    Double.parseDouble(node2.lat))));
        }
//Changed the parameters so that nearest directly takes in the projected x & y.
        public Node nearest(double x, double y, double distance, Node closestNode, boolean isitX) {
            Double treeX = projectToX(Double.parseDouble(this.item.lon),
                    Double.parseDouble(this.item.lat));
            Double treeY = projectToY(Double.parseDouble(this.item.lon),
                    Double.parseDouble(this.item.lat));
            Double itemdist = euclidean(x, treeX, y, treeY);
            if (itemdist <= distance) {
                distance = itemdist;
                closestNode = this.item;
                if (closestNode != null) {
                    closestNode.distance = distance;
                }
            }
            if (this.right == null && this.left == null) {
                return closestNode;
            }
            if (Double.compare(treeX, x) == 0 && Double.compare(treeY, y) == 0) {
                return this.item;
            }
            if (isitX) {
                Double treedist = x - treeX;
                isitX = false;
                if (treedist >= 0 && this.right != null) {
                    Node rightclosest = this.right.nearest(x, y, distance, closestNode, isitX);
                    if (this.left != null && rightclosest.distance > Math.abs(x - treeX)) {
                        Node leftclosest = this.left.nearest(x, y, distance, closestNode, isitX);
                        if (leftclosest.distance < rightclosest.distance) {
                            return leftclosest;
                        } else {
                            return rightclosest;
                        }
                    }
                    return rightclosest;
                } else if (this.left != null && treedist < 0) {
                    Node leftclosest = this.left.nearest(x, y, distance, closestNode, isitX);
                    if (this.right != null && leftclosest.distance > Math.abs(x - treeX)) {
                        Node rightclosest = this.right.nearest(x, y, distance, closestNode, isitX);
                        if (leftclosest.distance < rightclosest.distance) {
                            return leftclosest;
                        } else {
                            return rightclosest;
                        }
                    }
                    return leftclosest;
                }
            } else {
                Double treedist = y - treeY;
                isitX = true;
                if (treedist >= 0 && this.right != null) {
                    Node rightclosest = this.right.nearest(x, y, distance, closestNode, isitX);
                    if (this.left != null && rightclosest.distance > Math.abs(y - treeY)) {
                        Node leftclosest = this.left.nearest(x, y, distance, closestNode, isitX);
                        if (leftclosest.distance < rightclosest.distance) {
                            return leftclosest;
                        } else {
                            return rightclosest;
                        }
                    }
                    return rightclosest;
                } else if (this.left != null && treedist < 0) {
                    Node leftclosest = this.left.nearest(x, y, distance, closestNode, isitX);
                    if (this.right != null && leftclosest.distance > Math.abs(y - treeY)) {
                        Node rightclosest = this.right.nearest(x, y, distance, closestNode, isitX);
                        if (leftclosest.distance < rightclosest.distance) {
                            return leftclosest;
                        } else {
                            return rightclosest;
                        }
                    }
                    return leftclosest;
                }
            }
            return closestNode;
        }
    }
}
