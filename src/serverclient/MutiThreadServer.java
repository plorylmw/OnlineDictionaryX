package serverclient;

import database.AccessDB;
import translator.baidu.BaiduTranslate;
import translator.bing.BingTranslate;
import translator.iciba.IcibaTranslate;
import translator.youdao.YoudaoTranslate;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;

import static java.lang.Thread.sleep;

/**
 * Created by limingwei on 16/11/20.
 */


public class MutiThreadServer extends JFrame//多线程服务器
{
    private JTextArea jta = new JTextArea();
    private AccessDB accessDB = new AccessDB();//数据库成员
    private HashMap<Integer, String> onlineUsrs = new HashMap<>();
    private HashMap<String, DataOutputStream> outputToClientTotal = new HashMap<>();

    private static int session_id = 0;


    public static void main(String[] args)
    {
        new MutiThreadServer();
    }

    public MutiThreadServer()
    {
        setLayout(new BorderLayout());
        add(new JScrollPane(jta), BorderLayout.CENTER);

        setTitle("MutiThreadServer");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        try {
            accessDB.init();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }//链接到数据库

        try
        {
            ServerSocket serverSocket = new ServerSocket(8000);//建立端口号为8000的套接字
            jta.append("MutiThreadServer started at " + new Date() + '\n');

            int clientNo = 1;

            while(true)
            {
                Socket socket = serverSocket.accept();
                jta.append("Start thread for client " + clientNo + " at " + new Date() + '\n');

                InetAddress inetAddress = socket.getInetAddress();
                jta.append("Client " + clientNo + "'s host name is " + inetAddress.getHostName() + "\n");
                jta.append("Client " + clientNo + "'s IP Address is " + inetAddress.getHostAddress() + "\n");

                HandleAClient task = new HandleAClient(socket);

                new Thread(task).start();//开辟一个新的线程

                clientNo++;
            }
        }
        catch(IOException ex)
        {
            System.err.println(ex);
        }
    }

    public static String encode(String str)
    {
        str = str.replaceAll("%", "%9");
        str = str.replaceAll("&", "%8");
        return str;
    }

    public static String decode(String str)
    {
        str = str.replaceAll("%8", "&");
        str = str.replaceAll("%9", "%");
        return str;
    }



    class HandleAClient implements Runnable
    {
        private Socket socket;

        public HandleAClient(Socket socket)
        {
            this.socket = socket;
        }

