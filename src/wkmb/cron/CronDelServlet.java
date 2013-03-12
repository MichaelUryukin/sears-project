package wkmb.cron;
 
import java.io.IOException;
import java.util.logging.Logger;
 
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
 
@SuppressWarnings("serial")
public class CronDelServlet extends HttpServlet {
private static final Logger _logger = Logger.getLogger(CronDelServlet.class.getName());
public void doGet(HttpServletRequest req, HttpServletResponse resp)
throws IOException {
 
try {
_logger.info("Cron Job has been executed");
DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
Query q1 = new Query("GameSender");
Query q2 = new Query("SentGames");
PreparedQuery pq1 = datastore.prepare(q1);  
PreparedQuery pq2 = datastore.prepare(q2); 
for (Entity result1 : pq1.asIterable()) {
	boolean exists = false;
	for (Entity result2 : pq2.asIterable()) {
		  if (result1.getProperty("GameId").equals(result2.getProperty("GameId"))){
			  exists = true;
		  }
	} 
	if (exists==false) {
		datastore.delete(result1.getKey());
	}
}

Query q3 = new Query("GameReceivers");
PreparedQuery pq3 = datastore.prepare(q3);  
for (Entity result3 : pq3.asIterable()) {
	boolean exists = false;
	for (Entity result2 : pq2.asIterable()) {
		  if (result3.getProperty("GameId").equals(result2.getProperty("GameId"))){
			  exists = true;
		  }
	} 
	if (exists==false) {
		datastore.delete(result3.getKey());
	}
}

Query q4 = new Query("GameProducts");
PreparedQuery pq4 = datastore.prepare(q4);  
for (Entity result4 : pq4.asIterable()) {
	boolean exists = false;
	for (Entity result2 : pq2.asIterable()) {
		  if (result4.getProperty("GameId").equals(result2.getProperty("GameId"))){
			  exists = true;
		  }
	} 
	if (exists==false) {
		datastore.delete(result4.getKey());
	}
}
}
catch (Exception ex) {
//Log any exceptions in your Cron Job
}
}
 
@Override
public void doPost(HttpServletRequest req, HttpServletResponse resp)
throws ServletException, IOException {
doGet(req, resp);
}
}