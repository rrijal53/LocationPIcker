package com.rowsun.lestourydriver;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.Calendar;
import android.location.Location;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yayandroid.locationmanager.LocationBaseActivity;
import com.yayandroid.locationmanager.LocationConfiguration;
import com.yayandroid.locationmanager.LocationManager;
import com.yayandroid.locationmanager.constants.FailType;
import com.yayandroid.locationmanager.constants.LogType;
import com.yayandroid.locationmanager.constants.ProviderType;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends LocationBaseActivity implements SensorEventListener {

    private ImageView image;

    private float currentDegree = 0f;
    private float currentBearing = 0f;
    // device sensor manager
    private SensorManager mSensorManager;
    TextView tvHeading, tvDistance;
    ImageView add_landmark;
    DatabaseHelper db;
    List<Landmarks> list;
    RecyclerView rv_landmarks;
    Landmarks currentLandmark;
    ProgressBar pbar;

    @Override
    public LocationConfiguration getLocationConfiguration() {
        return new LocationConfiguration()
                .keepTracking(true)
                .askForGooglePlayServices(true)
                .setMinAccuracy(100.0f)
//                .setWaitPeriod(ProviderType.GOOGLE_PLAY_SERVICES, 5 * 1000)
                .setWaitPeriod(ProviderType.GPS,   1000)
//                .setWaitPeriod(ProviderType.NETWORK, 5 * 1000)
                .setGPSMessage("Would you mind to turn GPS on?")
                .setRationalMessage("Gimme the permission!");
    }

    @Override
    public void onLocationFailed(int failType) {
        switch (failType) {
            case FailType.PERMISSION_DENIED: {
                Utilities.toast(this,"Couldn't get location, because user didn't give permission!");
                break;
            }
            case FailType.GP_SERVICES_NOT_AVAILABLE:
            case FailType.GP_SERVICES_CONNECTION_FAIL: {
                Utilities.toast(this,"Couldn't get location, because Google Play Services not available!");
                break;
            }
            case FailType.NETWORK_NOT_AVAILABLE: {
               Utilities.toast(this,"Couldn't get location, because network is not accessible!");
                break;
            }
            case FailType.TIMEOUT: {
               Utilities.toast(this,"Couldn't get location, and timeout!");
                break;
            }
            case FailType.GP_SERVICES_SETTINGS_DENIED: {
               Utilities.toast(this,"Couldn't get location, because user didn't activate providers via settingsApi!");
                break;
            }
            case FailType.GP_SERVICES_SETTINGS_DIALOG: {
               Utilities.toast(this,"Couldn't display settingsApi dialog!");
                break;
            }
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLandmark.geo_lat = location.getLatitude();
        currentLandmark.geo_lng = location.getLongitude();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocationManager.setLogType(LogType.GENERAL);
        setTitle("Compass");
        currentLandmark = new Landmarks();
        pbar = (ProgressBar) findViewById(R.id.pbar);
        pbar.setVisibility(View.VISIBLE);
        image = (ImageView) findViewById(R.id.imageViewCompass);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        add_landmark = (ImageView) findViewById(R.id.add_landmark);
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, getApplicationContext(), MainActivity.this)) {
            updateUI();
        } else {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, 112, getApplicationContext(), this);
        }

        rv_landmarks = (RecyclerView) findViewById(R.id.rv_landmarks);
        rv_landmarks.setLayoutManager(new LinearLayoutManager(this));
        db = new DatabaseHelper(this);
        list = new ArrayList<>();
        list.addAll(db.getLandmarks());
