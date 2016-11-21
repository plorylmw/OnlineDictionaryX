package serverclient;

/**
 * Created by limingwei on 16/11/21.
 */
public class RequestFromClient
{
    private String username;
    private String password;

    private boolean login;
    private boolean logout;

    private String words;
    private boolean chooseBaidu;
    private boolean chooseYoudao;
    private boolean chooseBing;
    private boolean praiseForBaidu;
    private boolean praiseForYoudao;
    private boolean praiseForBing;

    private String wordsTobeSend;
    private String accountTobeSend;

    public RequestFromClient(String username, String password, boolean login) {
        this.username = username;
        this.password = password;
        this.login = false;
    }

    public void setWords(String words) {
        this.words = words;
        this.praiseForBaidu = false;
        this.praiseForBing = false;
        this.praiseForBaidu = false;
    }

    public void setChooseBaidu(boolean chooseBaidu) {
        this.chooseBaidu = chooseBaidu;
    }

    public void setChooseYoudao(boolean chooseYoudao) {
        this.chooseYoudao = chooseYoudao;
    }

    public void setChooseBing(boolean chooseBing) {
        this.chooseBing = chooseBing;
    }


}
