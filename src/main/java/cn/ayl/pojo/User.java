package cn.ayl.pojo;

/**
 * @Author ayl
 * @Date 2021-09-19
 */
public class User {

    //用户的cookieId
    private String cookieId = null;
    //用户id
    private long userId;

    public String getCookieId() {
        return cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
