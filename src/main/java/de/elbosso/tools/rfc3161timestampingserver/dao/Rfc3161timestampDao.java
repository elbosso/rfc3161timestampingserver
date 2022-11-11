package de.elbosso.tools.rfc3161timestampingserver.dao;

import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.util.JpaDao;

public class Rfc3161timestampDao extends JpaDao<Rfc3161timestamp>
{
	Rfc3161timestampDao()
	{
		super(Rfc3161timestamp.class);
	}

}
