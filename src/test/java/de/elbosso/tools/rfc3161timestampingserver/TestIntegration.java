package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.Javalin;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.security.Security;

public class TestIntegration
{
    private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(TestIntegration.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    static Javalin javalin;
    @BeforeAll
    static void setup()
    {
        Security.addProvider(new BouncyCastleProvider());
        javalin=App.init(13456);
    }
    @AfterAll
    static void tearDown()
    {
        javalin.stop();
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = "PERSISTENCE_UNIT_NAME_FOR_TESTS")
    public void test_POST_unsupportedContentType() throws Exception
    {

    }
}
