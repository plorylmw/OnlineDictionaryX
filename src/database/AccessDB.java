package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by limingwei on 16/11/21.
 */
public class AccessDB
{
    private Statement statement;

    public void init() throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Driver loaded");

        Connection connection = DriverManager.getConnection
                ("jdbc:mysql://localhost/java_project_2", "lmw", "lmw");
        System.out.println("DataBase connected");

        statement = connection.createStatement();
    }

    public boolean login()
    {
        return true;
    }

    public void addPraise()
    {

    }

    public void seachOnlineUser()
    {
        
    }

    /*public static void main(String[] args)
    {
        AccessDB accessDB = new AccessDB();
        try {
            accessDB.init();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }*/
}
