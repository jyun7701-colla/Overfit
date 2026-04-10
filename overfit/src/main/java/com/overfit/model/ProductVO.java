package com.overfit.model;

/**
 * VO (Value Object)
 * t_product 테이블과 1:1 매핑되는 클래스
 *
 * t_product 컬럼:
 *   prod_idx    INT          PK, AI
 *   prod_name   VARCHAR(100)
 *   brand_name  VARCHAR(100)
 *   category    VARCHAR(50)
 *   target_skin VARCHAR(50)
 *   img_url     VARCHAR(255)
 *   created_at  DATETIME     DEFAULT NOW()
 */
public class ProductVO {

    private int    prod_idx;
    private String prod_name;
    private String brand_name;
    private String category;
    private String target_skin;
    private String img_url;
    private String created_at;

    // ── Getter & Setter ──

    public int    getProd_idx()                  { return prod_idx; }
    public void   setProd_idx(int prod_idx)      { this.prod_idx = prod_idx; }

    public String getProd_name()                 { return prod_name; }
    public void   setProd_name(String prod_name) { this.prod_name = prod_name; }

    public String getBrand_name()                    { return brand_name; }
    public void   setBrand_name(String brand_name)   { this.brand_name = brand_name; }

    public String getCategory()                  { return category; }
    public void   setCategory(String category)   { this.category = category; }

    public String getTarget_skin()                     { return target_skin; }
    public void   setTarget_skin(String target_skin)   { this.target_skin = target_skin; }

    public String getImg_url()                   { return img_url; }
    public void   setImg_url(String img_url)     { this.img_url = img_url; }

    public String getCreated_at()                    { return created_at; }
    public void   setCreated_at(String created_at)   { this.created_at = created_at; }
}
