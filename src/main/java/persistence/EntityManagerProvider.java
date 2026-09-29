package persistence;


import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface EntityManagerProvider {
    EntityManager get();
}