package com.starcom.interfaces;

public interface IObjectResponse
{
	/** Is used in Threading.invoceAsyncTask and returns the result, but may also return or throw an Exception */
	public Object run() throws Exception;
}
