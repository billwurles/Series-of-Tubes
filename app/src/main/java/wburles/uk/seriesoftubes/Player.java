package wburles.uk.seriesoftubes;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;


public class Player {

    private Stop stop;
    private Marker current;
    private int weapon;
    private int health;
    private int boost;
    private float cash;
    private ArrayList<LatLng> history;

    public Player(Stop stop, Marker current, int health, int weapon, int cash) {
        this.stop = stop;
        this.current = current;
        this.cash = cash;
        this.health = health;
        this.weapon = weapon;
        history = new ArrayList<>();
    }

    public void move(Stop stop, Marker current, float cost){
        this.stop = stop;
        this.current = current;
        this.cash = this.cash - cost;
    }

    public Stop getStop() {
        return stop;
    }

    public Marker getCurrent() {
        return current;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getWeapon() {
        return weapon;
    }

    public void setWeapon(int weapon) {
        this.weapon = weapon;
    }

    public float getCash() {
        return cash;
    }

    public void setCash(float cash) {
        this.cash = cash;
    }

    public int getBoost() {
        return boost;
    }

    public void setBoost(int boost) {
        this.boost = boost;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Pos: ").append(stop.getPos().latitude).append(" / ").append(stop.getPos().longitude);
        builder.append("\nHealth: ").append(health);
        builder.append("\nCash: ").append(cash);
        builder.append("\nWeapon: ").append(weapon);
        builder.append("\nBoost: ").append(boost);
        return builder.toString();
    }

    public ArrayList<LatLng> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<LatLng> history) {
        this.history = history;
    }

    public void addToHistory(LatLng val){
        this.history.add(val);
    }

}
