package serverclient;

/**
 * Created by limingwei on 16/11/21.
 */
public class RequestFromClient
{
    private String username;
    private String password;
    private boolean isOnline;
    //---------------------------

    private String words;
    private boolean isSearch;
    //---------------------------

    private boolean praiseForBaidu;
    private boolean praiseForYoudao;
    private boolean praiseForBing;
    private boolean isPraise;
    //---------------------------

    private String wordsTobeSend;
    private String accountTobeSend;
    private boolean isSend;
    //---------------------------

    public RequestFromClient(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public void setPraiseForBaidu(boolean praiseForBaidu){
        this.praiseForBaidu = praiseForBaidu;
    }

    public void setPraiseForBing(boolean praiseForBing){
        this.praiseForBing = praiseForBing;
    }

    public void setPraiseForYoudao(boolean praiseForYoudao){
        this.praiseForYoudao = praiseForYoudao;
    }

    public void setWordsTobeSend(String wordsTobeSend){
        this.wordsTobeSend = wordsTobeSend;
    }

    public void setIsOnline(boolean isOnline){
        this.isOnline = isOnline;
    }

    public void setIsSearch(boolean isSearch){
        this.isSearch = isSearch;
    }

    public void setIsPraise(boolean isPraise){
        this.isPraise = isPraise;
    }

    public void setIsSend(boolean isSend){
        this.isSend = isSend;
    }

    //-------------------------------------------

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getWords(){
        return words;
    }

    public boolean getPraiseForBaidu(){
        return praiseForBaidu;
    }

    public boolean getPraiseForYoudao(){
        return praiseForYoudao;
    }

    public boolean getPraiseForBing(){
        return praiseForBing;
    }

    public String getWordsTobeSend(){
        return wordsTobeSend;
    }

    public String getAccountTobeSend(){
        return accountTobeSend;
    }

    public boolean getIsOnline(){
        return isOnline;
    }

    public boolean getIsSearch(){
        return isSearch;
    }

    public boolean getIsPraise(){
        return isPraise;
    }

    public boolean getIsSend(){
        return isSend;
    }
}
