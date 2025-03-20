package com.starcom.tts;

import java.util.Locale;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public interface VoiceEngine
{
  public static final String DISPLAY_LANG = Locale.getDefault().getISO3Language();

  public static VoiceEngine createInstance()
  {
    try
    {
      return (VoiceEngine)Class.forName("com.starcom.tts.VoiceEngineImpl").getConstructor().newInstance();
    } catch (InstantiationException e)
    {
      e.printStackTrace();
    } catch (IllegalAccessException e)
    {
      e.printStackTrace();
    } catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    } catch (InvocationTargetException e)
    {
      e.printStackTrace();
    } catch (NoSuchMethodException e)
    {
      e.printStackTrace();
    } catch (SecurityException e)
    {
      e.printStackTrace();
    } catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  public void setEngine(String selectedEngine);

  public void setVoice(Voice selectedVoice);

  public void init();
  
  public void shutdown();

  public boolean isReady();


  public void speak(String txt);

  public void stop();

  public ArrayList<String> getEngineList();

  public ArrayList<Voice> getVoiceList();

//TODO: getVoiceListAvailable + downloadVoice

}
