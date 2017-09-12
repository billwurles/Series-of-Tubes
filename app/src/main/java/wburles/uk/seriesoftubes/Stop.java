package wburles.uk.seriesoftubes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class Stop {

    private String name;
    private String id;
    private LatLng pos;
    private Marker marker;
    private ArrayList<Line> lines;
    private ArrayList<String> bikePoints;
    private boolean visited;
    private boolean shop;
    private boolean mugger;

    public Stop(String name, String id, LatLng pos) {
        this.name = name;
        this.id = id;
        this.pos = pos;
        lines = new ArrayList<>();
        bikePoints = new ArrayList<>();
        visited = false;
        shop = false;
        mugger = false;
    }

    public String getName() { return name; }

    public void setMarker(Marker marker) { this.marker = marker; }

    public Marker getMarker() { return marker; }

    public LatLng getPos() { return pos; }

    public String getId() { return id; }

    public boolean isVisited() { return visited; }

    public void setVisited() { this.visited = true; }

    public boolean isShop() { return shop; }

    public void makeShop(){this.shop = true; this.mugger = false; }

    public boolean isMugger() { return mugger; }

    public void makeMugger(){this.shop = false; this.mugger = true; }

    public void beatMugger(){ this.mugger = false; }

    public ArrayList<Line> getLines() { return lines; }

    public void setLines(ArrayList<Line> routes) {
        for(Line line : routes){
            lines.add(line);
        }
    }

    public ArrayList<String> getBikePoints() {
        return bikePoints;
    }

    public void setBikePoints(ArrayList<String> bikePoints) {
        this.bikePoints = bikePoints;
    }
}
