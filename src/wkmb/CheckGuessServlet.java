package wkmb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

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
public class CheckGuessServlet extends HttpServlet {
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
	    String game_chosen = req.getParameter("game_chosen");
	    String products_chosen = req.getParameter("products_chosen");	    
	    int game_id = Integer.parseInt(game_chosen);
	    
        
	    String real_products = "";
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Filter eq_filter =
        		  new FilterPredicate("GameId",FilterOperator.EQUAL,game_id);
        Query q = new Query("GameProducts").setFilter(eq_filter);
        PreparedQuery pq = datastore.prepare(q);        
        for (Entity result1 : pq.asIterable()) {
        	real_products = real_products + result1.getProperty("ProductId") + ",";
        }
        
        real_products = real_products.substring(0, real_products.length()-1);
        products_chosen = products_chosen.substring(0, products_chosen.length()-1);
        String[] chosen = products_chosen.split(",");
        String[] real = real_products.split(","); 
        
        String wrong = "";
       // String wrong_guess = "";
        String right = "";
    	int b;
		/*for ( int i = 0; i < real.length; i++) {
			b = 0;
			for ( int j = 0; j < chosen.length; j++) {
				if (chosen[j].equals(real[i])){
					right = right + real[i] + ",";
					b = 1;
					}
				}
			if (b==0){
				wrong = wrong + real[i] + ",";
				}
			}
		*/
		for ( int i = 0; i < chosen.length; i++) {
			b = 0;
			for ( int j = 0; j < real.length; j++) {
				if (chosen[i].equals(real[j])){
					right = right + chosen[i] + ",";
					b = 1;
					}
				}
			if (b==0){
				wrong = wrong + chosen[i] + ",";
				}
			}
		
		
		String SenderId ="";
		Filter eq_filter1 =
      		  new FilterPredicate("GameId",FilterOperator.EQUAL,game_id);
		Query q1 = new Query("GameSender").setFilter(eq_filter1);
		PreparedQuery pq1 = datastore.prepare(q1);  
        for (Entity result1 : pq1.asIterable()) {
        	SenderId = result1.getProperty("SenderId") + "";
        }
        
        int sender_id = Integer.parseInt(SenderId);
        
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
        int receiver_id = 0;
       
        JSONObject json = null;
        try {
			 json = new JSONObject(user);
		} catch (JSONException e) {
			receiver_id = -1;
		}
        try {
        	receiver_id = json.getInt("id");
		} catch (JSONException e) {
			receiver_id = -2;
		}
                
        Set<Integer> s;  
        int y=0;
        s = new TreeSet<Integer>();
        String k="";
        String right_set = "";
        if (right.length()>0){
        	right_set = right.substring(0, right.length()-1);
        }
        for(int i=0;i<(right_set.length());i++){
            
            if(right_set.charAt(i) != ','){
                 k+=right_set.charAt(i);
            }
            else{
                 y = Integer.parseInt(k);
                 s.add(y);
                 k="";
            } 
        }
        if (k.length()>0){
        	y = Integer.parseInt(k);
            s.add(y);
            k="";
        }
        int a=0;
        int guessed_right=0;
        Iterator<Integer> iterator = s.iterator();
        while(iterator.hasNext()){
        	guessed_right = guessed_right +1;
        	a=iterator.next();
        }
        int score = guessed_right*20;
        Iterator<Integer> iterator2 = s.iterator();
        while(iterator2.hasNext()){
            Entity finished_game = new Entity("FinishedGames");
            finished_game.setProperty("GameId", game_id);
            finished_game.setProperty("SenderId", sender_id);
            finished_game.setProperty("ReceiverId", receiver_id);
            finished_game.setProperty("ProductId", iterator2.next());
            finished_game.setProperty("GuessedRight", 1);
            finished_game.setProperty("Score", score);
            datastore.put(finished_game);
        }
        
