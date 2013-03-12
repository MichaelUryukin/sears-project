package wkmb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;




@SuppressWarnings("serial")
public class GameProductsServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    resp.setContentType("text/plain");
	    String products_chosen = req.getParameter("products_chosen");
	    String gameId = req.getParameter("gameId");
	    
	     	Set<Integer> s;  
	        int y=0;
	        s = new TreeSet<Integer>();
	        String k="";
	        products_chosen = products_chosen.substring(0, products_chosen.length()-1);
	        for(int i=0;i<(products_chosen.length());i++){
	            
	            if(products_chosen.charAt(i) != ','){
	                 k+=products_chosen.charAt(i);
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
	        int game_id = Integer.parseInt(gameId);
	        while(iterator.hasNext()){
	            Entity game_receiver = new Entity("GameProducts");
	            game_receiver.setProperty("GameId", game_id);
	            game_receiver.setProperty("ProductId", iterator.next());
	        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	            datastore.put(game_receiver);
	        }
	        resp.getWriter().println("done");
	}
}