package com.overfit.database;

import com.overfit.model.UserVO;

import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object)
 * t_user 테이블의 CRUD 담당
 */
public class UserDAO {

    // ── 비밀번호 SHA-256 해시 ──
    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("비밀번호 해시 오류", e);
        }
    }

    // ────────────────────────────────
    // 1. 회원가입 (INSERT)
    // ────────────────────────────────
    public int insertUser(UserVO user) {
        String sql = "INSERT INTO t_user (user_id, user_pw, nick, phone, email, gender, birthdate, skin_type) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getUser_id());
            pstmt.setString(2, sha256(user.getUser_pw()));   // 비밀번호 해시 저장
            pstmt.setString(3, user.getNick());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getGender());
            pstmt.setString(7, user.getBirthdate());
            pstmt.setString(8, user.getSkin_type());
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] insertUser 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 2. 로그인 (SELECT)
    // ────────────────────────────────
    public UserVO loginUser(String user_id, String user_pw) {
        String sql = "SELECT * FROM t_user WHERE user_id = ? AND user_pw = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        UserVO user = null;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user_id);
            pstmt.setString(2, sha256(user_pw));   // 해시 비교
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new UserVO();
                user.setUser_id(rs.getString("user_id"));
                user.setNick(rs.getString("nick"));
                user.setPhone(rs.getString("phone"));
                user.setEmail(rs.getString("email"));
                user.setGender(rs.getString("gender"));
                user.setBirthdate(rs.getString("birthdate"));
                user.setSkin_type(rs.getString("skin_type"));
                user.setJoined_at(rs.getString("joined_at"));
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] loginUser 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return user;
    }

    // ────────────────────────────────
    // 3. 아이디 중복 확인 (SELECT)
    // ────────────────────────────────
    public boolean isIdDuplicate(String user_id) {
        String sql = "SELECT COUNT(*) FROM t_user WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user_id);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[UserDAO] isIdDuplicate 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return false;
    }

    // ────────────────────────────────
    // 4. 회원 정보 수정 (UPDATE)
    // ────────────────────────────────
    public int updateUser(UserVO user) {
        String sql = "UPDATE t_user SET nick=?, phone=?, email=?, gender=?, birthdate=? "
                   + "WHERE user_id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getNick());
            pstmt.setString(2, user.getPhone());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getGender());
            pstmt.setString(5, user.getBirthdate());
            pstmt.setString(6, user.getUser_id());
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateUser 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 5. 비밀번호 변경 (UPDATE)
    // ────────────────────────────────
    public int updatePassword(String user_id, String cur_pw, String new_pw) {
        // 현재 비밀번호 먼저 확인
        String checkSql = "SELECT COUNT(*) FROM t_user WHERE user_id=? AND user_pw=?";
        String updateSql = "UPDATE t_user SET user_pw=? WHERE user_id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int result = 0;

        try {
            conn = SqlSessionManager.getConnection();

            // 현재 비밀번호 확인
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, user_id);
            pstmt.setString(2, sha256(cur_pw));
            rs = pstmt.executeQuery();
            rs.next();
            if (rs.getInt(1) == 0) return -1;   // -1 = 현재 비밀번호 불일치
            SqlSessionManager.close(rs, pstmt);

            // 새 비밀번호로 변경
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, sha256(new_pw));
            pstmt.setString(2, user_id);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] updatePassword 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 6. 피부 타입 수정 (UPDATE)
    // ────────────────────────────────
    public int updateSkinType(String user_id, String skin_type) {
        String sql = "UPDATE t_user SET skin_type=? WHERE user_id=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, skin_type);
            pstmt.setString(2, user_id);
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] updateSkinType 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 7. 회원 탈퇴 (DELETE)
    // ────────────────────────────────
    public int deleteUser(String user_id, String user_pw) {
        String sql = "DELETE FROM t_user WHERE user_id=? AND user_pw=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        int result = 0;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user_id);
            pstmt.setString(2, sha256(user_pw));
            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] deleteUser 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(pstmt, conn);
        }
        return result;
    }

    // ────────────────────────────────
    // 8. 이메일로 아이디 찾기 (SELECT)
    // ────────────────────────────────
    public String findIdByEmail(String email) {
        String sql = "SELECT user_id FROM t_user WHERE email=?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn  = SqlSessionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("user_id");
        } catch (SQLException e) {
            System.err.println("[UserDAO] findIdByEmail 오류: " + e.getMessage());
        } finally {
            SqlSessionManager.close(rs, pstmt, conn);
        }
        return null;
    }
}
