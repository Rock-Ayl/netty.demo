package cn.ayl.pojo;

/**
 * 用户实体,当前登录用户的相关信息
 *
 * @Author ayl
 * @Date 2021-09-19
 */
public class User {

    //用户的cookieId
    private String cookieId;
    //用户id
    private Long userId;

    public String getCookieId() {
        return cookieId;
    }

    public void setCookieId(String cookieId) {
        this.cookieId = cookieId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

}
