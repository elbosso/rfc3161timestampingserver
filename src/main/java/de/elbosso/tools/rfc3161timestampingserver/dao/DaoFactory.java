package de.elbosso.tools.rfc3161timestampingserver.dao;

import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.util.JpaDaoFactory;

public class DaoFactory extends JpaDaoFactory
{
	public DaoFactory()
	{
		super();
	}

	public Rfc3161timestampDao createRfc3161timestampDao()
	{
		return (Rfc3161timestampDao)super.<Rfc3161timestampDao>createDao(Rfc3161timestamp.class);
	}

}
