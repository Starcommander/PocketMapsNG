package com.starcom.tts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TextToSpeech
{
	public static final String SUCCESS = "successful";
	
	public TextToSpeech(Object appContext, OnInitListener l) //TODO: Implement
	{
		l.onInit(SUCCESS);
	}

	public TextToSpeech(Object appContext, OnInitListener l, String engineName) //TODO: Implement
	{
		this(appContext, l);
	}
	
	public List<EngineInfo> getEngines() //TODO: Implement
	{
		ArrayList<EngineInfo> lst = new ArrayList<EngineInfo>();
		lst.add(new EngineInfo("xxx.xxx.espeak"));
		return lst;
	}
	
	/** Returns all existing voices, or an empty list. */
	public Set<Voice> getVoices() { return new HashSet<>(); } //TODO: Implement
	public Voice getVoice() { return null; } //TODO: Implement
	public void setVoice(Voice voice) {} //TODO: Implement
	
	public Set<Voice> getVoices(Locale lang)
	{
	    Set<Voice> allV = getVoices();
	    Set<Voice> selV = new HashSet<>();
	    for (Voice curV : allV)
	    {
	        if (curV.getLocale().getISO3Language().equals(lang.getISO3Language())) { selV.add(curV); }
	    }
	    return selV;
	}
	
	public boolean isSpeaking() { return false; } //TODO: Implement
	public void speak(String txt) {} //TODO: Implement
	/** Stops the current speaking and clears cache. */
	public void stop() {} //TODO: Implement
	/** Shut down the engine */
	public void shutdown() {} //TODO: Implement

	public static class EngineInfo
	{
		public String name;
		public EngineInfo(String name) { this.name = name; }
	}
	
	public static interface OnInitListener
	{
		public void onInit(String result);
	}
}
