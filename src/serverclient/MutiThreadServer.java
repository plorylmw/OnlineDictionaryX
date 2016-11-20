package serverclient;

import translator.bing.BingTranslate;

import javax.swing.*;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * Created by limingwei on 16/11/20.
 */

public class MutiThreadServer extends JFrame//多线程服务器
{
    private JTextArea jta = new JTextArea();

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
                    String bingTranslateResult = bingTranslate(words);

                    outputToClient.writeUTF(bingTranslateResult);

                    jta.append("Words received frome client: " + words + '\n');
                    jta.append("explaination found: " + bingTranslateResult + '\n');
                }
            }
            catch(IOException e)
            {
                System.err.println(e);
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
            return null;
        }

        public String youdaoTranslate(String words)
        {
            return null;
        }
    }
}
