package biggodo.com;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;



public class CmsRegister implements Runnable {
		static final String STATUS_OK = "Ok";
		public static byte[] sBuffer = new byte[512];
		private Biggodo fCallback = null;
		private String url = "";
		
		public CmsRegister(Biggodo _callback){
			fCallback = _callback;
		}

		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public void run() {
			
			System.out.println("Connecting : " + url);
			String ret = "";
			OutputStream out = null;
			InputStream is = null;
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost(url);
			try {
				
				HttpResponse response = client.execute(request);

				// Check if server response is valid
				StatusLine status = response.getStatusLine();
				if (status.getStatusCode() != 200) {
					ret="Invalid response from server: "+ status.toString();
					
				}else {
				// Pull content stream from response
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();

				ByteArrayOutputStream content = new ByteArrayOutputStream();

				// Read response into a buffered stream
				int readBytes = 0;
				while ((readBytes = inputStream.read(sBuffer)) != -1) {
					content.write(sBuffer, 0, readBytes);
				}

				// Return result from buffered stream
				ret = new String(content.toByteArray());
				System.out.println("Got back: " + ret);
				}
			} catch (IOException e) {
				ret = "Network Error!";
				e.printStackTrace();
				
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}

			}

			
			fCallback.gotResponse(ret);

		}
}