//        list.add(new Landmarks("Bkt", 27.6774348, 85.4069141));
        add_landmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(currentLandmark!=null){
                    View v_alert = getLayoutInflater().inflate(R.layout.alert_input, null);
                    final EditText t = (EditText) v_alert.findViewById(R.id.title);
                    final EditText t1 = (EditText) v_alert.findViewById(R.id.lat);
                    final EditText t2 = (EditText) v_alert.findViewById(R.id.lng);
                    t1.setText(currentLandmark.geo_lat + "");
                    t2.setText(currentLandmark.geo_lng + "");
                    new AlertDialog.Builder(MainActivity.this).setTitle("Add Landmark:").setView(v_alert).setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String title = t.getText().toString();
                            Double lat = Double.parseDouble(t1.getText().toString());
                            Double lng = Double.parseDouble(t2.getText().toString());
                            if (title.isEmpty()) {
                                Snackbar.make(add_landmark, "Invalid title", Snackbar.LENGTH_LONG).show();
                                return;
                            }
                            if (lat.isNaN()) {
                                Snackbar.make(add_landmark, "Invalid lat", Snackbar.LENGTH_LONG).show();
                                return;

                            }
                            if (lat.isNaN()) {
                                Snackbar.make(add_landmark, "Invalid long", Snackbar.LENGTH_LONG).show();
                                return;

                            }
                            Landmarks l = new Landmarks(title, lat, lng);
                            db.addLandmark(l);
                            list.add(l);
                            rv_landmarks.getAdapter().notifyDataSetChanged();
                        }
                    }).setNegativeButton("Cancel", null).show();
                } else {
                    getLocation();
                }
            }
        });
        rv_landmarks.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.row_landmark, parent, false);
                return new ViewHolderLandmarks(v);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                ViewHolderLandmarks v = (ViewHolderLandmarks) holder;
                v.title.setText(list.get(position).title);
                v.latlng.setText(list.get(position).geo_lat + "," + list.get(position).geo_lng);
                v.clear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        db.deleteByName(list.get(position).title);
                        list.remove(list.get(position));
                        updateUI();
                        notifyDataSetChanged();
                    }
                });
                v.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentLandmark = list.get(position);
                        updateUI();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });

        getLocation();

    }


    public static void requestPermission(String strPermission, int perCode, Context _c, Activity _a) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(_a, strPermission)) {
            Toast.makeText(_c, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {

            ActivityCompat.requestPermissions(_a, new String[]{strPermission}, perCode);
        }
    }

    public static boolean checkPermission(String strPermission, Context _c, Activity _a) {
        int result = ContextCompat.checkSelfPermission(_c, strPermission);
        if (result == PackageManager.PERMISSION_GRANTED) {

            return true;

        } else {

            return false;

        }
    }

    class ViewHolderLandmarks extends RecyclerView.ViewHolder {

        TextView title, latlng;
        ImageView clear;

        public ViewHolderLandmarks(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            latlng = (TextView) itemView.findViewById(R.id.latlng);
            clear = (ImageView) itemView.findViewById(R.id.clear);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getLocation();
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float val = Math.round(event.values[0]);
        if (val != -currentDegree) {
            updateCompass(Math.round(event.values[0]));
        }

    }

    public void updateUI() {
        if (pbar.getVisibility() == View.VISIBLE) {
            pbar.setVisibility(View.GONE);
        }
        try {
            if (currentLandmark != null) {
                Double newLat = currentLandmark.geo_lat;
                Double newLon = currentLandmark.geo_lng;
                currentBearing = Float.valueOf(Utilities.getDirection(newLat, newLon, currentLandmark.geo_lat, currentLandmark.geo_lng) + "");
                Float d = Float.valueOf(Utilities.distance(newLat, newLon, currentLandmark.geo_lat, currentLandmark.geo_lng) + "");
                tvDistance.setText("Distance: " + d + "Km");
                setTitle(currentLandmark.title);
            }else {
                tvDistance.setText("NORTH");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


//    private Landmarks getNextLandMark() {
//        try {
//            Landmarks landmark = null;
//            int nextIndex = currentGPSPointIndex + 1; // probably next landmark index
//            // not valid for slide marks so looping till end until its not slide
//            while(nextIndex < myPackage.GPSPointTimeList.size()){ // loop till the end
//                landmark = myPackage.landMarksMap.get(myPackage.GPSPointTimeList.get(nextIndex));
//                if(!landmark.type.equals("slide")){
//                    break;
//                }
//            }
//            return landmark;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 111){
            exportToCsv();
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportToCsv() {
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getApplicationContext(), MainActivity.this)) {
         boolean success =  db.exportToCsv();
            if(success ){
                Utilities.toast(this, "Csv Exported to sdcard/whatsupdata/data.csv ");
            }else {
                Utilities.toast(this, "Export fail");
            }
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 112, getApplicationContext(), this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem i = menu.add(1, 111, 300, "Share");
        i.setIcon(R.drawable.ic_export);
        i.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }
    private void updateCompass(float degree) {
        tvHeading.setText("Bearing: " + currentBearing + ", Compass: " + degree);
        degree = (degree - currentBearing + 360) % 360;
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        ra.setDuration(250);
        ra.setFillAfter(true);
        image.startAnimation(ra);
        currentDegree = -degree;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}