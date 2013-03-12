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




@SuppressWarnings("serial")
public class ProductsServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	
	
	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	private static final String PRODUCTS = "products/get";
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
	    resp.setContentType("text/plain");
	    sessionToken = req.getParameter("sessionToken");
	    String prod_string = req.getParameter("prod_string");
	    if (prod_string.length()>0)	  {
	    	String requestURL1 = null;
	        try {
				requestURL1 = PLATFORM_URL + "/" + PRODUCTS + "?token=" + sessionToken +
				           "&hash=" + getHash() +  "&ids=" + prod_string.substring(0, prod_string.length()-1);
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        URL url1 = new URL(requestURL1);
	        URLConnection conn1 = url1.openConnection();
	        BufferedReader rd1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
	        String result = rd1.readLine();
	        
	        resp.getWriter().println(result);
	    }
	    else {
	    	resp.getWriter().println("");
	    }
	    
        
	}
}