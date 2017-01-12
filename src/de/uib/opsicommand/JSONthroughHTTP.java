package de.uib.opsicommand;

/*  Copyright (c) 2006-2016 uib.de
 
Usage of this portion of software is allowed unter the restrictions of the GPL
 
*/

import utils.*;
import org.json.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import de.uib.configed.configed;
import de.uib.configed.Globals;
import java.io.*;
import java.io.Reader.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
*
* @author Rupert Roeder 
*/

public class JSONthroughHTTP extends JSONExecutioner
{
	/*
	static{
		if (NONE == null)
			NONE = new JSONthroughHTTP();
	}
	*/
	public static boolean gzipTransmission = false;
	protected final int POST = 0;
	protected final int GET = 1;
	protected final String CODING_TABLE = "UTF8";
	protected String host;
	protected String username;
	protected String password;
	protected int portHTTP = 4444;
	public static final int defaultPort = 4447; 
	protected int portHTTPS = defaultPort;
	//protected HttpURLConnection connection;
	protected boolean startConnecting = false;
	protected boolean endConnecting = false;
	protected URL serviceURL;
	protected String sessionId;
	protected String lastSessionId;
	protected int requestMethod = POST;
	public final java.nio.charset.Charset UTF8DEFAULT = java.nio.charset.Charset.forName("UTF-8");
	
	
	class JSONCommunicationException extends Exception
	{
		JSONCommunicationException(String message)
		{
			super(message);
		}
	}
	
	/**
	
	 
	public JSONthroughHTTP ()
	{
	}
	*/
	
	/**
	@param host
	@param username
	@param password
	  
	*/
	public JSONthroughHTTP (String host, String username, String password)
	{
		this.host = host;
		int idx = host.indexOf(':');
		if (idx > -1) {
			this.host = host.substring(0, idx);
			this.portHTTP = this.portHTTPS = new java.lang.Integer(
						host.substring(idx+1, host.length()) ).intValue();
		}
		this.username = username;
		this.password = password;
		conStat = new ConnectionState();
	}
	
	
	
	protected String makeRpcPath( OpsiMethodCall omc )
	{
		StringBuffer result =  new StringBuffer ("/rpc");
		if (omc.getRpcPath() != null
			&& !(omc.getRpcPath().equals(""))
		)
		{
			result.append("/");
			result.append(omc.getRpcPath());
		}
		
		return result.toString();
	}

	/**
	 This method takes hostname, port and the OpsiMethodCall rpcPatht transformed to String in order to build an URL 
	 as expected by the opsiconfd.
	 <p>
	 The HTTPS subclass overwrites the method to modify "http" to "https".
	 @param omc
	 
	*/
	protected String produceBaseURL(String rpcPath)
	{
		return "http://" + host + ":" +  portHTTP + rpcPath;
	}
	
	private void appendGETParameter(String urlS, String json)
	{
		if (requestMethod == GET)
		{
			try
			{
				String urlEnc = URLEncoder.encode (json, "UTF8");
				//logging.debug(this, "a JSONObject as URL encoded>> "  + urlEnc);
				
				urlS += "?" + urlEnc;
			}
			catch ( UnsupportedEncodingException ux)
			{
				logging.error(this, ux.toString()); 
			}
		}
		
		logging.debug(this, "we shall try to connect to " + urlS);
		try
		{
			serviceURL = new URL (urlS);
		}
		catch (java.net.MalformedURLException ex)
		{
			logging.error (urlS + " no legal URL, " + ex.toString());
		}
	}
	
	/**
	* @param omc 
	*/    
	public void makeURL( OpsiMethodCall omc )
	{
		logging.debug(this, "make url for " + omc);
		
		String urlS = produceBaseURL(makeRpcPath(omc));
		String json = omc.getJsonString();
		appendGETParameter(urlS, json);
	}
	
	
	protected String produceJSONstring( List<OpsiMethodCall> omcList )
	{
		StringBuffer json = new StringBuffer("[");
		for (int i = 0; i < omcList.size(); i++)
		{
			json.append(omcList.get(i).getJsonString());
			if (i < omcList.size()-1)
				json.append(",");
		}
		json.append("]");
		return json.toString();
	}
	
