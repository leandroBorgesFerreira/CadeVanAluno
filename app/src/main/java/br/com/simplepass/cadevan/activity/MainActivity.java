package br.com.simplepass.cadevan.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.adapters.MapInfoAdapter;
import br.com.simplepass.cadevan.domain.Van;
import br.com.simplepass.cadevan.drawer.DrawerItemClickListener;
import br.com.simplepass.cadevan.maps.LatLngInterpolator;
import br.com.simplepass.cadevan.maps.MapSync;
import br.com.simplepass.cadevan.maps.MarkerAnimation;
import br.com.simplepass.cadevan.maps.VanPointsDrawer;
import br.com.simplepass.cadevan.retrofit.AccessToken;
import br.com.simplepass.cadevan.utils.Constants;
import br.com.simplepass.cadevan.utils.LocationUtils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        ProgressShower,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        VanPointsDrawer {

    @Bind(R.id.looking_for_driver_message) TextView mSearchingMessageTextView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private GoogleApiClient mGoogleApiClient;
    private Toolbar mToolbar;
    private GoogleMap mGoogleMap;
    private Marker mFollowingMarker;
    private Map<Integer, Van> mVansMap;
    private MapSync mMapSync;
    //private AccessToken mAccessToken; -- Token não tem utilidade atual.
    private AccountManager mAccountManager;
    private Account mAccount;
    private Van mSingleVan;
    boolean mIsOnProgress;

    public static final String EXTRA_CHOOSEN_VAN_ID =
            "br.com.simplepass.cadevan.extra.EXTRA_CHOOSEN_VAN_ID";

    private static boolean mapReady;
    private static final int ACCESS_LOCATION = 1;

    @Override
    public void showProgress(boolean show){
        if(show){
            mSearchingMessageTextView.setVisibility(View.VISIBLE);
            mIsOnProgress = true;
        } else{
            mSearchingMessageTextView.setVisibility(View.GONE);
            mIsOnProgress = false;
        }
    }

    @Override
    public boolean isOnProgress() {
        return mIsOnProgress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("");
        }

        getLocationPermission(findViewById(R.id.content_layout));

        mIsOnProgress = false;

        mAccountManager = AccountManager.get(MainActivity.this);
        mAccount = new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
        mVansMap = new HashMap<>();

        drawerInit();
        mapInit();
    }

    private boolean getLocationPermission(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(view, R.string.permission_rationale_location, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
                        }
                    });
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, ACCESS_LOCATION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(LocationServices.API)
                        .build();
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    private void drawerInit(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(
                new DrawerItemClickListener(this, mDrawerLayout));

        View headerView = navigationView.getHeaderView(0);

        TextView drawerName = (TextView) headerView.findViewById(R.id.text_view_drawer_name);
        drawerName.setText(mAccountManager.getUserData(mAccount, "name"));

        TextView drawerPhone = (TextView) headerView.findViewById(R.id.text_view_drawer_phone_number);
        drawerPhone.setText(mAccountManager.getUserData(mAccount, "phoneNumber"));
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_change_driver)
    public void goToChooseDriver(){
        Intent intent = new Intent(this, ChooseDriverActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(this);

            startActivity(intent, activityOptions.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_maps_zoom_plus)
    public void zoomIn(View view){
        if (mGoogleMap != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_maps_zoom_minus)
    public void zommOut(View view){
        if (mGoogleMap != null) {
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_maps_self_position)
    public void selfLocation(View view){
        if (mGoogleMap != null && mGoogleApiClient.isConnected()) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location =
                        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location != null) {
                    LatLng selfLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selfLatLng, 16));
                }
            }

        }
    }

    private void mapInit(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        mapReady = false;

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawers();
        } else{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        Intent intent = getIntent();

        //Caso o mapa esteja carregado, começa a syncronizar a posição
        if(mapReady){
             if(mMapSync == null){
                if(intent.hasExtra(EXTRA_CHOOSEN_VAN_ID)) {
                    mMapSync = new MapSync(this, this, intent.getLongExtra(EXTRA_CHOOSEN_VAN_ID, -1), this);
                } else{
                    mMapSync = new MapSync(this, this, this);
                }
             }

            mMapSync.start();
        }
    }

    @Override
    public void onPause(){
        if(mMapSync != null) {
            mMapSync.stop();
        }

        super.onPause();
    }


    @Override
    public void onConnected(Bundle bundle) {
        if(ContextCompat.checkSelfPermission(MainActivity.this,
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            LatLng selfLatLng;
            LatLng moveCameraLatLng;

        /* Caso o mapa carregue primeiro, quando conectar com o location API vai para o
         * carrega o lugar da pessoa */
            if (location != null) {
                selfLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(selfLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_map_self))
                        .title("")
                        .snippet(getString(R.string.maps_self_mark_snippet)));

            /* Caso conecte primeiro no Location API move a camera para o lugar certo */
                moveCameraLatLng = selfLatLng;


                if (mGoogleMap != null) {
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCameraLatLng, LocationUtils.getDefaultZoom()));
                }
            }
        } else{
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationUtils.getDefaultLatLng(),
                    LocationUtils.getDefaultZoom()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        mGoogleMap = googleMap;

        View windowInfoView = getLayoutInflater().inflate(R.layout.info_window_view, null);
        mGoogleMap.setInfoWindowAdapter(new MapInfoAdapter(windowInfoView));

        if(ContextCompat.checkSelfPermission(MainActivity.this,
                ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LatLng selfLatLng;
            LatLng moveCameraLatLng;
            if (location != null) {
                selfLatLng = new LatLng(location.getLatitude(), location.getLongitude());

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(selfLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_self_position))
                        .title("")
                        .snippet(getString(R.string.maps_self_mark_snippet)));

                /* Caso conecte primeiro no Location API move a camera para o lugar certo */
                moveCameraLatLng = selfLatLng;

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCameraLatLng,
                        LocationUtils.getDefaultZoom()));
            } else{
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LocationUtils.getDefaultLatLng(),
                        LocationUtils.getDefaultZoom()));
            }
        }


        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mFollowingMarker = marker;
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                marker.showInfoWindow();
                return true;
            }
        });

        /*AuthAsyncTask authAsyncTask = new AuthAsyncTask();
        authAsyncTask.execute();*/

        Intent intent = MainActivity.this.getIntent();

        //Caso o mapa esteja carregado, começa a syncronizar a posição
        if (mMapSync == null) {
            if(intent.hasExtra(EXTRA_CHOOSEN_VAN_ID)) {
                mMapSync = new MapSync(this, this, intent.getLongExtra(EXTRA_CHOOSEN_VAN_ID, -1), this);
            } else{
                mMapSync = new MapSync(this, this, this);

            }
        }

        mMapSync.start();

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mFollowingMarker = null;
            }
        });
    }

    @Override
    public boolean isShowingNoInternetMessage() {
        return false;
    }

    @Override
    public void drawVanPoints(Map<Integer, Van> receivedVanMap) {
        for(Map.Entry<Integer, Van> entry : receivedVanMap.entrySet()) {
            Van receivedVan = entry.getValue();

            Van van = mVansMap.get(receivedVan.getId());

            if(van == null){
                van = receivedVan;
                mVansMap.put(receivedVan.getId(), van);

                //É necessário atualizar o marker
                String titleString = van.getName();
                String direction = van.getDirection();
                String timeToArrive = van.getTimeToArrive();

                String info = "";

                if(direction != null && !direction.isEmpty()) {
                    info += "Direção: " + direction;
                }

                if(timeToArrive != null && !timeToArrive.isEmpty()) {
                    info += "\n Chegada: " + timeToArrive;
                }

                van.setMarker(mGoogleMap.addMarker(new MarkerOptions()
                        .position(van.getLatLng())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus))
                        .title(titleString)
                        .snippet(info)));
            } else{
                van.updateInfo(receivedVan);

                Marker marker = van.getMarker();
                LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();

                MarkerAnimation.animateMarkerToGB(marker, receivedVan.getLatLng(), latLngInterpolator);

                //Atualiza o info window
                if(marker.isInfoWindowShown()){
                    marker.showInfoWindow();
                }

                if(marker.equals(mFollowingMarker)){
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                }
            }
        }
    }

    @Override
    public void drawSingleVan(Van van) {
        if(mSingleVan == null){
            mSingleVan = van;

            //É necessário atualizar o marker
            String titleString = van.getName();
            String direction = van.getDirection();
            String timeToArrive = van.getTimeToArrive();

            String info = "";

            if(direction != null && !direction.isEmpty()) {
                info += "Direção: " + direction;
            }

            if(timeToArrive != null && !timeToArrive.isEmpty()) {
                info += "\n Chegada: " + timeToArrive;
            }

            mSingleVan.setMarker(mGoogleMap.addMarker(new MarkerOptions()
                    .position(mSingleVan.getLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus))
                    .title(titleString)
                    .snippet(info)));
            mGoogleMap.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mSingleVan.getLatitude(),
                            mSingleVan.getLongitude()),
                            LocationUtils.getDefaultZoom()));
        } else{
            mSingleVan.updateInfo(van);

            Marker marker = mSingleVan.getMarker();
            LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Linear();

            MarkerAnimation.animateMarkerToGB(marker, mSingleVan.getLatLng(), latLngInterpolator);

            if(marker.equals(mFollowingMarker)){
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
            }
        }

        //Atualiza o info window
        if(mSingleVan.getMarker().isInfoWindowShown()){
            mSingleVan.getMarker().showInfoWindow();
        }
    }

    @Override
    public void drawFixedMapPoints() {

    }

    @Override
    public void showNoInternetMessage(boolean show) {

    }

    private class AuthAsyncTask extends AsyncTask<Void, Void, AccessToken>{
        @Override
        protected AccessToken doInBackground(Void... params) {
            AccountManagerFuture<Bundle> accountManagerFuture = mAccountManager.getAuthToken(mAccount,
                    "bearer",
                    null,
                    MainActivity.this,
                    null,
                    null);
            Bundle authTokenBundle = null;
            try {
                authTokenBundle = accountManagerFuture.getResult();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }

            if(authTokenBundle != null){
                return new AccessToken(authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString(),
                        "bearer");
            }

            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            super.onPostExecute(accessToken);

            //mAccessToken = accessToken;

            Intent intent = MainActivity.this.getIntent();

            //Caso o mapa esteja carregado, começa a syncronizar a posição
            if(mapReady){
                if(mMapSync != null) {
                    mMapSync.start();
                } else{
                    mMapSync = new MapSync(MainActivity.this, MainActivity.this, MainActivity.this);
                    mMapSync.start();
                }
            }
        }
    }

}
