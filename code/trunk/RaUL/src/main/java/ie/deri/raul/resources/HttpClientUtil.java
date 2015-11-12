package ie.deri.raul.resources;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

public class HttpClientUtil {
	
	public static String getRemoteContent(String url) throws Exception{
		HttpClient client=new HttpClient();
		HttpMethod method = new GetMethod("http://www.armin-haller.com/foaf.rdf");
		try {
			int responseCode=client.executeMethod(method);
			if (responseCode != HttpStatus.SC_OK) {
		        throw new Exception("Method failed: " + method.getStatusLine());
		    }
			byte[] responseBody = method.getResponseBody();
			String remoteConetent=new String(responseBody);
			method.releaseConnection();
			return remoteConetent;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
}
