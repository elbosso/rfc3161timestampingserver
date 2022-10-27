// https://dzone.com/articles/jpa-tutorial-setting-jpa-java
package de.elbosso.tools.rfc3161timestampingserver;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceManager
{
	private static PersistenceManager sharedInstance;
	private final org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(PersistenceManager.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
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
			java.lang.String pw=null;
			java.net.URL url=de.netsysit.util.ResourceLoader.getDockerSecretResource(Constants.JDBC_PASSWORD_FILE);
			try
			{
				if (url != null)
				{
					java.io.InputStream is = url.openStream();
					pw = de.elbosso.util.Utilities.readIntoString(is).trim();
					is.close();
				}
			}
			catch(java.io.IOException exp)
			{
				CLASS_LOGGER.error(exp.getMessage(),exp);
			}
			if(pw==null)
				pw=System.getenv(Constants.JDBC_PASSWORD)!=null?System.getenv(Constants.JDBC_PASSWORD):"xxx";
			java.util.Map<java.lang.String, java.lang.String> emConfig=new java.util.HashMap();
			emConfig.put(Constants.JDBC_URL, System.getenv(Constants.JDBC_URL)!=null?System.getenv(Constants.JDBC_URL): Constants.JDBC_DEFAULT_URL);
			emConfig.put(Constants.JDBC_USER, System.getenv(Constants.JDBC_USER)!=null?System.getenv(Constants.JDBC_USER):"xxx");
			emConfig.put(Constants.JDBC_PASSWORD, pw);
			// "rfc3161timestampingserver" is the value of the name attribute of the persistence-unit element.
			emFactory = Persistence.createEntityManagerFactory(Constants.PERSISTENCE_UNIT_NAME,emConfig);
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