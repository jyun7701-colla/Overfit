package com.overfit.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.*;

@WebServlet("/api/logout")
public class LogoutCon extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {

        res.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        res.getWriter().print("{\"success\":true}");
    }
}