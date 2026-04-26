package com.mygdx.game;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.badlogic.gdx.backends.android.DefaultAndroidFiles;

import org.oscim.android.MapView;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.gdx.GdxAssets;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import com.starcom.navigation.gps.StaticClientImpl;

import java.io.File;

public class HybridLauncher extends Activity {

    private MapView mapView;
    private AssetManager assetManager;
    private File externalFilesDir;
    private File filesDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        assetManager = getAssets();
        externalFilesDir = getExternalFilesDir(null);
        filesDir = getFilesDir();
        
        initLibGdxFilesystem();
        initVtmAssets();
        
        FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        
        mapView = new MapView(this);
        layout.addView(mapView, new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        
        setContentView(layout);
        
        loadMapFromStorage();
    }
    
    private void initLibGdxFilesystem() {
        DefaultAndroidFiles androidFiles = new DefaultAndroidFiles(getAssets(), this, true);
        com.badlogic.gdx.Gdx.files = androidFiles;
    }
    
    private void initVtmAssets() {
        GdxAssets.init("assets/");
        int dpi = getResources().getDisplayMetrics().densityDpi;
        AndroidGraphics.dpi = dpi;
        AndroidGraphics.init();
        DateTimeAdapter.init(new DateTime());
        StaticClientImpl.setAvailable();
    }
    
    private void loadMapFromStorage() {
        String[] searchPaths = {
            "/storage/emulated/0/Android/data/com.starcom.pocketmapsng/files/maps/",
            externalFilesDir != null ? externalFilesDir.getAbsolutePath() + "/maps/" : "",
            filesDir.getAbsolutePath() + "/maps/"
        };
        
        for (String basePath : searchPaths) {
            if (basePath == null || basePath.isEmpty()) continue;
            java.io.File mapsDir = new java.io.File(basePath);
            if (mapsDir.exists() && mapsDir.isDirectory()) {
                java.io.File[] continents = mapsDir.listFiles();
                if (continents != null) {
                    for (java.io.File continent : continents) {
                        if (continent.isDirectory()) {
                            String mapFile = continent.getAbsolutePath() + "/" + continent.getName() + ".map";
                            java.io.File f = new java.io.File(mapFile);
                            if (f.exists()) {
                                loadMap(mapFile);
                                return;
                            }
                        }
                    }
                }
            }
        }
        android.util.Log.w("HybridLauncher", "No map found");
    }
    
    private void loadMap(String mapPath) {
        try {
            MapFileTileSource source = new MapFileTileSource();
            source.setMapFile(mapPath);
            mapView.map().setBaseMap(source);
            mapView.map().setTheme(VtmThemes.DEFAULT);
            android.util.Log.d("HybridLauncher", "Loaded map: " + mapPath);
        } catch (Exception e) {
            android.util.Log.e("HybridLauncher", "Error: " + e.getMessage());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}