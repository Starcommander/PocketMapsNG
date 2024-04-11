package com.starcom.pocketmaps.views;

import com.starcom.gdx.ui.ToastMsg;
import com.badlogic.gdx.Gdx;
import com.starcom.gdx.ui.GuiUtil;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class SearchPanel
{
    private static SearchPanel instance = new SearchPanel();
    enum Engines { OpenStreetMaps, GoogleMaps; }
    private TextField txtField;
    private Actor engineDD;
    private Window dialog;
    VerticalGroup vBox;
    boolean visible;
    
    public static SearchPanel getInstance() {
        return SearchPanel.instance;
    }
    
    private SearchPanel() {
        this.txtField = new TextField("", GuiUtil.getDefaultSkin());
        this.engineDD = GuiUtil.genDropDown(o -> this.onEngineDropDown(o.toString()), 0, 0, Engines.OpenStreetMaps.toString(), Engines.GoogleMaps.toString());
        this.dialog = new Window("Search", GuiUtil.getDefaultSkin());
        this.vBox = new VerticalGroup();
        this.visible = false;
        this.dialog.setModal(true);
        this.vBox.addActor(this.engineDD);
        this.vBox.addActor((Actor)this.txtField);
        this.vBox.addActor(GuiUtil.genButton("Search", 0, 0, (a, x, y) -> this.onSearch(this.txtField.getText())));
        this.dialog.add((Actor)this.vBox);
        this.dialog.setSize(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.8f);
        this.dialog.setX(Gdx.graphics.getWidth() * 0.1f);
        this.dialog.setY(Gdx.graphics.getHeight() * 0.1f);
        this.engineDD.setWidth(Gdx.graphics.getWidth() * 0.8f);
        this.txtField.setWidth(Gdx.graphics.getWidth() * 0.8f);
        this.vBox.setWidth(Gdx.graphics.getWidth() * 0.8f);
        setupTxtField();
    }
    
    private void setupTxtField()
    {
    	txtField.getOnscreenKeyboard().show(true);
    }
    
    public void setVisible(final boolean visible) {
        if (this.visible == visible) {
            return;
        }
        if (visible) {
            GuiUtil.getStage().addActor((Actor)this.dialog);
        }
        else {
            this.dialog.remove();
        }
        this.visible = visible;
    }
    
    private void onEngineDropDown(final String engine) {
    }
    
    private void onSearch(final String txt) {
        if (txt.isEmpty() || txt.isBlank()) {
            ToastMsg.getInstance().toastShort("Enter text");
        }
        else {
            this.setVisible(false);
            NavSelect.getInstance().setVisible(true);
        }
    }
}
