package translator.bing;

/**
 * Created by limingwei on 16/11/19.
 */
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BingTranslate
{
    public String translate(String word) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        word = word.replaceAll(" ","+");

        //根据查找单词构造查找地址
        HttpGet getWordMean = new HttpGet("http://cn.bing.com/dict/search?q=" + word + "&go=%E6%90%9C%E7%B4%A2&qs=n&form=Z9LH5&pq="+word);
        CloseableHttpResponse response = httpClient.execute(getWordMean);//取得返回的网页源码

        String result = EntityUtils.toString(response.getEntity());
        response.close();
        //注意(?s)，意思是让'.'匹配换行符，默认情况下不匹配
        Pattern searchMeanPattern = Pattern.compile("<meta name=\"description\" content=(.*?) />");
        Matcher m1 = searchMeanPattern.matcher(result);
        if (m1.find())
        {
            String means = m1.group(1);//所有解释，包含网页标签
            System.out.println(means);
            String[] m2=means.split("，");
            //System.out.println(m2[1]);
            if(means.compareTo("\"词典\"") == 0)
            {
                return "未找到释义.";
            }
            else
            {
                return m2[m2.length - 1];
            }
        }
        else
        {
            return "未找到释义.";
        }
    }

    public static void main(String[] args) throws IOException {
        BingTranslate bingTranslate = new BingTranslate();
        System.out.println(bingTranslate.translate("sorry about"));
    }

}
