package wburles.uk.seriesoftubes;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Will on 21/11/2016.
 */

public class BikePoint {

    private String id;
    private boolean hasBikes;
    private boolean hasFree;
    private LatLng pos;
    private GroundOverlay overlay;
    private Circle circle;

    public BikePoint(String id, boolean hasBikes, boolean hasFree, LatLng pos) {
        this.id = id;
        this.hasBikes = hasBikes;
        this.hasFree = hasFree;
        this.pos = pos;
    }

    public String getId() {
        return id;
    }

    public LatLng getPos() {
        return pos;
    }

    public boolean hasFree() {
        return hasFree;
    }

    public boolean hasBikes() {
        return hasBikes;
    }

    public void clearOverlay() {
        overlay.remove();
        circle.remove();
    }

    public void setOverlays(GroundOverlay overlay, Circle circle) {
        this.overlay = overlay;
        this.circle = circle;
    }
}
