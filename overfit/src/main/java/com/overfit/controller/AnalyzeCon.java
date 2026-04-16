package com.overfit.controller;

import com.overfit.database.ProductDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;

@WebServlet("/api/analyze")
@MultipartConfig(
    maxFileSize    = 10 * 1024 * 1024,
    maxRequestSize = 15 * 1024 * 1024
)
public class AnalyzeCon extends HttpServlet {

    private static final String PYTHON_URL = "http://localhost:8000/analyze";
    private ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();

        Part filePart = req.getPart("photo");
        if (filePart == null || filePart.getSize() == 0) {
            res.setStatus(400);
            out.print("{\"success\":false,\"message\":\"사진을 업로드해주세요.\"}");
            return;
        }

        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            res.setStatus(400);
            out.print("{\"success\":false,\"message\":\"이미지 파일만 업로드 가능합니다.\"}");
            return;
        }

        byte[] imageBytes = filePart.getInputStream().readAllBytes();
        String pythonResult = sendToPython(imageBytes, contentType);

        if (pythonResult == null) {
            res.setStatus(500);
            out.print("{\"success\":false,\"message\":\"분석 서버에 연결할 수 없습니다.\"}");
            return;
        }

        String personalColor = extractJson(pythonResult, "personal_color");

        if (personalColor.isEmpty()) {
            out.print(pythonResult);
            return;
        }

        List<Map> celebs = productDAO.getCelebByPersonalColor(personalColor);

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,");
        json.append("\"personal_color\":\"").append(personalColor).append("\",");
        json.append("\"description\":\"").append(extractJson(pythonResult, "description")).append("\",");
        json.append("\"colors\":\"").append(extractJson(pythonResult, "colors")).append("\",");
        json.append("\"avoid\":\"").append(extractJson(pythonResult, "avoid")).append("\",");
        json.append("\"makeup\":\"").append(extractJson(pythonResult, "makeup")).append("\",");
        json.append("\"celebs\":[");
        for (int i = 0; i < celebs.size(); i++) {
            Map celeb = celebs.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"celeb_name\":\"").append(nvl(celeb.get("celeb_name"))).append("\",")
                .append("\"celeb_skintype\":\"").append(nvl(celeb.get("celeb_skintype"))).append("\",")
                .append("\"prod_name\":\"").append(nvl(celeb.get("prod_name"))).append("\",")
                .append("\"brand_name\":\"").append(nvl(celeb.get("brand_name"))).append("\"")
                .append("}");
        }
        json.append("]}");
        out.print(json.toString());
    }

    private String sendToPython(byte[] imageBytes, String contentType) {
        try {
            String boundary = "----Boundary" + System.currentTimeMillis();
            URL url = new URL(PYTHON_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                String ext    = contentType.contains("png") ? "png" : "jpg";
                String header = "--" + boundary + "\r\n"
                		+ "Content-Disposition: form-data; name=\"photo\"; filename=\"photo." + ext + "\"\r\n"
                        + "Content-Type: " + contentType + "\r\n\r\n";
                os.write(header.getBytes("UTF-8"));
                os.write(imageBytes);
                os.write(("\r\n--" + boundary + "--\r\n").getBytes("UTF-8"));
            }

            int responseCode = conn.getResponseCode();
            InputStream is   = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            System.out.println("[DEBUG] Python 응답: " + sb.toString());
            return sb.toString();

        } catch (Exception e) {
            System.err.println("[AnalyzeCon] Python 서버 연결 실패: " + e.getMessage());
            return null;
        }
    }
    
    

    private String extractJson(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return "";
        int colon = json.indexOf(":", idx) + 1;
        int start = json.indexOf("\"", colon) + 1;
        int end   = json.indexOf("\"", start);
        if (start <= 0 || end <= 0) return "";
        return json.substring(start, end).trim();
    }

    private String nvl(Object o) {
        return o == null ? "" : o.toString();
    }
}