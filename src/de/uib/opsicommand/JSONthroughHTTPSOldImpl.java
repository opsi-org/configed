package de.uib.opsicommand;

/*  Copyright (c) 2006 uib.de
 
Usage of this portion of software is allowed unter the restrictions of the GPL
 
*/

import utils.*;
import org.json.*;
import de.uib.utilities.logging.*;
import java.io.*;
import java.io.Reader.*;
import java.net.*;
import com.sun.net.ssl.*;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import java.security.cert.*;


/**
*
*/

public class JSONthroughHTTPSOldImpl extends JSONthroughHTTPS
{
	protected final String CODING_TABLE = "UTF8";
	static private javax.net.ssl.SSLSocketFactory sslFactory;
	
	/**
	*  @param host
	*  @param username
	*  @param password 
	
	*/
	
	public JSONthroughHTTPSOldImpl (String host, String username, String password)
	{
		super (host, username, password);
		createDullSSLContext(); //produces sslFactory (which does not really look for certificates)
	}
	
	
	/**
	* Opening the connection and set the SSL parameters
	*/
	protected void produceConnection () throws java.io.IOException
	{
		connection =  (com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl)serviceURL.openConnection ( );
		
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
				return true;
			}
			public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
				return true;
			}
		} };
		
		try
		{
			@SuppressWarnings("deprecation")
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			sslFactory = sslContext.getSocketFactory();
			((com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl)connection).setSSLSocketFactory(sslFactory);
			((com.sun.net.ssl.internal.www.protocol.https.HttpsURLConnectionOldImpl)connection).setHostnameVerifier(new DullHostnameVerifier());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	class DullHostnameVerifier implements com.sun.net.ssl.HostnameVerifier
	{
		public boolean verify (String s1, String s2)
		{
			return true;
		}
	}
	
	
	public static void main (String args[])
	{
		String resulting = "";
		JSONthroughHTTPSOldImpl instance;
		
		instance = new JSONthroughHTTPSOldImpl ("194.31.185.160",  "cn=admin,dc=uib,dc=local", "umwelt");
		instance.retrieveJSONObject ( new  OpsiMethodCall ("getProductStates_listOfHashes", new String[]{"pcbon1.uib.local"} ) );
	}
}

