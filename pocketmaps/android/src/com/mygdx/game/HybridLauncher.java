package com.mygdx.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;

import org.oscim.android.MapView;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.gdx.GdxAssets;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import com.starcom.navigation.gps.StaticClientImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT));
        
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
        // TODO: Cleanup fallback path after better map management is implemented
        // Fallback to /sdcard/Download if internal storage maps not found
        String[] searchPaths = {
            filesDir.getAbsolutePath() + "/maps/",
            "/storage/emulated/0/Android/data/com.starcom.pocketmapsng/files/maps/",
            "/sdcard/Download/pocketmaps/maps/",
            "/storage/emulated/0/Download/pocketmaps/maps/"
        };
        
        for (String basePath : searchPaths) {
            if (basePath == null || basePath.isEmpty()) continue;
            android.util.Log.d("HybridLauncher", "Scanning: " + basePath);
            File mapsDir = new File(basePath);
            if (mapsDir.exists() && mapsDir.isDirectory()) {
                File[] continents = mapsDir.listFiles();
                if (continents != null) {
                    for (File continent : continents) {
                        if (continent.isDirectory()) {
                            String mapFile = continent.getAbsolutePath() + "/" + continent.getName() + ".map";
                            File f = new File(mapFile);
                            if (f.exists()) {
                                availableMaps.add(mapFile);
                                android.util.Log.d("HybridLauncher", "Found map: " + mapFile);
                            }
                        }
                    }
                }
            }
        }
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
            Toast.makeText(this, "No maps found! Downloading europe_austria...", Toast.LENGTH_SHORT).show();
            downloadDefaultMap();
        }
    }
    
    private void downloadDefaultMap() {
        String mapName = "europe_austria";
        String downloadUrl = "http://vsrv15044.customer.xenway.de/maps/maps/20240623/" + mapName + ".ghz";
        // TODO: Cleanup fallback path after better map management is implemented
        String mapsDir = "/sdcard/Download/pocketmaps/maps/";
        
        ProgressDialog progress = ProgressDialog.show(this, "Downloading", "Downloading " + mapName + "...", true);
        
        new Thread(() -> {
            try {
                android.util.Log.d("HybridLauncher", "Downloading from: " + downloadUrl);
                
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                
                File mapDir = new File(mapsDir + mapName);
                mapDir.mkdirs();
                
                File zipFile = new File(mapDir, mapName + ".ghz");
                FileOutputStream fos = new FileOutputStream(zipFile);
                InputStream is = conn.getInputStream();
                
                byte[] buffer = new byte[4096];
                int len;
                long total = 0;
                long size = conn.getContentLength();
                
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    total += len;
                    final long prog = total;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (progress != null) {
                            progress.setMessage("Downloading " + mapName + "... " + (int)(prog * 100 / size) + "%");
                        }
                    });
                }
                
                fos.close();
                is.close();
                conn.disconnect();
                
                android.util.Log.d("HybridLauncher", "Download complete, unzipping...");
                
                unzipMap(zipFile, mapDir);
                zipFile.delete();
                
                final String mapPath = mapDir.getAbsolutePath() + "/" + mapName + ".map";
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (progress != null) progress.dismiss();
                    saveSelectedMap(mapPath);
                    loadMap(mapPath);
                    Toast.makeText(HybridLauncher.this, "Map downloaded successfully!", Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                android.util.Log.e("HybridLauncher", "Download failed: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (progress != null) progress.dismiss();
                    Toast.makeText(HybridLauncher.this, "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    private void unzipMap(File zipFile, File destDir) throws Exception {
        destDir.mkdirs();
        ZipInputStream zis = new ZipInputStream(new java.io.FileInputStream(zipFile));
        byte[] buffer = new byte[4096];
        ZipEntry entry;
        
        while ((entry = zis.getNextEntry()) != null) {
            File newFile = new File(destDir, entry.getName());
            
            if (entry.isDirectory()) {
                newFile.mkdirs();
            } else {
                newFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
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
        try {
            android.util.Log.d("HybridLauncher", "Setting up tile source...");
            MapFileTileSource source = new MapFileTileSource();
            source.setMapFile(mapPath);
            android.util.Log.d("HybridLauncher", "Setting base map...");
            mapView.map().setBaseMap(source);
            android.util.Log.d("HybridLauncher", "Setting theme...");
            mapView.map().setTheme(VtmThemes.DEFAULT);
            
            mapView.map().setMapPosition(48.2, 16.4, 1 << 10);
            
            android.util.Log.d("HybridLauncher", "Map setup complete - center: 48.2, 16.4");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}