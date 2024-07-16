package com.mygdx.game;

import android.app.Activity;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.content.Context;

import com.badlogic.gdx.graphics.glutils.GLVersion;
import com.badlogic.gdx.backends.android.AndroidApplication;

import org.oscim.backend.DateTimeAdapter;
import org.oscim.backend.DateTime;
import org.oscim.backend.GLAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.gdx.AndroidGL;
import org.oscim.gdx.AndroidGL30;
import org.oscim.android.MapView;

/* Hints from gdx/AndroidApplication as used for LWJGL in DesktopLauncher. */
public class AndroidLauncher extends AndroidApplication {
//public class AndroidLauncher extends Activity {

	public static void initAssets() {
		GdxAssets.init("assets/");
org.oscim.android.canvas.AndroidGraphics.dpi = 320;
org.oscim.android.canvas.AndroidGraphics.init();
		DateTimeAdapter.init(new DateTime());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
System.loadLibrary("vtm-jni");
		MyGdxGame gdxGame = new MyGdxGame()
		{
			@Override
			protected void initGLAdapter(GLVersion version) {
				if (version.getMajorVersion() >= 3)
					GLAdapter.init(new AndroidGL30());
				else
					GLAdapter.init(new AndroidGL());
				}
		};
		initAssets();
		requestPermissions();
		initialize(gdxGame);
//        MapView mapView = new MapView(this);
//        setContentView(mapView);

//		initialize(gdxGame, androidConfig);

		// Map view
//		mapView = new MapView(this);
//		setContentView(mapView);
//		new MyGdxGame();
	}
	
	private void requestPermissions()
	{
		String fine_loc = android.Manifest.permission.ACCESS_FINE_LOCATION;
		if (!(checkSelfPermission(fine_loc) == PackageManager.PERMISSION_GRANTED))
		{
			requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, getId());
		}
		else
		{
			LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			
			locationManager.getCurrentLocation(
				    LocationManager.GPS_PROVIDER,
				    null,
				    application.getMainExecutor(),
				    new Consumer<Location>() {
				  @Override
				  public void accept(Location location) {
				    // code
				  }
				});
			
			
			
			
		      try
		      {
		        if (Variable.getVariable().isSmoothON()) {
		          locationManager.removeUpdates(this);
		          kalmanLocationManager.requestLocationUpdates(UseProvider.GPS, FILTER_TIME, GPS_TIME, NET_TIME, this, false);
		          lastProvider = KalmanLocationManager.KALMAN_PROVIDER;
		          logUser("LocationProvider: " + lastProvider);
		        } else {
		          kalmanLocationManager.removeUpdates(this);
		          Criteria criteria = new Criteria();
		          criteria.setAccuracy(Criteria.ACCURACY_FINE);
		          String provider = locationManager.getBestProvider(criteria, true);
		          if (provider == null) {
		            lastProvider = null;
		            locationManager.removeUpdates(this);
		            logUser("LocationProvider is off!");
		            return;
		          } else if (provider.equals(lastProvider)) {
		            if (showMsgEverytime) {
		              logUser("LocationProvider: " + provider);
		            }
		            return;
		          }
		          locationManager.removeUpdates(this);
		          lastProvider = provider;
		          locationManager.requestLocationUpdates(provider, 3000, 5, this);
		          logUser("LocationProvider: " + provider);
		        }
		        locationListenerStatus = PermissionStatus.Enabled;
		      }
		      catch (SecurityException ex)
		      {
		        logUser("Location_Service not allowed by user!");
		      }
			
			
		}
	}
	
    private static int getId()
    {
      idCounter ++;
      return idCounter;
    }

/*
    protected static Lwjgl3ApplicationConfiguration getConfig() {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.disableAudio(true);
cfg.setTitle("PocketMaps");
com.starcom.gdx.system.App.setAppName("PocketMaps", false);
//        int[] sizes = new int[]{128, 64, 32, 16};
//        String[] paths = new String[sizes.length];
//        for (int i = 0; i < paths.length; i++)
//            paths[i] = "res/ic_vtm_" + sizes[i] + ".png";
//        cfg.setWindowIcon(Files.FileType.Internal, paths);

        cfg.setWindowedMode(1024, 768);
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        //cfg.useOpenGL3(true, 3, 2);
        cfg.setIdleFPS(10);
        //cfg.forceExit = false;
        return cfg;
    }

    @Override
    protected void initGLAdapter(GLVersion version) {
        if (version.getMajorVersion() >= 3)
            GLAdapter.init(new Lwjgl3GL30());
        else
            GLAdapter.init(new Lwjgl3GL20());
    }

    @Override
    public void dispose() {
    	super.dispose();
    }

    public static void main(String[] args) {
        DesktopLauncher.init();
//        GdxMapApp.run(new DesktopLauncher());
new Lwjgl3Application(new DesktopLauncher(), getConfig());
    }
*/
}
