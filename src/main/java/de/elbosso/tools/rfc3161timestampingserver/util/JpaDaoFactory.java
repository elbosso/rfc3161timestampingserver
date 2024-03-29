package de.elbosso.tools.rfc3161timestampingserver.util;

import javax.persistence.EntityManager;

public class JpaDaoFactory <T extends JpaDaoFactory>
{
	protected java.util.Map<Class,JpaDao> map=new java.util.HashMap();
	protected JpaDaoFactory()
	{
		super();
	}
	protected EntityManager getEntityManager()
	{
		return PersistenceManager.getSharedInstance().getEntityManager();
	}

	public <T> JpaDao<T> createDao(Class<T> cls)
	{
		if(map.containsKey(cls)==false)
		{
			JpaDao<T> jpaDao = new JpaDao<T>(cls);
			jpaDao.setEntityManager(getEntityManager());
			map.put(cls,jpaDao);
		}
		return map.get(cls);
	}

}
