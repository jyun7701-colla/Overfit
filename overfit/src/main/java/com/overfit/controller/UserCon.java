package com.overfit.controller;

import com.overfit.database.UserDAO;
import com.overfit.model.UserVO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;

@WebServlet("/api/user/*")
public class UserCon extends HttpServlet {

    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        PrintWriter out  = res.getWriter();
        String pathInfo  = req.getPathInfo();
        String body      = readBody(req);

        // user_id: 세션 우선, 없으면 body에서 읽기
        HttpSession session = req.getSession(false);
        String loginId = (session != null) ? (String) session.getAttribute("user_id") : null;
        if (loginId == null || loginId.isEmpty()) {
            loginId = extract(body, "user_id");
        }

        // withdraw 제외하고 로그인 필수
        if ((loginId == null || loginId.isEmpty()) && !"/withdraw".equals(pathInfo)) {
            res.setStatus(401);
            out.print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        switch (pathInfo) {

            // ── 기본 정보 수정 ──
            case "/update": {
                String nick      = extract(body, "nick");
                String phone     = extract(body, "phone");
                String email     = extract(body, "email");
                String gender    = extract(body, "gender");
                String birthdate = extract(body, "birthdate");

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
                    if (session != null) {
                        session.setAttribute("nick",  nick);
                        session.setAttribute("email", email);
                    }
                    out.print("{\"success\":true,\"message\":\"정보가 수정되었습니다.\"}");
                } else {
                    res.setStatus(500);
                    out.print("{\"success\":false,\"message\":\"수정에 실패했습니다.\"}");
                }
                break;
            }

            // ── 비밀번호 변경 ──
            case "/password": {
                String curPw = extract(body, "cur_pw");
                String newPw = extract(body, "new_pw");

                if (curPw.isEmpty() || newPw.isEmpty() || newPw.length() < 8) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"비밀번호를 올바르게 입력해주세요.\"}");
                    return;
                }

                // UserVO에 cur_pw, new_pw, user_id 모두 담아서 전달
                UserVO user = new UserVO();
                user.setUser_id(loginId);
                user.setUser_pw(newPw);   // 새 비밀번호
                user.setCur_pw(curPw);    // 현재 비밀번호 (SQL에서 검증)

                int result = userDAO.updatePassword(user);
                if (result > 0) {
                    if (session != null) session.invalidate();
                    out.print("{\"success\":true,\"message\":\"비밀번호가 변경되었습니다.\"}");
                } else {
                    res.setStatus(401);
                    out.print("{\"success\":false,\"message\":\"현재 비밀번호가 틀렸습니다.\"}");
                }
                break;
            }

            // ── 피부 타입 수정 ──
            case "/skin": {
                String skin_type = extract(body, "skin_type");

                if (skin_type.isEmpty()) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"피부 타입을 선택해주세요.\"}");
                    return;
                }

                UserVO user = new UserVO();
                user.setUser_id(loginId);
                user.setSkin_type(skin_type);

                int result = userDAO.updateSkinType(user);
                if (result > 0) {
                    if (session != null) session.setAttribute("skin_type", skin_type);
                    out.print("{\"success\":true,\"skin_type\":\"" + skin_type + "\"}");
                } else {
                    res.setStatus(500);
                    out.print("{\"success\":false,\"message\":\"수정에 실패했습니다.\"}");
                }
                break;
            }

            // ── 회원 탈퇴 ──
            case "/withdraw": {
                String userId = extract(body, "user_id");
                String userPw = extract(body, "user_pw");

                // 세션에 있으면 세션 우선
                if (loginId != null && !loginId.isEmpty()) {
                    userId = loginId;
                }

                if (userId.isEmpty() || userPw.isEmpty()) {
                    res.setStatus(400);
                    out.print("{\"success\":false,\"message\":\"정보가 올바르지 않습니다.\"}");
                    return;
                }

                UserVO user = new UserVO();
                user.setUser_id(userId);
                user.setUser_pw(userPw);

                int result = userDAO.deleteUser(user);
                if (result > 0) {
                    if (session != null) session.invalidate();
                    out.print("{\"success\":true,\"message\":\"탈퇴가 완료되었습니다.\"}");
                } else {
                    res.setStatus(401);
                    out.print("{\"success\":false,\"message\":\"비밀번호가 틀렸습니다.\"}");
                }
                break;
            }

            default:
                res.setStatus(404);
                out.print("{\"success\":false,\"message\":\"잘못된 요청입니다.\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo();
        PrintWriter out = res.getWriter();

        // 아이디 중복 확인
        if ("/check-id".equals(pathInfo)) {
            String user_id = req.getParameter("user_id");
            if (user_id == null || user_id.trim().isEmpty()) {
                res.setStatus(400);
                out.print("{\"success\":false,\"message\":\"아이디를 입력해주세요.\"}");
                return;
            }
            boolean isDuplicate = userDAO.isIdDuplicate(user_id.trim());
            if (isDuplicate) {
                out.print("{\"success\":true,\"available\":false,\"message\":\"이미 사용 중인 아이디입니다.\"}");
            } else {
                out.print("{\"success\":true,\"available\":true,\"message\":\"사용 가능한 아이디입니다.\"}");
            }

        // 이메일로 아이디 찾기
        } else if ("/find-id".equals(pathInfo)) {
            String email   = req.getParameter("email");
            String foundId = userDAO.findIdByEmail(email);
            if (foundId != null) {
                out.print("{\"success\":true,\"user_id\":\"" + foundId + "\"}");
            } else {
                res.setStatus(404);
                out.print("{\"success\":false,\"message\":\"해당 이메일로 가입된 아이디가 없습니다.\"}");
            }
        }
    }

    // ── 공통 유틸 ──
    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private String extract(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colon = json.indexOf(":", idx) + 1;
        int start = json.indexOf("\"", colon) + 1;
        int end   = json.indexOf("\"", start);
        if (start <= 0 || end <= 0) return "";
        return json.substring(start, end).trim();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}