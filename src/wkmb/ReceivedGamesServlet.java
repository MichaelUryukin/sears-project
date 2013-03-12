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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.common.primitives.Bytes;




@SuppressWarnings("serial")
public class ReceivedGamesServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	
	
	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	
	private static final String USER_CURR = "users/current";
	
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
			//e.printStackTrace();
		}
        try {
			user_id = json.getInt("id");
		} catch (JSONException e) {
			user_id = -2;
			//e.printStackTrace();
		}
        
        String games="";
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query q = new Query("SentGames");
        PreparedQuery pq = datastore.prepare(q);        
        for (Entity result : pq.asIterable()) {
        	String game = result.getProperty("GameId") + "";
        	int game_id = Integer.parseInt(game);
        	Filter eq_filter =
            		  new FilterPredicate("ReceiverId",
            		                      FilterOperator.EQUAL,
            		                      user_id);
        	Filter eq_filter1 =
          		  new FilterPredicate("GameId",
          		                      FilterOperator.EQUAL,
          		                      game_id);
        	Filter game_receiver =
          		  CompositeFilterOperator.and(eq_filter, eq_filter1);
            Query q1 = new Query("GameReceivers").setFilter(game_receiver);
            PreparedQuery pq1 = datastore.prepare(q1);        
            for (Entity result1 : pq1.asIterable()) {
            	games = games + result1.getProperty("GameId") + ",";
            }
        }
        if (games.length()>0) {
        	resp.getWriter().println(games);
        }
	}
}
