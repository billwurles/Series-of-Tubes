package wburles.uk.seriesoftubes.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final String COMMA_SEP = ", ";
    public static final int DATABASE_VERSION = 55;
    public static final String DATABASE_NAME = "database.db";
    public static final String CREATE_PLAYER_ENTRY =
            "CREATE TABLE "+ GameContract.TABLE_NAME_PLAYER + " (" +
                    GameContract._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_LAT + " REAL NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_LNG + " REAL NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_HEALTH + " INTEGER NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_WEAPON + " INTEGER NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_CASH + " INTEGER NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_BOOST + " INTEGER NOT NULL"+ COMMA_SEP +
                    GameContract.COL_NAME_PLAYER_HISTORY + " TEXT NOT NULL)";
    public static final String CREATE_STOPS_ENTRY =
            "CREATE TABLE "+ GameContract.TABLE_NAME_STOPS + " (" +
                    GameContract._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_NAME + " TEXT NOT NULL" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_ID + " TEXT NOT NULL" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_LAT + " REAL NOT NULL" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_LNG + " REAL NOT NULL" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_VISITED + " INTEGER NOT NULL" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_TYPE + " INTEGER DEFAULT 0" + COMMA_SEP +
                    GameContract.COL_NAME_STOP_BIKES + " TEXT NOT NULL)";
    public static final String CREATE_HASGAME_ENTRY =
            "CREATE TABLE "+ GameContract.TABLE_NAME_HASGAME + " (" +
                    GameContract._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    GameContract.COL_NAME_HASGAME + " INTEGER DEFAULT 0)";

    private static final String DELETE_PLAYER = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_PLAYER;
    private static final String DELETE_STOPS = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_STOPS;
    private static final String DELETE_HASGAME = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_HASGAME;

    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        Log.d("Maps", "creating sqlite \n\n" + CREATE_PLAYER_ENTRY);
        db.execSQL(CREATE_PLAYER_ENTRY);
        db.execSQL(CREATE_STOPS_ENTRY);
        db.execSQL(CREATE_HASGAME_ENTRY);

        ContentValues defaultValues = new ContentValues();
        defaultValues.put(GameContract.COL_NAME_HASGAME, 0);
        db.insert(GameContract.TABLE_NAME_HASGAME, null, defaultValues);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(DELETE_PLAYER);
        db.execSQL(DELETE_STOPS);
        db.execSQL(DELETE_HASGAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }
}
