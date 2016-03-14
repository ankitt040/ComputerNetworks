/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 *
 * @author ANKIT
 */
public class SQLiteJDBC
{
    Connection c;
    Statement stmt;
    public SQLiteJDBC(){
        c = null;
        stmt = null;
    }
    public boolean con(){
      try {
        Class.forName("org.sqlite.JDBC");
        c = DriverManager.getConnection("jdbc:sqlite:test.db");
        c.setAutoCommit(false);
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        //System.exit(0);
        return false;
      }
     // System.out.println("Opened database successfully --/");
      return true;
    }
    public void close(){
        try {
              c.close();
          } catch (Exception e) {
          }
        try {
            stmt.close();
        } catch (Exception e) {
        }
       //System.out.println("closed database connection --/");
    }
    public boolean create_table_ulist(){
        try {
          stmt = c.createStatement();
          String sql = "CREATE TABLE USERS " +
                       "(" +
                       " NAME           TEXT PRIMARY KEY   NOT NULL, " + 
                       " PASSWORD       TEXT    NOT NULL, " + 
                       " LOGGEDIN       INT " + 
                       " IP             TEXT " + 
                       " PORT           INT " + 
                       ")"; 
          stmt.executeUpdate(sql);
          c.commit();
          stmt.close();
  
        } catch ( Exception e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          //System.exit(0);
          return false;
        }
        System.out.println("USERS Table created successfully");
        return true;
    }
    public boolean create_table_friendslist(){
        try {
          stmt = c.createStatement();
          String sql = "CREATE TABLE FRIENDS " +
                       "(" +
                       " NAME           TEXT   NOT NULL, " + 
                       " F_NAME       TEXT    NOT NULL " + 
                       ")"; 
          stmt.executeUpdate(sql);
          c.commit();
          stmt.close();
      
        } catch ( Exception e ) {
          System.err.println( e.getClass().getName() + ": " + e.getMessage() );
          //System.exit(0);
          return false;
        }
        System.out.println("FRIENDS Table created successfully");
        return true;
    }
    
    public boolean check_user_presence(String u){
        try {
            if( c.createStatement().executeQuery("SELECT * FROM USERS WHERE NAME='"+ u +"'").next() ){
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
    public boolean update_ip_port(String u, String ip, int port ){
        try {
            String sql = "UPDATE USERS set LOGGEDIN = 1,IP= '"+ip+"', PORT= '"+port+"' where NAME= '"+ u +"' ;";
            c.createStatement().executeUpdate(sql);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean login(String un){
        try {
            stmt = c.createStatement();
            String sql = "UPDATE USERS set LOGGEDIN = 1 where NAME= '"+ un +"' ;";
            stmt.executeUpdate(sql);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean add_user(String un,String password){
        if( check_user_presence(un) ){return false;}
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO USERS (NAME,PASSWORD,LOGGEDIN) " +
                         "VALUES ('"+ un +"', '"+ password +"' ,0 );"; 
            stmt.executeUpdate(sql);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean add_friend(String un,String fn){
        if( !check_user_presence(fn) ){ return false; } // no such user
        try {// already friend
            if( c.createStatement().executeQuery("SELECT * FROM FRIENDS WHERE NAME='"+ un +"' AND F_NAME= '"+fn+"'  ").next() ){
                return false;
            }
        } catch (Exception e) {
        }
        try {
            String sql = "INSERT INTO FRIENDS (NAME,F_NAME) " +
                         "VALUES ('"+ un +"', '"+ fn +"' );"; 
            c.createStatement().executeUpdate(sql);
            c.commit();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean check_login(String un,String password){
        try {
            ResultSet rs = c.createStatement().executeQuery( "SELECT NAME FROM USERS where NAME= '"+ un +"' AND PASSWORD='"+password+"'  ;" );
            if(rs.next()){
                login(un);
                return true;
            }else return false;
        } catch (Exception e) {
            return false;
        }
    }
    public String getFriends(String un){
        String res="";
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT F_NAME FROM FRIENDS where NAME= '"+ un +"' ;" );
            while ( rs.next() ) {
                res += rs.getString("f_name")+"\n";
             }
             rs.close();
             stmt.close();
            return res;
        } catch (Exception e) {
            return "";
        }
    }
    public String getUsers(){
        String res="";
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT NAME FROM USERS ;" );
            while ( rs.next() ) {
                res += rs.getString("name")+"\n";
             }
             rs.close();
             stmt.close();
            return res;
        } catch (Exception e) {
            return "";
        }
    }
    
    
  public static void main( String args[] )
  {
      SQLiteJDBC db1 = new SQLiteJDBC();
      if(db1.con()){
//          try {
//              //db1.c.createStatement().execute("DROP TABLE USERS");
//              //db1.c.createStatement().execute("DROP TABLE FRIENDS");
//              //System.out.println("droped");
//             // System.out.println( "flist:ankit:"+ db1.getFriends("ankit") );
//          } catch (Exception e) {
//             // System.out.println("not droped");
//          }
//          db1.create_table_ulist();
//          db1.create_table_friendslist();
         if( db1.add_user("akshay", "123") ) {System.out.println("added akshay-123");}
         else{ System.out.println("!added akshay-123"); }
//          db1.add_user("anna", "123");
//          db1.add_user("alok", "123");
          db1.add_user("anish", "123");
          
          db1.add_friend("ankit", "akshay");
          System.out.println( "flist:ankit:"+ db1.getFriends("ankit") );
          System.out.println("users: "+ db1.getUsers());
          
          db1.close();
      
      
      }
   

  }
}
