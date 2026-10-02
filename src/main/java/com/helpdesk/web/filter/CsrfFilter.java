package com.helpdesk.web.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Protection CSRF (Synchronizer Token Pattern) :
 * un jeton secret par session, obligatoire dans chaque formulaire POST.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    public static final String TOKEN_ATTRIBUTE = "csrfToken";
    public static final String TOKEN_PARAMETER = "_csrf";

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        String token = (String) session.getAttribute(TOKEN_ATTRIBUTE);
        if (token == null) {
            token = generateToken();
            session.setAttribute(TOKEN_ATTRIBUTE, token);
        }
        req.setAttribute(TOKEN_ATTRIBUTE, token); // Accessible dans les JSP : ${csrfToken}

        if ("POST".equalsIgnoreCase(req.getMethod())) {
            String sent = req.getParameter(TOKEN_PARAMETER);
            if (sent == null || !constantTimeEquals(token, sent)) {
                ((HttpServletResponse) response).sendError(
                        HttpServletResponse.SC_FORBIDDEN, "Jeton de sécurité invalide ou expiré. Rechargez la page.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** Comparaison en temps constant : empêche de deviner le jeton en mesurant le temps de réponse. */
    private static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}