import java.util.ArrayList;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /** The max image depth level. */
    public static final int MAX_DEPTH = 7;
    private static ArrayList<Double> dpplist = new ArrayList<>();
    static double lowerLat;
    static double upperLat;
    static double rightLong;
    static double leftLong;


    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     *     <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     *     possible, while still covering less than or equal to the amount of longitudinal distance
     *     per pixel in the query box for the user viewport size.</li>
     *     <li>Contains all tiles that intersect the query bounding box that fulfill the above
     *     condition.</li>
     *     <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */

//         * Hint: Define additional classes to make it easier to pass around multiple values, and
//         * define additional methods to make it easier to test and reason about code. */
    public RasterResultParams getMapRaster(RasterRequestParams params) {
        if (params.lrlon > MapServer.ROOT_LRLON && params.lrlat < MapServer.ROOT_LRLAT
                && params.ullat > MapServer.ROOT_ULLAT && params.ullon < MapServer.ROOT_ULLON) {
            return RasterResultParams.queryFailed();
        }
        if (params.lrlon <= params.ullon || params.lrlat >= params.ullat) {
            return RasterResultParams.queryFailed();
        }
        double ullon = params.ullon;
        double lrlon = params.lrlon;
        double ullat = params.ullat;
        double lrlat = params.lrlat;
        double londpp = lonDPP(params.lrlon, params.ullon, params.w);
        if (params.lrlon > MapServer.ROOT_LRLON) {
            lrlon = MapServer.ROOT_LRLON;
        }
        if (params.lrlat < MapServer.ROOT_LRLAT) {
            lrlat = MapServer.ROOT_LRLAT;
        }
        if (params.ullon < MapServer.ROOT_ULLON) {
            ullon = MapServer.ROOT_ULLON;
        }
        if (params.ullat  > MapServer.ROOT_ULLAT) {
            ullat = MapServer.ROOT_ULLAT;
        }
        instantiateDepths();
        double reqdpp = 0;
        if (londpp < dpplist.get(dpplist.size() - 1)) {
            reqdpp = dpplist.get(dpplist.size() - 1);
        } else {
            for (Double templong : dpplist) {
                if (templong <= londpp) {
                    reqdpp = templong;
                    break;
                }
            }
        }

        int depth = dpplist.indexOf(reqdpp);
        int imagex = startingLeft(depth, ullon);
        int imagey = startingUp(depth, ullat);
        int rows = imagesvertical(height(depth), imagey, lrlat);
        int cols = imageshorizon(width(depth), imagex, lrlon);
        String[][] grid = matrix(rows, cols, imagex, imagey, depth);
        RasterResultParams.Builder result = new RasterResultParams.Builder();
        result.setDepth(depth);
        result.setRasterLrLat(lowerLat);
        result.setRasterUlLat(upperLat);
        result.setRasterLrLon(rightLong);
        result.setRasterUlLon(leftLong);
        result.setQuerySuccess(true);
        result.setRenderGrid(grid);
        return result.create();
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    /**Method will add the longitude distance of all 7 depth levels.*/
    private void instantiateDepths() {
        for (int i = 0; i < 8; i++) {
            double start = width(i) + MapServer.ROOT_ULLON;
            dpplist.add(lonDPP(start, MapServer.ROOT_ULLON, MapServer.TILE_SIZE));
        }
    }

    /**will calculate the width of an image at a certain depth*/
    private double width(int depth) {
        double power = Math.pow(4, depth);
        double d = (MapServer.ROOT_ULLON - MapServer.ROOT_LRLON) / Math.sqrt(power);
        double lowerLong = Math.abs(d);
        return lowerLong;
    }

    /**will calculate the height of an image at a certain depth*/
    private double height(int depth) {
        double power = Math.pow(4, depth);
        double d = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / Math.sqrt(power);
        double lowerLong = Math.abs(d);
        return lowerLong;
    }
 /** determines which image to start with from the left */
    private int startingLeft(int depth, double queryLong) {
        double difference = Math.abs(queryLong - MapServer.ROOT_ULLON);
        return (int) (difference / width(depth));
    }
    /** determines which images to start with from the top */
    private int startingUp(int depth, double queryLat) {
        double difference = Math.abs(MapServer.ROOT_ULLAT - queryLat);
        return (int) (difference / height(depth));
    }
    /** Determines the number of images horizontally within the query box.
     * Also determines the left and right
    longitudes  of the images used for the query box.
     */
    private int imageshorizon(double width, int startimage, double querylonglr) {
        double startimagelong = (width * startimage) + MapServer.ROOT_ULLON;
        leftLong = startimagelong;
        int result = 0;
        while (startimagelong <= querylonglr) {
            startimagelong += width;
            result++;
            if (startimagelong > MapServer.ROOT_LRLON) {
                startimagelong -= width;
                result--;
                break;
            }
        }
        rightLong = startimagelong;
        return result;
    }
    /** Determines the number of images vertically within the query box.
     * Also determines the top and bottom
    latitudes  of the images used for the query box.
     */
    private int imagesvertical(double height, int startimage, double querylatlr) {
        double startimagelat = MapServer.ROOT_ULLAT - (height * startimage);
        upperLat = startimagelat;
        int result = 0;
        while (startimagelat >= querylatlr) {
            startimagelat -= height;
            result++;
            if (startimagelat < MapServer.ROOT_LRLAT) {
                startimagelat += height;
                result -= 1;
                break;
            }
        }
        lowerLat = startimagelat;
        return result;
    }
/** Gives the matrix of all the images used for the query box */
    private String[][] matrix(int row, int col, int horizontal, int vertical, int depth) {
        String[][] grid = new String[row][col];
        String filename = "d" + depth + "_x";
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                grid[i][j] = filename + (horizontal + j) + "_y" + (vertical + i) + ".png";
            }
        }
        return grid;
    }
}
