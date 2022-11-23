package de.elbosso.tools.rfc3161timestampingserver.dao;

import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.util.JpaDao;
import de.elbosso.tools.rfc3161timestampingserver.util.JpaDaoFactory;

public class DaoFactory extends JpaDaoFactory
{
	public DaoFactory()
	{
		super();
	}

	public Rfc3161timestampDao createRfc3161timestampDao()
	{
		if(map.containsKey(Rfc3161timestamp.class)==false)
		{
			JpaDao<Rfc3161timestamp> dao=new Rfc3161timestampDao();
			dao.setEntityManager(getEntityManager());
			map.put(Rfc3161timestamp.class,dao);
		}
		return (Rfc3161timestampDao) map.get(Rfc3161timestamp.class);
	}

}
