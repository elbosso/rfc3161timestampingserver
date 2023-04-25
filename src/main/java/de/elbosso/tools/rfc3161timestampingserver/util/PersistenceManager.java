// https://dzone.com/articles/jpa-tutorial-setting-jpa-java
package de.elbosso.tools.rfc3161timestampingserver.util;

import de.elbosso.tools.rfc3161timestampingserver.Constants;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceManager
{
	private static PersistenceManager sharedInstance;
	private static final org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
	private static final org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
	private EntityManagerFactory emFactory;

	public static PersistenceManager getSharedInstance()
	{
		if(sharedInstance==null)
			sharedInstance=new PersistenceManager();
		return sharedInstance;
	}

	private PersistenceManager()
	{
	}

	private synchronized void ensureEmFactory()
	{
		if(emFactory==null)
		{
			java.lang.String pw=DockerSecrets.readPassword(Constants.JDBC_PASSWORD_FILE,Constants.JDBC_PASSWORD,"xxx");
			java.util.Map<java.lang.String, java.lang.String> emConfig=new java.util.HashMap();
			emConfig.put(Constants.JDBC_URL, Utilities.getEnvSysPropertyFallback(Constants.JDBC_URL,Constants.JDBC_DEFAULT_URL));
			emConfig.put(Constants.JDBC_USER, Utilities.getEnvSysPropertyFallback(Constants.JDBC_USER,"xxx"));
			emConfig.put(Constants.JDBC_PASSWORD, pw);
			// "rfc3161timestampingserver" is the value of the name attribute of the persistence-unit element.
			emFactory = Persistence.createEntityManagerFactory(Utilities.getEnvSysPropertyFallback(Constants.PERSISTENCE_UNIT_NAME,Constants.PERSISTENCE_UNIT_NAME_DEFAULT),emConfig);
		}
	}

	public EntityManager getEntityManager()
	{
		ensureEmFactory();
		return emFactory.createEntityManager();
	}
	public void close()
	{
		if(emFactory!=null)
			emFactory.close();
	}
}