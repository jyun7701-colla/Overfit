package com.overfit.controller;

import com.overfit.database.UserDAO;
import com.overfit.model.UserVO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;

/**
 * Controller (Servlet)
 * 회원 정보 수정 / 비밀번호 변경 / 피부타입 수정 /
 * 회원 탈퇴 / 아이디 중복확인 / 아이디 찾기
 *
 * URL 매핑:
 *   POST /api/user/update    → 기본 정보 수정
 *   POST /api/user/password  → 비밀번호 변경
 *   POST /api/user/skin      → 피부 타입 수정
 *   POST /api/user/withdraw  → 회원 탈퇴
 *   GET  /api/check-id       → 아이디 중복확인
 *   POST /api/find-id        → 이메일로 아이디 찾기
 */
@WebServlet("/api/user/*")
public class UserCon extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        // 로그인 세션 확인
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            res.setStatus(401);
            res.getWriter().print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        String loginId = (String) session.getAttribute("user_id");

        // URL 경로에 따라 분기
        String pathInfo = req.getPathInfo(); // /update, /password, /skin, /withdraw

        String body = readBody(req);
        PrintWriter out = res.getWriter();

        switch (pathInfo) {

            // ── 기본 정보 수정 ──
            case "/update": {
                String nick      = extractJson(body, "nick");
                String phone     = extractJson(body, "phone");
                String email     = extractJson(body, "email");
                String gender    = extractJson(body, "gender");
                String birthdate = extractJson(body, "birthdate");

                if (nick.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"필수 항목을 입력해주세요.\"}");
                    return;
                }

                UserVO user = new UserVO();
                user.setUser_id(loginId);
                user.setNick(nick);
                user.setPhone(phone);
                user.setEmail(email);
                user.setGender(gender);
                user.setBirthdate(birthdate);

                int result = userDAO.updateUser(user);
                if (result > 0) {
                    session.setAttribute("nick",  nick);
                    session.setAttribute("email", email);
                    out.print("{\"success\":true,\"message\":\"정보가 수정되었습니다.\"}");
                } else {
                    res.setStatus(500);
                    out.print("{\"success\":false,\"message\":\"수정에 실패했습니다.\"}");
                }
                break;
            }

            // ── 비밀번호 변경 ──
            case "/password": {
                String curPw = extractJson(body, "cur_pw");
                String newPw = extractJson(body, "new_pw");

                if (curPw.isEmpty() || newPw.isEmpty() || newPw.length() < 8) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"비밀번호를 올바르게 입력해주세요.\"}");
                    return;
                }

                int result = userDAO.updatePassword(loginId, curPw, newPw);
                if (result > 0) {
                    // 비밀번호 변경 후 로그아웃
                    session.invalidate();
                    out.print("{\"success\":true,\"message\":\"비밀번호가 변경되었습니다. 다시 로그인해주세요.\"}");
                } else if (result == -1) {
                    res.setStatus(401);
                    out.print("{\"success\":false,\"message\":\"현재 비밀번호가 일치하지 않습니다.\"}");
                } else {
                    res.setStatus(500);
                    out.print("{\"success\":false,\"message\":\"변경에 실패했습니다.\"}");
                }
                break;
            }

            // ── 피부 타입 수정 ──
            case "/skin": {
                String skin_type = extractJson(body, "skin_type");

                if (skin_type.isEmpty()) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"피부 타입을 선택해주세요.\"}");
                    return;
                }

                int result = userDAO.updateSkinType(loginId, skin_type);
                if (result > 0) {
                    session.setAttribute("skin_type", skin_type);
                    out.print("{\"success\":true,\"message\":\"피부 타입이 수정되었습니다.\","
                            + "\"skin_type\":\"" + skin_type + "\"}");
                } else {
                    res.setStatus(500);
                    out.print("{\"success\":false,\"message\":\"수정에 실패했습니다.\"}");
                }
                break;
            }

            // ── 회원 탈퇴 ──
            case "/withdraw": {
                String user_pw = extractJson(body, "user_pw");

                if (user_pw.isEmpty()) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"비밀번호를 입력해주세요.\"}");
                    return;
                }

                int result = userDAO.deleteUser(loginId, user_pw);
                if (result > 0) {
                    session.invalidate();
                    out.print("{\"success\":true,\"message\":\"탈퇴가 완료되었습니다.\"}");
                } else {
                    res.setStatus(401);
                    out.print("{\"success\":false,\"message\":\"비밀번호가 일치하지 않습니다.\"}");
                }
                break;
            }

            default:
                res.setStatus(404);
                out.print("{\"success\":false,\"message\":\"잘못된 요청입니다.\"}");
        }
    }

    // ── GET /api/check-id?user_id=xxx → 아이디 중복확인 ──
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = res.getWriter();

        // GET /api/user/check-id?user_id=xxx
        if ("/check-id".equals(pathInfo)) {
            String user_id = req.getParameter("user_id");

            if (user_id == null || user_id.trim().isEmpty()) {
                res.setStatus(400);
                out.print("{\"success\":false,\"message\":\"아이디를 입력해주세요.\"}");
                return;
            }

            boolean isDuplicate = userDAO.isIdDuplicate(user_id.trim());
            if (isDuplicate) {
                out.print("{\"success\":true,\"available\":false,"
                        + "\"message\":\"이미 사용 중인 아이디입니다.\"}");
            } else {
                out.print("{\"success\":true,\"available\":true,"
                        + "\"message\":\"사용 가능한 아이디입니다.\"}");
            }

        // GET /api/user/find-id?email=xxx
        } else if ("/find-id".equals(pathInfo)) {
            String email = req.getParameter("email");

            if (email == null || email.trim().isEmpty()) {
                res.setStatus(400);
                out.print("{\"success\":false,\"message\":\"이메일을 입력해주세요.\"}");
                return;
            }

            String foundId = userDAO.findIdByEmail(email.trim());
            if (foundId != null) {
                out.print("{\"success\":true,\"user_id\":\"" + foundId + "\"}");
            } else {
                res.setStatus(404);
                out.print("{\"success\":false,\"message\":\"해당 이메일로 가입된 아이디가 없습니다.\"}");
            }
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
