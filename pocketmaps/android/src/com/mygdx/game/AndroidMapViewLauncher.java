package com.mygdx.game;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.gdx.GdxMap;
import org.oscim.android.MapView;
import org.oscim.android.gl.AndroidGL;
import org.oscim.android.gl.AndroidGL30;
import com.starcom.navigation.gps.StaticClientImpl;

/**
 * Hybrid: Android MapView (vtm-android) + libGDX UI overlay.
 * 
 * Architecture:
 * - MapView: renders map via native Android OpenGL (vtm-android)  
 * - libGDX via initializeForView(): UI layer overlay on top
 */
public class AndroidMapViewLauncher extends AndroidApplication {

    private MapView mapView;
    private View libGdxView;
    private GdxMap gdxMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout to stack views: MapView (bottom) + libGDX (top)
        FrameLayout root = new FrameLayout(this);
        root.setLayoutParams(new android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        // 1. Android MapView - uses vtm-android native rendering
        // This fixes tile gap issue by using Android GL directly
        mapView = new MapView(this);
        root.addView(mapView, new FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        // 2. libGDX for UI overlay
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = true;

        // Create GdxMap but only for UI, not map rendering
        gdxMap = createGdxMap();

        libGdxView = initializeForView(gdxMap, config);
        root.addView(libGdxView, new FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);

        // Now initialize VTM - must be done AFTER setContentView
        initializeVtmAssets();
    }

    private GdxMap createGdxMap() {
        return new GdxMap() {
            @Override
            protected void initGLAdapter(GLVersion version) {
                if (version.getMajorVersion() >= 3) {
                    GLAdapter.init(new AndroidGL30());
                } else {
                    GLAdapter.init(new AndroidGL());
                }
            }
        };
    }

    private void initializeVtmAssets() {
        GdxAssets.init("assets/");
        int dpi = getResources().getDisplayMetrics().densityDpi;
        org.oscim.android.canvas.AndroidGraphics.dpi = dpi;
        org.oscim.android.canvas.AndroidGraphics.init();
        DateTimeAdapter.init(new DateTime());
        StaticClientImpl.setAvailable();
    }

    public MapView getMapView() {
        return mapView;
    }

    public org.oscim.map.Map getMap() {
        return mapView.map();
    }

    public Stage getGuiStage() {
        return null; // GUI stage accessed via gdxMap directly
    }
}