package com.starcom.pocketmaps.text;

import java.util.Locale;

public interface Text
{
	public String getAppName();
	public String getOnGoogleMap();
	public String getTitleActivityMain();
	public String getTitleActivityMap();
	public String getActionQuit();
	public String getShowMyPosition();
	public String getTitleActivityMainActivity2();
	public String getWeightingShortest();
	public String getWeightingFastest();
	public String getUseCurrentLocation();
	public String getFromLocation();
	public String getToLocation();
	public String getFromLoc();
	public String getToLoc();
	public String getChooseFromFavorite();
	public String getEnterLatLon();
	public String getExample();
	public String getErrorOccured();
	public String getPointOnMap();
	public String getTravelMode();
	public String getTravelModeFoot();
	public String getTravelModeBike();
	public String getTravelModeCar();
	public String getTime();
	public String getDistance();
	public String getStopNavigation();
	public String getStopNavigationMsg();
	public String getDeleteMsg();
	public String getCancel();
	public String getOk();
	public String getSave();
	public String getStop();
	public String getHM();
	public String getSettings();
	public String getChangeMap();
	public String getDownloadMap();
	public String getNavigation();
	public String getAlternateRoute();
	public String getFastest();
	public String getShortest();
	public String getDirectionsNavi();
	public String getTitleActivitySelect();
	public String getTitleActivityDownload();
	public String getRemove();
	public String getLoadingDotdotdot();
	public String getPathFinding();
	public String getSearchLocation();
	public String getTrackingStop();
	public String getTrackingStart();
	public String getTrackingLoad();
	public String getDialogStopSaveTracking();
	public String getTitleActivityAnalytics();
	public String getVisitHomePage();
	public String getTitleActivityAbout();
	public String getLocaleCode();
	public String getLocation();
	public String getSearchEngine();
	public String getAddAddress();
	public String getTitle();
	public String getAddText();
	public String getRatePocketMaps();
	public String getSwipeOut();
	public String getDontShowAgain();
	public String getWrongDirection();
	public String getWaitForLocation();
	public String getLightNavi();
	public String getVoiceNavi();
	public String getSwitchMapsDir();
	public String getNeedsVersLollipop();
	public String getAutoselectMap();
	public String getAutoselectMapText();
	public String getImp();
	public String getExp();
	public String getUnits();
	public String getUnitsImperal();
	public String getUnitsMetric();
	public String getGpsIsOff();
	public String getGpsSettings();
	public String getGpsSmooth();
	public String getShowSpeed();
	public String getSpeakSpeed();
	public String getSearchHint();
	public String getAnyVoice();
	public String getEngineStartup();
	public String getEdit();
	public String getHelp();
	public String getNavivoiceWrongdir();
	public String getNavivoiceIn();
	public String getNavivoiceMeters();
	public String getNavivoiceFeet();
	public String getNavivoiceUseround();
	public String getNavivoiceTurnxxx();
	public String getNavivoiceKeepxxx();
	public String getNavivoiceLeft();
	public String getNavivoiceRight();
	public String getNavivoiceSlightl();
	public String getNavivoiceSlightr();
	public String getNavivoiceSharpl();
	public String getNavivoiceSharpr();
	public String getNavivoiceOn();
	public String getNavivoiceOnto();
	public String getNavivoiceContinue();
	public String getNavivoiceLeaveround();
	public String getNavivoiceNavend();
	public String getShowHints();
	public String getHintSettNav();
	public String getHintSettGen();
	public String getHintFavSel();
	public String getHintFavAdd();
	public String getHintCenter();
	public String getHintNav();

	public static Text getInstance()
	{
		String lang = Locale.getDefault().getLanguage();
		if (lang.isEmpty()) { lang = "en"; }
		try
		{
			Object o = Class.forName(Text.class.getName() + lang.toUpperCase()).getConstructor().newInstance();
			return (Text)o;
		}
		catch (Exception e)
		{
			return new TextEN();
		}
	}
}
