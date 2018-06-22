import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    public static int levels = 8;
    public double[] lonDPPs;
    public double[] lonDiffs;
    public double[] latDiffs;

    public Rasterer() {
        // YOUR CODE HERE
        lonDPPs = new double[levels];
        lonDiffs = new double[levels];
        latDiffs = new double[levels];

        double lonDiff = MapServer.ROOT_LRLON - MapServer.ROOT_ULLON;
        double latDiff = MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT;
        for (int i = 0; i < lonDPPs.length; i++) {
            lonDiffs[i] = lonDiff;
            latDiffs[i] = latDiff;
            lonDPPs[i] = lonDiff / MapServer.TILE_SIZE;
            lonDiff = lonDiff / 2;
            latDiff = latDiff / 2;
        }
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        //System.out.println(params);
        Map<String, Object> results = new HashMap<>();
        /*System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
                           + "your browser.");*/
        double query_lrlon = params.get("lrlon");
        double query_lrlat = params.get("lrlat");
        double query_ullon = params.get("ullon");
        double query_ullat = params.get("ullat");
        double query_w = params.get("w");
        double query_h = params.get("h");
        double query_lonDPP = (query_lrlon - query_ullon)/query_w;

        int depth = getDepth(query_lonDPP);
        boolean query_success;
        if (query_lrlon < query_ullon || query_ullat < query_lrlat || !overlapped(query_lrlon, query_lrlat, query_ullon, query_ullat)) {
            query_success = false;
            fillArbitraryRenderGrid(results);
        }else{
            query_success = true;
            fillRenderGrid(query_lrlon, query_lrlat, query_ullon, query_ullat, depth, results);
        }
        results.put("query_success", query_success);
        results.put("depth", depth);

        return results;
    }

    public int getDepth(double query_lonDPP) {
        int depth = levels - 1;
        for (int i = 0; i < lonDPPs.length; i++) {
            if(lonDPPs[i] <= query_lonDPP) { // Have the greatest LonDPP that is less than or equal to the LonDPP of the query box
                depth = i;
                break;
            }
        }
        return depth;
    }

    public boolean overlapped(double query_lrlon, double query_lrlat, double query_ullon, double query_ullat) {
        if (MapServer.ROOT_ULLON < query_lrlon && MapServer.ROOT_LRLON > query_ullon &&
                MapServer.ROOT_ULLAT > query_lrlat && MapServer.ROOT_LRLAT < query_ullat) {
            return true;
        }else{
            return false;
        }
    }

    public void fillRenderGrid(double query_lrlon, double query_lrlat, double query_ullon, double query_ullat,
                               int depth, Map<String, Object> results) {
        int xStart = 0;
        int yStart = 0;
        int xEnd = (int) (Math.pow(2, depth)) - 1;
        int yEnd = xEnd;

        double tileWidth = lonDiffs[depth];
        double tileHeight = latDiffs[depth];

        double ul_xDiff = query_ullon - MapServer.ROOT_ULLON;
        xStart = ul_xDiff > 0 ? (int)(ul_xDiff / tileWidth) : xStart;
        double ul_yDiff = MapServer.ROOT_ULLAT - query_ullat;
        yStart = ul_yDiff > 0 ? (int)(ul_yDiff / tileHeight) : yStart;
        double raster_ul_lon = MapServer.ROOT_ULLON + xStart * tileWidth;
        double raster_ul_lat = MapServer.ROOT_ULLAT - yStart * tileHeight;

        double lr_xDiff = MapServer.ROOT_LRLON - query_lrlon;
        xEnd = lr_xDiff > 0 ? xEnd - (int)(lr_xDiff / tileWidth): xEnd;
        double lr_yDiff = query_lrlat - MapServer.ROOT_LRLAT;
        yEnd = lr_yDiff > 0 ? yEnd - (int)(lr_yDiff / tileHeight) : yEnd;
        double raster_lr_lon = MapServer.ROOT_ULLON + (xEnd+1) * tileWidth;
        double raster_lr_lat = MapServer.ROOT_ULLAT - (yEnd+1) * tileHeight;

        String[][] render_grid = new String[yEnd - yStart + 1][xEnd - xStart + 1];
        for (int i = yStart; i <= yEnd; i++) {
            for (int j = xStart; j <= xEnd; j++) {
                render_grid[i-yStart][j-xStart] = "d"+depth+"_x"+j+"_y"+i+".png";
            }
        }

        results.put("render_grid", render_grid);
        results.put("raster_ul_lon", raster_ul_lon);
        results.put("raster_ul_lat", raster_ul_lat);
        results.put("raster_lr_lon", raster_lr_lon);
        results.put("raster_lr_lat", raster_lr_lat);
    }

    public void fillArbitraryRenderGrid(Map<String, Object> results) {
        String[][] render_grid = new String[1][1];
        results.put("render_grid", render_grid);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
    }
}
