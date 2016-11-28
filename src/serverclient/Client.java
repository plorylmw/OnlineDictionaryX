package serverclient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by limingwei on 16/11/20.
 */
public class Client extends JFrame
{
    private JTextField jtf = new JTextField();

    private JTextArea jta = new JTextArea();

    private DataOutputStream toServer;
    private DataInputStream fromServer;

    public static void main(String[] args)
    {
        new Client();
    }

    public Client()
    {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(new JLabel("Enter words"), BorderLayout.WEST);
        p.add(jtf, BorderLayout.CENTER);
        jtf.setHorizontalAlignment(JTextField.RIGHT);

        setLayout(new BorderLayout());
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(jta), BorderLayout.CENTER);

        jtf.addActionListener(new TextFieldListener());

        setTitle("Client");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        try
        {
            Socket socket = new Socket("localhost", 8000);
            fromServer = new DataInputStream(socket.getInputStream());
            toServer = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException ex)
        {
            jta.append(ex.toString() + '\n');
        }
    }

    private class TextFieldListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            try
            {
                String words = jtf.getText().trim();
                toServer.writeUTF(words);
                toServer.flush();
                String explaination = fromServer.readUTF();
                jta.append("Words is " + words + "\n");
                jta.append("Explaination received from the server is: " + '\n' + explaination + '\n');
            }
            catch(IOException ex)
            {
                System.err.println(ex);
            }
        }
    }


}
