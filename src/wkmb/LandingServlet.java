package wkmb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;

import com.google.common.primitives.Bytes;

//import com.google.appengine.datanucleus.EntityUtils;

@SuppressWarnings("serial")
public class LandingServlet extends HttpServlet {
	   // private static final Logger logger = Logger.getLogger(WkmbServlet.class);
		private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	    private String sessionToken;
		
	    private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	    private static final String USER_STATE = "auth/user-state";  
  
	    
	    public static byte[] reverse(byte[] array) {
	        if (array == null) {
	            return array;
	        }
	        int i = 0;
	        int j = array.length - 1;
	        byte tmp;
	        while (j > i) {
	            tmp = array[j];
	            array[j] = array[i];
	            array[i] = tmp;
	            j--;
	            i++;
	        }
	        return array;
	    }
	    

	    
	    private String getHash() throws Exception {
	        MessageDigest md = MessageDigest.getInstance("SHA-256");
	        md.reset();
	        ArrayList<Byte> temp = new ArrayList<Byte>();
	        temp.addAll(Bytes.asList(sessionToken.getBytes("UTF-8")));
	        temp.addAll(Bytes.asList(appSecret.getBytes("UTF-8")));
	        return Hex.encodeHexString(md.digest(Bytes.toArray(temp)));
	   }
	 
	    
	    public void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws IOException {
		  	resp.setContentType("text/html");
			String[] tokens = req.getQueryString().split("=");		
			sessionToken= tokens[1];
			String requestURL = null;
	        try {
				requestURL = PLATFORM_URL + "/" + USER_STATE + "?token=" + sessionToken +
				           "&hash=" + getHash();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        URL url = new URL(requestURL);
	        URLConnection conn = url.openConnection();
	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String auth = rd.readLine();
	        rd.close();
	         if (auth.equals("0")) {
	        	resp.sendRedirect("landing.html");
	        }
	        if (auth.equals("1")) {
	        	resp.getWriter().println(
		        		   "<script>" + 
		        		   "window.top.location.href = 'http://galileo.shopyourway.com/secured/app/1450/install'" +
		        		   "" +
		        		   "</script>"
		        		);
	        }
	        if (auth.equals("2")) {
	        	resp.getWriter().println(
		        		   "<script>" + 
		        		   "window.top.location.href = 'http://galileo.shopyourway.com/app/1450/post-login'" +
		        		   "</script>"
		        		);
	        }
	        
		}
}











