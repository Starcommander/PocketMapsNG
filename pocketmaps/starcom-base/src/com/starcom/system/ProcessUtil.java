package com.starcom.system;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;

public class ProcessUtil
{
	/** Executes the command.
	 * @return The error output stream as string, or null on error. */
	public static String execErrOut(String... command)
	{
		return execInternal(true, command);
	}
	
	/** Executes the command.
	 * @return The standard output stream as string, or null on error. */
	public static String execStdOut(String... command)
	{
		return execInternal(false, command);
	}
	
	/** Executes the command, redirects out and err.
	 * @param bg False to waitFor and get the exit code.
	 * @param command The command to execute.
	 * @return The exit-code of the command, or 0 on bg, or Integer.MAX_VALUE on Exception. */
	public static int exec(boolean bg, String... command)
	{
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectError(Redirect.INHERIT);
		pb.redirectOutput(Redirect.INHERIT);
		try
		{
			Process p = pb.start();
			if (bg) { return 0; }
			p.waitFor();
			return p.exitValue();
		}
		catch (IOException|InterruptedException e) { e.printStackTrace(); }
		return Integer.MAX_VALUE;
	}
	
	private static String execInternal(boolean readErr, String... command)
	{
		ProcessBuilder pb = new ProcessBuilder(command);
		pb.redirectError(readErr? Redirect.PIPE : Redirect.INHERIT);
		pb.redirectOutput(readErr? Redirect.INHERIT : Redirect.PIPE);
		try
		{
			Process p = pb.start();
			String result = new String(p.getInputStream().readAllBytes());
			p.waitFor();
			if (p.exitValue() != 0) { return null; }
			return result;
		}
		catch (IOException|InterruptedException e) { e.printStackTrace(); }
		return null;
	}

	/** Converts a free-text table, where multible tabs and spaces are allowed.
	 * <br> Not alowed is a column with spaces/tabs/newlines, that would be splitted into new columns.
	 * @return A list of line-arrays */
	public static ArrayList<String[]> convertTable(String table)
	{
		ArrayList<String[]> lines = new ArrayList<>();
		if (table==null) { return lines; }
		table.lines().forEach((l) -> lines.add(l.trim().split("[ \\t\\n]+")));
		return lines;
	}
	
	/** Returns the collumn as String array. */
	public static String[] getTableCollumn(ArrayList<String[]> table, String headTitle)
	{
		if (table.size() == 0) { return new String[0]; }
		int index = 0;
		for (String h : table.get(0))
		{
			if (h.equals(headTitle)) { break; }
			index++;
		}
		return getTableCollumn(table, index, true);
	}

	/** Returns the collumn as String array. */
	public static String[] getTableCollumn(ArrayList<String[]> table, int colIndex, boolean skipHeaderLine)
	{
		int start = 0;
		int len = table.size();
		if (skipHeaderLine)
		{
			start++;
			len--;
		}
		String result[] = new String[len];
		for (int i=0; i<result.length; i++)
		{
			if (table.get(i+start).length < colIndex) { result[i] = ""; }
			else { result[i] = table.get(i+start)[colIndex]; }
		}
		return result;
	}
}
