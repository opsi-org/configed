package de.uib.opsicommand;

import de.uib.utilities.logging.*;
import java.util.*;
import org.json.*;
import java.io.*;

public class OpsiMethodCall 
{
	private  HashMap theCall; 
	private String methodname;
	
	private Object[] parameters;
	public final static boolean BACKGROUND = true;
	private boolean background;
	
	protected final static int defaultJsonId = 1;
	
	public static String extendRpcPath = "extend/configed";
	private String rpcPath = "";// extendRpcPath;
	
	public final static Vector<String>collectedCalls = new Vector<String>();
	public static int maxCollectSize = -1;
	
	
	/*
	public OpsiMethodCall(String methodname,  Object[] parameters)
	{
		this(standardRpcPath, methodname, parameters);
	}
		
	public OpsiMethodCall (String methodname,  Object[] parameters, boolean background)
	{
		this(standardRpcPath, methodname, parameters, background);
	}
	*/
	
		
	/**
	*	@param rpcPath  subpath for the rpc call (not including "/rpc/")
	*  @param methodName name of rpc method
	*  @param parameters the parameters for the method
	*  @param background if background then no waiting info is shown
	*/
	public OpsiMethodCall (String methodname,  Object[] parameters, boolean background)
	{
		this.methodname = methodname;
		this.parameters = parameters;
		theCall = new HashMap();
		theCall.put ("method", methodname);
		theCall.put ("params", parameters);
		theCall.put("rpcpath", rpcPath);
		this.background = background;
		collectCall();
	}
	
	/**
	*	@param rpcPath  subpath for the rpc call (not including "/rpc/")
	*  @param methodName name of rpc method
	*  @param parameters the parameters for the method
	*/
	public OpsiMethodCall (String methodname,  Object[] parameters)
	{
		this.methodname = methodname;
		this.parameters = parameters;
		theCall = new HashMap();
		theCall.put ("method", methodname);
		theCall.put ("params", parameters);
		theCall.put("rpcpath", rpcPath);
		this.background = false;
		collectCall();
		
	}
	
	private void collectCall()
	{
		if (
			(maxCollectSize < 0) 
			// -1 means deactivated
			||
			(maxCollectSize != 0 && collectedCalls.size() >= maxCollectSize)
			// 0 means infinite
			)
			return;
		
		collectedCalls.add(this.getMethodname() + "\n\t" + this.getParameter());
	}
		
	
	public static void report()
	{
		logging.check("==========   collected calls");
		
		for (String c : collectedCalls)
		{
			logging.check(c);
		}
		logging.check("==========");
	}
	

	
	public String getRpcPath()
	{
		return rpcPath;
	}
	
	
	public OpsiMethodCall activateExtendedRpcPath()
	{
		logging.info(this, "activateExtendedRpcPath");
		rpcPath = extendRpcPath;
		return this;
	}
	
	
	
	public String getMethodname ()
	{
		return methodname;  
	}
	
	public String getParameter()
	{
		return Arrays.toString(parameters);
	}
	
	public boolean isBackground()
	{
		return background;
	}
	/*
	private String resolveToString(Object element)
	{
		if (element == null)
			return null;
		else if (element instanceof Object[])
			return Arrays.toString( (Object[]) element);
		else
			return element.toString();
	}
	*/
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer("{");
		sb.append("rpcpath=");
		sb.append(rpcPath);
		sb.append(", ");
		sb.append("method=");
		sb.append(methodname);
		sb.append(", ");
		sb.append("params=");
		sb.append("[");
		if (parameters != null && parameters.length > 0)
		{
			for (int i = 0; i < parameters.length; i++)
			{
				Object paramI = parameters[i];
				
				if ( paramI instanceof Object[] )
				{
					sb.append(Arrays.toString( (Object[]) paramI));
				}
				
				else if ( paramI instanceof java.util.Map )
				{
					sb.append("{");
					 
					for (Object key : ((java.util.Map) paramI).keySet())
					{
						sb.append("" + key + ": ");
						if (((java.util.Map) paramI).get(key) instanceof Object[])
						{
							sb.append( Arrays.toString(  (Object[]) ((java.util.Map) paramI).get(key)) );
						}
						else
						{
							sb.append( "" + ((java.util.Map) paramI).get(key) );
						}
						sb.append(" ");
					}
					sb.append("}");
				}
				
				else 
					sb.append("" + paramI);
				
			}
			
			/*
			sb.append( resolveToString(parameters[0] ) ) ;
			for (int i = 1; i < parameters.length; i++)
			{
				sb.append(",");
				sb.append( resolveToString( parameters[i] ) );
			}
			*/
			
		}
		sb.append("]");
		sb.append("}");
		return sb.toString();  
	}
	
	public String getCommandLineString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append (methodname);
		sb.append("(");
		if (parameters.length>0)
		{     
			sb.append (parameters[0].toString());
			for (int i = 1;   i  < parameters.length; i ++)
			{
				sb.append  ("&");
				/*String p = parameters[i].toString();
				if (p.length() > 0 &&  p.charAt(0) = '[')
				{
					sb.append('[');
					sb.append('parameters[i].toString());
				}
				*/
				
				sb.append (parameters[i].toString());
			}
		}
		sb.append (")");
		
		return sb.toString();
	}
	
	public String getJsonString()
	{
		String result = "";
		try
		{
			JSONObject jO = new JSONObject();
			
			JSONArray joParams = new JSONArray();
			
			/*Vector v = new Vector();
			for (int i = 0; i<parameters.length; i++)
			{ v.add (parameters[i]);
			}
			*/
			for (int i = 0; i<parameters.length; i++)
			{
				if ( parameters[i] instanceof Object[] )
				{	
					Object[] obs = (Object[]) parameters[i];
					JSONArray arr = new JSONArray();
					for (int j=0; j < obs.length; j++)
						arr.put ( obs[j] );
					
					joParams.put ( arr );
				}
				
				else if ( parameters[i] instanceof Map)
				{
					JSONObject job = new JSONObject( (Map) parameters[i] );
					joParams.put ( job );
					
				}
				
				else
				{
					joParams.put ( parameters[i] );
				}
				
			}
			
			jO.put("id",  defaultJsonId);
			jO.put("method", methodname);
			jO.put("params", joParams);
			result = jO.toString();
			//logging.debug(this, "a JSONObject  as String>> "  + result);
		}
		catch (org.json.JSONException jex)
		{
			logging.error(this, "Exception while producing a JSONObject, " + jex.toString());
		}
		
		return result;
	}
}


