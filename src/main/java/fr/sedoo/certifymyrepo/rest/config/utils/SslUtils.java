package fr.sedoo.certifymyrepo.rest.config.utils;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SslUtils {

	public static  SSLContext getSslContext() {
		SSLContext sc = null;
		try {
		sc = SSLContext.getInstance("SSL");
		sc.init(null, getTrustManagers(), new java.security.SecureRandom());
		
	    HttpsURLConnection.setDefaultHostnameVerifier(getHostnameVerifier());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		}
		catch (Exception e) {
		}
		return sc;
	}


	public static TrustManager[] getTrustManagers() { 
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		return trustAllCerts;
	}


	public static HostnameVerifier getHostnameVerifier() {
		HostnameVerifier allHostsValid = new HostnameVerifier() {
	        public boolean verify(String hostname, SSLSession session) {
	            return true;
	        }
	    };
	    return allHostsValid;
	}

}
