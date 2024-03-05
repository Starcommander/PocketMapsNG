package com.starcom.interfaces;

public interface IProgressListener
{
	public enum Type {ERROR, SUCCESS, CANCEL, PROGRESS};
	public void onProgress(Type type, Object txt);
}
