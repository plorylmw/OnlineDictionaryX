package translator.baidu;

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

public class BaiduTranslate
{
    public String translate(String word) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        //System.out.print("请输入你要查的单词:");
        //Scanner s = new Scanner(System.in);
        //String word = s.nextLine();
        word = word.replaceAll(" ","+");

        //根据查找单词构造查找地址
        HttpGet getWordMean = new HttpGet("http://dict.baidu.com/s?wd=" + word + "&go=%E6%90%9C%E7%B4%A2&qs=n&form=Z9LH5&pq="+word);
        CloseableHttpResponse response = httpClient.execute(getWordMean);//取得返回的网页源码

        String result = EntityUtils.toString(response.getEntity());

        //System.out.println(result);

        response.close();
        //注意(?s)，意思是让'.'匹配换行符，默认情况下不匹配
        Pattern searchMeanPattern = Pattern.compile("(?s)<div class=\"en-content\">.*?<div>.*?</div>.*?</div>");
        Matcher m1 = searchMeanPattern.matcher(result);

        if (m1.find()) {
            String means = m1.group();//所有解释，包含网页标签
            //System.out.println(m1);
            //System.out.println(means);

            String means1=means;
            Pattern getChinese = Pattern.compile("<strong>(.*?)</strong>"); //(?m)代表按行匹配
            Matcher m2 = getChinese.matcher(means);
            Pattern getChinese1 = Pattern.compile("<span>(.*?)</span>"); //(?m)代表按行匹配
            Matcher m3 = getChinese1.matcher(means1);

            //System.out.println("释义:");
            StringBuilder str = new StringBuilder();
            while (m2.find()&&m3.find()) {
                //在Java中(.*?)是第1组，所以用group(1)
                str.append("\t" + m2.group(1) + m3.group(1) + "\n");
            }
            return str.toString();
        } else {
            return "未查找到释义.";
        }
    }
}
