package wkmb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

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
public class CatalogsProductsGuessServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";

	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	private static final String POPULAR_PRODUCTS = "/products/discover/popular";
	private static final String USER_CURR = "users/current";
	private static final double  SUPPORT_TRESHOLD = 0.4;
	private static final double  CONFIDENCE_TRESHOLD = 0.2;
	
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
	    String game_chosen = req.getParameter("game_chosen");
	    
	    int game_id = Integer.parseInt(game_chosen);
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService(); 	
      	String products = "";
      	Filter eq_filter =
    		  new FilterPredicate("GameId",
    		                      FilterOperator.EQUAL,
    		                      game_id);
      	Query q1 = new Query("GameProducts").setFilter(eq_filter);
      	PreparedQuery pq1 = datastore.prepare(q1);        
      	for (Entity result1 : pq1.asIterable()) {
      		products = products + result1.getProperty("ProductId") + ",";
      	}
      	
      	
      	String requestURL2 = null;
        try {
			requestURL2 = PLATFORM_URL + "/" + USER_CURR + "?token=" + sessionToken +
			           "&hash=" + getHash();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        URL url2 = new URL(requestURL2);
        URLConnection conn2 = url2.openConnection();
        BufferedReader rd2 = new BufferedReader(new InputStreamReader(conn2.getInputStream()));
        String user = rd2.readLine();
        int receiver_id = 0;
       
        JSONObject json = null;
        try {
			 json = new JSONObject(user);
		} catch (JSONException e) {
			receiver_id = -1;
			//e.printStackTrace();
		}
        try {
        	receiver_id = json.getInt("id");
		} catch (JSONException e) {
			receiver_id = -2;
			//e.printStackTrace();
		}
        
        HashSet<Integer> products_chosen = new HashSet<Integer>();
      	Filter eq_filter1 =
    		  new FilterPredicate("SenderId",
    		                      FilterOperator.EQUAL,
    		                      receiver_id);
      	Filter alg_filter =
      		  CompositeFilterOperator.and(eq_filter1, eq_filter1);
      	Query q2 = new Query("FinishedGames").setFilter(alg_filter);
      	PreparedQuery pq2 = datastore.prepare(q2);        
      	for (Entity result2 : pq2.asIterable()) {
      		int prod = Integer.parseInt(result2.getProperty("ProductId")+"");
      		if (products_chosen.contains(prod)== false){
      			products_chosen.add(prod);
      		}
      	}      	
      	
      	HashSet<Integer> a_products = new HashSet<Integer>();
      	if (products_chosen.size() >= 5 ){
      		Random rand = new Random();
            int e;
            HashSet<Integer> randomNumbers = new HashSet<Integer>();

            while (randomNumbers.size() < 5){
                e = rand.nextInt(products_chosen.size());
                if (randomNumbers.contains(e)== false){
                	randomNumbers.add(e);
          		}    
            }
     
            Iterator<Integer> iterator = products_chosen.iterator();
            
            int k = 0;
            while(iterator.hasNext()){
            	if (randomNumbers.contains(k)== true){
            		a_products.add(iterator.next());
            	}
            	else{
            		String  not_used = iterator.next() + "";
            	}	
            	k = k+1;
            }
    	}
      	
      	int count = 0;
      	String algorithm_products = "";
      	Filter eq_filter3 =
      		  new FilterPredicate("confidence",
      		                      FilterOperator.GREATER_THAN,
      		                      CONFIDENCE_TRESHOLD);
      	Query q3 = new Query("Rules").setFilter(eq_filter3);
      	PreparedQuery pq3 = datastore.prepare(q3);        
      	for (Entity result3 : pq3.asIterable()) {
      		if (count < 5) {
      			if (a_products.contains(Integer.parseInt(result3.getProperty("Product1")+""))== true){
      				if (Double.parseDouble(result3.getProperty("support")+"")>SUPPORT_TRESHOLD){
      					algorithm_products = algorithm_products + result3.getProperty("Product2") + ",";
              			count = count + 1;
      				}
          		}
      		}
      	}
      	int items = 7 - count;
      	if (items > 0 ){
      		String requestURL1 = null;
            try {
    			requestURL1 = PLATFORM_URL + "/" + POPULAR_PRODUCTS + "?token=" + sessionToken +
    			           "&hash=" + getHash() +  "&timePeriod" + "Day" + "&maxItems=" + items + "";
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
            
            URL url1 = new URL(requestURL1);
            URLConnection conn1 = url1.openConnection();
            BufferedReader rd1 = new BufferedReader(new InputStreamReader(conn1.getInputStream()));
            String result = rd1.readLine();
            
          	resp.getWriter().println(products + result.substring(1, result.length()-1));
      	}
      	else{
      		resp.getWriter().println(products + algorithm_products.substring(1, algorithm_products.length()-1));
      	}
      
      	
	}
}