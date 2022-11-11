package de.elbosso.tools.rfc3161timestampingserver.domain;

import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Rfc3161timestampTest
{
	static DaoFactory df;

	@BeforeAll
	public static void setUp() throws ClassNotFoundException
	{
		df=new DaoFactory();
	}

    @Test
    public void alwaysSuccess()
    {
        Assertions.assertTrue(true);
    }

}
