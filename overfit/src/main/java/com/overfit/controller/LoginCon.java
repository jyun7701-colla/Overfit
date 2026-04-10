package com.overfit.controller;

import com.overfit.database.UserDAO;
import com.overfit.model.UserVO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;

/**
 * Controller (Servlet)
 * 로그인 / 회원가입 / 로그아웃 / 아이디 중복확인 / 아이디 찾기 처리
 *
 * URL 매핑:
 *   POST /api/login          → 로그인
 *   POST /api/signup         → 회원가입
 *   GET  /api/logout         → 로그아웃
 *   GET  /api/check-id       → 아이디 중복확인
 *   POST /api/find-id        → 이메일로 아이디 찾기
 */
@WebServlet("/api/login")
public class LoginCon extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    // ── POST /api/login ──
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        // 요청 JSON 읽기
        String body   = readBody(req);
        String userId = extractJson(body, "user_id");
        String userPw = extractJson(body, "user_pw");

        PrintWriter out = res.getWriter();
        UserVO user = userDAO.loginUser(userId, userPw);

        if (user != null) {
            // 세션에 로그인 정보 저장
            HttpSession session = req.getSession(true);
            session.setAttribute("user_id",   user.getUser_id());
            session.setAttribute("nick",      user.getNick());
            session.setAttribute("email",     user.getEmail());
            session.setAttribute("skin_type", user.getSkin_type());

            out.print("{\"success\":true,"
                    + "\"user_id\":\""   + user.getUser_id()   + "\","
                    + "\"nick\":\""      + user.getNick()       + "\","
                    + "\"email\":\""     + user.getEmail()      + "\","
                    + "\"skin_type\":\"" + nvl(user.getSkin_type()) + "\"}");
        } else {
            res.setStatus(401);
            out.print("{\"success\":false,\"message\":\"아이디 또는 비밀번호가 틀렸습니다.\"}");
        }
    }

    // ── GET /api/logout ──
    @WebServlet("/api/logout")
    public static class LogoutCon extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse res)
                throws IOException {
            res.setContentType("application/json;charset=UTF-8");
            HttpSession session = req.getSession(false);
            if (session != null) session.invalidate();
            res.getWriter().print("{\"success\":true}");
        }
    }

    // ── 공통 유틸 ──
    String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    String extractJson(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colon = json.indexOf(":", idx) + 1;
        int start = json.indexOf("\"", colon) + 1;
        int end   = json.indexOf("\"", start);
        if (start <= 0 || end <= 0) return "";
        return json.substring(start, end).trim();
    }

    String nvl(String s) { return s == null ? "" : s; }
}
