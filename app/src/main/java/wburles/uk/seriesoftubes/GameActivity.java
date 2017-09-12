package wburles.uk.seriesoftubes;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.games.Game;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import wburles.uk.seriesoftubes.SQLite.GameDAO;
import wburles.uk.seriesoftubes.SQLite.PlayerBuilder;

public class GameActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap game;
    private Menu menu;
    private GameDAO database;
    private boolean gameOver;
    private String gameOverMsg;

    private Map<Marker,Stop> stopsMap = new HashMap<>();
    private Map<Stop,Marker> markerMap = new HashMap<>();
    private ArrayList<Marker> markersList = new ArrayList<>();
    private ArrayList<Polyline> polyLinesArray = new ArrayList<>();
    private ArrayList<Polyline> polyLinesHistoryArray = new ArrayList<>();

    private Player player;
    private Stop selected;

    private MenuItem move;
    private MenuItem shop;
    private MenuItem bike;
    private boolean bikeToggle = false;
    private ArrayList<BikePoint> bikePointsList = new ArrayList<>();
    private MenuItem walk;
    private boolean walkToggle = false;
    private adjacentStationTask walkableTask;
    private boolean historyToggle = false;

    public static final int RESULT_WON = 191;
    public static final int RESULT_DEAD = 919;

    private String[] tubeLines = {"bakerloo","central","circle","district","hammersmith-city","jubilee",
                                    "metropolitan","northern","piccadilly","victoria","waterloo-city"};
    private String[] tubeColors = {"#9c4d00","#e11b12","#fbcc00","#1b7424","#e96f90","#838a90",
                                    "#7f004f","#1a1718","#162988","#2088da","#78c4ad"}; //The colours of the underground in the same order as tubeLines

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Maps", "Starting up ------------------------------------------------------------");
        database = new GameDAO();
        setContentView(R.layout.activity_game);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        game = googleMap;

        // Add a marker in London and move the camera
        LatLng london = new LatLng(51.58, -0.159);
        game.moveCamera(CameraUpdateFactory.newLatLngZoom(london, 9));

        if(!database.gameExists(getBaseContext())){
            Toast.makeText(getBaseContext(),getString(R.string.tap_map),Toast.LENGTH_SHORT).show();
            Log.d("Maps","The map is being built");
            buildMap();
        } else {
            dbRetriever();
        }

        game.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(player == null && markersList.size() > 10){
                    Random rand = new Random();
                    Marker mark = markersList.get(rand.nextInt(markersList.size()));
                    Stop stop = stopsMap.get(marker);
                    player = new Player(stop,mark,100,0,30);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
                    player.addToHistory(marker.getPosition());
                    lineRetrieverTask task = new lineRetrieverTask();
                    task.execute(stop);
                }
                if (polyLinesArray.size() > 0) { //remove all lines on the screen
                    for (Polyline polyLine : polyLinesArray) {
                        polyLine.remove();
                    }
                    polyLinesArray.clear();
                }
                Stop stop = stopsMap.get(marker);

                if(!stop.getId().equals(player.getStop().getId())){
                    selected = stop;
                    move.setEnabled(true);
                    move.setIcon(R.drawable.move_wht);
                }
                if(stop.getLines().isEmpty()) { // if route info hasn't been downloaded yet, get it
                    lineRetrieverTask lineRet = new lineRetrieverTask();
                    lineRet.execute(stop);
                } else {
                    polyLineDraw polyDraw = new polyLineDraw();
                    polyDraw.execute(stop.getLines()); // finally draw the routes from the station
                }
                return false;
            }
        });
        game.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                move.setEnabled(false);
                move.setIcon(R.drawable.move_blk);
                selected = null;
                if(player == null && markersList.size() > 10){
                    Random rand = new Random();
                    Marker marker = markersList.get(rand.nextInt(markersList.size()));
                    Stop stop = stopsMap.get(marker);
                    player = new Player(stop,marker,100,0,30);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
                    player.addToHistory(marker.getPosition());
                    lineRetrieverTask task = new lineRetrieverTask();
                    task.execute(stop);
                }
            }
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        move = menu.findItem(R.id.menu_move);
        shop = menu.findItem(R.id.menu_shop);
        bike = menu.findItem(R.id.menu_bike);
        walk = menu.findItem(R.id.menu_walk);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbBuilder();
    }

    public void bikeMove(){
        borisBikeCheckTask borisBikeTask = new borisBikeCheckTask();
        borisBikeTask.execute(selected);
        ArrayList<BikePoint> points = null;
        try {
            points = borisBikeTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        boolean hasBikes = false;
        for(BikePoint point : points){
            if (point.hasBikes()) hasBikes = true;
        }
        if(hasBikes){
            if(player.getCash() >= 5) {
                player.setCash(player.getCash() - 5);
                move();
            } else {
                Toast.makeText(getBaseContext(),getString(R.string.not_enough_cash_bike), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getBaseContext(),getString(R.string.no_bikes_available),Toast.LENGTH_SHORT).show();
        }
        for (int i = 0; i < bikePointsList.size(); i++) {
            bikePointsList.get(i).clearOverlay();
            for(Marker marker : markersList){
                marker.setVisible(true);
            }
        }
        polyLineDraw polyDraw = new polyLineDraw();
        polyDraw.execute(selected.getLines());
        bikePointsList.clear();
        bikeToggle = false;
    }

    public void walkMove(){
        for(Marker marker : markersList){
            marker.setVisible(true);
        }
        walkToggle = false;
        Random rand = new Random();
        if(rand.nextInt(2) != 0){
            startMuggingActivity();
        }
        move();
    }

    public void move() {
        Marker marker = markerMap.get(selected);
        markerMap.get(selected).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
        setIcon(player.getCurrent(),stopsMap.get(player.getCurrent()));
        player.addToHistory(selected.getPos());
        selected.setVisited();
        player.move(stopsMap.get(marker), marker, 0);
        try {
            walkableTask = new adjacentStationTask();
            walkableTask.execute(selected);
            if (selected.isShop()) {
                shop.setEnabled(true);
                shop.setIcon(R.drawable.shop_wht);
            } else {
                shop.setEnabled(false);
                shop.setIcon(R.drawable.shop_blk);
            }
            if (!selected.getBikePoints().get(0).equals("null")) {
                borisBikeCheckTask task = new borisBikeCheckTask();
                task.execute(selected);
                ArrayList<BikePoint> points = task.get();
                for(BikePoint point : points){
                    Log.d("Maps","moving point id: "+point.getId());
                    if (point.hasBikes()) {
                        bike.setEnabled(true);
                        bike.setIcon(R.drawable.bike_wht);
                    }
                }
            } else {
                bike.setEnabled(false);
                bike.setIcon(R.drawable.bike_blk);
            }
            if (selected.isMugger()) {
                startMuggingActivity();
            }
            Log.d("Maps","WalkableTask including: "+walkableTask.get().size());
            if (walkableTask.get().size() != 0){
                walk.setEnabled(true);
                walk.setIcon(R.drawable.walk_wht);
            } else {
                walk.setEnabled(false);
                walk.setIcon(R.drawable.walk_blk);
            }
        } catch (ExecutionException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_move){
            if(!gameOver && !player.getStop().getId().equals(selected.getId())){ // if not the stop the player is at
                int validCode = -1;
                if(bikeToggle){
                    bikeMove();
                }
                if(walkToggle){
                    walkMove();
                }
                else {
                    for (Line line : player.getStop().getLines()) {
                        if (line.getStops().contains(selected.getPos())) { // if the cur. stop is on the players' route
                            validCode = 1;
                        }
                    }
                    if (validCode == 1) {
                        if(player.getCash() >= 2) {
                            player.setCash(player.getCash() - 2);
                            move();
                        } else {
                            setGameOver(getString(R.string.gameover_no_money));
                        }
                    } else {
                        Toast.makeText(getBaseContext(), getString(R.string.not_same_line), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        if (id == R.id.menu_shop){
            startShopFragment();
        }
        if (id == R.id.menu_player){
            startPlayerFragment();
        }
        if (id == R.id.menu_bike){
            if(!bikeToggle){
                for(Marker marker : markersList){
                    Stop stop = stopsMap.get(marker);
                    if(stop.getBikePoints().get(0).equals("null")){
                        marker.setVisible(false);
                    } else if(distCalculator(player.getCurrent().getPosition(), marker.getPosition()) > 4){
                        marker.setVisible(false);
                    } else {
                        borisBikeCheckTask borisBikeTask = new borisBikeCheckTask();
                        borisBikeTask.execute(stop);
                        ArrayList<BikePoint> bikePoints = null;
                        try {
                            bikePoints = borisBikeTask.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        bikeOverlayBuilder(bikePoints);
                    }
                }
                bikeToggle = true;
            } else {
                for(Marker marker : markersList){
                    marker.setVisible(true);
                }
                bikeToggle = false;
            }
        }
        if (id == R.id.menu_walk){
            if(!walkToggle){
                try {
                    ArrayList<Marker> walkableStops = walkableTask.get();
                    for(Marker marker : markersList){
                        if(!walkableStops.contains(marker)){
                            marker.setVisible(false);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                walkToggle = true;
            } else {
                for(Marker marker : markersList){
                    marker.setVisible(true);
                }
                walkToggle = false;
            }
        }
        if (id == R.id.menu_history){
            if(historyToggle){
                historyToggle = false;
                for(Polyline line : polyLinesHistoryArray){
                    line.remove();
                }
                polyLinesHistoryArray.clear();
            } else{
                historyToggle = true;
                polyLineHistoryDraw drawHistory = new polyLineHistoryDraw();
                drawHistory.execute(player.getHistory());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void buildMap(){
        for(String line : tubeLines){
            stopRetrieverTask task = new stopRetrieverTask();
            StringBuilder urlStr = new StringBuilder(getString(R.string.tfl_url));
            urlStr.append("Line/");
            urlStr.append(line);
            urlStr.append("/StopPoints?");
            urlStr.append(getString(R.string.tfl_api_keys));
            Log.d("Maps", "URL = " + urlStr.toString());

            try{
                URL url = new URL(urlStr.toString());
                task.execute(url);
            } catch (MalformedURLException e) {
                Log.d("Maps", "Bad URL --------------------------------------");
                e.printStackTrace();
            }
        }
    }

    public void dbBuilder() {
        if(!gameOver && player != null) {
            ArrayList<Stop> stops = new ArrayList<>();
            for (Marker marker : markersList) {
                Log.d("Maps", "stopDbBuilder ------- " + stopsMap.get(marker).getName());
                stops.add(stopsMap.get(marker));
            }
            database.setExists(getBaseContext());
            database.addStops(getBaseContext(), stops);
            database.addPlayer(getBaseContext(), player);
        }
    }

    public void dbRetriever() {
        ArrayList<Stop> stops = database.getStops(getBaseContext());
        PlayerBuilder playerBuilder = database.getPlayerBuilder(getBaseContext());
        Log.d("Maps","The player is: "+playerBuilder.getPos().toString()+playerBuilder.getCash()+" "+playerBuilder.getHealth());

        for(Stop stop : stops){
//            Log.d("Maps","stopDbRetriever --- - -- - " + stop.getName());
//            Log.d("Maps","stopDbRetriever --- - -- - " + stop.getPos());
            MarkerOptions opt = new MarkerOptions().position(stop.getPos()).title(stop.getName());
            Marker marker = game.addMarker(opt);
            marker = setIcon(marker, stop);
            stop.setMarker(marker);
            stopsMap.put(marker, stop);
            markerMap.put(stop, marker);
            markersList.add(marker);

            if(stop.getPos().latitude == playerBuilder.getPos().latitude &&
                    stop.getPos().longitude == playerBuilder.getPos().longitude){
                Log.d("Maps","stopDbRetriever playerConstructor --- - -- - " + stop.getName());
                lineRetrieverTask lineRet = new lineRetrieverTask();
                lineRet.execute(stop);
                player = new Player(stop,marker,playerBuilder.getHealth(),playerBuilder.getWeapon(),playerBuilder.getCash());
                player.setBoost(playerBuilder.getBoost());
                player.setHistory(playerBuilder.getHistory());
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.player));
                player.move(stop,marker,0);
            }
        }
    }

    public void startShopFragment(){
        ShopFragment shopFragment = new ShopFragment();
        Bundle args = new Bundle();
        args.putInt("health",player.getHealth());
        args.putFloat("cash",player.getCash());
        args.putInt("weapon",player.getWeapon());
        shopFragment.setArguments(args);
        shopFragment.show(getFragmentManager(), "shop_fragment");
    }

    public void retrieveShopItems(int health, int weapon, float cash){
        player.setHealth(health);
        player.setWeapon(weapon);
        player.setCash(cash);
        Log.d("Maps",player.toString());
    }

    public void startPlayerFragment(){
        ViewPlayerFragment playerFragment = new ViewPlayerFragment();
        Bundle args = new Bundle();
        args.putInt("health",player.getHealth());
        args.putFloat("cash",player.getCash());
        args.putInt("weapon",player.getWeapon());
        args.putInt("boost",player.getBoost());
        Log.d("Maps",player.toString());
        playerFragment.setArguments(args);
        playerFragment.show(getFragmentManager(), "view_player_fragment");
    }

    public void startMuggingActivity(){
        Intent intent = new Intent(getApplicationContext(), MuggingActivity.class);
        intent.putExtra("weapon",player.getWeapon());
        intent.putExtra("health",player.getHealth());
        intent.putExtra("cash",player.getCash());
        intent.putExtra("boost",player.getBoost());
        startActivityForResult(intent, RESULT_WON);
    }

    public void setGameOver(String msg){
        gameOver = true;
        Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
        for(Marker marker : markersList){
            marker.setVisible(false);
        }
        move.setEnabled(false);
        move.setIcon(R.drawable.move_blk);
        shop.setEnabled(false);
        shop.setIcon(R.drawable.shop_blk);
        bike.setEnabled(false);
        bike.setIcon(R.drawable.bike_blk);
        walk.setEnabled(false);
        walk.setIcon(R.drawable.walk_blk);
        game.addCircle(new CircleOptions()
                .center(new LatLng(51.58, -0.159))
                .radius(50000)
                .fillColor(Color.argb(120,0,0,0)));
        for(Polyline line : polyLinesArray){
            line.remove();
        }
        database.gameOver(getBaseContext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_WON) {
            if(player.getCash() <= 0) {
                setGameOver(getString(R.string.gameover_no_money));
            } else {
                Bundle extras = data.getExtras();
                player.setHealth(extras.getInt("health"));
                player.setCash(extras.getFloat("cash"));
                player.setBoost(extras.getInt("boost"));
                player.setWeapon(extras.getInt("weapon"));
                stopsMap.get(player.getCurrent()).beatMugger();
            }
        } else if(resultCode == RESULT_DEAD){
            setGameOver(getString(R.string.gameover_killed));
        }
    }

    public Marker setIcon(Marker marker, Stop stop){
        if(stop.isVisited()){
            if(stop.isShop()){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.underground_visited));
            }
        } else {
            if(stop.isShop()){
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.shop_marker));
            } else {
                marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.underground));
            }
        }
        return marker;
    }

    public class lineRetrieverTask extends AsyncTask<Stop,Void,Stop>{
        String statusMessage = "null";
        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Stop doInBackground(Stop... params) {
            HttpURLConnection connection = null;
            Stop stop = params[0];
            String id = stop.getId();

            StringBuilder urlStr = new StringBuilder(getString(R.string.tfl_url));
            urlStr.append("StopPoint/");
            urlStr.append(id);
            urlStr.append("/Route?");
            urlStr.append(R.string.tfl_api_keys);
            //Log.d("Maps", "URL = " + urlStr.toString());

            try {
                URL url = new URL(urlStr.toString());
                connection = (HttpURLConnection) url.openConnection();

                InputStream stream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                statusMessage = "Connection Successful";
                String line;
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null){
                    //Log.d("Maps","lineRetrieverTask loop running - "+line.toString());
                    builder.append(line);
                }
                JSONArray json = new JSONArray(builder.toString());

                ArrayList<Line> lineList = new ArrayList<>();

                for(int i = 0; i < json.length(); i++){
                    JSONObject route = json.getJSONObject(i);
                    String lineId = route.getString("lineId");
                    line = route.getString("lineString");

                    int lineNo = 0;
                    for(int r = 0; r<tubeLines.length; r++){
                        if(tubeLines[r].equals(lineId)){
                            lineNo = r;
                        }
                    }
                    line = line.substring(3,line.length()-3);
                    String[] stops = line.split("\\],\\["); // use jsonobject
                    ArrayList<LatLng> parsedStops = new ArrayList<>();
                    for(int r = 0; r < stops.length; r++){ //parse all of the stops into lat/lngs & add to arraylist
                        String str = stops[r];
                        //Log.d("Maps","onPost lineRetriever pos: "+str);

                        String[] posStr = str.split(",");
                        parsedStops.add(new LatLng(Double.parseDouble(posStr[1]),Double.parseDouble(posStr[0])));
                    }
                    lineList.add(new Line(lineId,tubeColors[lineNo],parsedStops));
                }
                stop.setLines(lineList);

                return stop;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Stop stop) {
            stopsMap.put(stop.getMarker(),stop);

            polyLineDraw polyDraw = new polyLineDraw();
            polyDraw.execute(stop.getLines());
        }
    }

    public class polyLineDraw extends AsyncTask<ArrayList<Line>,Void,ArrayList<PolylineOptions>>{

        @Override
        protected ArrayList<PolylineOptions> doInBackground(ArrayList<Line>... lines) {
            ArrayList<PolylineOptions> polyLines = new ArrayList<>();
            for(Line line : lines[0]){
                PolylineOptions tubePoly = new PolylineOptions()
                        .geodesic(true)
                        .color(Color.parseColor(line.getColor()))
                        .width(6);
                LatLng prevLatLng = line.getStops().get(0);
                //Log.d("Maps","onPost lineDraw route: "+line.getRoute());
                for(LatLng pos : line.getStops()){

                    tubePoly.add(prevLatLng,pos);
                    prevLatLng=pos;
                }
                polyLines.add(tubePoly);
            }
            return polyLines;
        }

        @Override
        protected void onPostExecute(ArrayList<PolylineOptions> tubePoly) {
            if(!bikeToggle) {
                if(!walkToggle) {
                    for (PolylineOptions line : tubePoly) {
                        polyLinesArray.add(game.addPolyline(line));
                    }
                }
            }

        }
    }

    public class polyLineHistoryDraw extends AsyncTask<ArrayList<LatLng>,Void,ArrayList<PolylineOptions>>{

        @Override
        protected ArrayList<PolylineOptions> doInBackground(ArrayList<LatLng>... history) {
            ArrayList<PolylineOptions> polyLines = new ArrayList<>();

            PolylineOptions historyPoly = new PolylineOptions()
                    .geodesic(true)
                    .color(Color.parseColor("#ff9326"))
                    .width(8);
            LatLng prevLatLng = history[0].get(0);
            //Log.d("Maps","onPost lineDraw route: "+line.getRoute());
            for(LatLng pos : history[0]){
                historyPoly.add(prevLatLng, pos);
                prevLatLng=pos;
            }
            polyLines.add(historyPoly);

            return polyLines;
        }

        @Override
        protected void onPostExecute(ArrayList<PolylineOptions> tubePoly) {
            for (PolylineOptions line : tubePoly) {
                polyLinesHistoryArray.add(game.addPolyline(line));
            }
        }
    }

    public class borisBikeCheckTask extends AsyncTask<Stop, Void, ArrayList<BikePoint>> {

        @Override
        protected ArrayList<BikePoint> doInBackground(Stop... stops) {
            HttpURLConnection connection = null;
            ArrayList<BikePoint> bikePoints = new ArrayList<>();

            Stop stop = stops[0];

            for(String point : stop.getBikePoints()){
                try {
                    StringBuilder urlStr = new StringBuilder(getString(R.string.tfl_url));
                    urlStr.append("BikePoint/");
                    urlStr.append(point).append("?");
                    urlStr.append(getString(R.string.tfl_api_keys));
                    Log.d("Maps", "URL = " + urlStr.toString());

                    URL url = new URL(urlStr.toString());
                    connection = (HttpURLConnection) url.openConnection();

                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    String line;
                    StringBuilder builder = new StringBuilder();
                    while((line = reader.readLine()) != null){
                        Log.d("Maps","borisBikeChecker loop running - "+line.toString());
                        builder.append(line);
                    }
                    JSONObject json = new JSONObject(builder.toString());
                    LatLng pos = new LatLng(json.getDouble("lat"),json.getDouble("lon"));

                    JSONArray properties = json.getJSONArray("additionalProperties");
                    JSONObject nbBikes = properties.getJSONObject(6);
                    boolean hasBikes;
                    if(nbBikes.getInt("value") > 0){
                        hasBikes = true;
                    } else {
                        hasBikes = false;
                    }
                    JSONObject nbEmptyDocks = properties.getJSONObject(7);
                    boolean hasFree;
                    if(nbEmptyDocks.getInt("value") > 0){
                        hasFree = true;
                    } else {
                        hasFree = false;
                    }
                    bikePoints.add(new BikePoint(point, hasBikes, hasFree, pos));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
            return bikePoints;
        }
    }

    public void bikeOverlayBuilder(ArrayList<BikePoint> bikePoints){
        for(BikePoint point : bikePoints) {
            Log.d("Maps", point.getId() + " " + point.hasBikes() + " " + point.hasFree());
            GroundOverlayOptions opt = new GroundOverlayOptions().position(point.getPos(), 100f);
            CircleOptions circleOptions = new CircleOptions()
                    .center(point.getPos())
                    .strokeWidth(3f)
                    .strokeColor(Color.parseColor("#208bd9"))
                    .radius(150);
            if(point.hasFree()){
                opt.image(BitmapDescriptorFactory.fromResource(R.drawable.cycle_hire));
            } else {
                opt.image(BitmapDescriptorFactory.fromResource(R.drawable.cycle_hire_unavailable));
                circleOptions.visible(false);
            }
            GroundOverlay overlay = game.addGroundOverlay(opt);
            Circle circle = game.addCircle(circleOptions);



            point.setOverlays(overlay, circle);
            bikePointsList.add(point);
        }
    }

    public class stopRetrieverTask extends AsyncTask<URL,Void,ArrayList<Stop>> {

        String statusMessage = "null";

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected ArrayList<Stop> doInBackground(URL... params) {
            ArrayList<Stop> stops = new ArrayList<Stop>();

            HttpURLConnection connection = null;

            try { connection = (HttpURLConnection) params[0].openConnection();

                InputStream stream = connection.getInputStream();
                Log.d("Maps","\n\nStopRetrieverTask is Running");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                statusMessage = "Connection Successful";
                String line;
                StringBuilder builder = new StringBuilder();
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                JSONArray json = new JSONArray(builder.toString());

                for(int i = 0; i < json.length(); i++){
                    JSONObject stop = json.getJSONObject(i);

                    stops.add(getStop(stop));
                }

                for(Stop stop : stops){
                    ArrayList<String> bikePoints = new ArrayList<>();

                    StringBuilder urlStr = new StringBuilder(getString(R.string.tfl_url));
                    urlStr.append("BikePoint?lat=");
                    urlStr.append(stop.getPos().latitude).append("&lon=");
                    urlStr.append(stop.getPos().longitude).append("&radius=150&");
                    urlStr.append(getString(R.string.tfl_api_keys));
                    Log.d("Maps", "URL = " + urlStr.toString());

                    URL url = new URL(urlStr.toString());
                    connection = (HttpURLConnection) url.openConnection();

                    stream = connection.getInputStream();
                    Log.d("Maps","\n\nStopRetrieverTask - bikePoints is Running");
                    reader = new BufferedReader(new InputStreamReader(stream));
                    statusMessage = "Connection Successful";
                    String bikeLine;
                    builder = new StringBuilder();
                    while((bikeLine = reader.readLine()) != null){
                        builder.append(bikeLine);
                    }
                    JSONObject bikePointSearch = new JSONObject(builder.toString());
                    JSONArray places = bikePointSearch.getJSONArray("places");

                    for(int i = 0; i < places.length(); i++){
                        JSONObject bikePoint = places.getJSONObject(i);
                        bikePoints.add(bikePoint.getString("id"));
                        Log.d("Maps","bikePoint addition: "+bikePoint.getString("id"));
                    }
                    if(bikePoints.size() == 0){
                        bikePoints.add("null");
                    }
                    stop.setBikePoints(bikePoints);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
            }
            return stops;
        }

        @Override
        protected void onPostExecute(ArrayList<Stop> stops) {
            Log.d("Maps", statusMessage);
            for(Stop stop : stops){
               // Log.d("Maps", stop.getName() + " " + stop.getPos());
                MarkerOptions opt = new MarkerOptions().position(stop.getPos()).title(stop.getName());
                Random rand = new Random();
                int distribution = rand.nextInt(10);
                if(distribution < 2){ // if rand == 0 - 1 make it a shop
                    stop.makeShop();
                } else if(distribution > 1 && distribution < 5){
                    stop.makeMugger(); // if rand == 2 - 4 make it a mugger
                }
                Marker marker = game.addMarker(opt);
                marker = setIcon(marker, stop);
                stop.setMarker(marker);
                stopsMap.put(marker,stop);
                markerMap.put(stop,marker);
                markersList.add(marker);
            }
        }
        private Stop getStop(JSONObject stop) throws JSONException {
            String name = stop.getString("commonName");
            String id = stop.getString("id");
            //Log.d("Maps", "We have retrieval - " + name + " " + id);
            LatLng pos = new LatLng(stop.getDouble("lat"),stop.getDouble("lon"));

            return new Stop(name, id, pos);
        }
    }

    public class adjacentStationTask extends AsyncTask<Stop,Void,ArrayList<Marker>> {

        String statusMessage;

        @Override
        protected ArrayList<Marker> doInBackground(Stop... stops) {
            Stop stop = stops[0];
            ArrayList<Marker> stopMarkers = new ArrayList<>();

            StringBuilder urlStr = new StringBuilder();
            urlStr.append(getString(R.string.tfl_url));
            urlStr.append("StopPoint?lat=").append(stop.getPos().latitude);
            urlStr.append("&lon=").append(stop.getPos().longitude);
            urlStr.append("&stopTypes=NaptanMetroStation&radius=350&returnLines=false&");
            urlStr.append(getString(R.string.tfl_api_keys));
            Log.d("Maps", "URL = "+urlStr.toString());

            HttpURLConnection connection = null;

            try {
                URL url = new URL(urlStr.toString());
                connection = (HttpURLConnection) url.openConnection();

                InputStream stream = connection.getInputStream();
                Log.d("Maps", "\n\nadjacentStationTask is Running");
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                statusMessage = "Connection Successful";
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                JSONObject json = new JSONObject(builder.toString());
                JSONArray stopPoints = json.getJSONArray("stopPoints");
                ArrayList<String> stopIds = new ArrayList<>();
                for(Marker marker : markersList){
                    Stop i = stopsMap.get(marker);
                    stopIds.add(i.getId());
                }
                for(int i = 0; i < stopPoints.length(); i++){
                    JSONObject stopPoint = stopPoints.getJSONObject(i);
                    String spId = stopPoint.getString("id");
                    if(stopIds.contains(spId)){
                        stopMarkers.add(markersList.get(stopIds.indexOf(spId)));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
            }
            return stopMarkers;
        }
    }

    public double distCalculator(LatLng start, LatLng end){
        double dist, theta, lon1, lon2, lat1, lat2;
        lon1 = start.longitude;
        lon2 = end.longitude;
        lat1 = start.latitude;
        lat2 = end.latitude;
        theta = lon1 - lon2;
        dist = Math.sin(degreesToRadians(lat1)) * Math.sin(degreesToRadians(lat2)) + Math.cos(degreesToRadians(lat1)) * Math.cos(degreesToRadians(lat2)) * Math.cos(degreesToRadians(theta));
        dist = Math.acos(dist);
        dist = radiansToDegrees(dist);
        dist = dist * 60 * 1.1515;
        Log.d("Maps","Calculated distance ----------- " + dist * 1.609344);
        return dist * 1.609344;
    }

    public double degreesToRadians(double degrees){
        return (degrees * Math.PI / 180.0);
    }

    public double radiansToDegrees(double radians){
        return (radians * 180 / Math.PI);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        this.menu = menu;
        return true;
    }

}
