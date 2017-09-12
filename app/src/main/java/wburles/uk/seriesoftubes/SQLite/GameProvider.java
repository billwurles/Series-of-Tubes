package wburles.uk.seriesoftubes.SQLite;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class GameProvider extends ContentProvider {
    public static final String AUTHORITY = "wburles.uk.seriesoftubes";
    public static final Uri PLAYER_URI = Uri.parse("content://" + AUTHORITY + "/player");
    public static final Uri STOPS_URI = Uri.parse("content://" + AUTHORITY + "/stops");
    public static final Uri HASGAME_URI = Uri.parse("content://" + AUTHORITY + "/hasgame");
    public static final String PLAYER_PATH = GameContract.TABLE_NAME_PLAYER;
    public static final String STOPS_PATH = GameContract.TABLE_NAME_STOPS;
    public static final String HASGAME_PATH = GameContract.TABLE_NAME_HASGAME;
    private static final int PLAYER = 0;
    private static final int STOPS = 1;
    private static final int STOPS_ID = 2;
    private static final int HASGAME = 3;

    private DBHelper helper;
    private UriMatcher urImatcher;

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        helper = new DBHelper(getContext());
        urImatcher = new UriMatcher(UriMatcher.NO_MATCH);
        urImatcher.addURI(AUTHORITY,PLAYER_PATH, PLAYER);
        urImatcher.addURI(AUTHORITY,HASGAME_PATH, HASGAME);
        urImatcher.addURI(AUTHORITY,STOPS_PATH, STOPS);
        urImatcher.addURI(AUTHORITY,STOPS_PATH + "/#",STOPS_ID);
        return true;
    }

    public void deleteDatabase() {
        db = helper.getWritableDatabase();
        final String DELETE_PLAYER = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_PLAYER;
        final String DELETE_STOPS = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_STOPS;
        final String DELETE_HASGAME = "DROP TABLE IF EXISTS " + GameContract.TABLE_NAME_HASGAME;
        db.execSQL(DELETE_PLAYER);
        db.execSQL(DELETE_HASGAME);
        db.execSQL(DELETE_STOPS);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rowsDeleted = 0;
        int uriType = urImatcher.match(uri);
        String newSelection;
        switch (uriType) {
            case PLAYER:
                rowsDeleted = db.delete(GameContract.TABLE_NAME_PLAYER,
                        selection, selectionArgs);
                break;
            case STOPS:
                rowsDeleted = db.delete(GameContract.TABLE_NAME_STOPS,
                        selection, selectionArgs);
                break;
            case STOPS_ID:
                newSelection = appendToSelection(uri, selection);
                rowsDeleted = db.delete(GameContract.TABLE_NAME_STOPS,
                        newSelection, selectionArgs);
                break;
            case HASGAME:
                newSelection = appendToSelection(uri, selection);
                rowsDeleted = db.delete(GameContract.TABLE_NAME_HASGAME,
                        newSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getReadableDatabase();
        int uriType = urImatcher.match(uri);
        Uri resultUri = null;
        long rowId;
        switch (uriType){
            case PLAYER:
                rowId = db.insert(GameContract.TABLE_NAME_PLAYER, null, values);
                resultUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(resultUri, null);
                break;
            case STOPS:
                rowId = db.insert(GameContract.TABLE_NAME_STOPS, null, values);
                resultUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(resultUri, null);
                break;
            case STOPS_ID:
                rowId = db.insert(GameContract.TABLE_NAME_STOPS, null, values);
                resultUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(resultUri, null);
                break;
            case HASGAME:
                rowId = db.insert(GameContract.TABLE_NAME_HASGAME, null, values);
                resultUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(resultUri, null);
                break;
            default: throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return resultUri;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        int uriType = urImatcher.match(uri);
        switch (uriType) {
            case PLAYER:
                builder.setTables(GameContract.TABLE_NAME_PLAYER);
                break;
            case STOPS:
                builder.setTables(GameContract.TABLE_NAME_STOPS);
                break;
            case STOPS_ID:
                builder.setTables(GameContract.TABLE_NAME_STOPS);
                builder.appendWhere(GameContract._ID + "=" + uri.getLastPathSegment());
                break;
            case HASGAME:
                builder.setTables(GameContract.TABLE_NAME_HASGAME);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised URI");
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = builder.query(db, projection, selection, selectionArgs,null,null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int rowsUpdated = 0;
        int uriType = urImatcher.match(uri);
        String newSelection;
        switch (uriType) {
            case PLAYER:
                rowsUpdated = db.update(GameContract.TABLE_NAME_PLAYER, values, selection, selectionArgs);
                break;
            case STOPS:
                rowsUpdated = db.update(GameContract.TABLE_NAME_STOPS, values, selection, selectionArgs);
                break;
            case HASGAME:
                rowsUpdated = db.update(GameContract.TABLE_NAME_HASGAME, values, selection, selectionArgs);
                break;
            case STOPS_ID:
                newSelection = appendToSelection(uri, selection);
                rowsUpdated = db.update(GameContract.TABLE_NAME_STOPS, values, newSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return rowsUpdated;
    }

    private String appendToSelection(Uri uri, String selection){
        String id = uri.getLastPathSegment();
        StringBuilder newSelection = new StringBuilder(GameContract._ID + "=" + id);
        if(selection != null && !selection.isEmpty()){
            newSelection.append(" AND "+selection);
        }
        return newSelection.toString();
    }
}
