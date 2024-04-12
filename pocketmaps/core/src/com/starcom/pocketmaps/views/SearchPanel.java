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
    enum Engines { OpenStreetMaps, GoogleMaps, Offline }
    private TextField txtField;
    private Actor engineDD;
    private Window dialog;
    boolean visible;
    
    public static SearchPanel getInstance() {
        return SearchPanel.instance;
    }
    
    private SearchPanel() {
    	float ww = Gdx.graphics.getWidth();
        txtField = new TextField("", GuiUtil.getDefaultSkin());
        engineDD = GuiUtil.genDropDown(o -> this.onEngineDropDown(o.toString()), 0, 0,
        		Engines.OpenStreetMaps.toString(),
        		Engines.GoogleMaps.toString(),
        		Engines.Offline.toString());
        dialog = new Window("Search", GuiUtil.getDefaultSkin());
        dialog.setModal(true);
        dialog.add(engineDD).colspan(2);
        dialog.row();
        dialog.add(txtField).width(ww * 0.8f).expand().colspan(2);
        dialog.row();
        dialog.add(GuiUtil.genButton("Break", 0, 0, (a, x, y) -> this.onSearch(null))).width(ww/3);//.left();//.width(ww/4.0f).left();
        dialog.add(GuiUtil.genButton("Search", 0, 0, (a, x, y) -> this.onSearch(this.txtField.getText()))).width(ww/3);
        dialog.setSize(ww * 0.8f, Gdx.graphics.getHeight() * 0.8f);
        dialog.setX(ww * 0.1f);
        dialog.setY(Gdx.graphics.getHeight() * 0.1f);
        
    }
    
    private void setupTxtField()
    { //TODO Clear this, if not doing anything
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
    	System.out.println("Select engine: " + engine);
    }
    
    private void onSearch(final String txt) {
    	if (txt == null)
    	{
            this.setVisible(false);
            NavSelect.getInstance().setVisible(true);
    	}
    	else if (txt.isEmpty() || txt.isBlank()) {
            ToastMsg.getInstance().toastShort("Enter text");
        }
        else {
            System.out.println("Searching for " + txt);
            this.setVisible(false);
            NavSelect.getInstance().setVisible(true);
        }
    }
}
