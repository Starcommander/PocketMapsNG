package com.starcom.pocketmaps.navigator;

import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

import com.starcom.tts.TextToSpeech;
import com.starcom.tts.TextToSpeech.EngineInfo;
import com.starcom.tts.Voice;
import com.starcom.LoggerUtil;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;

public class NaviVoice
{
  public static final String DISPLAY_LANG = Locale.getDefault().getISO3Language();
  TextToSpeech tts;
  Voice curVoice;
  Locale wantedLang = new Locale(DISPLAY_LANG);
  Locale fallbackLang = Locale.ENGLISH;
  boolean ttsReady;
  String ttsError = null;
  boolean ttsMute = false;
  
  public NaviVoice(Object appContext)
  {
    String selEngine = Cfg.getValue(NavKey.TtsEngine, null);
    if (selEngine == null)
    { // Ensure to use applicationContext
        tts = new TextToSpeech(appContext, (s) -> onEngineInit(s));
    }
    else
    { // Ensure to use applicationContext
        tts = new TextToSpeech(appContext, (s) -> onEngineInit(s), selEngine);
    }
    updateVoice();
  }
  
  public void shutdownTts() { tts.shutdown(); }
  
  /** Returns the error, if any, or TextToSpeech.SUCCESS */
  public String getError() { return ttsError; }

  public boolean isTtsReady()
  {
    return ttsReady;
  }
  
  public void setTtsMute(boolean ttsMute) { this.ttsMute = ttsMute; }
  
  public void speak(String fallbackTxt, String txt)
  {
    if (!ttsReady) { return; }
    if (!Cfg.getBoolValue(NavKeyB.TtsOn, false)) { return; }
    if (ttsMute) { return; }
    updateVoice();
    if (wantedLang == fallbackLang) { tts.speak(fallbackTxt); }
    else { tts.speak(txt); }
  }
  
  public ArrayList<String> getEngineList()
  {
    ArrayList<String> list = new ArrayList<String>();
    if (!ttsReady) { return list; }
    for (EngineInfo curEngine : tts.getEngines())
    {
        String packName = curEngine.name;
        int lastDot = packName.lastIndexOf(".");
        String engName = packName.substring(lastDot + 1);
        list.add(engName + "/" + packName);
    }
    return list;
  }
  
  public ArrayList<String> getVoiceListCompat()
  {
    if (!ttsReady) { return null; }
      HashSet<Voice> allVoices = new HashSet<Voice>();
      Set<Voice> curVoices = tts.getVoices(wantedLang);
      log("Found " + curVoices.size() + " voices for " + wantedLang);
      if (curVoices != null) { allVoices.addAll(curVoices); }
      curVoices = tts.getVoices(fallbackLang);
      log("Found " + curVoices.size() + " voices for " + fallbackLang);
      if (curVoices != null) { allVoices.addAll(curVoices); }
      ArrayList<String> curList = new ArrayList<String>();
      for (Voice v : allVoices)
      {
        curList.add(v.getLocale() + ":" + v.getName());
      }
      return curList;
  }


  private void updateVoice()
  {
    if (!ttsReady) { return; }
    if (curVoice == null)
    {
      curVoice = searchWantedVoice(wantedLang);
      if (curVoice == null)
      {
          wantedLang = fallbackLang;
          curVoice = searchWantedVoice(fallbackLang);
      }
      if (curVoice == null) { return; }
    }
    else
    {
      if (tts.getVoice().equals(curVoice)) { return; }
    }
    tts.setVoice(curVoice);
  }

  private Voice searchWantedVoice(Locale selLang)
  {
    Set<Voice> curVoices = tts.getVoices(selLang);
    if (curVoices.size() == 0) { return null; }
    if (Cfg.getValue(NavKey.TtsWantedVoice, null) != null)
    {
      for (Voice v : curVoices)
      {
        if (v.getName().equals(Cfg.getValue(NavKey.TtsWantedVoice, null).split(":")[1]))
        {
            return v;
        }
      }
    }
    return curVoices.iterator().next();
  }

  private void onEngineInit(String result)
  {
    if (result==TextToSpeech.SUCCESS) { ttsReady = true; }
    ttsError = result;
  }
  
  private void log(String txt)
  {
	LoggerUtil.get(getClass()).info(txt);
  }
}
