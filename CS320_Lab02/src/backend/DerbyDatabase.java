package backend;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

//import gameSqldemo.SQLDemo.RowList;
import backend.DBUtil;

public class DerbyDatabase implements IDatabase { /// most of the gamePersist package taken from Lab06 ----CITING
	static {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load Derby driver");
		}
	}
	
	//decleration
	
	static class RowList extends ArrayList<List<String>> {
		private static final long serialVersionUID = 1L;
	}
	
	private static final String PAD =
			"                                                    " +
			"                                                    " +
			"                                                    " +
			"                                                    ";
		private static final String SEP =
			"----------------------------------------------------" +
			"----------------------------------------------------" +
			"----------------------------------------------------" +
			"----------------------------------------------------";
	
	private interface Transaction<ResultType> {
		public ResultType execute(Connection conn) throws SQLException;
	}

	private static final int MAX_ATTEMPTS = 10;
	
///////////////////////////////////////////////////////////////////////////////////
///////////////////// REGISTER ACCOUNT////////////////////////////////////
/////////////////////////////////////////////////////////////////////////
	public boolean registerAccount(String userName, String pass, String email, String name, String gender, String age, String location) throws SQLException {

		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmt2 = null;
		ResultSet resultSet = null;
		

		conn = DriverManager.getConnection("jdbc:derby:belres.db;create=true");

		try {
			// retreive username attribute from login
			stmt = conn.prepareStatement("select userName " // user attribute
					+ "  from account " // from account table
					+ "  where userName = ?"

			);

			// substitute the title entered by the user for the placeholder in
			// the query
			stmt.setString(1, userName);

			// execute the query
			resultSet = stmt.executeQuery();

			if (!resultSet.next()) { /// if username doesnt exist

				stmt2 = conn.prepareStatement( // enter username
						"insert into account(userName, password, email, name, gender, age, location)" + "values(?, ?, ?, ?, ?, ?, ?)");

				stmt2.setString(1, userName);
				stmt2.setString(2, pass);
				stmt2.setString(3, email);
				stmt2.setString(4, name);
				stmt2.setString(5, gender);
				stmt2.setString(6, age);
				stmt2.setString(7, location);

				stmt2.execute();

				//int accountID = getAccountID(userName);


				return true;

			} else {
				return false; // username already exists
			}


		} finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(stmt2);	
			DBUtil.closeQuietly(conn);
		}
	}

	public int getAccountID(String username) throws SQLException{
		int id = -1;
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;

        // retreive username attribute from login
        stmt = conn.prepareStatement("login_id " // user attribute
                + "  from account " // from account table
                + "  where userName = ?"

        );

        resultSet = stmt.executeQuery();

        if (!resultSet.next()) {
            id = resultSet.getRow();
        }

		return id;
	}
	public boolean accountExist(String username, String password){ ///checks if account exists
		//Checks if the user exist and if the password matches
		
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		String user = null;
		String pass = null;
		boolean exist = false;
		int count = 0;
		
		try {
			
			conn = DriverManager.getConnection("jdbc:derby:belres.db;create=true");
		
			// retreive username attribute from login
			stmt = conn.prepareStatement(
					"select * from account"
			);		

			// execute the query
			resultSet = stmt.executeQuery();
			
			//harry = resultSet.getString("username");/// this might not work 
			while(resultSet.next()) {
				user = resultSet.getString("userName");
				//System.out.println("9" + username + "9");
				//System.out.println("9" + user + "9");
				if(username.equals(user)) {
					
					pass = resultSet.getString("password");
					//System.out.println(password);
					//System.out.println(pass);
					if(BCrypt.checkpw(password, pass)) {
						exist = true;
					}
				}
				
			}
			
			//System.out.println(exist);
			if(exist == true) { 
				return true;//account exists				
			}
			else{
				return false;//account doesnt exists		
			}
		
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(conn);
		}
		return false;
		
	}
	
	//return a db
	public void printDB(String dbName) {
		ArrayList<String> returnStmt = new ArrayList<String>();
		Connection conn = null;
		String database = dbName;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;
		int rowCount = 0;
		
		try {
			
			conn = DriverManager.getConnection("jdbc:derby:belres.db;create=true");
			if(dbName.toLowerCase().equals("account")) {
				// retreive username attribute from login
				stmt = conn.prepareStatement(
						"select * from account"
				);		
			}else {
				System.out.println("Invalid database name.\n");
			}
			//stmt.setString(1, database);
			// execute the query
			
			resultSet = stmt.executeQuery();
			
			//harry = resultSet.getString("username");/// this might not work 
			//int i = 1;
			ResultSetMetaData schema = resultSet.getMetaData();

			List<String> colNames = new ArrayList<String>();
			for (int i = 1; i <= schema.getColumnCount(); i++) {
				colNames.add(schema.getColumnName(i));
			}

			RowList rowList = getRows(resultSet, schema.getColumnCount());
			rowCount = rowList.size();

			List<Integer> colWidths = getColumnWidths(colNames, rowList);

			printRow(colNames, colWidths);
			printSeparator(colWidths);
			for (List<String> row : rowList) {
				printRow(row, colWidths);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(conn);
		}
		//return returnStmt;
		
	}
	
	//used for printing sql statments
	private static void printRow(List<String> row, List<Integer> colWidths) {
		for (int i = 0; i < row.size(); i++) {
			if (i > 0) {
				System.out.println(" ");
			}
			String item = row.get(i);
			System.out.println(PAD.substring(0, colWidths.get(i) - item.length()));
			System.out.println(item);
		}
		System.out.println("\n");
	}

	private static void printSeparator(List<Integer> colWidths) {
		List<String> sepRow = new ArrayList<String>();
		for (Integer w : colWidths) {
			sepRow.add(SEP.substring(0, w));
		}
		printRow(sepRow, colWidths);
	}

	private static RowList getRows(ResultSet resultSet, int numColumns) throws SQLException {
		RowList rowList = new RowList();
		while (resultSet.next()) {
			List<String> row = new ArrayList<String>();
			for (int i = 1; i <= numColumns; i++) {
				row.add(resultSet.getObject(i).toString());
			}
			rowList.add(row);
		}
		return rowList;
	}

	
	public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
		try {
			return doExecuteTransaction(txn);
		} catch (SQLException e) {
			throw new PersistenceException("Transaction failed", e);
		}
	}
	
	private static List<Integer> getColumnWidths(List<String> colNames, RowList rowList) {
		List<Integer> colWidths = new ArrayList<Integer>();
		for (String colName : colNames) {
			colWidths.add(colName.length());
		}
		for (List<String> row: rowList) {
			for (int i = 0; i < row.size(); i++) {
				colWidths.set(i, Math.max(colWidths.get(i), row.get(i).length()));
			}
		}
		return colWidths;
	}
	
	public<ResultType> ResultType doExecuteTransaction(Transaction<ResultType> txn) throws SQLException {
		Connection conn = connect();
		
		try {
			int numAttempts = 0;
			boolean success = false;
			ResultType result = null;
			
			while (!success && numAttempts < MAX_ATTEMPTS) {
				try {
					result = txn.execute(conn);
					conn.commit();
					success = true;
				} catch (SQLException e) {
					if (e.getSQLState() != null && e.getSQLState().equals("41000")) {
						// Deadlock: retry (unless max retry count has been reached)
						numAttempts++;
					} else {
						// Some other kind of SQLException
						throw e;
					}
				}
			}
			
			if (!success) {
				throw new SQLException("Transaction failed (too many retries)");
			}
			
			// Success!
			return result;
		} finally {
			DBUtil.closeQuietly(conn);
		}
	}

	private Connection connect() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:derby:belres.db;create=true");
		
		// Set autocommit to false to allow execution of
		// multiple queries/statements as part of the same transaction.
		conn.setAutoCommit(false);
		
		return conn;
	}
	
	public void loadInitialData() { ///taken from lab06

	}
	
	public void createTables() {
		executeTransaction(new Transaction<Boolean>() {
			@Override
			public Boolean execute(Connection conn) throws SQLException {
				PreparedStatement stmt1 = null;
				PreparedStatement stmt2 = null;
								
				try {
					System.out.println("Making account table");
					stmt1 = conn.prepareStatement( //creates account table
						"create table account (" +
						"	login_id integer primary key " +
						"		generated always as identity (start with 1, increment by 1), " +									
						"	userName varchar(40),"  +
						"	password varchar(100)," +
						"   email varchar(40),"     +
						"   name varchar(40),"      +
						"   gender varchar(40),"    +
						"   age varchar(40),"		+
						"   location varchar(40)"  	+
						")"
					);	
					stmt1.executeUpdate();
					
					System.out.println("Making idea table");
					stmt2 = conn.prepareStatement( //creates idea table
							"create table idea (" +
							"	idea_id integer primary key " +
							"		generated always as identity (start with 1, increment by 1), " +									
							"	name varchar(40),"  +
							"	descs varchar(300)," +
							"   descl varchar(1000),"     +
							"   authorid varchar(40),"      +
							"   otherid varchar(40),"    +
							"   image varchar(40)"    +
							")"
						);	
						stmt2.executeUpdate();
				
					return true;
				} finally {
					DBUtil.closeQuietly(stmt1);
				}
			}
		});
	}

	public ArrayList<String> getCardData(int idea_id) throws SQLException { //gets all card data

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet resultSet = null;


		//Loads from database
		ArrayList<String> content = new ArrayList<String>();
		conn = DriverManager.getConnection("jdbc:derby:test.db;create=true");
		try {
			stmt = conn.prepareStatement(
					"select * from idea "
							+ "where area_id = ?"

			);

			//Throws in the area id for sql statement
			stmt.setInt(1, idea_id);

			resultSet = stmt.executeQuery();

			//Turns sql result into an array list then returns it
			while(resultSet.next()){
				for(int i = 0; i < 7; i++){
					content.add(resultSet.getString(i + 1));
				}
			}
			return content;

		} finally {
			DBUtil.closeQuietly(resultSet);
			DBUtil.closeQuietly(stmt);
			DBUtil.closeQuietly(conn);
		}
	}
	
	// The main method creates the database tables and loads the initial data.
	public static void main(String[] args) throws IOException {
		System.out.println("Creating tables...");
		DerbyDatabase db = new DerbyDatabase();
		db.createTables();
		
		System.out.println("Loading initial data...");
		db.loadInitialData();
		
		System.out.println("Success!");
	}
}