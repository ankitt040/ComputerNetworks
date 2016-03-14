/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab3;
import java.sql.*;
/**
 *
 * @author ANKIT
 */
public class Jdbc_check
{
  public static void main( String args[] )
  {
    Connection c = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      c = DriverManager.getConnection("jdbc:sqlite:test.db");
      c.setAutoCommit(false);
      System.out.println("Opened database successfully");

      ResultSet rs = c.createStatement().executeQuery( "SELECT * FROM USERS;" );
      int port=0,login_status;String un,pass,ip="",fn;
      while ( rs.next() ) {
         un = rs.getString("name");
         //port  = rs.getInt("port");
         pass = rs.getString("password");
         //ip = rs.getString("ip");
         login_status = rs.getInt("loggedin");
         
         System.out.println(""+un +" "+pass +" " +login_status+" "+ip +" "+port );
      }
      rs.close();
      
        System.out.println("\nfrinds list:");
      rs = c.createStatement().executeQuery( "SELECT * FROM FRIENDS;" );
      while ( rs.next() ) {
         un = rs.getString("name");
         fn = rs.getString("f_name");
         
         System.out.println(""+un +" "+fn  );
      }
      rs.close();
      c.close();
    } catch ( Exception e ) {
      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
      System.exit(0);
    }
    System.out.println("Operation done successfully");
  }
}