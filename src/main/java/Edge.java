import java.util.List;

public class Edge {
    public long id;
    public List<Long> verticeIds;
    public String roadName;

    public Edge(long id, List<Long> verticeIds) {
        this.id = id;
        this.verticeIds = verticeIds;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }
}
