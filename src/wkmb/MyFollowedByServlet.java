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

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.common.primitives.Bytes;


@SuppressWarnings("serial")
public class MyFollowedByServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	
	
	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	private static final String USER_CURR = "users/current";
	private static final String FOLLOWED_BY = "users/followed-by";
	
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
	    
	    String requestURL1 = null;
        try {
			requestURL1 = PLATFORM_URL + "/" + USER_CURR + "?token=" + sessionToken +
			           "&hash=" + getHash();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        URL url1 = new URL(requestURL1);
        URLConnection conn1 = url1.openConnection();
        BufferedReader rd1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
        String user = rd1.readLine();
        int user_id = 0;
       
        JSONObject json = null;
        try {
			 json = new JSONObject(user);
		} catch (JSONException e) {
			user_id = -1;
		}
        try {
			user_id = json.getInt("id");
		} catch (JSONException e) {
			user_id = -2;
		}
		
        String requestURL2 = null;
        try {
			requestURL2 = PLATFORM_URL + "/" + FOLLOWED_BY + "?userId=" + user_id +
					"&token=" + sessionToken + "&hash=" + getHash();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        URL url2 = new URL(requestURL2);
        URLConnection conn2 = url2.openConnection();
        BufferedReader rd2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
        String result = rd2.readLine();
        
        String phrase = result.substring(1, result.length()-1);
        
		String requestURL3 = null;
		try {
			requestURL3 = PLATFORM_URL + "/users/get" + "?token=" 
					+ sessionToken + "&hash=" + getHash() + "&ids=" + phrase;			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
        URL url3 = new URL(requestURL3);
        URLConnection conn3 = url3.openConnection();
        BufferedReader rd3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
        String get_result = rd3.readLine();
        
        try {
			JSONArray friends = new JSONArray(get_result);
			for (int i=0; i < friends.length()-1; i++){
				JSONObject j1 = friends.getJSONObject(i);
				JSONObject j2 = friends.getJSONObject(i+1);
				String name1 = j1.getString("name")+"";
				String name2 = j2.getString("name")+"";
				if (name1.compareTo(name2)>0){
					friends.put(i,j2);
					friends.put(i+1,j1);
				}
			}
	        //String final_result = friends.toString();
	        resp.getWriter().println(friends);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
	}
		
}





