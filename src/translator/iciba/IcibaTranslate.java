package translator.iciba;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by limingwei on 16/12/10.
 */

class HttpClientUtil
{
    public static String requestByGetMethod(String s) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        StringBuilder entityStringBuilder = null;
        try {
            HttpGet get = new HttpGet(s);
            CloseableHttpResponse httpResponse = null;
            httpResponse = httpClient.execute(get);
            try {
                HttpEntity entity = httpResponse.getEntity();
                entityStringBuilder = new StringBuilder();
                if (null != entity) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"), 8 * 1024);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        entityStringBuilder.append(line + "/n");
                    }
                }
            } finally {
                httpResponse.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return entityStringBuilder.toString();
    }
}

public class IcibaTranslate {
    public static String translate(String word) {
        StringBuilder ret = new StringBuilder();
        String res;

        String url = "http://dict-co.iciba.com/api/dictionary.php?w=" + word + "&key=43F8369FEE7C1BCD98250D3C08899C89";
        try {
            res = HttpClientUtil.requestByGetMethod(url);
            System.out.println(res);

            //Pattern getPs = Pattern.compile("<ps>(.*?)</ps>"); //(?m)代表按行匹配
            //Matcher m0 = getPs.matcher(res);

            Pattern getPos = Pattern.compile("<pos>(.*?)</pos>"); //(?m)代表按行匹配
            Matcher m1 = getPos.matcher(res);

            Pattern getAcceptation = Pattern.compile("<acceptation>(.*?)/n</acceptation>"); //(?m)代表按行匹配
            Matcher m2 = getAcceptation.matcher(res);

            boolean flag = false;

            while (m1.find() && m2.find()) {
                flag = true;
                //System.out.println(m0.group(1) + '\t' + m1.group(1) + '\t' + m2.group(1));
                ret.append(m1.group(1) + " " + m2.group(1));
                if (m1.find() && m2.find())
                    ret.append("\n");
            }

            if (!flag)
                ret.append("未找到释义.");


        } catch (Exception e) {
            e.printStackTrace();
        }


        return ret.toString();
    }

    /*public static void main(String[] args) {
        System.out.print("sorry\n");
    }*/

}
