package wburles.uk.seriesoftubes;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Random;

public class Mugger implements Parcelable {

    private int cost;
    private int weapon;
    private int health;

    public Mugger(){
        final Random rand = new Random();
        cost = rand.nextInt(14)+1;

        health = rand.nextInt(20) + 20;
        weapon = rand.nextInt(10) + 15;
        if(rand.nextInt(10) == 10){
            health += 30;
            weapon += 20;
            cost += 15;
        }
    }

    protected Mugger(Parcel in) {
        cost = in.readInt();
        weapon = in.readInt();
        health = in.readInt();
    }

    public static final Creator<Mugger> CREATOR = new Creator<Mugger>() {
        @Override
        public Mugger createFromParcel(Parcel in) {
            return new Mugger(in);
        }

        @Override
        public Mugger[] newArray(int size) {
            return new Mugger[size];
        }
    };

    public int getCost() {
        return cost;
    }

    public int getWeapon() {
        return weapon;
    }

    public int getHealth() {
        return health;
    }

    public void hit(int amount) {
        this.health -= amount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(cost);
        parcel.writeInt(weapon);
        parcel.writeInt(health);
    }
}
