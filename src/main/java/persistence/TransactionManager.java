package persistence;


import java.util.function.Supplier;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

/**
 * Délimite les transactions et attache l'EntityManager au thread courant.
 * Équivalent simplifié de @Transactional (Spring) ou des transactions JTA.
 */
public class TransactionManager implements EntityManagerProvider {

    private final EntityManagerFactory emf;
    private final ThreadLocal<EntityManager> currentEntityManager = new ThreadLocal<>();

    public TransactionManager(EntityManagerFactory emf) {
        this.emf = emf;
    }

    /** Exécute un traitement dans une transaction et retourne son résultat. */
    public <T> T inTransaction(Supplier<T> work) {
        // Transaction déjà ouverte sur ce thread : on la rejoint (propagation REQUIRED)
        if (currentEntityManager.get() != null) {
            return work.get();
        }

        EntityManager em = emf.createEntityManager();
        currentEntityManager.set(em);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.get();
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            currentEntityManager.remove();
            em.close();
        }
    }

    /** Variante sans valeur de retour. */
    public void runInTransaction(Runnable work) {
        inTransaction(() -> {
            work.run();
            return null;
        });
    }

    @Override
    public EntityManager get() {
        EntityManager em = currentEntityManager.get();
        if (em == null) {
            throw new IllegalStateException(
                    "Aucune transaction active : l'appel au DAO doit se faire via TransactionManager");
        }
        return em;
    }
}