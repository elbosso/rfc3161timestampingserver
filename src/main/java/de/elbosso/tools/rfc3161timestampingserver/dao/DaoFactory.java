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

	public JpaDao<Rfc3161timestamp> createRfc3161timestampDao()
	{
		return super.<Rfc3161timestampDao>createDao(Rfc3161timestamp.class);
	}

}
