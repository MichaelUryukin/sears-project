package wkmb;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;




@SuppressWarnings("serial")
public class SendGameServlet extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
	    resp.setContentType("text/plain");
	    String gameId = req.getParameter("gameId");
	    int game_id = Integer.parseInt(gameId);
	    Date dt = new Date();
	    Entity game_send = new Entity("SentGames");
	    game_send.setProperty("GameId", game_id);
	    game_send.setProperty("SendDate", dt);
	    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	    datastore.put(game_send);      
	    resp.getWriter().println("donemjkjh");
	}
}