	/**
	* @param omcList 
	*/ 
	public void makeURL( List<OpsiMethodCall> omcList )
	{
		logging.debug(this, "make url for " + omcList);
		if (omcList == null || omcList.size() == 0)
		{
			logging.error("missing method call");
			return;
		}
		
		String rpcPath0 = makeRpcPath(omcList.get(0));
		
		for (OpsiMethodCall omc : omcList)
		{
			String rpcPath = makeRpcPath(omc);
			if (!rpcPath.equals(rpcPath0))
			{
				logging.error("no common RPC path:  " + rpcPath0 + " cf. " + omcList.get(0));
				return;
			}
		}
			
		String urlS = produceBaseURL(rpcPath0);
		String json = produceJSONstring(omcList);
		appendGETParameter(urlS, json);
	}
	
	
	/**
	* Opening the connection. The method is prepared to be subclassed and take additional 
	* informations for the connection.  
	*/
	protected HttpURLConnection produceConnection () throws java.io.IOException
	{
		return (HttpURLConnection) serviceURL.openConnection ( );
	}
	
	private void setGeneralRequestProperties(HttpURLConnection connection)
	{
		String authorization = Base64OutputStream.encode (username + ":" + password);
		connection.setRequestProperty ("Authorization", "Basic " + authorization);
		
		if (gzipTransmission)
			connection.setRequestProperty("Accept-Encoding", "gzip");
		
		connection.setRequestProperty("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		connection.setRequestProperty("Connection", "close");
		
	}
	
	/* Lazarus solution for compatibility with old and new server version:
			
		definiere mir die Parameter für compress und nocompress
		und belege sie mit den Werten vor welche rfc conform sind:
		-------------------------------
		var
		  ContentTypeCompress : string = 'application/json';
		  ContentTypeNoCompress : string = 'application/json';
		  ContentEncodingCommpress : string = 'deflate';
		  ContentEncodingNoCommpress : string = '';
		  AcceptCompress : string = '';
		  AcceptNoCompress : string = '';
		  AcceptEncodingCompress : string = 'deflate';
		  AcceptEncodingNoCompress : string = '';
		
		-------------------------------
		sollte es bei retrieveJSONObject (und verwandten) zu einer expetion kommen,
		so switche ich um:
		-------------------------------
		 // retry with other parameters
					if ContentTypeCompress = 'application/json' then
					begin
					  ContentTypeCompress := 'gzip-application/json-rpc';
					  AcceptCompress := 'gzip-application/json-rpc';
					  ContentTypeNoCompress := 'application/json-rpc';
					  AcceptNoCompress := 'application/json-rpc';
					  ContentEncodingNoCommpress := '';
					  ContentEncodingCommpress := '';
					  AcceptEncodingCompress := '';
					  AcceptEncodingNoCompress := '';
					end
					else
					begin
					  ContentTypeCompress := 'application/json';
					  AcceptCompress := '';
					  ContentTypeNoCompress := 'application/json';
					  AcceptNoCompress := '';
					  ContentEncodingNoCommpress := '';
					  ContentEncodingCommpress := 'deflate';
					  AcceptEncodingCompress := 'deflate';
					  AcceptEncodingNoCompress := '';
					end;
		
					LogDatei.log('Changing to MimeType: ' + ContentTypeCompress, LLDebug);
		
			
	*/
	
	
	public JSONObject retrieveJSONObject (  OpsiMethodCall omc )
	{
		ArrayList<OpsiMethodCall> omcList = new  ArrayList<OpsiMethodCall>();
		omcList.add(omc);
		List<JSONObject> responseList = retrieveJSONObjects(omcList);
		
		
		if (responseList == null)
			return null;
		
		return responseList.get(0);
	}
	
	
	//synchronized 
	public List<JSONObject> retrieveJSONObjects(List<OpsiMethodCall> omcList)
	/** 
	 This method receives the JSONObject via HTTP.
	  @param omc
	  
	*/
	{
		logging.info(this, "retrieveJSONObjects started");
		de.uib.utilities.thread.WaitCursor waitCursor = null;
		
		if (omcList.size() > 0 && !omcList.get(0).isBackground())
		{
			waitCursor 
			= new de.uib.utilities.thread.WaitCursor( null, new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR), this.getClass().getName() );
		}
			
		ArrayList<JSONObject>result = null;
		
		conStat = new ConnectionState (ConnectionState.STARTED_CONNECTING);
		
		//omcList.add(new OpsiMethodCall("authenticated", new Object[]{}));
		
		makeURL( omcList );
		
		//logging.check(this, "retrieveJSONObject  " + omcList + " FROM " + serviceURL);
		TimeCheck timeCheck = new TimeCheck(this, "retrieveJSONObject  FROM " + serviceURL 
		+ " \n" + omcList);
		timeCheck.start();
		
		HttpURLConnection connection = null;
		try
		{
			connection = produceConnection();
			//the underlying network connection can be shared, 
			//only disconnect() may close the underlying socket
			//System.out.println (" retrieving 1 " + conStat);
			
			
			
			setGeneralRequestProperties(connection);
			
			//String authorization = Base64OutputStream.encode (username + ":" + password);
			//connection.setRequestProperty ("Authorization", "Basic " + authorization);
			//connection.setRequestProperty("Accept-Encoding", "gzip");
			if (requestMethod == POST)
			{
				connection.setRequestMethod("POST");
				connection.setDoOutput(true); 
				connection.setDoInput(true);
				connection.setUseCaches(false);
			}
			else
			{
				connection.setRequestMethod("GET");
			}
			
			/*
			//it would be an option:
			// TLSv1.2 which is default since java 8 does sometimes not work with opsiconfd for java8 
			if (configed.javaVersion.startsWith("1.8"))
			{
				logging.warning(this,"set TLSv1 in java 8");
				System.setProperty("https.protocols", "TLSv1");
			}
			*/
			
			
			logging.info(this, "https protocols given by system " + configed.systemSSLversion);
		
			if(sessionId != null)
			{
				connection.setRequestProperty("Cookie", sessionId);
				// System.out.println ("Session id sent: " + sessionId);
			}
			//connection.setRequestProperty("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
			logging.info(this, "retrieveJSONObjects request old or " + " new session ");
			logging.info(this, "retrieveJSONObjects connected " + " new session ");
		
			connection.connect();
			
			if (connection instanceof HttpsURLConnection)
				logging.info(this, "connection cipher suite " + ((HttpsURLConnection)connection).getCipherSuite());
			
			if (requestMethod == POST)
			{
				BufferedWriter out = null;
				
				try
				{
					OutputStreamWriter writer = new OutputStreamWriter(
							connection.getOutputStream(),
							UTF8DEFAULT
							);
					out = new BufferedWriter( writer );
					String json = produceJSONstring(omcList);
					logging.debug (this, "(POST) sending: " + json);
					out.write(json);
					out.flush();
					out.close();
					out = null;
				}
				catch(IOException iox)
				{
					logging.info(this, "exception on writing json request " + iox);
				}
				finally
				{
					logging.debug(this, "handling finally json request close,  out == null " + (out == null) );
					if (out != null)
						try { out.close(); } 
						catch (IOException iox) 
						{
							logging.debug(this, "handling finally json request close " + iox);
						};
				}
						
			}
			
			/** 
			pausing for testing purposes 
			
			try
			{
			Thread.currentThread().sleep(10000);
			}
			catch (InterruptedException iex)
			{
			}
			System.out.println (" retrieving 2 " + conStat);
			*/
			
			
		}
		/*
		catch (java.net.NoRouteToHostException ex)
		{
			if (waitCursor != null) waitCursor.stop();
			WaitCursor.stopAll();
			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString()); 
			logging.error("no route to host, URL: "  + serviceURL + " message " + ex.toString());
			return null;
		}
		*/
		
		catch (IOException ex)
		{
			if (waitCursor != null) waitCursor.stop();
			WaitCursor.stopAll();
			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString()); 
			logging.error("Exception on connecting, " + ex.toString());
			return null;
		}
		
		
		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) // we continue
		{    
			try
			{
				logging.debug(this, 
				"Response " + connection.getResponseCode() + " " + connection.getResponseMessage());
				
				StringBuffer errorInfo = new StringBuffer("");
					
				if (connection.getErrorStream() != null)
				{
					BufferedReader in = null;
					try
					{
						in = new BufferedReader (new InputStreamReader (connection.getErrorStream(), UTF8DEFAULT));
						while (in.ready())
						{		
							errorInfo.append(in.readLine());
							errorInfo.append("  ");
						}
						in.close();
					}
					
					catch(IOException iox)
					{
						logging.warning(this, "exception on reading error stream " + iox);
						throw new JSONCommunicationException("error on reading error stream");
					}
					finally
					{
						logging.info(this, "handling finally reading error stream");
						if (in != null)
							try { in.close(); } catch (IOException iox) {
								logging.info(this, "handling finally reading error stream " + iox);
							};
					}
				}
					
				logging.debug(this, "response code: " + connection.getResponseCode());
				
				if (  connection.getResponseCode() == connection.HTTP_ACCEPTED 
					|| connection.getResponseCode() == connection.HTTP_OK)
				{
					conStat = new ConnectionState (ConnectionState.CONNECTED, "ok");
				}
				else
				{
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
					if (connection.getResponseCode() != connection.HTTP_UNAUTHORIZED) // this case is handled by the login routine
					  logging.error(this,  	"Response " + connection.getResponseCode() + " " +   connection.getResponseMessage() + " " + errorInfo.toString()); 
					
				}
				
				if (conStat.getState() == ConnectionState.CONNECTED)
				{    
					// Retrieve session ID from response.
					String cookieVal = connection.getHeaderField("Set-Cookie");
					
					if(cookieVal != null)
					{
						lastSessionId = sessionId;
						sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));
						//System.out.println( "Session id received:" +  sessionId);
						
						boolean gotNewSession = 
							sessionId != null && !sessionId.equals(lastSessionId);
						
						if (gotNewSession)
							logging.info(this, "retrieveJSONObjects " + " got new session ");
						
						//	"lastSessionId, sessionId: "
						//	+ lastSessionId +", " + sessionId);
						
					}
					
					boolean gzipped = false;
					boolean deflated = false;
					
					
					if (connection.getHeaderField("Content-Encoding") != null)
					{
						gzipped = connection.getHeaderField("Content-Encoding").equalsIgnoreCase("gzip");
						logging.debug(this, "gzipped " + gzipped);
						deflated = connection.getHeaderField("Content-Encoding").equalsIgnoreCase("deflate");
						logging.debug(this, "deflated " + deflated);
					}
					
					
					InputStream stream = null;
					logging.debug(this, "initiating input stream");
					if (gzipped || deflated)
					{
						if (deflated ||connection.getHeaderField("Content-Type").startsWith("gzip-application"))
						{
							//not valid gzipt, we take inflater
							logging.debug(this, "initiating InflaterInputStream");
							InputStream str = connection.getInputStream();
							stream = new java.util.zip.InflaterInputStream(str);
						}
						
						else 
						{
							logging.debug(this, "initiating GZIPInputStream");
							stream = new GZIPInputStream(connection.getInputStream()); //not working, if no GZIP
						}
							
					}
					else
					{
						logging.debug(this, "initiating plain input stream");
						stream = connection.getInputStream();
					}
					
					
					BufferedReader in = null; 
					String line;
					int readCounter = 0;
					try
					{
						in = new BufferedReader(new InputStreamReader  (stream, UTF8DEFAULT), 1000);
						
						// reading in one line
						in = new BufferedReader (new InputStreamReader  (stream, UTF8DEFAULT));
						line = in.readLine();
						
						// reading in chunks for testing purposes
						
						/*
						StringBuffer buf = new StringBuffer();
						
						char[] cBuf = new char[1000]; 
						int maxRead = 999;
						
						int readResult = in.read(cBuf, 0, maxRead);
						while (readResult > 0)
						{
							buf.append(cBuf, 0, readResult);
							StringBuffer buf1 = new StringBuffer();
							buf1.append(cBuf, 0 , readResult);
							logging.info(this, "temporarily read count  " + readResult);
							readCounter = readCounter + readResult;
							logging.info(this, "temporarily read count (total)  " + readResult + " ( " + readCounter  + " ) " );
							logging.info(this, "temporarily read " + buf1);
							readResult = in.read(cBuf, 0, maxRead);
						}
						line = buf.toString();
						*/
							
					
						logging.info(this, "received line of length " + line.length());
						if (line != null)
						{
							result = new ArrayList<JSONObject>();
							if (omcList.size() > 1)
							{
								
								JSONArray combinedResult = new JSONArray (new JSONTokener(line));
								
								for (int i = 0; i < combinedResult.length(); i++)
								{
									result.add((JSONObject) combinedResult.optJSONObject(i));
								}
							}
							else
								result.add(new JSONObject(line));
						}
						
						line = in.readLine();
						// System.out.println (line);
						if (line != null)
							logging.debug(this, "received second line of length " + line.length());
						
						
					}
					
					catch(IOException iox)
					{
						logging.info(this, "exception on receiving json " + iox);
						logging.logTrace(iox);
						throw new JSONCommunicationException("receiving json");
					}
					finally
					{
						logging.debug(this, "handling finally receiving json close");
						if (in != null)
						{
							try { in.close(); } 
							catch (IOException iox) 
							{
								logging.info(this, "handling finally receiving json close " + iox);
							}
						}
					}
							
					
					
					//return result;
					
				}
			}
			
			catch (Exception ex)
			{
				if (waitCursor != null) waitCursor.stop();
				WaitCursor.stopAll();
				logging.error(this,  "Exception while data reading, " + ex.toString());
			}
		}
		//logging.check(this, "retrieveJSONObject  got result " + (result != null));
		
		timeCheck.stop("retrieveJSONObject  got result " + (result != null) + " ");
		logging.info(this, "retrieveJSONObject ready");
		if (waitCursor != null) waitCursor.stop();
		return result;
	}
  
	
	public static void main (String[] args)
	{
		String resulting = "";
		JSONthroughHTTP instance;
		
		instance = new JSONthroughHTTP ("194.31.185.160",  "cn=admin,dc=uib,dc=local", "umwelt");
		instance.retrieveJSONObject ( new  OpsiMethodCall ("getProductStates_listOfHashes", new String[]{"pcbon1.uib.local"} ) );
	}
}

