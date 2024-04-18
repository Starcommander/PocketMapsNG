package com.starcom.interfaces;

public interface IProgressListener<T>
{
	public enum Type {ERROR, SUCCESS, CANCEL, PROGRESS};
	public void onProgress(Type type, T txt);
}
