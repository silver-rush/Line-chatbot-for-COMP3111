package com.example.bot.spring;

import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.net.URISyntaxException;
import java.net.URI;

@Slf4j
public class SQLDatabaseEngine extends DatabaseEngine {

	@Override
	String search(String text) throws Exception {
		PreparedStatement partialMatchStatement = getConnection().prepareStatement(
			"SELECT id, response, count FROM response_table WHERE STRPOS(LOWER(?), LOWER(keyword)) > 0;");

		partialMatchStatement.setString(1, text);
		ResultSet rs = partialMatchStatement.executeQuery();
		System.err.println("QUERY: " + partialMatchStatement.toString());

		String result = null;
		if (rs.next()){
			result = rs.getString(2) + " Count: " + rs.getInt(3);
			System.err.println("FOUND: " + result);
// 
			PreparedStatement updateCountStatement = getConnection().prepareStatement(
				"UPDATE response_table SET count = ? WHERE id = ?;");
			updateCountStatement.setInt(1, rs.getInt(1));
			updateCountStatement.setInt(2, rs.getInt(3));

			System.err.println("UPDATE: " + updateCountStatement);
			updateCountStatement.executeUpdate();
		}else{
			System.err.println("NO RESULT: " + partialMatchStatement.toString());
		}
		rs.close();
		partialMatchStatement.close();

		if (result != null){
			return result;
		}else{
			throw new Exception("NOT FOUND");
		}
	}
	
	
	private Connection getConnection() throws URISyntaxException, SQLException {
		Connection connection;
		URI dbUri = new URI(System.getenv("DATABASE_URL"));

		String username = dbUri.getUserInfo().split(":")[0];
		String password = dbUri.getUserInfo().split(":")[1];
		String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath() +  "?ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory";

		log.info("Username: {} Password: {}", username, password);
		log.info ("dbUrl: {}", dbUrl);
		
		connection = DriverManager.getConnection(dbUrl, username, password);

		return connection;
	}

}
