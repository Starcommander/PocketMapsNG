package com.starcom.tts;

import java.util.Locale;

public class Voice
{
	Locale locale;
	String name;
	
	public Voice(Locale locale, String name)
	{
		this.locale = locale;
		this.name = name;
	}
	
	public Locale getLocale() { return locale; }
	public String getName() { return name; }
}
