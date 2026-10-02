package com.helpdesk.web.controller;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Racine de l'application ("" = exactement la racine du contexte). */
@WebServlet("")
public class HomeController extends BaseController {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        redirect(req, resp, "/users"); // Deviendra "/tickets" en partie B
    }
}