        Set<Integer> s1;  
        int y1=0;
        s1 = new TreeSet<Integer>();
        String k1="";
        String wrong_set = "";
        if (wrong.length()>0){
        	wrong_set = wrong.substring(0, wrong.length()-1);
        }
        for(int i=0;i<(wrong_set.length());i++){
            
            if(wrong_set.charAt(i) != ','){
                 k1+=wrong_set.charAt(i);
            }
            else{
                 y1 = Integer.parseInt(k1);
                 s1.add(y1);
                 k1="";
            } 
        }
        if (k1.length()>0){
        	y1 = Integer.parseInt(k1);
            s1.add(y1);
            k1="";
        }
        
        Iterator<Integer> iterator1 = s1.iterator();
        while(iterator1.hasNext()){
            Entity finished_game = new Entity("FinishedGames");
            finished_game.setProperty("GameId", game_id);
            finished_game.setProperty("SenderId", sender_id);
            finished_game.setProperty("ReceiverId", receiver_id);
            finished_game.setProperty("ProductId", iterator1.next());
            finished_game.setProperty("GuessedRight", 0);
            finished_game.setProperty("Score", score);
            datastore.put(finished_game);
        }
        
        Filter eq_filter2 =
        		  new FilterPredicate("GameId",FilterOperator.EQUAL,game_id);
        Filter eq_filter3 =
      		  new FilterPredicate("ReceiverId",FilterOperator.EQUAL,receiver_id);
        Filter game_receiver =
        		  CompositeFilterOperator.and(eq_filter2, eq_filter3);
  		Query q2 = new Query("GameReceivers").setFilter
  				(game_receiver);
  		PreparedQuery pq2 = datastore.prepare(q2);  
          for (Entity result2 : pq2.asIterable()) {
        	  datastore.delete(result2.getKey());
        }
        
        int num_of_entities = 0;
        Filter eq_filter4 =
        		  new FilterPredicate("GameId",FilterOperator.EQUAL,game_id);
  		Query q4 = new Query("GameReceivers").setFilter(eq_filter4);
  		PreparedQuery pq4 = datastore.prepare(q4);  
          for (Entity result4 : pq4.asIterable()) {
        	  num_of_entities++;
        }
        
        
        if (num_of_entities ==0){
        	Filter eq_filter5 =
          		  new FilterPredicate("GameId",FilterOperator.EQUAL,game_id);
    		Query q5 = new Query("GameSender").setFilter(eq_filter5);
    		PreparedQuery pq5 = datastore.prepare(q5);  
            for (Entity result5 : pq5.asIterable()) {
          	  datastore.delete(result5.getKey());
            	}
            Query q6 = new Query("GameProducts").setFilter(eq_filter5);
    		PreparedQuery pq6 = datastore.prepare(q6);  
            for (Entity result6 : pq6.asIterable()) {
          	  datastore.delete(result6.getKey());
            	}
            Query q7 = new Query("SentGames").setFilter(eq_filter5);
    		PreparedQuery pq7 = datastore.prepare(q7);  
            for (Entity result7 : pq7.asIterable()) {
          	  datastore.delete(result7.getKey());
            	}
        }
		String reciver = "";  
        String score1 = "";
        //int flag = 0;
        Query q3 = new Query("Player");
        PreparedQuery pq3 = datastore.prepare(q3);
        
        for (Entity result1 : pq3.asIterable()) {
        	reciver = result1.getProperty("PlayerId")+"";
        	int r_id = Integer.parseInt(reciver);
        	if (r_id == receiver_id){
        		//flag = 1;
        		score1 = result1.getProperty("score")+"";
        		int new_score = Integer.parseInt(score1) + score;
        			result1.setProperty("score", new_score);
        			datastore.put(result1);
        		}
        	}
        	/*if (flag == 0){
        		Entity Player = new Entity("Player");
        		Score.setProperty("PlayerId", receiver_id);
        		Score.setProperty("score", score);
        		datastore.put(Score);
        	}
        	flag =0;*/
		
        resp.getWriter().println(right + "|||" + wrong);
	}
}