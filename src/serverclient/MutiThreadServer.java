package serverclient;

import database.AccessDB;
import translator.baidu.BaiduTranslate;
import translator.bing.BingTranslate;
import translator.youdao.YoudaoTranslate;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

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

    class HandleAClient implements Runnable
    {
        private Socket socket;

        public HandleAClient(Socket socket)
        {
            this.socket = socket;
        }

        public void run()
        {
            try
            {
                DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
                DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

                while(true)
                {
                    String request = inputFromClient.readUTF();
                    String reply = dealRequest(request, outputToClient);

                    System.out.println(reply);

                    outputToClient.writeUTF(reply);

                    jta.append("Request frome client: " + request + '\n');
                    jta.append("Reply to client: " + reply + '\n');
                }
            }
            catch(IOException e)
            {
                System.err.println(e);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        String dealRequest(String request, DataOutputStream dataOutputStream) throws SQLException
        {
            StringBuilder reply = new StringBuilder();

            String[] splitRequest = request.split("&");

            if(splitRequest[0].compareTo("register") == 0)
            {
                synchronized (accessDB)
                {
                    boolean flag = accessDB.register(splitRequest[1], splitRequest[2]);
                    if (flag)
                        reply.append("success");
                    else
                        reply.append("fail");
                }
            }
            if(splitRequest[0].compareTo("login") == 0)
            {
                synchronized (accessDB)
                {
                    boolean flag = accessDB.login(splitRequest[1], splitRequest[2]);
                    if (flag)
                    {
                        System.out.println("hit");
                        reply.append("success");
                        synchronized (onlineUsrs)
                        {
                            onlineUsrs.put(session_id++, splitRequest[1]);
                        }
                        outputToClientTotal.put(splitRequest[1], dataOutputStream);//将用户与对应的输出流绑定
                    }
                    else
                        reply.append("fail");
                }
            }
            if(splitRequest[0].compareTo("getword") == 0)
            {
                String word = splitRequest[1];
                reply.append(baiduTranslate(word) + "&");
                reply.append(bingTranslate(word) + "&");
                reply.append(youdaoTranslate(word) + "&");
                synchronized (accessDB)
                {
                    reply.append(accessDB.getPraise(word));
                }
            }
            if(splitRequest[0].compareTo("love") == 0)
            {
                synchronized (accessDB)
                {
                    accessDB.addPraise(splitRequest[1], splitRequest[2]);
                }
            }

            return reply.toString();
        }

        public String bingTranslate(String words)
        {
            StringBuilder result = new StringBuilder();
            result.append("Bing释义:" + '\n');

            BingTranslate bingTranslate = new BingTranslate();

            try {
                String youdaoResult = bingTranslate.translate(words);
                result.append(youdaoResult);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        public String baiduTranslate(String words)
        {
            StringBuilder result = new StringBuilder();
            result.append("Baidu释义:" + '\n');

            BaiduTranslate baiduTranslate = new BaiduTranslate();

            try {
                String baiduResult = baiduTranslate.translate(words);
                result.append(baiduResult);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }

        public String youdaoTranslate(String words)
        {
            StringBuilder result = new StringBuilder();
            result.append("Youdao释义:" + '\n');

            YoudaoTranslate youdaoTranslate = new YoudaoTranslate();

            youdaoTranslate.translate(words);
            String youdaoResult = youdaoTranslate.getAns();
            result.append(youdaoResult);

            return result.toString();
        }
    }
}
