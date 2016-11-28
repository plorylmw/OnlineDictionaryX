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

        //System.out.print("请输入你要查的单词:");
        //Scanner s = new Scanner(System.in);
        //String word = s.nextLine();
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
            String means = m1.group();//所有解释，包含网页标签
            String[] m2=means.split("，");
            System.out.println("释义:");
            if(m2.length>=3)
            {
                StringBuilder str = new StringBuilder();
                String[] p=m2[3].split("\"");
                String[] m=p[0].split(" ");
                for(int i=0;i*2<m.length;i++)
                    str.append("\t"+m[i*2]+" "+m[i*2+1] + "\n");
                return str.toString();
            }
            else
            {
                return "未找到释义.";
            }
        }
        else
        {
            return "未找到释义.";
        }
    }
}
