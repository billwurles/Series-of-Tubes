package wburles.uk.seriesoftubes.SQLite;

import android.provider.BaseColumns;

public class GameContract implements BaseColumns {
    public static final int COL_INDEX_ID = 0;
    public static final String TABLE_NAME_STOPS = "stops";

    public static final String COL_NAME_STOP_NAME = "stopname";
    public static final String COL_NAME_STOP_ID = "sid";
    public static final String COL_NAME_STOP_LAT = "slat";
    public static final String COL_NAME_STOP_LNG = "slng";
    public static final String COL_NAME_STOP_VISITED = "visited";
    public static final String COL_NAME_STOP_TYPE = "type";
    public static final String COL_NAME_STOP_BIKES = "bikes";

    public static final int COL_INDEX_STOP_NAME = 1;
    public static final int COL_INDEX_STOP_ID = 2;
    public static final int COL_INDEX_STOP_LAT = 3;
    public static final int COL_INDEX_STOP_LNG = 4;
    public static final int COL_INDEX_STOP_VISITED = 5;
    public static final int COL_INDEX_STOP_TYPE = 6;
    public static final int COL_INDEX_STOP_BIKES = 7;


    public static final String TABLE_NAME_PLAYER = "player";

    public static final String COL_NAME_PLAYER_LAT = "plat";
    public static final String COL_NAME_PLAYER_LNG = "plng";
    public static final String COL_NAME_PLAYER_CASH = "cash";
    public static final String COL_NAME_PLAYER_WEAPON = "weapon";
    public static final String COL_NAME_PLAYER_HEALTH = "health";
    public static final String COL_NAME_PLAYER_BOOST = "boost";
    public static final String COL_NAME_PLAYER_HISTORY = "history";

    public static final int COL_INDEX_PLAYER_LAT = 1;
    public static final int COL_INDEX_PLAYER_LNG = 2;
    public static final int COL_INDEX_PLAYER_HEALTH = 3;
    public static final int COL_INDEX_PLAYER_WEAPON = 4;
    public static final int COL_INDEX_PLAYER_CASH = 5;
    public static final int COL_INDEX_PLAYER_BOOST = 6;
    public static final int COL_INDEX_PLAYER_HISTORY = 7;

    public static final String TABLE_NAME_HASGAME = "hasgame";
    public static final String COL_NAME_HASGAME = "game";
    public static final int COL_INDEX_HASGAME = 1;
}
