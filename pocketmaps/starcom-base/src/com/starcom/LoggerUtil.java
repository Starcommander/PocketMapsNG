package com.starcom;

import java.util.logging.Logger;

public class LoggerUtil
{

	public static Logger get(Class<?> clazz)
	{
		return Logger.getLogger(clazz.getPackageName() + ":" + clazz.getName());
	}
}
