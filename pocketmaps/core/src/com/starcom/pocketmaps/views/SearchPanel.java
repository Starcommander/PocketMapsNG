package com.starcom.pocketmaps.views;

import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.GeoKey;
import com.starcom.pocketmaps.Cfg.GeoKeyI;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.geocoding.GeocoderGlobal;
import com.starcom.pocketmaps.geocoding.GeocoderLocal;
import com.starcom.pocketmaps.map.MapLayer;
import com.starcom.pocketmaps.map.MapLayer.MapFileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.badlogic.gdx.Gdx;
import com.starcom.LoggerUtil;
import com.starcom.gdx.ui.GuiUtil;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

public class SearchPanel
{
    private static SearchPanel instance = new SearchPanel();
    private Logger logger = LoggerUtil.get(SearchPanel.class);
    enum Engines { OpenStreetMaps, GoogleMaps, Offline }
    Engines selectedEngine = Engines.OpenStreetMaps;
    private TextField txtField;
    private SelectBox<String> engineDD;
    private SelectBox<String> mapDD;
    private Window dialog;
    private boolean visible;
    private boolean isStart;
    private Table offlinePanel = new Table();
    private String offlineCountry;
    private String offlineContinent;
    
    public static SearchPanel getInstance() {
        return SearchPanel.instance;
    }
    
    private SearchPanel() {
    	float ww = Gdx.graphics.getWidth();
        txtField = new TextField("", GuiUtil.getDefaultSkin());
        engineDD = GuiUtil.genDropDown(o -> this.onEngineDropDown(o.toString()), 0, 0, true,
        		Engines.OpenStreetMaps.toString(),
        		Engines.GoogleMaps.toString(),
        		Engines.Offline.toString());
        dialog = new Window("Search", GuiUtil.getDefaultSkin());
        dialog.setModal(true);
        dialog.add(engineDD).colspan(2);
        dialog.row();
        dialog.add(txtField).width(ww * 0.8f).expand().colspan(2);
        setupTxtField();
        dialog.row();
        dialog.add(genOfflinePanel()).colspan(2);
        dialog.row();
        dialog.add(GuiUtil.genButton("Break", 0, 0, (a, x, y) -> this.onSearch(null))).width(ww/3);
        dialog.add(GuiUtil.genButton("Search", 0, 0, (a, x, y) -> this.onSearch(this.txtField.getText()))).width(ww/3);
        dialog.setSize(ww * 0.8f, Gdx.graphics.getHeight() * 0.8f);
        dialog.setX(ww * 0.1f);
        dialog.setY(Gdx.graphics.getHeight() * 0.1f);
    	String sel = Cfg.getValue(Cfg.GeoKey.SearchEngine, Engines.OpenStreetMaps.toString());
    	engineDD.setSelected(sel);
    }
    
    private void setupTxtField()
    { //TODO Clear this, if not doing anything (Test on android)
    	txtField.getOnscreenKeyboard().show(true);
    }
    
    private void updateMapDD()
    {
    	ArrayList<String> maps = new ArrayList<>();
    	for (MapLayer m : MapList.getInstance().mapLayers)
    	{
    		String countryName = m.getMapFile(MapFileType.Country);
    		String contName = m.getMapFile(MapFileType.Continent);
    		if (offlineCountry == null) { offlineCountry = countryName; offlineContinent = contName; }
    		String name = contName + "_" + countryName;
    		if (name.equals(Cfg.getValue(GeoKey.OfflineCountry, null)))
    		{
    			maps.add(0, name);
    		}
    		else
    		{
    			maps.add(name);
    		}
    	}
    	mapDD.clearItems();
    	mapDD.setItems(maps.toArray(new String[0]));
    }
    
    private Table genOfflinePanel()
    {
    	int sel = Cfg.getIntValue(Cfg.GeoKeyI.SearchBits, 6);
    	mapDD = GuiUtil.genDropDown((o) -> onOfflineCountrySelect(o), 0, 0, true, new String[0]);
    	updateMapDD();
    	offlinePanel.add(mapDD);
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
    	int sel = Cfg.getIntValue(Cfg.GeoKeyI.SearchBits, 6);
    	boolean wasSelected = (sel & bit) != 0;
    	if (wasSelected)
    	{
    		sel = sel - bit;
		}
		if (selected == Boolean.TRUE)
		{
			sel += bit;
		}
    	logger.info("Geocoding Checkbox-Bits stored: " + sel);
    	Cfg.setIntValue(GeoKeyI.SearchBits, sel);
    }
    
    private void onOfflineCountrySelect(String continentCountry)
    {
    	if (continentCountry == null) { return; } // On init the country is null.
    	offlineContinent = continentCountry.split("_")[0];
    	offlineCountry = continentCountry.split("_")[1];
    	Cfg.setValue(GeoKey.OfflineCountry, continentCountry);
    }
    
    public void setVisible(final boolean visible, final boolean isStart) {
    	this.isStart = isStart;
        if (this.visible == visible) {
            return;
        }
        if (visible) {
        	updateMapDD();
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
    	Cfg.setValue(Cfg.GeoKey.SearchEngine, engine);
    	Cfg.save(ConfType.Geocoding);
    }
    
    private void onSearch(final String txt) {
    	if (txt == null)
    	{
            this.setVisible(false, isStart);
            NavSelect.getInstance().setVisible(true, true);
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
            	Cfg.save(ConfType.Geocoding); //TODO: Only on offline, or hints also on others?
            	list = new GeocoderGlobal(Locale.getDefault()).find_local(offlineCountry, offlineContinent, txt, (t,o) -> onOfflineSearch(t,o.toString()));
            }
            else if (selectedEngine == Engines.GoogleMaps)
            {
            	list = new GeocoderGlobal(Locale.getDefault()).find_google(txt);
            }
            else
            {
            	throw new IllegalStateException("No engine found: " + selectedEngine);
            }
            if (list == null)
            {
            	ToastMsg.getInstance().toastShort("Error on searching");
            	return;
            }
            AddressList.getInstance().viewList(list, isStart);
            this.setVisible(false, isStart);
        }
    }
    
    private void onOfflineSearch(Type t, String txt)
    {
    	System.out.println("Progress offlineSearch " + t + ": " + txt); //TODO: Update this, show visible progress.
    }
}
