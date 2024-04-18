package com.starcom.pocketmaps.views;

import java.util.List;

import org.oscim.core.GeoPoint;

import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.interfaces.IObjectListener;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;

public class AddressList
{
	private static AddressList instance = new AddressList();
	private AddressList() {}
	
	public static AddressList getInstance() { return instance; }
	
	public void viewList(List<Address> list, boolean from)
	{
		ListSelect ll = new ListSelect("SelectAddress", () -> onClose());
		for (Address a : list)
		{
			ll.addElement(a.toNiceString(), (o,x,y) -> onAddressSelected(a, from));
		}
		ll.showAsWindow(GuiUtil.getStage());
	}
	
	private void onAddressSelected(Address a, boolean from)
	{
		MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), a, from, true);
		NavSelect.getInstance().setVisible(true);
	}
	
	private void onClose()
	{
		NavSelect.getInstance().setVisible(true);
	}
}
