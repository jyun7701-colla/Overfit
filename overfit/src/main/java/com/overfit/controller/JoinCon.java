package com.overfit.controller;

import com.overfit.database.UserDAO;
import com.overfit.model.UserVO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;

/**
 * Controller (Servlet)
 * 회원가입 / 아이디 중복확인 / 아이디 찾기 처리
 *
 * URL 매핑:
 *   POST /api/signup      → 회원가입
 *   GET  /api/check-id    → 아이디 중복확인
 *   POST /api/find-id     → 이메일로 아이디 찾기
 */
@WebServlet("/api/signup")
public class JoinCon extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    // ── POST /api/signup ── 회원가입
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        String body     = readBody(req);
        String user_id  = extractJson(body, "user_id");
        String user_pw  = extractJson(body, "user_pw");
        String nick     = extractJson(body, "nick");
        String phone    = extractJson(body, "phone");
        String email    = extractJson(body, "email");
        String gender   = extractJson(body, "gender");
        String birthdate= extractJson(body, "birthdate");
        String skin_type= extractJson(body, "skin_type");

        PrintWriter out = res.getWriter();

        // 필수 값 검증
        if (user_id.isEmpty() || user_pw.isEmpty() || nick.isEmpty()
                || phone.isEmpty() || email.isEmpty()) {
            res.setStatus(400);
            out.print("{\"success\":false,\"message\":\"필수 항목을 모두 입력해주세요.\"}");
            return;
        }

        // 아이디 중복 재확인 (보안)
        if (userDAO.isIdDuplicate(user_id)) {
            res.setStatus(409);
            out.print("{\"success\":false,\"message\":\"이미 사용 중인 아이디입니다.\"}");
            return;
        }

        // UserVO 생성 후 INSERT
        UserVO user = new UserVO();
        user.setUser_id(user_id);
        user.setUser_pw(user_pw);
        user.setNick(nick);
        user.setPhone(phone);
        user.setEmail(email);
        user.setGender(gender);
        user.setBirthdate(birthdate.isEmpty() ? null : birthdate);
        user.setSkin_type(skin_type);

        int result = userDAO.insertUser(user);
        if (result > 0) {
            out.print("{\"success\":true,\"message\":\"회원가입이 완료되었습니다.\"}");
        } else {
            res.setStatus(500);
            out.print("{\"success\":false,\"message\":\"서버 오류가 발생했습니다.\"}");
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
}
