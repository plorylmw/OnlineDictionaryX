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

import static java.lang.Thread.sleep;

/**
 * Created by limingwei on 16/11/20.
 */

public class MutiThreadServer extends JFrame//多线程服务器
{
    private JTextArea jta = new JTextArea();

    private AccessDB accessDB = new AccessDB();//数据库成员

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
                    String words = inputFromClient.readUTF();


                    String translateResult = baiduTranslate(words);

                    sleep(2000);

                    outputToClient.writeUTF(translateResult);

                    jta.append("Words received frome client: " + words + '\n');
                    jta.append("explaination found: " + translateResult + '\n');
                }
            }
            catch(IOException e)
            {
                System.err.println(e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            //System.out.println("ss+"+youdaoResult);
            result.append(youdaoResult);

            return result.toString();
        }
    }
}
