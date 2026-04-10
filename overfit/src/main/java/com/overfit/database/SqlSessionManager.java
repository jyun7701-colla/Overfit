package com.overfit.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MariaDB 연결을 관리하는 유틸리티 클래스
 *
 * 사용법:
 *   Connection conn = SqlSessionManager.getConnection();
 *   // ... DB 작업 ...
 *   SqlSessionManager.close(conn, pstmt, rs);
 */
public class SqlSessionManager {

    // ── DB 연결 정보 (본인 환경에 맞게 수정) ──
    private static final String DRIVER   = "org.mariadb.jdbc.Driver";
    private static final String URL      = "jdbc:mariadb://localhost:3306/overfit";
    private static final String DB_USER  = "root";        // MariaDB 계정
    private static final String DB_PASS  = "1234";        // MariaDB 비밀번호

    // 드라이버 한 번만 로딩
    static {
        try {
            Class.forName(DRIVER);
            System.out.println("[DB] MariaDB 드라이버 로딩 성공");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] 드라이버 로딩 실패: " + e.getMessage());
        }
    }

    /**
     * DB 커넥션 반환
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    /**
     * 자원 해제 (Connection + PreparedStatement + ResultSet)
     */
    public static void close(AutoCloseable... resources) {
        for (AutoCloseable r : resources) {
            if (r != null) {
                try { r.close(); }
                catch (Exception e) { System.err.println("[DB] 자원 해제 오류: " + e.getMessage()); }
            }
        }
    }
}
