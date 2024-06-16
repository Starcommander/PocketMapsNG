package com.starcom.interfaces;

public interface IObjectResponse<T>
{
	/** Is used in Threading.invoceAsyncTask and returns the result, but may also return or throw an Exception */
	public T run() throws Exception;
}
