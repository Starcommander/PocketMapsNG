package com.starcom.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.starcom.interfaces.IProgressListener;
import com.starcom.interfaces.IProgressListener.Type;

/** Hints from https://www.baeldung.com/java-compress-and-uncompress */
public class Unzip
{
	/** Returns Long.MAX_VALUE on error. */
	public static long calculateExtractedSize(File zipFile)
	{
		long fullSize = 0;
		try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
        {
			ZipEntry zipEntry = zis.getNextEntry();
	        // Calculate fullSize first.
	        while (zipEntry != null) {
	        	fullSize += zipEntry.getSize();
	            zipEntry = zis.getNextEntry();
	        }
        }
        catch (IOException e)
        {
        	e.printStackTrace();
        	return Long.MAX_VALUE;
        }
		return fullSize;
	}
	
	public static void unzip(File zipFile, File destDir, IProgressListener<Object> callback)
	{
       	long fullSize = calculateExtractedSize(zipFile);
       	long writtenSize = 0;
        byte[] buffer = new byte[1024];
        callback.onProgress(Type.PROGRESS, "Unzipping...");
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile)))
        {
        	ZipEntry zipEntry = zis.getNextEntry();
	        while (zipEntry != null) {
	            File newFile = newFile(destDir, zipEntry);
	            if (zipEntry.isDirectory()) {
	                if (!newFile.isDirectory() && !newFile.mkdirs()) {
	                    throw new IOException("Failed to create directory " + newFile);
	                }
	            } else {
	                File parent = newFile.getParentFile();
	                if (!parent.isDirectory() && !parent.mkdirs()) {
	                    throw new IOException("Failed to create directory " + parent);
	                }
	
	                // write file content
	                FileOutputStream fos = new FileOutputStream(newFile);
	                int len;
	                while ((len = zis.read(buffer)) > 0) {
	                    fos.write(buffer, 0, len);
	                    writtenSize += len;
	                    float percent = ((float)writtenSize / fullSize) * 100.0f;
	                    callback.onProgress(Type.PROGRESS, Integer.valueOf((int)percent));
	                    
System.out.println("On progress " + fullSize + "/" + writtenSize + " = " + percent + "%");
	                }
	                fos.close();
	            }
	            zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
	        callback.onProgress(Type.SUCCESS, "Successful");
        }
        catch (IOException e)
        {
        	callback.onProgress(Type.ERROR, e);
        }
    }

	private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException
	{
	    File destFile = new File(destinationDir, zipEntry.getName());

	    String destDirPath = destinationDir.getCanonicalPath();
	    String destFilePath = destFile.getCanonicalPath();

	    if (!destFilePath.startsWith(destDirPath + File.separator)) {
	        throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	    }

	    return destFile;
	}
}
