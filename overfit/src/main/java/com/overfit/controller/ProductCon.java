package com.overfit.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.overfit.database.FeedbackDAO;
import com.overfit.database.ProductDAO;
import com.overfit.model.FeedbackVO;
import com.overfit.model.ProductVO;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller (Servlet)
 * 클린 검색 / 맞춤 추천 / 피드백 등록 처리
 *
 * URL 매핑:
 *   GET  /api/products           → 클린 검색 (ad_flag=N)
 *   GET  /api/products/recommend → 피부 타입 맞춤 추천
 *   POST /api/feedback           → 리뷰(피드백) 등록
 */
@WebServlet("/api/products/*")
public class ProductCon extends HttpServlet {

    private ProductDAO  productDAO  = new ProductDAO();
    private FeedbackDAO feedbackDAO = new FeedbackDAO();

    // ── GET ──
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");

        String pathInfo = req.getPathInfo(); // null 또는 /recommend
        PrintWriter out = res.getWriter();

        // ── 클린 검색: GET /api/products ──
        if (pathInfo == null || pathInfo.equals("/")) {
            String skin_type = nvl(req.getParameter("skin_type"));
            String category  = nvl(req.getParameter("category"));

            List<ProductVO> list = productDAO.getCleanProducts(skin_type, category);
            int adCount = productDAO.getAdReviewCount();

            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"ad_removed\":").append(adCount)
                .append(",\"count\":").append(list.size())
                .append(",\"products\":[");

            for (int i = 0; i < list.size(); i++) {
                ProductVO p = list.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"prod_idx\":").append(p.getProd_idx()).append(",")
                    .append("\"prod_name\":\"").append(p.getProd_name()).append("\",")
                    .append("\"brand_name\":\"").append(p.getBrand_name()).append("\",")
                    .append("\"category\":\"").append(p.getCategory()).append("\",")
                    .append("\"target_skin\":\"").append(nvl(p.getTarget_skin())).append("\",")
                    .append("\"img_url\":\"").append(nvl(p.getImg_url())).append("\"")
                    .append("}");
            }
            json.append("]}");
            out.print(json.toString());

        // ── 맞춤 추천: GET /api/products/recommend?skin_type=건성 ──
        } else if (pathInfo.equals("/recommend")) {

            // 로그인 세션에서 skin_type 가져오기
            HttpSession session = req.getSession(false);
            String skin_type = req.getParameter("skin_type");
            if ((skin_type == null || skin_type.isEmpty()) && session != null) {
                skin_type = (String) session.getAttribute("skin_type");
            }
            if (skin_type == null) skin_type = "";

            List<ProductVO> list = productDAO.getRecommendedProducts(skin_type);

            StringBuilder json = new StringBuilder();
            json.append("{\"success\":true,\"skin_type\":\"").append(skin_type)
                .append("\",\"count\":").append(list.size())
                .append(",\"products\":[");

            for (int i = 0; i < list.size(); i++) {
                ProductVO p = list.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"prod_idx\":").append(p.getProd_idx()).append(",")
                    .append("\"prod_name\":\"").append(p.getProd_name()).append("\",")
                    .append("\"brand_name\":\"").append(p.getBrand_name()).append("\",")
                    .append("\"category\":\"").append(p.getCategory()).append("\",")
                    .append("\"target_skin\":\"").append(nvl(p.getTarget_skin())).append("\"")
                    .append("}");
            }
            json.append("]}");
            out.print(json.toString());

        } else {
            res.setStatus(404);
            out.print("{\"success\":false,\"message\":\"잘못된 요청입니다.\"}");
        }
    }

    // ── POST /api/feedback → 피드백(리뷰) 등록 ──
    @WebServlet("/api/feedback")
    public static class FeedbackCon extends HttpServlet {

        private FeedbackDAO feedbackDAO = new FeedbackDAO();

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse res)
                throws IOException {

            req.setCharacterEncoding("UTF-8");
            res.setContentType("application/json;charset=UTF-8");

            // 로그인 확인
            HttpSession session = req.getSession(false);
            PrintWriter out = res.getWriter();

            if (session == null || session.getAttribute("user_id") == null) {
                res.setStatus(401);
                out.print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
                return;
            }

            String loginId = (String) session.getAttribute("user_id");
            String body    = readBody(req);

            String recoIdxStr  = extractJson(body, "reco_idx");
            String fb_content  = extractJson(body, "fb_content");
            String fb_img      = extractJson(body, "fb_img");

            if (fb_content.isEmpty() || fb_content.length() < 10) {
                res.setStatus(400);
                out.print("{\"success\":false,\"message\":\"리뷰 내용을 10자 이상 입력해주세요.\"}");
                return;
            }

            FeedbackVO fb = new FeedbackVO();
            fb.setUser_id(loginId);
            fb.setReco_idx(recoIdxStr.isEmpty() ? 0 : Integer.parseInt(recoIdxStr));
            fb.setFb_content(fb_content);
            fb.setFb_img(fb_img.isEmpty() ? null : fb_img);

            int result = feedbackDAO.insertFeedback(fb);
            if (result > 0) {
                out.print("{\"success\":true,\"message\":\"리뷰가 등록되었습니다.\"}");
            } else {
                res.setStatus(500);
                out.print("{\"success\":false,\"message\":\"등록에 실패했습니다.\"}");
            }
        }

        static String readBody(HttpServletRequest req) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line;
            try (BufferedReader reader = req.getReader()) {
                while ((line = reader.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        }

        static String extractJson(String json, String key) {
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

    // ── 유틸 ──
    String nvl(String s) { return s == null ? "" : s; }

    String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
