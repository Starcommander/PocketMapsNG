package com.starcom.io;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;

public class ManifestReader
{
	/** Iterates existing manifest-files and reads the first existing attribute-value that matches the key.
	 * @param key The key of requested value.
	 * @return The first matiching attr-value, or null if not existing.
	 * @throws IOException When any read-exception occurs. */
	public static String readFirstMainAttribute(String key) throws IOException
	{
		Enumeration<URL> resources = ManifestReader.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
		while (resources.hasMoreElements())
		{
			Manifest manifest = new Manifest(resources.nextElement().openStream());
			String v = manifest.getMainAttributes().getValue(key);
			if (v != null) { return v; }
		}
		return null;
	}
}
