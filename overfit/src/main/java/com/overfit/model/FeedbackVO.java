package com.overfit.model;

/**
 * VO (Value Object)
 * t_feedback 테이블과 1:1 매핑되는 클래스
 *
 * t_feedback 컬럼:
 *   fb_idx      INT          PK, AI
 *   reco_idx    INT          FK → t_recommendation
 *   user_id     VARCHAR(50)  FK → t_user
 *   fb_content  TEXT
 *   fb_img      VARCHAR(255)
 *   created_at  DATETIME     DEFAULT NOW()
 */
public class FeedbackVO {

    private int    fb_idx;
    private int    reco_idx;
    private String user_id;
    private String fb_content;
    private String fb_img;
    private double rating;
    private String created_at;

    // ── Getter & Setter ──

    public int    getFb_idx()                    { return fb_idx; }
    public void   setFb_idx(int fb_idx)          { this.fb_idx = fb_idx; }

    public int    getReco_idx()                  { return reco_idx; }
    public void   setReco_idx(int reco_idx)      { this.reco_idx = reco_idx; }

    public String getUser_id()                   { return user_id; }
    public void   setUser_id(String user_id)     { this.user_id = user_id; }

    public String getFb_content()                        { return fb_content; }
    public void   setFb_content(String fb_content)       { this.fb_content = fb_content; }

    public String getFb_img()                    { return fb_img; }
    public void   setFb_img(String fb_img)       { this.fb_img = fb_img; }

    public double getRating()                    { return rating; }
    public void   setRating(double rating)       { this.rating = rating; }

    public String getCreated_at()                        { return created_at; }
    public void   setCreated_at(String created_at)       { this.created_at = created_at; }
}
