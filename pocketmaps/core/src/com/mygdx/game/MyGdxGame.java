package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.starcom.LoggerUtil;
import com.starcom.gdx.io.Storage;
import com.starcom.system.Threading;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.navigator.NaviDebugSimulator;
import com.starcom.pocketmaps.navigator.NaviEngine;
import com.starcom.pocketmaps.util.PolyParser;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.views.SettingsView;
import com.starcom.pocketmaps.views.TopPanel;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.InputMultiplexer;
import org.oscim.backend.GL;
import org.oscim.event.Event;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.oscim.map.Map.UpdateListener;
import org.oscim.core.MapPosition;
import org.oscim.gdx.GdxMap;
import static org.oscim.backend.GLAdapter.gl;

import java.util.logging.Logger;

public abstract class MyGdxGame extends GdxMap {

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch spriteBatch;
    private Stage guiStage;
    private Logger logger = LoggerUtil.get(MyGdxGame.class);
    private int oldW,oldH;

//    private Texture texture;

    @Override
    public void createLayers() {
    	Storage.getFileHandle("config").mkdirs();
    	Cfg.setDirectory(Storage.getFileHandle("config").file());
//String mapFile = "/home/paul/workspace_gdx/pocketmaps/europe_andorra/europe_andorra.map";
//MapList.getInstance().loadMap(mapFile, getMap());

//mapFile = "/home/paul/workspace_gdx/pocketmaps/europe_austria/europe_austria.map";
//String aPath = "/home/paul/workspace_gdx/pocketmaps/europe_austria/";
//GeoPoint mapCenter = MapList.getInstance().loadMap(mapFile, getMap());

//MapHandler.getInstance().createPathfinder(new File(aPath), (o) -> MapHandler.getInstance().setCurrentPathfinder(o));

//PolyParser.doIt(mMap, "europe/spain.poly");
//PolyParser.doIt(mMap, "europe/austria.poly");
PolyParser.doItAll(mMap);
//    getMap().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), scale);

        camera = new OrthographicCamera();
        viewport = new ScreenViewport(camera);
        spriteBatch = new SpriteBatch();

        mMapRenderer = new MapRenderer(mMap);
        mMapRenderer.onSurfaceCreated();
        mMapRenderer.onSurfaceChanged(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mMap.viewport().setViewSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Generate a simple texture for testing
//        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGB565);
//        pixmap.setColor(0, 0, 0, 1);
//        pixmap.fillRectangle(0, 0, 64, 64);
//        pixmap.setColor(1, 0, 1, 1);
//        pixmap.fillRectangle(0, 0, 32, 32);
//        pixmap.fillRectangle(32, 32, 32, 32);
////        texture = new Texture(pixmap);
//texture = new Texture("badlogic.jpg");



Threading.getInstance().init(Gdx.app::postRunnable);
//Skin uiSkin = Util.getDefaultSkin();
//Dialog dialog = new Dialog("Warning", uiSkin, "dialog") {
//    public void result(Object obj) {
//        System.out.println("result "+obj);
//    }
//};
guiStage = new Stage(viewport, spriteBatch);

InputMultiplexer inputMultiplexer = new InputMultiplexer();
inputMultiplexer.addProcessor(guiStage);
inputMultiplexer.addProcessor(Gdx.input.getInputProcessor());
Gdx.input.setInputProcessor(inputMultiplexer);

//Gdx.input.setInputProcessor(guiStage);

//	Table table = new Table();
//	table.setFillParent(true);
//	guiStage.addActor(table);
	
//dialog.text("Are you sure you want to yada yada?");
//dialog.button("Yes", true); //sends "true" as the result
//dialog.button("No", false); //sends "false" as the result

//ListSelect listSel = new ListSelect("MyListTitle");
//listSel.addTest();
//for (int i=0; i<100; i++)
//{
//	final int ii = i;
//	listSel.addElement("Wahl" + i, (x,y) -> System.out.println("Pressed item " + ii + " on x=" + x + " y=" + y));
//}
////listSel.showAsWindow(guiStage);
GuiUtil.setStage(guiStage);
getMap().events.bind(createUpdateListener());
TopPanel.getInstance().init(getMap());
TopPanel.getInstance().setVisible(true);
MapList.getInstance().loadSettings();
//dialog.show(guiStage);
SettingsView.getInstance(); //Ensure init for possible debugButton.

    }

    @Override
    public void render() {
        // Centre the camera
        camera.position.x = Gdx.graphics.getWidth() / 2;
        camera.position.y = Gdx.graphics.getHeight() / 2;
        camera.update();

        // Code taken from Cachebox
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ?
                GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        GLState.enableVertexArrays(GLState.DISABLED, GLState.DISABLED);

        GLState.viewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        gl.frontFace(GL.CW);

        mMapRenderer.onDrawFrame();

        gl.flush();
        GLState.bindVertexBuffer(GLState.UNBIND);
        GLState.bindElementBuffer(GLState.UNBIND);
        gl.frontFace(GL.CCW);

//        spriteBatch.setProjectionMatrix(camera.combined);
//        spriteBatch.begin();
//        Point point = new Point();
//        mMap.viewport().toScreenPoint(mMap.getMapPosition().getGeoPoint(), false, point);
//        Vector2 worldPos = viewport.unproject(new Vector2((int) point.x, (int) point.y));
//        spriteBatch.draw(texture, worldPos.x - 64, worldPos.y - 64, 128, 128);
//        spriteBatch.end();
GLState.enableVertexArrays(-1, -1);
    gl.disable(GL.DEPTH_TEST);
    gl.disable(GL.STENCIL_TEST);
guiStage.act(Gdx.graphics.getDeltaTime());
guiStage.draw();
ToastMsg.getInstance().render();
    }
    
    @Override
    public void resize(int w, int h) {
    	if (w==oldW && h == oldH) { return; }
        mMapRenderer.onSurfaceChanged(w, h);
        mMap.viewport().setViewSize(w, h);
        viewport.update(w, h);
        for (Actor a : GuiUtil.getStage().getActors())
        {
        	if (a instanceof Layout)
        	{
        		((Layout)a).invalidate();
        	}
        }
        logger.info("Resized to: " + w + "/" + h);
        oldW = w;
        oldH = h;
    }
    
    private UpdateListener createUpdateListener()
    {
        UpdateListener d = new UpdateListener(){
            @Override
            public void onMapEvent(Event e, MapPosition mapPosition)
            {
                if (e == org.oscim.map.Map.MOVE_EVENT && NaviEngine.getNaviEngine().isNavigating())
                {
                    NaviEngine.getNaviEngine().setMapUpdatesAllowed(false);
                }
            }
        };
        return d;
    }
    
    @Override
    public void dispose()
    {
    	NaviDebugSimulator.getSimu().stopDebugSimulator();
    	NaviEngine.dispose();
    	MapList.getInstance().unloadMaps();
    }
}
