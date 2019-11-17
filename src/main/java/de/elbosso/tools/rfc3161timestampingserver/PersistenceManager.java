// https://dzone.com/articles/jpa-tutorial-setting-jpa-java
package de.elbosso.tools.rfc3161timestampingserver;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public enum PersistenceManager
{
	INSTANCE;
	private EntityManagerFactory emFactory;
	private PersistenceManager()
	{
		// "rfc3161timestampingserver" is the value of the name attribute of the persistence-unit element.
		emFactory = Persistence.createEntityManagerFactory("rfc3161timestampingserver");
	}
	public EntityManager getEntityManager()
	{
		return emFactory.createEntityManager();
	}
	public void close()
	{
		emFactory.close();
	}
}