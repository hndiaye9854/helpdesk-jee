package dao.impl;


import java.util.List;
import java.util.Optional;

import dao.GenericDao;
import persistence.EntityManagerProvider;

import jakarta.persistence.EntityManager;

public abstract class AbstractJpaDao<T, ID> implements GenericDao<T, ID> {

    private final Class<T> entityClass;
    private final EntityManagerProvider emProvider;

    protected AbstractJpaDao(Class<T> entityClass, EntityManagerProvider emProvider) {
        this.entityClass = entityClass;
        this.emProvider = emProvider;
    }

    protected EntityManager em() {
        return emProvider.get();
    }

    @Override
    public T save(T entity) {
        Object id = em().getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
        if (id == null) {
            em().persist(entity);
            return entity;
        }
        return em().merge(entity);
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em().find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id";
        return em().createQuery(jpql, entityClass).getResultList();
    }

    @Override
    public void delete(T entity) {
        em().remove(em().contains(entity) ? entity : em().merge(entity));
    }

    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(em()::remove);
    }

    @Override
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
}