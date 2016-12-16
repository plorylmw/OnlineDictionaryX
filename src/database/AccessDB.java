package database;

import java.sql.*;

/**
 * Created by limingwei on 16/11/21.
 */
public class AccessDB
{
    private Statement statement;

    public void init() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        System.out.println("Driver loaded");

        Connection connection = DriverManager.getConnection
                ("jdbc:mysql://localhost/java_project_2", "lmw", "lmw");
        System.out.println("DataBase connected");

        statement = connection.createStatement();
    }

    public boolean register(String usrName, String passWord) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from User;");
        boolean flag = true;
        while(resultSet.next())
        {
            //System.out.println(resultSet.getString("userName") + "\t" + resultSet.getString("userKey"));
            if (resultSet.getString("userName").compareTo(usrName) == 0)
            {
                //System.out.println(resultSet.getString("userName") + "tt");
                flag = false;
                break;
            }
        }
        if(flag)
        {
            statement.execute("insert into User(userName, userKey)" +
                    "values (" + "'" + usrName + "' , '" + passWord + "')");
        }
        return flag;
    }

    public boolean login(String usrName, String passWord) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from User;");
        boolean flag = false;
        while(resultSet.next())
        {
            //System.out.println(resultSet.getString("userName") + "\t" + resultSet.getString("userKey"));
            if (resultSet.getString("userName").compareTo(usrName) == 0 && resultSet.getString("userKey").compareTo(passWord) == 0)
            {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public void addPraise(String ursName, String word, String comeFrom) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from WordsRecord;");
        boolean flag = false;
        while(resultSet.next())
        {
            if (resultSet.getString("word").compareTo(word) == 0)
            {
                flag = true;
                break;
            }
        }
        if(flag)
        {
            if(comeFrom.compareTo("baidu") == 0)
                statement.execute("update WordsRecord set fromBaidu = fromBaidu + 1 where word = '" + word + "'");
            if(comeFrom.compareTo("youdao") == 0)
                statement.execute("update WordsRecord set fromYoudao = fromYoudao + 1 where word = '" + word + "'");
            if(comeFrom.compareTo("bing") == 0)
                statement.execute("update WordsRecord set fromBing = fromBing + 1 where word = '" + word + "'");
        }
        else
        {
            if(comeFrom.compareTo("baidu") == 0)
                statement.execute("insert into WordsRecord(word, fromBaidu, fromYoudao, fromBing) " +
                                  "values ('" + word + "', 1, 0, 0);"
                                 );
            if(comeFrom.compareTo("youdao") == 0)
                statement.execute("insert into WordsRecord(word, fromBaidu, fromYoudao, fromBing) " +
                                  "values ('" + word + "', 0, 1, 0);"
                                 );
            if(comeFrom.compareTo("bing") == 0)
                statement.execute("insert into WordsRecord(word, fromBaidu, fromYoudao, fromBing) " +
                                  "values ('" + word + "', 0, 0, 1);"
                                 );
        }

        if(comeFrom.compareTo("baidu") == 0) {
            System.out.println("insert into PraiseRecord(usrName, word, baidu, bing, youdao) " +
                    "values('" + ursName + "','" + word + "', 'Yes' , 'No', 'No')");
            statement.execute("insert into PraiseRecord(usrName, word, baidu, bing, youdao) " +
                    "values('" + ursName + "','" + word + "', 'Yes' , 'No', 'No')");
        }
        if(comeFrom.compareTo("bing") == 0)
            statement.execute("insert into PraiseRecord(usrName, word, baidu, bing, youdao) " +
                              "values('" + ursName + "','" + word + "', 'No' , 'Yes', 'No')");
        if(comeFrom.compareTo("youdao") == 0)
            statement.execute("insert into PraiseRecord(usrName, word, baidu, bing, youdao) " +
                              "values('" + ursName + "','" + word + "', 'No' , 'No', 'Yes')");
    }

    public void deletePraise(String ursName, String word, String comeFrom) throws SQLException {
        if(comeFrom.compareTo("baidu") == 0)
            statement.execute("update WordsRecord set fromBaidu = fromBaidu - 1 where word = '" + word + "'");
        if(comeFrom.compareTo("youdao") == 0)
            statement.execute("update WordsRecord set fromYoudao = fromYoudao - 1 where word = '" + word + "'");
        if(comeFrom.compareTo("bing") == 0)
            statement.execute("update WordsRecord set fromBing = fromBing - 1 where word = '" + word + "'");

        if(comeFrom.compareTo("baidu") == 0)
            statement.execute("update PraiseRecord set baidu = 'No' where usrName = '" + ursName + "' AND word = '" + word + "'");
        if(comeFrom.compareTo("bing") == 0)
            statement.execute("update PraiseRecord set bing = 'No' where usrName = '" + ursName + "' AND word = '" + word + "'");
        if(comeFrom.compareTo("youdao") == 0)
            statement.execute("update PraiseRecord set youdao = 'No' where usrName = '" + ursName + "' AND word = '" + word + "'");
    }

    public String getPraise(String word) throws SQLException {
        ResultSet resultSet = statement.executeQuery("select * from WordsRecord where word = '" + word + "'");

        StringBuilder res = new StringBuilder();
        boolean flag = false;
        while(resultSet.next())
        {
            flag = true;
            int baidu = Integer.parseInt(resultSet.getString("fromBaidu"));
            int bing = Integer.parseInt(resultSet.getString("fromBing"));
            int youdao = Integer.parseInt(resultSet.getString("fromYoudao"));

            if(baidu >= bing && baidu >= youdao)
            {
                if(bing >= youdao)
                    res.append("012");
                else
                    res.append("021");
            }
            else if(bing >= baidu && bing >= youdao)
            {
                if(baidu >= youdao)
                    res.append("102");
                else
                    res.append("120");
            }
            else
            {
                if(baidu >= bing)
                    res.append("201");
                else
                    res.append("210");
            }
        }
        if(flag == false)
            res.append("012");
        return res.toString();
    }

    public String praiseRecord(String usrName, String word) throws SQLException {
        StringBuilder res = new StringBuilder();

        ResultSet resultSet = statement.executeQuery("select * from PraiseRecord;");

        boolean flag_baidu = false;
        boolean flag_bing = false;
        boolean flag_youdao = false;

        while(resultSet.next())
        {
            if(resultSet.getString("usrName").compareTo(usrName) == 0 && resultSet.getString("word").compareTo(word) == 0)
            {
                if(resultSet.getString("baidu").compareTo("Yes") == 0)
                    flag_baidu = true;
                if(resultSet.getString("bing").compareTo("Yes") == 0)
                    flag_bing = true;
                if(resultSet.getString("youdao").compareTo("Yes") == 0)
                    flag_youdao = true;
            }
        }

        if(flag_baidu)
            res.append("1");
        else
            res.append("0");
        if(flag_bing)
            res.append("1");
        else
            res.append("0");
        if(flag_youdao)
            res.append("1");
        else
            res.append("0");

        return res.toString();
    }

    public void addFriend(String friend1, String friend2) throws SQLException {
        statement.execute("insert into FriendRecord(friend1, friend2)" +
                          "values('" + friend1 + "','" + friend2 + "')");
    }

    public String selectFriend(String usrName) throws SQLException {
        StringBuilder res = new StringBuilder();
        ResultSet resultSet = statement.executeQuery("select * from FriendRecord;");
        while(resultSet.next())
        {
            //System.out.println(resultSet.getString("userName") + "\t" + resultSet.getString("userKey"));
            if (resultSet.getString("Friend1").compareTo(usrName) == 0)
            {
                res.append(resultSet.getString("Friend2") + "&");
            }
        }
        return res.toString();
    }

    public void deleteFriend(String friend1, String friend2) throws SQLException {
        statement.execute("delete from FriendRecord " +
                          "where friend1 = '" + friend1 + "' AND friend2 = '" + friend2 + "';");

        statement.execute("delete from FriendRecord " +
                "where friend1 = '" + friend2 + "' AND friend2 = '" + friend1 + "';");
    }

    /*public static void main(String[] args)
    {
        AccessDB accessDB = new AccessDB();
        try {
            accessDB.init();
            //System.out.println(accessDB.login("141220058", "141220058"));
            //System.out.println(accessDB.login("141220058", "141220059"));
            //accessDB.addPraise("happy", "Baidu");
            //accessDB.addPraise("happy", "Youdao");
            //System.out.println(accessDB.getPraise("happy"));
            //accessDB.register("test", "test");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }*/
}
