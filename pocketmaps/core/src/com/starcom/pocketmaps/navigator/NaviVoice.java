package com.starcom.pocketmaps.navigator;

import java.util.Locale;
import java.util.ArrayList;

import com.starcom.tts.Voice;
import com.starcom.tts.VoiceEngine;
import com.starcom.LoggerUtil;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;

public class NaviVoice
{
  public static final String DISPLAY_LANG = Locale.getDefault().getISO3Language();
  VoiceEngine engine = VoiceEngine.createInstance();
  String selEngine = Cfg.getValue(NavKey.TtsEngine, null);
  boolean selEngineDone = false;
  Voice curVoice;
  Locale wantedLang = new Locale(DISPLAY_LANG);
  Locale fallbackLang = Locale.ENGLISH;
  boolean ttsMute = false;
  
  public NaviVoice()
  {
	engine.init();
    if (selEngine != null)
    {
        engine.setEngine(selEngine);
    }
    updateVoice();
  }
  
  public void shutdownTts() { engine.shutdown(); }

  public boolean isTtsReady()
  {
    return engine.isReady();
  }
  
  public void setTtsMute(boolean ttsMute) { this.ttsMute = ttsMute; }
  
  public void speak(String fallbackTxt, String txt)
  {
    if (!engine.isReady()) { return; }
    if (!Cfg.getBoolValue(NavKeyB.TtsOn, true)) { return; }
    if (ttsMute) { return; }
    updateVoice();
    if (wantedLang == fallbackLang) { engine.speak(fallbackTxt); }
    else { engine.speak(txt); }
  }
  
  public ArrayList<String> getEngineList()
  {
    if (!engine.isReady()) { return new ArrayList<>(); }
    return engine.getEngineList();
  }
  
  public ArrayList<Voice> getVoiceList()
  {
	  if (!engine.isReady()) { return new ArrayList<>(); }
	  return engine.getVoiceList();
  }
  
  private void updateVoice()
  {
    if (!engine.isReady()) { return; }
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
    log("Voice selected: " + curVoice);
    engine.setVoice(curVoice);
  }

  private Voice searchWantedVoice(Locale selLang)
  {
    ArrayList<Voice> curVoices = engine.getVoiceList();
    if (curVoices.size() == 0) { return null; }
    if (Cfg.getValue(NavKey.TtsWantedVoice, null) != null)
    {
      for (Voice v : curVoices)
      {
    	  if (v.getLocale().equals(selLang))
        if (v.getName().equals(Cfg.getValue(NavKey.TtsWantedVoice, null).split(":")[1]))
        {
            return v;
        }
      }
    }
    return curVoices.iterator().next();
  }
  
  private void log(String txt)
  {
	LoggerUtil.get(getClass()).info(txt);
  }
}
