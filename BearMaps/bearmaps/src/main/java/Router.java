import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     * @param g <code>GraphDB</code> data source.
     * @param stlon The longitude of the starting coordinate.
     * @param stlat The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */

    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {
        GraphDB.Node start = g.closest(stlon, stlat, true);
        GraphDB.Node end = g.closest(destlon, destlat, true);
        /* Including a heuristic for the distance between o1 and endNode, o2 and endNode
         * as part of priority value
         */
        PriorityQueue<Vertex> fringe = new PriorityQueue<Vertex>((o1, o2) -> {
            double distance1 = o1.distance;
            double distance2 = o2.distance;

            if (distance1 < distance2) {
                return -1;
            } else if (distance1 == distance2) {
                return 0;
            } else {
                return 1;
            }
        });
        double bestDistance = 0.0;
        List<Long> correctPath = new ArrayList<>();
        List<Vertex> path = new ArrayList<>();

        HashSet<String> visited = new HashSet<>();
        Vertex closestStart = new Vertex(start);
        closestStart.bestdistance = 0.0;
        Vertex endNode = new Vertex(end);
        fringe.add(closestStart);
        while (!fringe.isEmpty()) {
            Vertex current = fringe.poll();
            if (current.equals(endNode)) {
                endNode = current;
                path.add(endNode);
                break;
            } else if (visited.contains(current.id)) {
                continue;
            } else {
                bestDistance = current.bestdistance;
                Iterable<GraphDB.Node> currentAdj = g.adjacent(current.node);
                for (GraphDB.Node n: currentAdj) {
                    if (n != null) {
                        Vertex neighbor = new Vertex(n);
                        if (!visited.contains(neighbor.id)) {
                            Double dist = g.distance(current.node, neighbor.node);
                            if (bestDistance + dist < neighbor.bestdistance) {
                                neighbor.bestdistance = bestDistance + dist;
                                neighbor.parent = current;
                                neighbor.distance = neighbor.bestdistance
                                        + g.distance(neighbor.node, endNode.node);
                                fringe.add(neighbor);
                            }
                        }
                    }

                }
            }
            path.add(current);
            visited.add(current.id);
        }
        traverse(closestStart, endNode, correctPath);

        return correctPath;
    }

    public static List<Long> traverse(Vertex startNode, Vertex endNode, List<Long> corrected) {
        if (endNode == null) {
            return corrected;
        }
        if (endNode.equals(startNode)) {
            corrected.add(Long.parseLong(startNode.id));
            Collections.reverse(corrected);
            return corrected;
        } else {
            corrected.add(Long.parseLong(endNode.id));
            traverse(startNode, endNode.parent, corrected);
        }
        return null;

    }

    public static class Vertex {
        String id;
        Double distance = Double.MAX_VALUE;
        Double bestdistance = Double.MAX_VALUE;
        Vertex parent = null;
        GraphDB.Node node;

        public Vertex(GraphDB.Node node) {
            this.node = node;
            this.id = node.id;
        }

        @Override
        public boolean equals(Object obj) {
            Vertex v = (Vertex) obj;
            return v.node.id.compareTo(this.node.id) == 0;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }


    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     * @param g <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // TODO
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction represented.*/
        int direction;
        /** The name of this way. */
        String way;
        /** The distance along this way. */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
