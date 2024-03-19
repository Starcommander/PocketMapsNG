package com.mygdx.game;

/** Modified copy of org.oscim.gdx.GdxMapApp, but extends MyGdxGame instead. */

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.oscim.backend.GL;
import org.oscim.core.Point;
import org.oscim.gdx.GdxMapApp;
import org.oscim.renderer.GLState;
import org.oscim.renderer.MapRenderer;
import org.oscim.tiling.TileSource;
//import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.layers.tile.vector.VectorTileLayer;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import org.oscim.awt.AwtGraphics;
import org.oscim.backend.DateTime;
import org.oscim.backend.DateTimeAdapter;
import org.oscim.gdx.GdxAssets;
import org.oscim.backend.GLAdapter;
import com.badlogic.gdx.graphics.glutils.GLVersion;
import org.oscim.gdx.Lwjgl3GL20;
import org.oscim.gdx.Lwjgl3GL30;

import org.oscim.theme.VtmThemes;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.core.GeoPoint;

import static org.oscim.backend.GLAdapter.gl;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher extends MyGdxGame {

    public static void init() {
        // load native library

        SharedLibraryLoader loader = new SharedLibraryLoader(null);
        loader.load("vtm-jni");

        AwtGraphics.init();
        GdxAssets.init("assets/");
        DateTimeAdapter.init(new DateTime());
    }

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
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 8, /*2*/0);
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
        System.exit(0); //TODO: Find a better way.
    }

    public static void main(String[] args) {
        DesktopLauncher.init();
//        GdxMapApp.run(new DesktopLauncher());
new Lwjgl3Application(new DesktopLauncher(), getConfig());
    }
}
