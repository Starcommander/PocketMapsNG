package com.starcom.pocketmaps.views;

import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.GeoKeyI;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.geocoding.GeocoderGlobal;
import com.starcom.pocketmaps.geocoding.GeocoderLocal;
import com.starcom.pocketmaps.map.MapLayer;
import com.starcom.pocketmaps.map.MapLayer.MapFileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.starcom.gdx.ui.GuiUtil;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class SearchPanel
{
    private static SearchPanel instance = new SearchPanel();
    enum Engines { OpenStreetMaps, GoogleMaps, Offline }
    Engines selectedEngine = Engines.OpenStreetMaps;
    private TextField txtField;
    private Actor engineDD;
    private Window dialog;
    boolean visible;
    private Table offlinePanel = new Table();
    private String offlineCountry;
    private String offlineContinent;
    
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
        dialog.add(genOfflinePanel()).colspan(2);
        dialog.row();
        dialog.add(GuiUtil.genButton("Break", 0, 0, (a, x, y) -> this.onSearch(null))).width(ww/3);
        dialog.add(GuiUtil.genButton("Search", 0, 0, (a, x, y) -> this.onSearch(this.txtField.getText()))).width(ww/3);
        dialog.setSize(ww * 0.8f, Gdx.graphics.getHeight() * 0.8f);
        dialog.setX(ww * 0.1f);
        dialog.setY(Gdx.graphics.getHeight() * 0.1f);
        
    }
    
    private void setupTxtField()
    { //TODO Clear this, if not doing anything
    	txtField.getOnscreenKeyboard().show(true);
    }
    
    private Table genOfflinePanel()
    {
    	ArrayList<String> maps = new ArrayList<>();
    	for (MapLayer m : MapList.getInstance().mapLayers)
    	{
    		String countryName = m.getMapFile(MapFileType.Country);
    		String contName = m.getMapFile(MapFileType.Continent);
    		if (offlineCountry == null) { offlineCountry = countryName; offlineContinent = contName; }
    		maps.add(contName + "_" + countryName);
    	}
    	int sel = Cfg.getIntValue(Cfg.GeoKeyI.SearchBits, 6);
    	offlinePanel.add(GuiUtil.genDropDown((o) -> onOfflineCountrySelect(o.toString()), 0, 0, maps.toArray(new String[0])));
    	offlinePanel.row();
    	boolean isSelected = (sel & GeocoderLocal.BIT_MULT) != 0;
    	offlinePanel.add(GuiUtil.genCheckBox((o) -> onOfflineCheckbox(GeocoderLocal.BIT_MULT, o), 0, 0, "MultiMatchOnly [very slow]", isSelected));
    	offlinePanel.row();
    	isSelected = (sel & GeocoderLocal.BIT_EXPL) != 0;
    	offlinePanel.add(GuiUtil.genCheckBox((o) -> onOfflineCheckbox(GeocoderLocal.BIT_EXPL, o), 0, 0, "ExplicitSearchText", isSelected));
    	offlinePanel.row();
    	offlinePanel.add(GuiUtil.genLabel("--------", 0, 0));
    	offlinePanel.row();
    	isSelected = (sel & GeocoderLocal.BIT_CITY) != 0;
    	offlinePanel.add(GuiUtil.genCheckBox((o) -> onOfflineCheckbox(GeocoderLocal.BIT_CITY, o), 0, 0, "CityNodes", isSelected));
    	offlinePanel.row();
    	isSelected = (sel & GeocoderLocal.BIT_STREET) != 0;
    	offlinePanel.add(GuiUtil.genCheckBox((o) -> onOfflineCheckbox(GeocoderLocal.BIT_STREET, o), 0, 0, "StreetNodes [slow]", isSelected));
    	offlinePanel.setVisible(false);
    	return offlinePanel;
    }
    
    private void onOfflineCheckbox(int bit, Object selected)
    {
    	System.out.println("Country Selected: " + selected);
    	int sel = Cfg.getIntValue(Cfg.GeoKeyI.SearchBits, 6) ^ bit; //XOR
    	if (selected == Boolean.TRUE)
    	{
    		sel += bit;
    	}
    	System.out.println("Bits stored: " + sel);
    	Cfg.setIntValue(GeoKeyI.SearchBits, sel);
    }
    
    private void onOfflineCountrySelect(String continentCountry)
    {
    	offlineContinent = continentCountry.split("_")[0];
    	offlineCountry = continentCountry.split("_")[1];
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
    	selectedEngine = Engines.valueOf(engine);
    	if (selectedEngine == Engines.Offline)
    	{
    		offlinePanel.setVisible(true);
    	}
    	else
    	{
    		offlinePanel.setVisible(false);
    	}
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
            List<Address> list;
            if (selectedEngine == Engines.OpenStreetMaps)
            {
            	list = new GeocoderGlobal(Locale.getDefault()).find_osm(txt);
            }
            else if (selectedEngine == Engines.Offline)
            {
            	Cfg.save(ConfType.Geocoding);
            	list = new GeocoderGlobal(Locale.getDefault()).find_local(offlineCountry, txt, (t,o) -> onOfflineSearch(t,o.toString())); //TODO: update this.
            }
            else if (selectedEngine == Engines.GoogleMaps)
            {
            	list = new GeocoderGlobal(Locale.getDefault()).find_google(txt);
            	throw new IllegalStateException("No engine implemented: " + selectedEngine);
            }
            else
            {
            	throw new IllegalStateException("No engine found: " + selectedEngine);
            }
            for (Address a : list)
            {
            	System.out.println("###### Fount entry #######\n" + a.toString());
            }
            this.setVisible(false);
            NavSelect.getInstance().setVisible(true);
        }
    }
    
    private void onOfflineSearch(Type t, String txt)
    {
    	System.out.println("Progress offlineSearch " + t + ": " + txt);
    }
}
