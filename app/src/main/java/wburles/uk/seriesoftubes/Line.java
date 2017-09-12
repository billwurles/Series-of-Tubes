package wburles.uk.seriesoftubes;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class Line {
    private String route;
    private String color;
    private ArrayList<LatLng> stops;

    public Line(String route, String color, ArrayList<LatLng> stops) {
        this.route = route;
        this.color = color;
        this.stops = stops;
    }

    public String getRoute() {
        return route;
    }

    public String getColor() {
        return color;
    }

    public ArrayList<LatLng> getStops() {
        return stops;
    }
}
