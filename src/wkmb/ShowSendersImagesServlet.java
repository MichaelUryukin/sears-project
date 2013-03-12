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
import com.google.common.primitives.Bytes;




@SuppressWarnings("serial")
public class ShowSendersImagesServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	
	
	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	
	
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
	    String gameId = req.getParameter("Game");
	    
	    long game_id = Integer.parseInt(gameId);
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    String receiver = "";
	    Filter eq_filter1 =
        		  new FilterPredicate("GameId",
        		                      FilterOperator.EQUAL,
        		                      game_id);
      	Query q1 = new Query("FinishedGames").setFilter(eq_filter1);
      	PreparedQuery pq1 = datastore.prepare(q1);        
      	for (Entity result1 : pq1.asIterable()) {
      		receiver =result1.getProperty("ReceiverId") + "";
      	}
      	long receiver_id = Integer.parseInt(receiver);
      	String score = "";
	    Filter eq_filter2 =
        		  new FilterPredicate("GameId",
        		                      FilterOperator.EQUAL,
        		                      game_id);
	    Filter eq_filter3 =
      		  new FilterPredicate("ReceiverId",
      		                      FilterOperator.EQUAL,
      		                    receiver_id);
	    Filter eq_filter4 =
      		  CompositeFilterOperator.and(eq_filter2, eq_filter3);
      	Query q2 = new Query("FinishedGames").setFilter(eq_filter4);
      	PreparedQuery pq2 = datastore.prepare(q2);        
      	for (Entity result2 : pq2.asIterable()) {
      		score =result2.getProperty("Score") + "";
      	}
      	
      	String requestURL3 = null;
		try {
			requestURL3 = PLATFORM_URL + "/users/get" + "?token=" 
					+ sessionToken + "&hash=" + getHash() + "&ids=" + receiver;			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	
        URL url3 = new URL(requestURL3);
        URLConnection conn3 = url3.openConnection();
        BufferedReader rd3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
        String result = rd3.readLine();
        
        resp.getWriter().println(result + "|||" + gameId + "|||" + score);
      	
	}
}
