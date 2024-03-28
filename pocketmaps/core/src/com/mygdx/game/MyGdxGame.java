package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.starcom.gdx.io.PolyParser;
import com.starcom.gdx.io.Storage;
import com.starcom.gdx.system.Threading;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.Util;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.views.TopPanel;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.InputMultiplexer;
import org.oscim.backend.GL;
import org.oscim.core.Point;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.oscim.tiling.TileSource;
//import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.layers.tile.vector.VectorTileLayer;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import org.oscim.backend.DateTime;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.gdx.GdxAssets;

import org.oscim.theme.VtmThemes;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.core.GeoPoint;
import org.oscim.gdx.GdxMap;
import static org.oscim.backend.GLAdapter.gl;

import java.io.File;

public abstract class MyGdxGame extends GdxMap {

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch spriteBatch;
private Stage guiStage;

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



Threading.getInstance().init();
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
TopPanel.getInstance().show(guiStage, getMap());
MapList.getInstance().loadSettings(mMap);
//dialog.show(guiStage);
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
        mMapRenderer.onSurfaceChanged(w, h);
        mMap.viewport().setViewSize(w, h);
        viewport.update(w, h);
    }
}
