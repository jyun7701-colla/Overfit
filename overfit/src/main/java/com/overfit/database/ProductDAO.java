package com.overfit.database;

import com.overfit.model.ProductVO;
import com.overfit.model.ReviewVO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object)
 * t_product, t_review, t_ingredient 테이블 담당
 */
public class ProductDAO {

    // ────────────────────────────────
    // 1. 전체 제품 조회 (클린 검색 - ad_flag = 'N')
    // ────────────────────────────────
    public List<ProductVO> getCleanProducts(String skin_type, String category) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM t_product p " +
            "WHERE EXISTS (" +
            "  SELECT 1 FROM t_review r " +
            "  WHERE r.prod_idx = p.prod_idx AND r.ad_flag = 'N'" +
            ")"
        );

        // 피부 타입 필터
        if (skin_type != null && !skin_type.isEmpty()) {
            sql.append(" AND p.target_skin LIKE ?");
        }
        // 카테고리 필터
        if (category != null && !category.isEmpty()) {
            sql.append(" AND p.category = ?");
        }
        sql.append(" ORDER BY p.prod_idx ASC");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProductVO> list = new ArrayList<>();
        int idx = 1;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql.toString());

            if (skin_type != null && !skin_type.isEmpty()) {
                pstmt.setString(idx++, "%" + skin_type + "%");
            }
            if (category != null && !category.isEmpty()) {
                pstmt.setString(idx++, category);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                ProductVO p = new ProductVO();
                p.setProd_idx(rs.getInt("prod_idx"));
                p.setProd_name(rs.getString("prod_name"));
                p.setBrand_name(rs.getString("brand_name"));
                p.setCategory(rs.getString("category"));
                p.setTarget_skin(rs.getString("target_skin"));
                p.setImg_url(rs.getString("img_url"));
                p.setCreated_at(rs.getString("created_at"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getCleanProducts 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return list;
    }

    // ────────────────────────────────
    // 2. 제품 상세 조회 (단건)
    // ────────────────────────────────
    public ProductVO getProductById(int prod_idx) {
        String sql = "SELECT * FROM t_product WHERE prod_idx = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ProductVO product = null;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod_idx);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                product = new ProductVO();
                product.setProd_idx(rs.getInt("prod_idx"));
                product.setProd_name(rs.getString("prod_name"));
                product.setBrand_name(rs.getString("brand_name"));
                product.setCategory(rs.getString("category"));
                product.setTarget_skin(rs.getString("target_skin"));
                product.setImg_url(rs.getString("img_url"));
                product.setCreated_at(rs.getString("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getProductById 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return product;
    }

    // ────────────────────────────────
    // 3. 피부 타입별 맞춤 추천 제품 조회
    //    (t_product × t_ingredient 조인)
    // ────────────────────────────────
    public List<ProductVO> getRecommendedProducts(String skin_type) {
        String sql =
            "SELECT DISTINCT p.* FROM t_product p " +
            "JOIN t_ingredient i ON p.prod_idx = i.prod_idx " +
            "WHERE p.target_skin LIKE ? " +
            "  AND i.allergy_check = 'N' " +        // 알레르기 성분 제외
            "  AND i.recommended_skin LIKE ? " +    // 권장 피부 타입 매칭
            "ORDER BY p.prod_idx ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<ProductVO> list = new ArrayList<>();

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, "%" + skin_type + "%");
            pstmt.setString(2, "%" + skin_type + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ProductVO p = new ProductVO();
                p.setProd_idx(rs.getInt("prod_idx"));
                p.setProd_name(rs.getString("prod_name"));
                p.setBrand_name(rs.getString("brand_name"));
                p.setCategory(rs.getString("category"));
                p.setTarget_skin(rs.getString("target_skin"));
                p.setImg_url(rs.getString("img_url"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getRecommendedProducts 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return list;
    }

    // ────────────────────────────────
    // 4. 광고 리뷰 수 조회 (ad_flag = 'Y')
    // ────────────────────────────────
    public int getAdReviewCount() {
        String sql = "SELECT COUNT(*) FROM t_review WHERE ad_flag = 'Y'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            rs    = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ProductDAO] getAdReviewCount 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return 0;
    }
}
