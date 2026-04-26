package com.mygdx.game;

import android.os.Bundle;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.backends.android.AndroidApplication;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.android.gl.AndroidGL;
import org.oscim.android.gl.AndroidGL30;
import com.starcom.navigation.gps.StaticClientImpl;

/* Original working launcher */
public class AndroidLauncher extends AndroidApplication {

    public static void initAssets(android.content.Context appContext) {
        GdxAssets.init("assets/");
        int dpi = appContext.getResources().getDisplayMetrics().densityDpi;
        android.util.Log.d("PocketMaps", "DPI: using=" + dpi);
        org.oscim.android.canvas.AndroidGraphics.dpi = dpi;
        org.oscim.android.canvas.AndroidGraphics.init();
        DateTimeAdapter.init(new DateTime());
        StaticClientImpl.setAvailable();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.loadLibrary("vtm-jni");
        
        MyGdxGame gdxGame = new MyGdxGame() {
            @Override
            protected void initGLAdapter(GLVersion version) {
                if (version.getMajorVersion() >= 3)
                    GLAdapter.init(new AndroidGL30());
                else
                    GLAdapter.init(new AndroidGL());
            }
        };
        
        initAssets(this);
        initialize(gdxGame);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
}