package com.helpdesk.web.controller;

import java.io.IOException;

import com.helpdesk.exception.BusinessRuleException;
import com.helpdesk.exception.ConcurrentUpdateException;
import com.helpdesk.exception.InvalidDataException;
import com.helpdesk.model.User;
import com.helpdesk.model.enums.Role;
import com.helpdesk.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = { "/users", "/users/*" })
public class UserController extends BaseController {

    private UserService users() {
        return app().userService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action(req)) {
            case "" -> list(req, resp);
            case "/new" -> showForm(req, resp, new User());
            case "/edit" -> showForm(req, resp, users().findById(requiredId(req)));
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        switch (action(req)) {
            case "/save" -> save(req, resp);
            case "/delete" -> delete(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void list(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("users", users().findAll());
        render(req, resp, "users/list");
    }

    private void showForm(HttpServletRequest req, HttpServletResponse resp, User user)
            throws ServletException, IOException {
        req.setAttribute("user", user);
        req.setAttribute("roles", Role.values());
        render(req, resp, "users/form");
    }

    private void save(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = new User();
        user.setId(optionalLong(req, "id"));
        user.setVersion(optionalLong(req, "version"));
        user.setFirstName(req.getParameter("firstName"));
        user.setLastName(req.getParameter("lastName"));
        user.setEmail(req.getParameter("email"));
        user.setRole(optionalEnum(req, "role", Role.class));

        try {
            if (user.getId() == null) {
                users().create(user);
                flash(req, "success", "Utilisateur " + user.getFullName() + " créé.");
            } else {
                users().update(user);
                flash(req, "success", "Utilisateur " + user.getFullName() + " modifié.");
            }
            redirect(req, resp, "/users"); // Post/Redirect/Get
        } catch (InvalidDataException e) {
            req.setAttribute("errors", e.getErrors());
            showForm(req, resp, user); // On réaffiche le formulaire AVEC les valeurs saisies
        } catch (BusinessRuleException | ConcurrentUpdateException e) {
            req.setAttribute("globalError", e.getMessage());
            showForm(req, resp, user);
        }
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            users().delete(requiredId(req));
            flash(req, "success", "Utilisateur supprimé.");
        } catch (BusinessRuleException e) {
            flash(req, "danger", e.getMessage());
        }
        redirect(req, resp, "/users");
    }
}