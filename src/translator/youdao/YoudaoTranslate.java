package translator.youdao;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by limingwei on 16/11/19.
 */
public class YoudaoTranslate {

    public String ans;
    public String words;

    public void translate(String words) {
        this.words = words;
        //System.out.println(this.words);
        ReadByPost tmp = new ReadByPost();
        tmp.run();
    }

    public String getAns() {
        System.out.println(ans);
        return ans;
    }


    class ReadByPost
    {
        public void run() {
            try {

                URL url = new URL("http://fanyi.youdao.com/openapi.do");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("encoding", "UTF-8");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                connection.setRequestMethod("POST");

                OutputStream os = connection.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);


                StringBuffer buf = new StringBuffer();
                buf.append("keyfrom=javaproject&key=671918409&type=data&doctype=json&version=1.1&q=");

                //String str;
                //System.out.print("请输入要查找的单词:");
                //Scanner scanner = new Scanner(System.in);
                //str = scanner.next();
                //System.out.println("ss+"+words);
                buf.append(words);

                bw.write(buf.toString());
                bw.flush();

                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader br = new BufferedReader(isr);

                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    builder.append(line);
                }

                bw.close();
                osw.close();
                os.close();

                br.close();
                isr.close();
                is.close();

                String[] result = convert(builder.toString());

                //System.out.println("释义:");

                StringBuilder tmp = new StringBuilder();
                for (int i = 0; i < result.length; i++)
                {
                    //System.out.println(result[i]);
                    tmp.append(result[i] + "\n");
                }
                ans = tmp.toString();
                System.out.println(ans);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String[] convert(String builder) {
            String result = builder;
            int start = result.indexOf("explains");
            int end = result.indexOf("query");
            String sub_result = result.substring(start + 12, end - 5);
            String[] arry = sub_result.split("\",\"");
            return arry;
        }
    }
}