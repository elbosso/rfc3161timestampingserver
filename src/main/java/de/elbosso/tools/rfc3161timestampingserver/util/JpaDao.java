package de.elbosso.tools.rfc3161timestampingserver.util;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

public class JpaDao<T> {

	protected Class<T> entityClass;
	protected EntityManager entityManager;

	public JpaDao(Class<T> cls) {
		super();
		this.entityClass=cls;
	}

	public EntityManager getEntityManager() {
		return entityManager;
	}

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional
	public void persist(T entity) {
		entityManager.persist(entity);
	}

	@Transactional
	public Optional<T> find(Object primaryKey) {
		return Optional.ofNullable(entityManager.find(entityClass, primaryKey));
	}

	@Transactional
	public List<T> findAll() {
		TypedQuery<T> typedQuery = entityManager.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
		return typedQuery.getResultList();
	}

	@Transactional
	public void remove(T entity) {
		entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
	}

	@Transactional
	public void update(T entity) {
		entityManager.merge(entity);
	}

	public void beginTransaction()
	{
		entityManager.getTransaction().begin();
	}

	public void commitTransaction()
	{
		entityManager.getTransaction().commit();
	}
	public void rollbackTransaction()
	{
		entityManager.getTransaction().rollback();
	}

	public void refresh(T entity)
	{
		entityManager.refresh(entity);
	}
}