        public void run() {
            try
            {
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

                while(true)
                {
                    String request = inputFromClient.readUTF();
                    String reply = dealRequest(request, outputToClient);


                    jta.append("Request frome client: " + request + '\n');
                    if(reply.compareTo("") == 0)
                        continue;

                    outputToClient.writeUTF(reply);
                    jta.append("Reply to client: " + reply + '\n');
                }
            }
            catch(IOException e) {
                //System.err.println(e);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public String bingTranslate(String words) {
            StringBuilder result = new StringBuilder();
            //result.append("Bing释义:" + '\n');

            BingTranslate bingTranslate = new BingTranslate();

            try {
                String youdaoResult = bingTranslate.translate(words);
                result.append(youdaoResult);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        public String baiduTranslate(String words) {
            StringBuilder result = new StringBuilder();
            //result.append("iciba释义:" + '\n');

            IcibaTranslate baiduTranslate = new IcibaTranslate();

            String baiduResult = baiduTranslate.translate(words);
            result.append(baiduResult);

            return result.toString();
        }

        public String youdaoTranslate(String words) {
            StringBuilder result = new StringBuilder();
            //result.append("Youdao释义:" + '\n');

            YoudaoTranslate youdaoTranslate = new YoudaoTranslate();

            youdaoTranslate.translate(words);
            String youdaoResult = youdaoTranslate.getAns();
            result.append(youdaoResult);

            return result.toString();
        }

        //------------------------------------------------------------------

        public String dealRegister(String request) throws SQLException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            reply.append("register" + "&");
            synchronized (accessDB)
            {
                boolean flag = accessDB.register(splitRequest[1], splitRequest[2]);
                if (flag)
                    reply.append("success");
                else
                    reply.append("fail");
            }
            return reply.toString();
        }

        public String dealLogin(String request, DataOutputStream dataOutputStream) throws IOException, SQLException {

            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            synchronized (accessDB)
            {
                boolean flag = accessDB.login(splitRequest[1], splitRequest[2]);//数据库查询是否有该用户
                if(flag)
                {
                    synchronized (onlineUsrs)//查看该用户是否在线
                    {
                       if(onlineUsrs.containsValue(splitRequest[1]))
                       {
                           String logoutName = splitRequest[1];

                           String friendSet;
                           friendSet = accessDB.selectFriend(logoutName);

                           String[] friends = friendSet.split("&");


                           for (int i = 0; i < friends.length; i++)
                           {
                               if (onlineUsrs.containsValue(friends[i]))
                               {
                                   synchronized (outputToClientTotal)
                                   {
                                       DataOutputStream value = outputToClientTotal.get(friends[i]);
                                       StringBuilder str = new StringBuilder();
                                       str.append("offline" + "&");
                                       str.append(logoutName);
                                       value.writeUTF(str.toString());
                                   }
                               }
                           }

                           synchronized (onlineUsrs)
                           {
                               Collection<String> collection = onlineUsrs.values();
                               collection.remove(logoutName);
                           }
                           synchronized (outputToClientTotal)
                           {
                               outputToClientTotal.remove(logoutName);
                           }
                       }
                    }
                }
                if (flag)
                {
                    reply.append("login" + "&");
                    reply.append(session_id);

                    String friendSet;
                    synchronized (accessDB)
                    {
                        friendSet = accessDB.selectFriend(splitRequest[1]);
                    }
                    String[] friends = friendSet.split("&");
                    synchronized (onlineUsrs)
                    {
                        for (int i = 0; i < friends.length; i++)
                        {
                            if (onlineUsrs.containsValue(friends[i]))
                            {
                                reply.append("&" + friends[i] + "&" + "1");
                                synchronized (outputToClientTotal)
                                {
                                    DataOutputStream value = outputToClientTotal.get(friends[i]);
                                    StringBuilder str = new StringBuilder();
                                    str.append("online" + "&");
                                    str.append(splitRequest[1]);
                                    value.writeUTF(str.toString());
                                }
                            }
                            else if(friends[i].compareTo("") != 0 )
                                reply.append("&" + friends[i] + "&" + "0");
                            else
                                reply.append("");
                        }
                    }

                    synchronized (onlineUsrs)
                    {
                        onlineUsrs.put(session_id, splitRequest[1]);
                        session_id += 1;
                    }
                    synchronized (outputToClientTotal)
                    {
                        outputToClientTotal.put(splitRequest[1], dataOutputStream);//将用户与对应的输出流绑定
                    }
                }
                else
                    reply.append("login&fail");
            }
            return reply.toString();
        }

        public String dealGetWord(String request) throws SQLException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String word = splitRequest[2];
            String usrName = onlineUsrs.get(tmp_session_id);

            reply.append(encode("getword") + "&");

            reply.append(encode(baiduTranslate(word)) + "&");
            reply.append(encode(bingTranslate(word)) + "&");
            reply.append(encode(youdaoTranslate(word)) + "&");

            synchronized (accessDB)//返回该单词的总体点赞情况
            {
                reply.append(accessDB.getPraise(word) + "&");
            }
            synchronized (accessDB)//返回该用户对该单词的点赞情况
            {
                reply.append(accessDB.praiseRecord(usrName, word));
            }
            return reply.toString();
        }

        public String dealLove(String request) throws SQLException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");
            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            synchronized (accessDB)
            {
                accessDB.addPraise(onlineUsrs.get(tmp_session_id), splitRequest[2], splitRequest[3]);
            }
            return reply.toString();
        }

        public String dealDisLove(String request) throws SQLException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");
            int tmp_session_id = Integer.parseInt(splitRequest[1]);

            String usrName = onlineUsrs.get(tmp_session_id);
            String word = splitRequest[2];
            String comeFrom = splitRequest[3];

            synchronized (accessDB)
            {
                accessDB.deletePraise(usrName, word, comeFrom);
            }
            return reply.toString();
        }

        public String dealLogout(String request) throws SQLException, IOException {
            StringBuilder reply = new StringBuilder();

            String[] splitRequest = request.split("&");
            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String logoutName = onlineUsrs.get(tmp_session_id);

            String friendSet;
            synchronized (accessDB)
            {
                friendSet = accessDB.selectFriend(logoutName);
            }

            String[] friends = friendSet.split("&");

            synchronized (onlineUsrs)
            {
                for (int i = 0; i < friends.length; i++)
                {
                    if (onlineUsrs.containsValue(friends[i]))
                    {
                        synchronized (outputToClientTotal)
                        {
                            DataOutputStream value = outputToClientTotal.get(friends[i]);
                            StringBuilder str = new StringBuilder();
                            str.append("offline" + "&");
                            str.append(logoutName);
                            value.writeUTF(str.toString());
                        }
                    }
                }
            }

            synchronized (onlineUsrs)
            {
                onlineUsrs.remove(tmp_session_id);
            }
            synchronized (outputToClientTotal)
            {
                outputToClientTotal.remove(logoutName);
            }

            return reply.toString();
        }

