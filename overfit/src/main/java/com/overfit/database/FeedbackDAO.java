package com.overfit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.overfit.model.FeedbackVO;

/**
 * DAO (Data Access Object)
 * t_feedback 테이블의 CRUD 담당
 */
public class FeedbackDAO {

    // ────────────────────────────────
    // 1. 피드백(리뷰) 등록 (INSERT)
    // ────────────────────────────────
    public int insertFeedback(FeedbackVO fb) {
        String sql = "INSERT INTO t_feedback (reco_idx, user_id, fb_content, fb_img) "
                   + "VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, fb.getReco_idx());
            pstmt.setString(2, fb.getUser_id());
            pstmt.setString(3, fb.getFb_content());
            pstmt.setString(4, fb.getFb_img());
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] insertFeedback 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 2. 특정 제품의 피드백 목록 조회
    // ────────────────────────────────
    public List<FeedbackVO> getFeedbackByProduct(int prod_idx) {
        String sql =
            "SELECT f.* FROM t_feedback f " +
            "JOIN t_recommendation r ON f.reco_idx = r.reco_idx " +
            "WHERE r.prod_idx = ? " +
            "ORDER BY f.created_at DESC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FeedbackVO> list = new ArrayList<>();

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, prod_idx);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackVO fb = new FeedbackVO();
                fb.setFb_idx(rs.getInt("fb_idx"));
                fb.setReco_idx(rs.getInt("reco_idx"));
                fb.setUser_id(rs.getString("user_id"));
                fb.setFb_content(rs.getString("fb_content"));
                fb.setFb_img(rs.getString("fb_img"));
                fb.setCreated_at(rs.getString("created_at"));
                list.add(fb);
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getFeedbackByProduct 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return list;
    }

    // ────────────────────────────────
    // 3. 내 피드백 목록 조회
    // ────────────────────────────────
    public List<FeedbackVO> getFeedbackByUser(String user_id) {
        String sql = "SELECT * FROM t_feedback WHERE user_id = ? ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<FeedbackVO> list = new ArrayList<>();

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user_id);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                FeedbackVO fb = new FeedbackVO();
                fb.setFb_idx(rs.getInt("fb_idx"));
                fb.setReco_idx(rs.getInt("reco_idx"));
                fb.setUser_id(rs.getString("user_id"));
                fb.setFb_content(rs.getString("fb_content"));
                fb.setFb_img(rs.getString("fb_img"));
                fb.setCreated_at(rs.getString("created_at"));
                list.add(fb);
            }
        } catch (SQLException e) {
            System.err.println("[FeedbackDAO] getFeedbackByUser 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return list;
    }
}
