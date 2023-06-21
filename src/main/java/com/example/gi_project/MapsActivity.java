package com.example.gi_project;

// objectives
//1-binding free code
//2- on Resume mein call karni hai
//3-created function for json utils

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final double circleRadius = 2000; // radius in meters
    private Circle geofenceCircle;
//    private Location User;
    private LatLng User;
    private ArrayList<Marker> markerList;
    private int counter=0;
    private boolean isMarkerClickEnabled = false;
    private boolean polygonclickenable=false;
    private Button fieldBtn,ok_btn;
//    private ActivityMapsBinding binding;
    private FusedLocationProviderClient cl;

    private ArrayList<Polygon> polygonList,withinradiusList;
    public static int LOCATION_REQ_CODE = 100;
    Json_read JSONUtils = new Json_read();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_maps); // removed binding


        polygonList = new ArrayList<>();

//        try {
//            JSONUtils.JsonData(this,"Dummy_Dataset.json");
//        }catch (Exception e){
//            e.printStackTrace();
//        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);



        mapFragment.getMapAsync(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        cl = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        withinradiusList=new ArrayList<>();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getUserLocation();
        } else {
            RequestPermissions();
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = cl.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    double lat=location.getLatitude();
                    double lng=location.getLongitude();
                    User=new LatLng(lat,lng);
                    // ONLY DOING THIS FOR TRIAL
                    LatLng ul=new LatLng(24.8114755155802,77.84562225698336);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(ul));
                    mMap.addMarker(new MarkerOptions().position(ul).title(lat+" , "+lng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ul,17f));
                }
            }
        });
    }

    private void RequestPermissions(){
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION },LOCATION_REQ_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_REQ_CODE){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                getUserLocation();
            }
            else{
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isPolygonIntersecting(Polygon j){
        boolean intersect = false;


        try {
            GeometryFactory geometryFactory = new GeometryFactory();
            Coordinate[] coordinates2 = new Coordinate[j.getPoints().size()];
            for (int i = 0; i < j.getPoints().size(); i++) {
                LatLng latLng = j.getPoints().get(i);
                coordinates2[i] = new Coordinate(latLng.latitude, latLng.longitude);
            }
            org.locationtech.jts.geom.Polygon jPolygon2 = geometryFactory.createPolygon(coordinates2);
            for(int k=0;k<withinradiusList.size();k++){

                Coordinate[] coordinates1 = new Coordinate[withinradiusList.get(k).getPoints().size()];
                for (int i = 0; i < withinradiusList.get(k).getPoints().size(); i++) {
                    LatLng latLng = withinradiusList.get(k).getPoints().get(i);
                    coordinates1[i] = new Coordinate(latLng.latitude, latLng.longitude);
                }
                org.locationtech.jts.geom.Polygon jPolygon1 = geometryFactory.createPolygon(coordinates1);
                intersect = jPolygon1.intersects(jPolygon2);
                if(intersect){
                    break;
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }


        return intersect;


    }

    private LatLng centerOfPpolygon(PolygonOptions w,GoogleMap mp){
        Polygon k=mp.addPolygon(w);
        k.remove();
        List<LatLng> polygonPoints = k.getPoints();
        double lat = 0.0,lng=0.0;
// Iterating over the points
        for (LatLng point : polygonPoints) {
            lat+=point.latitude;
            lng+= point.longitude;
        }
        lat=lat/polygonPoints.size();
        lng=lng/polygonPoints.size();
        LatLng temp=new LatLng(lat,lng);
        return temp;
    }

    private float distance_center_point(Circle c,LatLng D){
        Location centerLocation = new Location("center");
        centerLocation.setLatitude(geofenceCircle.getCenter().latitude);
        centerLocation.setLongitude(geofenceCircle.getCenter().longitude);

        Location pointLocation = new Location("point");
        pointLocation.setLatitude(D.latitude);
        pointLocation.setLongitude(D.longitude);
        float distance = centerLocation.distanceTo(pointLocation);
        return distance;
    }

    private void drawPolygons(PolygonOptions p,GoogleMap mp){
        LatLng center=centerOfPpolygon(p,mp);

        float distance=distance_center_point(geofenceCircle,center);

        if(distance<=circleRadius){
            Polygon k=mp.addPolygon(p.fillColor(Color.argb(75, 255, 255, 0)).strokeColor(Color.RED));
            withinradiusList.add(k);
        }

        Polygon k=mp.addPolygon(p.fillColor(Color.argb(75, 255, 255, 0)).strokeColor(Color.RED));
        polygonList.add(k);
        k.remove();


    }
// function for custom draw and check for violation
    private void customAddPolygon(ArrayList<Marker> m){
        ArrayList<LatLng> points = new ArrayList<>();
        for (Marker marker : m) {
            points.add(marker.getPosition());
        }

        // Create a polygon and add it to the map
        PolygonOptions polygon = new PolygonOptions()
                .addAll(points)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(75,255,255,0));
        Polygon pol = mMap.addPolygon(polygon);

        if(isPolygonIntersecting(pol)){
            Toast.makeText(this, "Field Overlapping with others Try Again!!", Toast.LENGTH_SHORT).show();
            pol.remove();
            removeMarkers(m);
        }
        else {

            drawPolygons(polygon,mMap);
            removeMarkers(m);
        }

    }
    private void enableMarkerClick() {
//        ArrayList<Marker> markerList= new ArrayList<>();
        markerList= new ArrayList<>();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                // Create a marker at the tapped location

                Marker marker = mMap.addMarker(new MarkerOptions().position(latLng));
                if(distance_center_point(geofenceCircle,marker.getPosition())<=circleRadius){

                    counter++;
                    // Check if the marker intersects with any existing polygons
                    boolean intersects = false;
                    for (Polygon polygon : withinradiusList) {
                        if (PolyUtil.containsLocation(latLng, polygon.getPoints(), true)) {
                            intersects = true;
                            break;
                        }
                    }

                    // If the marker is not inside any existing polygons, add it to the list and update the map
                    if (!intersects) {
                        LatLng templatlng = marker.getPosition();
                        double latitude = templatlng.latitude;
                        double longitude = templatlng.longitude;
                        markerList.add(marker);
                        isMarkerClickEnabled = true;
                        // my function for drawing polygon
                    }
                    else{
                        marker.remove();
                        Toast.makeText(getApplicationContext(), "Chosen point Overlapping with Fields", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    marker.remove();
                    Toast.makeText(MapsActivity.this, "You are not permitted outside the given range", Toast.LENGTH_SHORT).show();
                }



            }
        });


    }
    private void removeMarkers(ArrayList<Marker> n){
        for (Marker marker : n) {
            marker.remove();
        }
        n.clear(); // Clear the list after removing the markers
    }
//    private void RemovalEnable(GoogleMap gmap){
//        gmap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
//            @Override
//            public void onPolygonClick(@NonNull Polygon polygon) {
//                polygonList.remove(polygon);
//                polygon.remove();
//            }
//        });
//    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);


        // code for geofecning the area of google maps within  2km of range
        LatLng userLatLng = new LatLng(24.8114755155802,77.84562225698336);
        CircleOptions circleOptions = new CircleOptions()
                .center(userLatLng)
                .radius(2000) // 2km radius
                .strokeWidth(4) // stroke width
                .strokeColor(Color.YELLOW) //Optional: Customize stroke color
                .fillColor(Color.TRANSPARENT); // Transparent fill color
//        // Add the geofence circle to the map
        geofenceCircle = googleMap.addCircle(circleOptions);
        fieldBtn=(Button)findViewById(R.id.field_btn);
        ok_btn=(Button)findViewById(R.id.ok_btn);
//        remove_btn=(Button)findViewById(R.id.polygonremove_btn);

        String json=JSONUtils.JsonData(this,"Dummy_Dataset.json");
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    PolygonOptions po = new PolygonOptions();
//                    ArrayList<LatLng> latlngpoly=new ArrayList<LatLng>(); can be used but polyoption used

                    JSONObject locationObject = jsonArray.getJSONObject(i);
                    JSONArray indlatlng = locationObject.getJSONArray("Polygon" + (i + 1));
                    LatLng latLng = null;
                    for (int j = 0; j < indlatlng.length(); j++) {
                        JSONArray temp = indlatlng.getJSONArray(j);
                        double lattemp = temp.getDouble(1);
                        double lngtemp = temp.getDouble(0);
                        latLng = new LatLng(lattemp, lngtemp);
                        po.add(latLng);
                    }
                    drawPolygons(po, mMap);


//                    latlngpoly.add(latLng); can be used but polyiotion used.

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        fieldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    ok_btn.setVisibility(View.VISIBLE);
                    fieldBtn.setVisibility(View.GONE);
                    enableMarkerClick();

            }
        });

//        remove_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//               RemovalEnable(mMap);
//
//            }
//        });

        ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // for final storage and drawing of [polygon that is custom added by the field agent.
//                customAddPolygon();
                if(counter<3){
                    Toast.makeText(MapsActivity.this, "Cannot create field with Less than 3 points", Toast.LENGTH_SHORT).show();
                    removeMarkers(markerList);
                }
                else if(isMarkerClickEnabled && counter>=3){
                    customAddPolygon(markerList);
                }
                counter=0;
                isMarkerClickEnabled = false;
                mMap.setOnMapClickListener(null);
                ok_btn.setVisibility(View.GONE);
                fieldBtn.setVisibility(View.VISIBLE);

            }
        });


    }
}