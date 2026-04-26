package com.mygdx.game;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.android.gl.AndroidGL;
import org.oscim.android.gl.AndroidGL30;
import com.starcom.navigation.gps.StaticClientImpl;

public class UILibGdxLauncher extends AndroidApplication {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        initVtmAssets();
        
        MyGdxGame gdxGame = new MyGdxGame() {
            @Override
            protected void initGLAdapter(GLVersion version) {
                if (version.getMajorVersion() >= 3)
                    GLAdapter.init(new AndroidGL30());
                else
                    GLAdapter.init(new AndroidGL());
            }
        };
        
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useGL30 = false;
        
        initialize(gdxGame, config);
    }
    
    private void initVtmAssets() {
        GdxAssets.init("assets/");
        int dpi = getResources().getDisplayMetrics().densityDpi;
        android.util.Log.d("UILibGdxLauncher", "DPI: " + dpi);
        org.oscim.android.canvas.AndroidGraphics.dpi = dpi;
        org.oscim.android.canvas.AndroidGraphics.init();
        DateTimeAdapter.init(new DateTime());
        StaticClientImpl.setAvailable();
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