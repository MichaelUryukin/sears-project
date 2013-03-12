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
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;
import com.google.common.primitives.Bytes;




@SuppressWarnings("serial")
public class GameReceiversServlet extends HttpServlet {
	private String sessionToken = null;
	private String appSecret = "d822e4afe26d4a13906d6f101c5f7910";
	
	
	private static final String PLATFORM_URL = "http://galileoplatform.shopyourway.com";
	private static final String USER_CURR = "users/current";
	private static final String FOLLOWED_BY = "users/followed-by";
	private static final String FOLLOWERS = "users/followers";
	
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
	    String type = req.getParameter("type");
	    sessionToken = req.getParameter("sessionToken");
	    String gameId = req.getParameter("gameId");
	    int game_id = Integer.parseInt(gameId);
	    if (type.equals("a")) {
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
	        
	      //start of new additions
	        Set<Integer> s;  
	        int y=0;
	        s = new TreeSet<Integer>();
	        String k="";
	        
	        
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
	        
	        
	        
	        for(int i=1;i<(result.length()-1);i++){
	            
	            if(result.charAt(i) != ','){
	                 k+=result.charAt(i);
	            }
	            else{
	                 y = Integer.parseInt(k);
	                 s.add(y);
	                 k="";
	            } 
	        }
	        y = Integer.parseInt(k);
	        s.add(y);
	        k="";
	        
	        
	        String requestURL3 = null;
	        try {
				requestURL3 = PLATFORM_URL + "/" + FOLLOWERS + "?userId=" + user_id +
						"&token=" + sessionToken + "&hash=" + getHash();
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
	        URL url3 = new URL(requestURL3);
	        URLConnection conn3 = url3.openConnection();
	        BufferedReader rd3 = new BufferedReader(new InputStreamReader(conn3.getInputStream()));
	        String result2 = rd3.readLine();
	        
	        
	        
	        for(int i=1;i<(result2.length()-1);i++){  //the first and the last char are [ and ]
	            
	            if(result2.charAt(i) != ','){
	                 k+=result2.charAt(i);
	            }
	            else{
	                 y = Integer.parseInt(k);
	                 s.add(y);
	                 k="";
	            } 
	        }
	        
	        y = Integer.parseInt(k);
	        s.add(y);
	        k="";
	        //now set contains all the ids of the followers and the followed by
	        
	        int num_to_send = 5;
	        if (s.size()<num_to_send) {
	        	num_to_send = s.size();
	        }
	          
	        // add picking from set an subset
	        
	        Iterator<Integer> iterator = s.iterator();
	        String ids = "";
	        int person ;
	        int i=0;
	        while(iterator.hasNext()){
	        	person = iterator.next();
	        	if (i<=num_to_send){
	        		Entity game_receiver = new Entity("GameReceivers");
		            game_receiver.setProperty("GameId", game_id);
		            ids = ids + person + ",";
		            game_receiver.setProperty("ReceiverId",person);
		        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		            datastore.put(game_receiver);
	        	} 
	        	i=i+1;
	        }
	        
	        String phrase = ids.substring(0, ids.length()-1);
	        
			String requestURL4 = null;
			try {
				requestURL4 = PLATFORM_URL + "/users/get" + "?token=" 
						+ sessionToken + "&hash=" + getHash() + "&ids=" + phrase;			
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	        URL url4 = new URL(requestURL4);
	        URLConnection conn4 = url4.openConnection();
	        BufferedReader rd4 = new BufferedReader(new InputStreamReader(conn4.getInputStream()));
	        String get_result = rd4.readLine();
	        resp.getWriter().println(get_result); 
	    }
	    
	    
	    
	    
	    if (type.equals("m")){
	    	String ids = req.getParameter("ids");
	    	Set<Integer> s;  
	        int y=0;
	        s = new TreeSet<Integer>();
	        String k="";
	        
	        
	        for(int i=0;i<(ids.length()-1);i++){
	            
	            if(ids.charAt(i) != ','){
	                 k+=ids.charAt(i);
	            }
	            else{
	                 y = Integer.parseInt(k);
	                 s.add(y);
	                 k="";
	            } 
	        }
	        y = Integer.parseInt(k);
	        s.add(y);
	        k="";
	        
	        
	        Iterator<Integer> iterator = s.iterator();

	        while(iterator.hasNext()){
	            Entity game_receiver = new Entity("GameReceivers");
	            game_receiver.setProperty("GameId", game_id);
	            game_receiver.setProperty("ReceiverId", iterator.next());
	        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	            datastore.put(game_receiver);
	        }
	        
	       //resp.getWriter().println("done");

	    }
	}
}