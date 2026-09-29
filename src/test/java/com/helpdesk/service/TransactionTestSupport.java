package com.helpdesk.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.function.Supplier;

import com.helpdesk.persistence.TransactionManager;

public final class TransactionTestSupport {

    private TransactionTestSupport() {
    }

    /** TransactionManager simulé qui exécute le traitement immédiatement. */
    public static TransactionManager directTransactionManager() {
        TransactionManager tx = mock(TransactionManager.class);
        lenient().when(tx.inTransaction(any()))
                 .thenAnswer(inv -> inv.<Supplier<?>>getArgument(0).get());
        lenient().doAnswer(inv -> {
                     inv.<Runnable>getArgument(0).run();
                     return null;
                 }).when(tx).runInTransaction(any());
        return tx;
    }
}