        public String dealMessage(String request) throws IOException {

            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String sendTo = splitRequest[2];
            String message = splitRequest[3];

            String sendFrom = onlineUsrs.get(tmp_session_id);
            DataOutputStream value = outputToClientTotal.get(sendTo);
            value.writeUTF("message" + "&"  + sendFrom + "&" + message);

            return reply.toString();
        }

        public String dealAddRequest(String request) throws IOException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String sendTo = splitRequest[2];

            String requestName = onlineUsrs.get(tmp_session_id);
            DataOutputStream value = outputToClientTotal.get(sendTo);
            value.writeUTF("addrequest" + "&" + requestName);

            if(!onlineUsrs.containsValue(sendTo))
                reply.append("nosuchuser");

            return reply.toString();
        }

        public String dealReplyAddRequest(String request) throws IOException, SQLException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String sendTo = splitRequest[2];

            boolean flag = true;
            String replyName = onlineUsrs.get(tmp_session_id);
            DataOutputStream value = outputToClientTotal.get(sendTo);
            value.writeUTF(splitRequest[0] + "&" + replyName);

            if(splitRequest[0].compareTo("agree") == 0)
            {
                accessDB.addFriend(replyName, sendTo);
                accessDB.addFriend(sendTo, replyName);

                if(onlineUsrs.containsValue(replyName))
                    value.writeUTF("newfriend" + "&" + replyName + "&1");
                else
                    value.writeUTF("newfriend" + "&" + replyName + "&0");

                if(onlineUsrs.containsValue(sendTo))
                    reply.append("newfriend" + "&" + sendTo + "&1");
                else
                    reply.append("newfriend" + "&" + sendTo + "&0");
            }

            return reply.toString();
        }

        public String dealDeleteFriend(String request) throws SQLException, IOException {
            StringBuilder reply = new StringBuilder();
            String[] splitRequest = request.split("&");

            int tmp_session_id = Integer.parseInt(splitRequest[1]);
            String requestName;
            synchronized (onlineUsrs) {
                requestName = onlineUsrs.get(tmp_session_id);
            }
            String deleteName = splitRequest[2];

            synchronized (accessDB)
            {
                accessDB.deleteFriend(requestName, deleteName);
            }

            synchronized (onlineUsrs)
            {
                if(onlineUsrs.containsValue(deleteName))
                {
                    synchronized (outputToClientTotal)
                    {
                        DataOutputStream value = outputToClientTotal.get(deleteName);
                        StringBuilder str = new StringBuilder();
                        str.append("delete" + "&" + requestName);
                        value.writeUTF(str.toString());
                    }
                }
            }

            reply.append("delete" + "&" + deleteName);
            return reply.toString();
        }

        public String dealRequest(String request, DataOutputStream dataOutputStream) throws SQLException, IOException
        {

            String[] splitRequest = request.split("&");

            //用户注册
            if(splitRequest[0].compareTo("register") == 0)
                return dealRegister(request);

            //查询单词
            if(splitRequest[0].compareTo("getword") == 0)
                return dealGetWord(request);

            //用户点赞
            if(splitRequest[0].compareTo("love") == 0)
                return dealLove(request);

            //用户取消赞
            if(splitRequest[0].compareTo("dislove") == 0)
                return dealDisLove(request);

            //用户登录
            if(splitRequest[0].compareTo("login") == 0)
                return dealLogin(request, dataOutputStream);

            //用户登出
            if(splitRequest[0].compareTo("logout") == 0)
                return dealLogout(request);

            //发送消息
            if(splitRequest[0].compareTo("message") == 0)
                return dealMessage(request);

            //申请好友
            if(splitRequest[0].compareTo("addrequest") == 0)
                return dealAddRequest(request);

            //同意申请
            if(splitRequest[0].compareTo("agree") == 0 || splitRequest[0].compareTo("decline") == 0)
                return dealReplyAddRequest(request);

            //删除好友
            if(splitRequest[0].compareTo("delete") == 0)
                return dealDeleteFriend(request);

            return null;
        }
    }
}
