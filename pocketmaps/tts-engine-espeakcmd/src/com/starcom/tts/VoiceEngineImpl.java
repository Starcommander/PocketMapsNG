package com.starcom.tts;

import com.starcom.system.ProcessUtil;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceEngineImpl implements VoiceEngine
{
	String selectedEngine = "espeak";
	Voice selectedVoice = null;

	@Override
	public void setEngine(String selectedEngine)
	{
		// Nothing to do.
	}


	@Override
	public void setVoice(Voice selectedVoice)
	{
		this.selectedVoice = selectedVoice;
	}

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
			ProcessUtil.exec(true, "speak-ng", "-v", selectedVoice.getName(), txt);
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
	public ArrayList<Voice> getVoiceList()
	{
		ArrayList<Voice> a = new ArrayList<>();
		String table = ProcessUtil.execStdOut("espeak-ng", "--voices");
		int idxName = -1; // VoiceName
		int idxLoc = -1;
		for (String line[] : ProcessUtil.convertTable(table))
		{
			if (idxName == -1)
			{
				for (int i=0; i<line.length; i++)
				{
					if (line[i].equals("VoiceName")) { idxName = i; }
					else if (line[i].equals("Language")) { idxLoc = i; }
				}
				if (idxName == -1) { return a; }
				if (idxLoc == -1) { return a; }
			}
			a.add(new Voice(new Locale(line[idxLoc]), line[idxName]));
		}
		return a;
	}

	@Override
	public void init() {}

	@Override
	public void shutdown() {}

}
