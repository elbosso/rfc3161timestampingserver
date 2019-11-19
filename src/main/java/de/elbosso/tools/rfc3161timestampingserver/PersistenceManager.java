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
		java.util.Map<java.lang.String, java.lang.String> emConfig=new java.util.HashMap();
		emConfig.put("javax.persistence.jdbc.url", System.getenv("javax.persistence.jdbc.url")!=null?System.getenv("javax.persistence.jdbc.url"):"jdbc:postgresql://postgresqlserver/jdbctest");
        emConfig.put("javax.persistence.jdbc.user", System.getenv("javax.persistence.jdbc.user")!=null?System.getenv("javax.persistence.jdbc.user"):"jdbctestuser");
        emConfig.put("javax.persistence.jdbc.password", System.getenv("javax.persistence.jdbc.password")!=null?System.getenv("javax.persistence.jdbc.password"):"jdbctestuser");
		// "rfc3161timestampingserver" is the value of the name attribute of the persistence-unit element.
		emFactory = Persistence.createEntityManagerFactory("rfc3161timestampingserver",emConfig);
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