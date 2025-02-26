package com.starcom.tts;

import com.starcom.system.ProcessUtil;

import java.util.ArrayList;

public class VoiceEngineImpl implements VoiceEngine
{
	String selectedEngine = "espeak";
	String selectedVoice = null;

	@Override
	public void setEngine(String selectedEngine)
	{
		// Nothing to do.
	}

	@Override
	public void setVoice(String selectedVoice)
	{
		this.selectedVoice = selectedVoice;
	}

	@Override
	public void shutdownTts() {}

	@Override
	public boolean isReady()
	{
		return true;
	}

	@Override
	public void speak(String txt)
	{
		if (selectedVoice == null)
		{
			ProcessUtil.exec(true, "speak-ng", txt);
		}
		else
		{
			ProcessUtil.exec(true, "speak-ng", "-v", selectedVoice, txt);
		}
	}

	@Override
	public void stop()
	{
	}

	@Override
	public ArrayList<String> getEngineList()
	{
		ArrayList<String> a = new ArrayList<>();
		a.add(selectedEngine);
		return a;
	}

	@Override
	public ArrayList<String> getVoiceList()
	{
		ArrayList<String> a = new ArrayList<String>();
		String table = ProcessUtil.execStdOut("espeak-ng", "--voices");
		for (String voice : ProcessUtil.getTableCollumn(ProcessUtil.convertTable(table), "VoiceName"))
		{
			a.add(voice);
		}
		return a;
	}

}
