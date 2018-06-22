import java.util.ArrayList;
import java.util.List;

public class Vertex {
    public long id;
    public double lon;
    public double lat;
    public List<Long> adj;
    public String roadName;

    public Vertex(long id, double lon, double lat) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        adj = new ArrayList<>();
        roadName = "unknown road";
    }

    public void connectTo(long vertexId) {
        if(!adj.contains(vertexId)) {
            adj.add(vertexId);
        }
    }
}
