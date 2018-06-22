public class Location {
    public long id;
    public String name;
    public double lon;
    public double lat;

    public Location(long id, double lon, double lat, String name) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }
}
