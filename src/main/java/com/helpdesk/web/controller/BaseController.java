package com.helpdesk.web.controller;

import java.io.IOException;

import com.helpdesk.config.AppContext;
import com.helpdesk.exception.HelpdeskException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.web.BadRequestException;
import com.helpdesk.web.FlashMessage;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Socle commun des contrôleurs : accès aux services, vues, redirections, erreurs. */
public abstract class BaseController extends HttpServlet {

    private static final String VIEWS = "/WEB-INF/views/";

    private transient AppContext appContext;

    @Override
    public void init() throws ServletException {
        appContext = (AppContext) getServletContext().getAttribute(AppContext.ATTRIBUTE);
        if (appContext == null) {
            throw new ServletException("AppContext non initialisé : vérifier AppStartupListener");
        }
    }

    protected AppContext app() {
        return appContext;
    }

    /** Gestion centralisée des exceptions non traitées par les contrôleurs. */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            super.service(req, resp);
        } catch (ResourceNotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (BadRequestException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (HelpdeskException e) {
            resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
        }
        // Toute autre exception remonte à Tomcat : page 500 (web.xml) et trace dans les logs
    }

    // --- Navigation ---

    protected void render(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher(VIEWS + view + ".jsp").forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void flash(HttpServletRequest req, String type, String message) {
        req.getSession().setAttribute("flash", new FlashMessage(type, message));
    }

    /** "/users/edit" avec mapping "/users/*" → "/edit" ; "/users" → "". */
    protected String action(HttpServletRequest req) {
        String path = req.getPathInfo();
        return (path == null || path.equals("/")) ? "" : path;
    }

    // --- Lecture sécurisée des paramètres ---

    protected Long requiredId(HttpServletRequest req) {
        Long id = optionalLong(req, "id");
        if (id == null) {
            throw new BadRequestException("Paramètre 'id' manquant");
        }
        return id;
    }

    protected Long optionalLong(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Paramètre '" + name + "' invalide");
        }
    }

    protected <E extends Enum<E>> E optionalEnum(HttpServletRequest req, String name, Class<E> type) {
        String value = req.getParameter(name);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Paramètre '" + name + "' invalide");
        }
    }
}