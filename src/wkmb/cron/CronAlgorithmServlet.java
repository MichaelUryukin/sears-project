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
public class CronAlgorithmServlet extends HttpServlet {
	
	private static final Logger _logger = Logger.getLogger(CronAlgorithmServlet.class.getName());
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		
		try {
			_logger.info("Cron Job has been executed");

			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

			Query q1 = new Query("FinishedGames");
			PreparedQuery pq1 = datastore.prepare(q1);
			Query q11 = new Query("ProductNum");
			PreparedQuery pq11 = datastore.prepare(q11);

			//make table P(a)
			int size = 0;
			int flag = 0;    
			String productID1 = "";
			String productID2 = "";
			String Counter = "";

			for (Entity result1 : pq1.asIterable()) {
				size++;
				productID1 = result1.getProperty("ProductId")+"";
				int p_id1 = Integer.parseInt(productID1);
				for  (Entity result2 : pq11.asIterable()) {
					productID2 = result2.getProperty("ProductId")+"";
					int p_id2 = Integer.parseInt(productID2);
					if (p_id1 == p_id2){
						Counter = result2.getProperty("Counter")+"";
						int new_count = Integer.parseInt(Counter);
						result2.setProperty("Counter", new_count+1);
						datastore.put(result2);
						flag = 1;
					}
				}
				if (flag == 0){
					Entity ProductNum = new Entity("ProductNum");
					ProductNum.setProperty("ProductId", result1.getProperty("ProductId"));
					ProductNum.setProperty("Counter", 1);
					datastore.put(ProductNum);
				}
				flag =0;
			}
			size = size/5;

			//make table with all the Couples
			Query q2 = new Query("FinishedGames");
			PreparedQuery pq2 = datastore.prepare(q2);
			for (Entity result1 : pq1.asIterable()) {
				for  (Entity result2 : pq2.asIterable()) {
					Entity Couples = new Entity("Couples");
					if (result1.getProperty("GameId").equals(result2.getProperty("GameId")) 
							&& !(result1.getProperty("ProductId").equals(result2.getProperty("ProductId")))){
						Couples.setProperty("Product1", result1.getProperty("ProductId"));
						Couples.setProperty("Product2", result2.getProperty("ProductId"));
						datastore.put(Couples);
					}    		
				} 		
			}
			
			//make table with all the Couples and the number of times they appear together
			Query q3 = new Query("Couples");
			PreparedQuery pq3 = datastore.prepare(q3);
			Query q4 = new Query("Couples");
			PreparedQuery pq4 = datastore.prepare(q4);
			Object ID1 = null;
			Object ID2 = null;
			
			int count = 0;       
			for (Entity result1 : pq3.asIterable()) {
				Entity CountCouples = new Entity("CountCouples");
				ID1 = result1.getProperty("Product1");
				ID2 = result1.getProperty("Product2");
				for  (Entity result2 : pq4.asIterable()) {
					if (ID1.equals(result2.getProperty("Product1")) 
							&& (ID2.equals(result2.getProperty("Product2")))){
						count = count+1;         			
						datastore.delete(result2.getKey());
					}
				}
				if (count != 0){
					CountCouples.setProperty("Product1", ID1);
					CountCouples.setProperty("Product2", ID2);
					CountCouples.setProperty("Counter", count);
					datastore.put(CountCouples);
					datastore.delete(result1.getKey());
					count=0;
				}
			}

			String count1 = "";
			String count2 = "";

			//Support and Confidence
			Query q5 = new Query("CountCouples");
			PreparedQuery pq5 = datastore.prepare(q5);
			Query q6 = new Query("ProductNum");
			PreparedQuery pq6 = datastore.prepare(q6);
			double  conf =0;
			double sup = 0;
			for (Entity result1 : pq6.asIterable()){
				for (Entity result2 : pq5.asIterable()){
					productID1 = result1.getProperty("ProductId")+"";
					productID2 = result2.getProperty("Product1")+"";
					int id1 = Integer.parseInt(productID1);
					int id2 = Integer.parseInt(productID2);
					if(id1 == id2) {
						count1 = result2.getProperty("Counter")+"";
						count2 = result1.getProperty("Counter")+"";
						double num1 = Double.parseDouble(count1); 
						double num2 = Double.parseDouble(count2); 
						conf = (double)(num1/num2);
						sup = (double)(num1/size);
						Entity Rules = new Entity("Rules");
						Rules.setProperty("Product1", result2.getProperty("Product1"));
						Rules.setProperty("Product2", result2.getProperty("Product2"));
						Rules.setProperty("support", sup);
						Rules.setProperty("confidence", conf);
						datastore.put(Rules);
					}
				}
			}
			Query q7 = new Query("CountCouples");
			PreparedQuery pq7 = datastore.prepare(q7);
			for (Entity result1 : pq7.asIterable()){
				datastore.delete(result1.getKey());
			}
			
		}
		catch (Exception ex) {
	
			//Log any exceptions in your Cron Job
		}
	}
 
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
	}
}