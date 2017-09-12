package wburles.uk.seriesoftubes.SQLite;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PlayerBuilder {
    LatLng pos;
    int cash;
    int health;
    int weapon;
    int boost;
    ArrayList<LatLng> history;

    public PlayerBuilder(LatLng pos, int cash, int weapon, int health, int boost, ArrayList<LatLng> history) {
        this.pos = pos;
        this.cash = cash;
        this.weapon = weapon;
        this.health = health;
        this.boost = boost;
        this.history = history;
    }

    public LatLng getPos() {
        return pos;
    }

    public int getCash() {
        return cash;
    }

    public int getWeapon() {
        return weapon;
    }

    public int getHealth() {
        return health;
    }

    public int getBoost() {
        return boost;
    }

    public ArrayList<LatLng> getHistory() {
        return history;
    }
}
