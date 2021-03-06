/*
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddUser(Ticketmaster esql){//1
		try{
			System.out.printf("Email: ");
			String email = in.readLine();
			System.out.printf("First Name: ");
			String fname = in.readLine();
			System.out.printf("Last Name: ");
			String lname = in.readLine();
			System.out.printf("Phone #: ");	
			String phone = in.readLine();
			System.out.printf("Password: ");
			String pwd = in.readLine();
			esql.executeUpdate(String.format("INSERT INTO users VALUES ('%s', '%s', '%s', '%s', '%s');", email, lname, fname, phone,"7AEE99C0E48BC90FEF4C030DD7D3A867195966D452E699F97777157"));
			System.out.println("User Added Successfully");
    		}
    		catch( Exception e) {
      			e.printStackTrace();
    		}
  	}
	
	public static void AddBooking(Ticketmaster esql){//2
		try{
			List<List<String>> result = esql.executeQueryAndReturnResult("SELECT MAX(bid) FROM Bookings");
			int bid = Integer.parseInt(result.get(0).get(0))+1;
			boolean flag = true;
			String status;
			String bdatetime;
			int sid;
			int seats=0;
			String email=null;
			System.out.println("Show ID: ");
			sid = Integer.parseInt(in.readLine());		
			//System.out.println("Status: ");
			status = "Pending";
			System.out.println("DateTime: ");
			bdatetime = in.readLine();
			//Booking
			while(flag) {
				System.out.println("# Seats: ");
				seats = Integer.parseInt(in.readLine());
				result = esql.executeQueryAndReturnResult(String.format("SELECT seats FROM Bookings WHERE sid = %d",sid));
				int count=0;
				for(int i =0; i<result.size(); i++) {
					count = count+Integer.parseInt(result.get(i).get(0));
				}
				count = count+seats;			
				result = esql.executeQueryAndReturnResult(String.format("SELECT T.tseats FROM Theaters T, Plays P  WHERE P.sid = %d AND P.tid = T.tid", sid));
				if(count < Integer.parseInt(result.get(0).get(0))) {
					flag = false;
				} 
				if(flag == true) {
					System.out.println("Not enough seats available");
				}
			}
			flag = true;
			while(flag) {
				System.out.println("Email: ");
				email = in.readLine();
				System.out.println(email);
				if(esql.executeQuery(String.format("SELECT fname FROM Users WHERE email = '%s'",email))>0) {
					flag = false;
				} else {
					System.out.println("Invalid email address");
				}
			}
			esql.executeUpdate(String.format("INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES ('%d', '%s', '%s', '%d', '%d', '%s')", bid, status, bdatetime, seats, sid, email));
			//ShowSeats
			int cost=0;			
			System.out.println(String.format("Available seats for Show %d are: ",sid));
			String tid = esql.executeQueryAndReturnResult(String.format("SELECT tid FROM Plays Where sid = '%d'", sid)).get(0).get(0);
			result = esql.executeQueryAndReturnResult(String.format("SELECT C.sno FROM CinemaSeats C, ShowSeats S WHERE C.tid = '%s' AND C.csid = S.csid AND S.bid IS NULL",tid));
			System.out.println(result);
			if(result.size() == 0) {
				return;
			}
			int i =0;
			do {
				String select;
				String selection;
				System.out.println("Which Seat would you like: ");
				i++;
				select = in.readLine();
				selection = esql.executeQueryAndReturnResult(String.format("SELECT S.csid FROM CinemaSeats C, ShowSeats S WHERE C.sno = '%s' AND C.tid = '%s' AND C.csid = S.csid",select, tid)).get(0).get(0);
				System.out.println(selection);
				esql.executeUpdate(String.format("UPDATE ShowSeats SET bid = '%d' WHERE csid = '%s'", bid, selection));
				cost = cost+Integer.parseInt(esql.executeQueryAndReturnResult(String.format("SELECT price FROM ShowSeats WHERE csid = '%s'",selection)).get(0).get(0));
			} while(i<seats);
			System.out.println(String.format("Total cost is: %d",cost));
			//System.out.println(result.get(0));
			//Payment
			result = esql.executeQueryAndReturnResult("SELECT MAX(pid) FROM Payments");
			int pid = Integer.parseInt(result.get(0).get(0))+1;
			System.out.println("Payment Method: ");
			String method = in.readLine();
			System.out.println("Would you like to pay?(yes/no)");
			String response = in.readLine();
			
			int amount = cost;
			int min = 10000000;
			int max = 99999999;
			int trid = (int)Math.random() * (max - min + 1) + min;
			//Write to db
			if(response.equals("yes")) {
				esql.executeUpdate(String.format("UPDATE Bookings SET status = 'Paid' WHERE bid = '%d'",bid));
				esql.executeUpdate(String.format("INSERT INTO Payments (pid, bid, pmethod, pdatetime, amount, trid) VALUES ('%d', '%d', '%s', '%s', '%d', '%d')", pid, bid, method, bdatetime, amount, trid));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		try{
			int newMvid = Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(mvid) FROM movies").get(0).get(0))+ 1;
      			String mvid = Integer.toString(newMvid);
      			System.out.println("==========Movie Information==========");
      			System.out.printf("Title: ");
			String title = in.readLine();
			System.out.printf("Release Date (YYYY-MM-DD): ");
			String rdate = in.readLine();
			System.out.printf("Country: ");
			String country = in.readLine();
			System.out.printf("Description: ");	
			String description = in.readLine();
			System.out.printf("Duration (in seconds): ");
			String duration = in.readLine();
			System.out.printf("Language (eg: EN, DE): ");
			String lang = in.readLine();
			System.out.printf("Genre: ");
			String genre = in.readLine();
      			esql.executeUpdate(String.format("INSERT INTO movies VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');", mvid, title, rdate, country, description, duration, lang, genre));
			int newSid = Integer.parseInt(esql.executeQueryAndReturnResult("SELECT MAX(sid) FROM shows").get(0).get(0))+ 1;
      			String sid = Integer.toString(newSid);
      			System.out.println("==========Show  Information==========");
			System.out.printf("Date (YYYY-MM-DD): ");
			String sdate = in.readLine();
			System.out.printf("Start Time (HH:MM:SS): ");
			String sttime = in.readLine();
			System.out.printf("End Time (HH:MM:SS): ");
			String edtime = in.readLine();
      			esql.executeUpdate(String.format("INSERT INTO shows VALUES ('%s', '%s', '%s', '%s', '%s');", sid, mvid, sdate, sttime, edtime)); 
      			System.out.println("==========Play  Information==========");
      			System.out.printf("Theater ID: ");
      			String tid = in.readLine(); 
      			esql.executeUpdate(String.format("INSERT INTO plays VALUES ('%s', '%s');", sid, tid));
	    		System.out.println("Added Movie Showing to Theater Successfully");
    		}
    		catch( Exception e) {
      			e.printStackTrace();
    		}
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		try{
			esql.executeUpdate("UPDATE showseats ss SET bid = NULL From bookings b WHERE b.bid = ss.bid;");
			esql.executeUpdate("UPDATE Bookings SET status = 'Cancelled' WHERE status = 'Pending'");		
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		try{
			System.out.println("Enter your booking ID: ");
			String bid = in.readLine();
			String sid = esql.executeQueryAndReturnResult(String.format("SELECT sid FROM Bookings WHERE bid = '%s'",bid)).get(0).get(0);
			System.out.println("Current Seat(s) for this booking are: ");
			List<List<String>> result = esql.executeQueryAndReturnResult(String.format("SELECT csid FROM ShowSeats WHERE bid = '%s'", bid));
			//System.out.println(result);
			for(int i =0; i<result.size(); i++) {
				System.out.println(esql.executeQueryAndReturnResult(String.format("SELECT sno FROM CinemaSeats WHERE csid = '%s'",result.get(i).get(0))).get(0).get(0));
				System.out.println("Price: ");
				String price = esql.executeQueryAndReturnResult(String.format("SELECT price FROM ShowSeats WHERE csid = '%s'",result.get(i).get(0))).get(0).get(0);
				System.out.println(price);
				System.out.println("Available Exchanges: ");
				List<List<String>> posible = esql.executeQueryAndReturnResult(String.format("SELECT csid FROM ShowSeats WHERE price = '%s' AND sid = '%s' AND bid IS NULL", price, sid));
				for(int j=0; j<posible.size(); j++) {
					System.out.println(esql.executeQueryAndReturnResult(String.format("SELECT sno FROM CinemaSeats WHERE csid = '%s'", posible.get(i).get(0))));
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		try{
			System.out.println("Enter booking ID: ");
			String bid = in.readLine();
			
			esql.executeUpdate(String.format("UPDATE ShowSeats SET bid = '' WHERE bid = '%s'", bid));
			esql.executeUpdate(String.format("UPDATE Bookings SET status = 'Cancelled' WHERE bid = '%s'",bid));
			esql.executeUpdate(String.format("DELETE FROM Payments WHERE bid = '%s'",bid));
		}
		catch(Exception e) {
			e.printStackTrace();
		}		
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		try{
			esql.executeUpdate("DELETE FROM Bookings WHERE status = 'Cancelled'");
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		try{
			System.out.println("Which Cinema ID: ");
			String cid = in.readLine();
			System.out.println("What Date(#/#/##): ");
			String date = in.readLine();
			List<List<String>> result = esql.executeQueryAndReturnResult(String.format("SELECT S.sid FROM Shows S, Theaters T, Plays P WHERE S.sid = P.sid AND P.tid = T.tid AND T.cid = '%s' AND S.sdate = '%s'",cid, date));
			System.out.println(result);
			for(int i=0; i<result.size(); i++) {
				esql.executeUpdate(String.format("DELETE FROM Plays WHERE sid = '%s'", result.get(i).get(0)));
				esql.executeUpdate(String.format("DELETE FROM ShowSeats WHERE sid = '%s'", result.get(i).get(0)));
				esql.executeUpdate(String.format("DELETE FROM Shows WHERE sid = '%s'", result.get(i).get(0)));
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		try {
			System.out.println("Which show would you like to search?");
			String sid = in.readLine();
			List<List<String>> result = esql.executeQueryAndReturnResult(String.format("SELECT T.tname FROM Theaters T, Plays P Where P.sid = '%s' AND T.tid = P.tid",sid));
			System.out.println(result);			
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		try{
			System.out.printf("Date (YYYY-MM-DD): ");
			String sdate = in.readLine();
			System.out.printf("Start Time (HH:MM:SS): ");
			String sttime = in.readLine();
			esql.executeQueryAndPrintResult(String.format("SELECT * FROM shows where sdate = '%s' and sttime = '%s';", sdate, sttime));
		}
		catch(Exception e){
			e.printStackTrace();
		}	
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		try{
			esql.executeQueryAndPrintResult("SELECT * FROM movies WHERE LOWER(title) LIKE '%love%' and rdate >=  '2011-01-01';");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		try{
			esql.executeQueryAndPrintResult("SELECT u.* FROM bookings b, users u WHERE b.status = 'Pending' and u.email = b.email;");
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		try{

			System.out.printf("Movie ID: ");
			String mid = in.readLine();
			System.out.printf("Cinema ID: ");
			String cid = in.readLine();	
			System.out.printf("Start Date (YYYY-MM-DD): ");
			String startDate = in.readLine();
			System.out.printf("End Date (YYYY-MM-DD): ");
			String endDate = in.readLine();	
			esql.executeQueryAndPrintResult(String.format("SELECT m.title, m.duration, s.sdate, s.sttime FROM movies m, shows s, cinemas c WHERE c.cid = '%s' and m.mvid = '%s' and s.sdate BETWEEN '%s' and '%s';", cid, mid, startDate, endDate));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		try{
			System.out.printf("Email: ");
			String email = in.readLine();
			esql.executeQueryAndPrintResult(String.format("SELECT m.title, s.sdate, s.sttime, t.tname, cs.sno FROM bookings b, movies m, shows s, theaters t, cinemaseats cs, plays p WHERE b.email = '%s' and b.sid = s.sid and m.mvid = s.mvid and p.sid = s.sid and p.tid = t.tid and cs.tid = t.tid;", email));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
