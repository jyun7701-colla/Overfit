package com.overfit.model;

/**
 * VO (Value Object)
 * t_review 테이블과 1:1 매핑되는 클래스
 *
 * t_review 컬럼:
 *   review_idx     INT           PK, AI
 *   prod_idx       INT           FK → t_product
 *   review_content TEXT
 *   rating         DECIMAL(2,1)
 *   ad_flag        CHAR(1)       'Y'=광고 / 'N'=클린
 *   review_url     VARCHAR(255)
 *   created_at     DATETIME      DEFAULT NOW()
 */
public class ReviewVO {

    private int    review_idx;
    private int    prod_idx;
    private String review_content;
    private double rating;
    private String ad_flag;
    private String review_url;
    private String created_at;

    // ── Getter & Setter ──

    public int    getReview_idx()                      { return review_idx; }
    public void   setReview_idx(int review_idx)        { this.review_idx = review_idx; }

    public int    getProd_idx()                        { return prod_idx; }
    public void   setProd_idx(int prod_idx)            { this.prod_idx = prod_idx; }

    public String getReview_content()                          { return review_content; }
    public void   setReview_content(String review_content)    { this.review_content = review_content; }

    public double getRating()                          { return rating; }
    public void   setRating(double rating)             { this.rating = rating; }

    public String getAd_flag()                         { return ad_flag; }
    public void   setAd_flag(String ad_flag)           { this.ad_flag = ad_flag; }

    public String getReview_url()                          { return review_url; }
    public void   setReview_url(String review_url)         { this.review_url = review_url; }

    public String getCreated_at()                          { return created_at; }
    public void   setCreated_at(String created_at)         { this.created_at = created_at; }
}
