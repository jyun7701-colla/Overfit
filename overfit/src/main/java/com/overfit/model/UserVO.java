package com.overfit.model;

import java.util.Date;

/**
 * VO (Value Object)
 * t_user 테이블과 1:1 매핑되는 클래스
 *
 * t_user 컬럼:
 *   user_id   VARCHAR(50)  PK
 *   user_pw   VARCHAR(255)
 *   nick      VARCHAR(20)
 *   phone     VARCHAR(20)
 *   email     VARCHAR(50)
 *   gender    CHAR(1)
 *   birthdate DATE
 *   skin_type VARCHAR(50)
 *   joined_at DATETIME     DEFAULT NOW()
 */
public class UserVO {

    private String user_id;
    private String user_pw;
    private String nick;
    private String phone;
    private String email;
    private String gender;
    private String birthdate;
    private String skin_type;
    private String joined_at;
    

    private String cur_pw;

    public String getCur_pw()              { return cur_pw; }
    public void   setCur_pw(String cur_pw) { this.cur_pw = cur_pw; }

    // ── Getter & Setter ──

    public String getUser_id()               { return user_id; }
    public void   setUser_id(String user_id) { this.user_id = user_id; }

    public String getUser_pw()               { return user_pw; }
    public void   setUser_pw(String user_pw) { this.user_pw = user_pw; }

    public String getNick()                  { return nick; }
    public void   setNick(String nick)       { this.nick = nick; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String phone)     { this.phone = phone; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    public String getGender()                { return gender; }
    public void   setGender(String gender)   { this.gender = gender; }

    public String getBirthdate()                   { return birthdate; }
    public void   setBirthdate(String birthdate)   { this.birthdate = birthdate; }

    public String getSkin_type()                   { return skin_type; }
    public void   setSkin_type(String skin_type)   { this.skin_type = skin_type; }

    public String getJoined_at()                   { return joined_at; }
    public void   setJoined_at(String joined_at)   { this.joined_at = joined_at; }

    @Override
    public String toString() {
        return "UserVO{user_id='" + user_id + "', nick='" + nick +
               "', email='" + email + "', skin_type='" + skin_type + "'}";
    }
}
