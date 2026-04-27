package com.mygdx.game;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import org.oscim.android.MapView;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.gdx.GdxAssets;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import com.starcom.navigation.gps.StaticClientImpl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HybridLauncher extends Activity {

    private static final String PREFS_NAME = "PocketMapsPrefs";
    private static final String KEY_LAST_MAP = "lastMapPath";
    
    private MapView mapView;
    private File filesDir;
    private List<String> availableMaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        filesDir = getFilesDir();
        
        initLibGdxFilesystem();
        initVtmAssets();
        
        FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        
        mapView = new MapView(this);
        mapView.setClickable(true);
        layout.addView(mapView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        
        addMapControls(layout);
        
        setContentView(layout);
        
        findAvailableMaps();
        loadSavedMapOrPrompt();
    }
    
    private void initLibGdxFilesystem() {
        try {
            File localDir = filesDir;
            Files files = new Files() {
                private String externalPath = localDir.getAbsolutePath();
                
                @Override
                public FileHandle getFileHandle(String path, FileType type) {
                    switch (type) {
                        case Internal:
                            return new AndroidFileHandle(getAssets(), "assets/" + path, localDir);
                        case External:
                            return new AndroidFileHandle(new File(externalPath, path), localDir);
                        case Local:
                            return new AndroidFileHandle(new File(localDir, path), localDir);
                        case Absolute:
                            return new AndroidFileHandle(new File(path), localDir);
                        default:
                            return new AndroidFileHandle(new File(path), localDir);
                    }
                }

                @Override
                public FileHandle external(String path) {
                    return new AndroidFileHandle(new File(externalPath, path), localDir);
                }

                @Override
                public FileHandle internal(String path) {
                    return new AndroidFileHandle(getAssets(), "assets/" + path, localDir);
                }

                @Override
                public FileHandle classpath(String path) {
                    return new AndroidFileHandle(new File(path), localDir);
                }

                @Override
                public FileHandle absolute(String path) {
                    return new AndroidFileHandle(new File(path), localDir);
                }

                @Override
                public FileHandle local(String path) {
                    return new AndroidFileHandle(new File(localDir, path), localDir);
                }

                @Override
                public String getExternalStoragePath() {
                    return externalPath;
                }

                @Override
                public String getLocalStoragePath() {
                    return localDir.getAbsolutePath();
                }

                @Override
                public boolean isExternalStorageAvailable() {
                    return false;
                }

                @Override
                public boolean isLocalStorageAvailable() {
                    return true;
                }
            };
            com.badlogic.gdx.Gdx.files = files;
            android.util.Log.d("HybridLauncher", "Custom Gdx.files initialized");
        } catch (Exception e) {
            android.util.Log.e("HybridLauncher", "Failed to init Gdx.files: " + e.getMessage());
        }
    }
    
    private static class AndroidFileHandle extends FileHandle {
        private final File file;
        private final boolean isAsset;
        private final android.content.res.AssetManager assets;
        private final File localDir;
        
        public AndroidFileHandle(File file, File localDir) {
            this.file = file;
            this.isAsset = false;
            this.assets = null;
            this.localDir = localDir;
        }
        
        public AndroidFileHandle(android.content.res.AssetManager assets, String path, File localDir) {
            this.file = new File(path);
            this.isAsset = true;
            this.assets = assets;
            this.localDir = localDir;
        }
        
        @Override
        public File file() {
            return isAsset ? new File(localDir, path()) : file;
        }
        
        @Override
        public String path() {
            return isAsset ? file.getPath() : file.getAbsolutePath();
        }
        
        @Override
        public boolean exists() {
            if (isAsset) {
                try {
                    return assets.open(file.getPath()) != null;
                } catch (Exception e) {
                    return false;
                }
            }
            return file.exists();
        }
        
        @Override
        public InputStream read() {
            try {
                if (isAsset) {
                    return assets.open(file.getPath());
                }
                return new java.io.FileInputStream(file);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void initVtmAssets() {
        android.util.Log.d("HybridLauncher", "Initializing GdxAssets...");
        GdxAssets.init("assets/");
        int dpi = getResources().getDisplayMetrics().densityDpi;
        android.util.Log.d("HybridLauncher", "Setting DPI: " + dpi);
        AndroidGraphics.dpi = dpi;
        AndroidGraphics.init();
        DateTimeAdapter.init(new DateTime());
        StaticClientImpl.setAvailable();
        android.util.Log.d("HybridLauncher", "VTM initialized, DPI: " + dpi);
    }
    
    private void findAvailableMaps() {
        availableMaps.clear();
        android.util.Log.d("HybridLauncher", "=== findAvailableMaps start ===");
        android.util.Log.d("HybridLauncher", "filesDir: " + filesDir.getAbsolutePath());
        
        String[] searchPaths = {
            filesDir.getAbsolutePath() + "/maps/",
            "/storage/emulated/0/Android/data/com.starcom.pocketmapsng/files/maps/",
            "/sdcard/Download/pocketmaps/maps/",
            "/storage/emulated/0/Download/pocketmaps/maps/"
        };
        
        for (String basePath : searchPaths) {
            if (basePath == null || basePath.isEmpty()) continue;
            android.util.Log.d("HybridLauncher", "Scanning path: " + basePath);
            File mapsDir = new File(basePath);
            String dirExists = mapsDir.exists() ? "exists" : "NOT exists";
            String isDir = mapsDir.isDirectory() ? "is dir" : "NOT dir";
            android.util.Log.d("HybridLauncher", basePath + " -> " + dirExists + ", " + isDir);
            
            if (mapsDir.exists() && mapsDir.isDirectory()) {
                File[] continents = mapsDir.listFiles();
                if (continents != null) {
                    android.util.Log.d("HybridLauncher", "Found " + continents.length + " items in " + basePath);
                    for (File continent : continents) {
                        if (continent.isDirectory()) {
                            String mapFile = continent.getAbsolutePath() + "/" + continent.getName() + ".map";
                            File f = new File(mapFile);
                            if (f.exists()) {
                                availableMaps.add(mapFile);
                                android.util.Log.d("HybridLauncher", "FOUND MAP: " + mapFile);
                            } else {
                                android.util.Log.d("HybridLauncher", "No .map file in: " + continent.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
        
        android.util.Log.d("HybridLauncher", "Total maps found: " + availableMaps.size());
        android.util.Log.d("HybridLauncher", "=== findAvailableMaps end ===");
    }
    
    private void loadSavedMapOrPrompt() {
        String savedMap = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(KEY_LAST_MAP, null);
        
        if (savedMap != null && new File(savedMap).exists()) {
            android.util.Log.d("HybridLauncher", "Loading saved map: " + savedMap);
            loadMap(savedMap);
        } else if (!availableMaps.isEmpty()) {
            showMapSelectionDialog();
        } else {
            android.util.Log.w("HybridLauncher", "No maps found");
            Toast.makeText(this, "No maps found! Please download a map via adb.", Toast.LENGTH_LONG).show();
        }
    }
    
    private void showMapSelectionDialog() {
        if (availableMaps.isEmpty()) {
            Toast.makeText(this, "No maps available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] mapNames = new String[availableMaps.size()];
        for (int i = 0; i < availableMaps.size(); i++) {
            File f = new File(availableMaps.get(i));
            mapNames[i] = f.getParentFile().getName() + " (" + f.getName() + ")";
        }
        
        new AlertDialog.Builder(this)
            .setTitle("Select Map")
            .setItems(mapNames, (dialog, which) -> {
                String selectedMap = availableMaps.get(which);
                saveSelectedMap(selectedMap);
                loadMap(selectedMap);
            })
            .setCancelable(false)
            .show();
    }
    
    private void saveSelectedMap(String mapPath) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_MAP, mapPath)
            .apply();
    }
    
    private void loadMap(String mapPath) {
        android.util.Log.d("HybridLauncher", "=== loadMap start ===");
        android.util.Log.d("HybridLauncher", "Map path: " + mapPath);
        android.util.Log.d("HybridLauncher", "File exists: " + new File(mapPath).exists());
        try {
            android.util.Log.d("HybridLauncher", "Creating MapFileTileSource...");
            MapFileTileSource source = new MapFileTileSource();
            source.setMapFile(mapPath);
            android.util.Log.d("HybridLauncher", "Setting base map...");
            mapView.map().setBaseMap(source);
            android.util.Log.d("HybridLauncher", "Setting theme...");
            mapView.map().setTheme(VtmThemes.DEFAULT);
            
            mapView.map().setMapPosition(48.2, 16.4, 1 << 10);
            
            android.util.Log.d("HybridLauncher", "Map setup complete - center: 48.2, 16.4");
            android.util.Log.d("HybridLauncher", "=== loadMap end ===");
        } catch (Exception e) {
            android.util.Log.e("HybridLauncher", "Error: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Error loading map: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    private void addMapControls(FrameLayout parent) {
        int btnSize = (int)(56 * getResources().getDisplayMetrics().density);
        int margin = (int)(16 * getResources().getDisplayMetrics().density);
        
        ImageButton zoomIn = createZoomButton(true);
        ImageButton zoomOut = createZoomButton(false);
        ImageButton myLocation = createLocationButton();
        
        LinearLayout zoomLayout = new LinearLayout(this);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setLayoutParams(new FrameLayout.LayoutParams(btnSize, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        zoomLayout.setPadding(margin, margin, margin, margin);
        
        FrameLayout.LayoutParams zoomInParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        zoomInParams.setMargins(0, 0, 0, margin / 2);
        zoomLayout.addView(zoomIn, zoomInParams);
        
        FrameLayout.LayoutParams zoomOutParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        zoomOutParams.setMargins(0, margin / 2, 0, 0);
        zoomLayout.addView(zoomOut, zoomOutParams);
        
        FrameLayout.LayoutParams locParams = new FrameLayout.LayoutParams(btnSize, btnSize);
        locParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        locParams.setMargins(margin, margin, margin, margin + btnSize * 2 + margin);
        
        parent.addView(zoomLayout);
        parent.addView(myLocation, locParams);
    }
    
    private ImageButton createZoomButton(boolean zoomIn) {
        ImageButton btn = new ImageButton(this);
        btn.setBackgroundResource(android.R.drawable.btn_default);
        btn.setImageResource(zoomIn ? android.R.drawable.ic_menu_add : android.R.drawable.ic_menu_close_clear_cancel);
        btn.setScaleType(android.widget.ImageView.ScaleType.CENTER);
        btn.setOnClickListener(v -> {
            if (mapView != null) {
                MapPosition pos = mapView.map().getMapPosition();
                double currentScale = pos.getScale();
                double newScale = zoomIn ? currentScale * 2 : currentScale / 2;
                pos.setScale(newScale);
                mapView.map().setMapPosition(pos);
            }
        });
        return btn;
    }
    
    private ImageButton createLocationButton() {
        ImageButton btn = new ImageButton(this);
        btn.setBackgroundResource(android.R.drawable.btn_default);
        btn.setImageResource(android.R.drawable.ic_menu_mylocation);
        btn.setScaleType(android.widget.ImageView.ScaleType.CENTER);
        btn.setOnClickListener(v -> requestLocation());
        return btn;
    }
    
    private void requestLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location bestLocation = null;
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                bestLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            if (bestLocation == null && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                bestLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            
            if (bestLocation != null) {
                MapPosition pos = mapView.map().getMapPosition();
                mapView.map().setMapPosition(bestLocation.getLatitude(), bestLocation.getLongitude(), pos.getScale());
            } else {
                lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        MapPosition pos = mapView.map().getMapPosition();
                        mapView.map().setMapPosition(location.getLatitude(), location.getLongitude(), pos.getScale());
                        lm.removeUpdates(this);
                    }
                    @Override public void onStatusChanged(String p, int s, Bundle b) {}
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onProviderDisabled(String p) {}
                }, null);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}