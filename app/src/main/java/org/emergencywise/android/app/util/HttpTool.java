package org.emergencywise.android.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class HttpTool
{
	private static final String TAG = HttpTool.class.getSimpleName();
	//private static ThreadSafeClientConnManager cm;
	private static DefaultHttpClient client;
	//private static DefaultHttpClient clientGzip;
	private static final String USER_AGENT = "EmergencyWise/1.0";
	
	public static boolean isOnline( Context context )
	{
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ni = cm.getActiveNetworkInfo();
	    if( ni == null ) return false;
	    return ni.isConnectedOrConnecting();
	}

	public static DefaultHttpClient getClient()
	{
		return getClient(USER_AGENT, true);
	}

	public static DefaultHttpClient getClient( final boolean isGzip )
	{
		return getClient(USER_AGENT, isGzip);
	}

	public static DefaultHttpClient getClient( final String userAgent,
			final boolean isGzip )
	{
		if( client == null ) 
		{
			client = new DefaultHttpClient();
			//client.getConnectionManager().getSchemeRegistry().register( new Scheme("bluetooth",
			//		BluetoothSchemeSocketFactory.getInstance(), 1234) );
		}
		
		return client;
		
		/*
		if (client != null && !isGzip)
		{
			return client;
		}
		else if (isGzip && clientGzip != null)
		{
			return clientGzip;
		}

		final HttpParams params = new BasicHttpParams();
		if (cm == null)
		{
			ConnManagerParams.setMaxTotalConnections(params, 20);
			ConnManagerParams.setTimeout(params, 55000);
			HttpConnectionParams.setSoTimeout(params, 60000);
			HttpConnectionParams.setConnectionTimeout(params, 50000);
			HttpConnectionParams.setTcpNoDelay(params, true);
			HttpConnectionParams.setLinger(params, 0);
			HttpConnectionParams.setSocketBufferSize(params, 1024);
			HttpConnectionParams.setStaleCheckingEnabled(params, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

			if (userAgent != null)
				HttpProtocolParams.setUserAgent(params, userAgent);

			final SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", PlainSocketFactory
					.getSocketFactory(), 443));
			schemeRegistry.register(new Scheme("bluetooth",
					BluetoothSchemeSocketFactory.getInstance(), 1234));

			cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		}

		HttpClientParams.setRedirecting(params, true);
		final DefaultHttpClient result = new DefaultHttpClient(cm, params);

		if (isGzip)
		{
			clientGzip = result;
			result.addRequestInterceptor(new HttpRequestInterceptor()
			{
				@Override
				public void process( final HttpRequest request,
						final HttpContext context ) throws HttpException,
						IOException
				{
					if (!request.containsHeader("Accept-Encoding"))
						request.addHeader("Accept-Encoding", "gzip");
				}
			});

			result.addResponseInterceptor(new HttpResponseInterceptor()
			{

				@Override
				public void process( final HttpResponse response,
						final HttpContext context ) throws HttpException,
						IOException
				{
					final HttpEntity entity = response.getEntity();
					final Header ceheader = entity.getContentEncoding();
					if (ceheader != null)
					{
						final HeaderElement[] codecs = ceheader.getElements();
						for (final HeaderElement codec : codecs)
							if (codec.getName().equalsIgnoreCase("gzip"))
							{
								// Dbg.debug("using GZIP");
								response.setEntity(new GzipDecompressingEntity(
										response.getEntity()));
								return;
							}
					}
				}

			});
		}
		else
			client = result;

		result.removeRequestInterceptorByClass(RequestAddCookies.class);
		result.removeResponseInterceptorByClass(ResponseProcessCookies.class);
		result.setReuseStrategy(new ConnectionReuseStrategy()
		{
			@Override
			public boolean keepAlive( final HttpResponse resp,
					final HttpContext ctx )
			{
				return true;
			}
		});
		result.setKeepAliveStrategy(new ConnectionKeepAliveStrategy()
		{
			@Override
			public long getKeepAliveDuration( final HttpResponse arg0,
					final HttpContext arg1 )
			{
				return 5;
			}
		});

		return result;
		*/
	}

	static class GzipDecompressingEntity
		extends HttpEntityWrapper
	{
		public GzipDecompressingEntity(final HttpEntity entity)
		{
			super(entity);
		}

		@Override
		public InputStream getContent() throws IOException,
				IllegalStateException
		{
			// the wrapped entity's getContent() decides about repeatability
			final InputStream wrappedin = this.wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		@Override
		public long getContentLength()
		{
			// length of ungzipped content is not known
			return -1;
		}
	}

	public static List<String> getLocalIPAddress()
	{
		List<String> result = new ArrayList<String>();
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						result.add(inetAddress.getHostAddress());
					}
				}
			}
		}
		catch (SocketException ex)
		{
		    Log.e(TAG, ex.toString());
		}

		return result;
	}
}