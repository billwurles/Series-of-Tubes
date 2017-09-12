package wburles.uk.seriesoftubes.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.games.Game;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import wburles.uk.seriesoftubes.Line;
import wburles.uk.seriesoftubes.Player;
import wburles.uk.seriesoftubes.Stop;

public class GameDAO {

    public void addPlayer(Context context, Player player){
        context.getContentResolver().delete(GameProvider.PLAYER_URI,null,null);
        ContentValues values = new ContentValues();
        values.put(GameContract.COL_NAME_PLAYER_LAT, player.getStop().getPos().latitude);
        values.put(GameContract.COL_NAME_PLAYER_LNG, player.getStop().getPos().longitude);
        values.put(GameContract.COL_NAME_PLAYER_HEALTH, player.getHealth());
        values.put(GameContract.COL_NAME_PLAYER_WEAPON, player.getWeapon());
        values.put(GameContract.COL_NAME_PLAYER_CASH, player.getCash());
        values.put(GameContract.COL_NAME_PLAYER_BOOST, player.getBoost());

        StringBuilder history = new StringBuilder();
        for(LatLng pos : player.getHistory()){
            history.append(pos.latitude).append(",");
            history.append(pos.longitude).append(" ");
        }
        values.put(GameContract.COL_NAME_PLAYER_HISTORY, history.toString());
        context.getContentResolver().insert(GameProvider.PLAYER_URI, values);
    }

    public PlayerBuilder getPlayerBuilder(Context context){
        Cursor cursor = context.getContentResolver().query(GameProvider.PLAYER_URI, null, null, null, null);
        Log.d("Maps", "Player Columns: "+ cursor.getColumnName(GameContract.COL_INDEX_PLAYER_LAT) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_PLAYER_LNG) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_PLAYER_CASH) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_PLAYER_HEALTH));
        cursor.moveToFirst();
        LatLng pos = new LatLng(cursor.getDouble(GameContract.COL_INDEX_PLAYER_LAT),
                                cursor.getDouble(GameContract.COL_INDEX_PLAYER_LNG));
        int cash = cursor.getInt(GameContract.COL_INDEX_PLAYER_CASH);
        int weapon = cursor.getInt(GameContract.COL_INDEX_PLAYER_WEAPON);
        int health = cursor.getInt(GameContract.COL_INDEX_PLAYER_HEALTH);
        int boost = cursor.getInt(GameContract.COL_INDEX_PLAYER_BOOST);
        String historyString = cursor.getString(GameContract.COL_INDEX_PLAYER_HISTORY);
        String[] latLons = historyString.split(" ");
        ArrayList<LatLng> history = new ArrayList<>();
        for(String latLon : latLons){
            String[] position = latLon.split(",");
            history.add(new LatLng(
                    Double.parseDouble(position[0]),
                    Double.parseDouble(position[1])
            ));
        }
        return new PlayerBuilder(pos, cash, weapon, health, boost, history);
    }

    public void addStops(Context context, ArrayList<Stop> stopsList){
        context.getContentResolver().delete(GameProvider.STOPS_URI,null,null);
        for(Stop stop : stopsList){
            ContentValues values = new ContentValues();
            //Log.d("Maps","SQLite addStops builder --- - -- - " + stop.getName());
            values.put(GameContract.COL_NAME_STOP_NAME, stop.getName());
            values.put(GameContract.COL_NAME_STOP_ID, stop.getId());
            values.put(GameContract.COL_NAME_STOP_LAT, stop.getPos().latitude);
            values.put(GameContract.COL_NAME_STOP_LNG, stop.getPos().longitude);
            values.put(GameContract.COL_NAME_STOP_VISITED, stop.isVisited() ? 1 : 0);
            if(stop.isMugger()){
                values.put(GameContract.COL_NAME_STOP_TYPE, 1);
            } else if(stop.isShop()){
                values.put(GameContract.COL_NAME_STOP_TYPE, 2);
            } else {
                values.put(GameContract.COL_NAME_STOP_TYPE, 0);
            }
            StringBuilder bikes = new StringBuilder();
            for(String bikePoint : stop.getBikePoints()){
                bikes.append(bikePoint).append(",");
            }
            values.put(GameContract.COL_NAME_STOP_BIKES, bikes.toString());
            context.getContentResolver().insert(GameProvider.STOPS_URI, values);
        }
    }

    public ArrayList<Stop> getStops(Context context){
        ArrayList<Stop> stops = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(GameProvider.STOPS_URI, null, null, null, null);
        Log.d("Maps", "Stops Columns: "+ cursor.getColumnName(GameContract.COL_INDEX_STOP_NAME) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_STOP_ID) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_STOP_LAT) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_STOP_LNG) + " " +
                cursor.getColumnName(GameContract.COL_INDEX_STOP_VISITED));
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Stop stop = new Stop(
                    cursor.getString(GameContract.COL_INDEX_STOP_NAME),
                    cursor.getString(GameContract.COL_INDEX_STOP_ID),
                    new LatLng(cursor.getDouble(GameContract.COL_INDEX_STOP_LAT),
                            cursor.getDouble(GameContract.COL_INDEX_STOP_LNG)));
            if(cursor.getInt(GameContract.COL_INDEX_STOP_VISITED) != 0) {
                stop.setVisited();
            }
            switch (cursor.getInt(GameContract.COL_INDEX_STOP_TYPE)){
                case 1: stop.makeMugger();
                    break;
                case 2: stop.makeShop();
                    break;
            }
            String bikes = cursor.getString(GameContract.COL_INDEX_STOP_BIKES);
            String[] bikePoint = bikes.split(",");
            ArrayList<String> points = new ArrayList<>();
            for(String point : bikePoint){
                points.add(point);
            }
            stop.setBikePoints(points);
            stops.add(stop);
            cursor.moveToNext();
        }
        return stops;
    }

    public boolean gameExists(Context context){
        Cursor cursor = context.getContentResolver().query(GameProvider.HASGAME_URI, null, null, null, null);
        cursor.moveToFirst();
        return cursor.getInt(GameContract.COL_INDEX_HASGAME) != 0;
    }

    public void setExists(Context context){
        ContentValues values = new ContentValues();
        values.put(GameContract.COL_NAME_HASGAME, 1);

        if(context.getContentResolver().query(GameProvider.HASGAME_URI,null,null,null,null).getCount()==0){
            context.getContentResolver().insert(GameProvider.HASGAME_URI,values);
        } else {
            context.getContentResolver().update(GameProvider.HASGAME_URI, values, null, null);
        }
    }

    public void gameOver(Context context){
        context.deleteDatabase(DBHelper.DATABASE_NAME);
    }
}
