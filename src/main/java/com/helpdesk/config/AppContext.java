package com.helpdesk.config;

import com.helpdesk.dao.TicketDao;
import com.helpdesk.dao.UserDao;
import com.helpdesk.dao.impl.TicketDaoJpa;
import com.helpdesk.dao.impl.UserDaoJpa;
import com.helpdesk.persistence.TransactionManager;
import com.helpdesk.service.TicketService;
import com.helpdesk.service.UserService;
import com.helpdesk.service.impl.TicketServiceImpl;
import com.helpdesk.service.impl.UserServiceImpl;
import com.helpdesk.service.validation.EntityValidator;

/** Racine de composition : crée et assemble les objets de l'application (injection manuelle). */
public final class AppContext {

    public static final String ATTRIBUTE = "helpdesk.context";

    private final UserService userService;
    private final TicketService ticketService;

    public AppContext(TransactionManager tx) {
        EntityValidator validator = new EntityValidator();
        UserDao userDao = new UserDaoJpa(tx);
        TicketDao ticketDao = new TicketDaoJpa(tx);

        this.userService = new UserServiceImpl(userDao, ticketDao, tx, validator);
        this.ticketService = new TicketServiceImpl(ticketDao, userDao, tx, validator);
    }

    public UserService userService() { return userService; }
    public TicketService ticketService() { return ticketService; }
}