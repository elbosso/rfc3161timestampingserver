package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.Javalin;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.security.Security;

public class TestIntegration
{
    private final static org.slf4j.Logger CLASS_LOGGER=org.slf4j.LoggerFactory.getLogger(TestIntegration.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER=org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
    private final static int TEST_PORT=13456;

    static Javalin javalin;
    @BeforeAll
    static void setup()
    {
        Security.addProvider(new BouncyCastleProvider());
        javalin=App.init(TEST_PORT);
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
    public void test_Success() throws Exception
    {
        HttpUriRequest request=new HttpGet( "http://localhost:"+TEST_PORT+"/hguhu");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

        // Then
        Assertions.assertEquals(HttpStatus.SC_NOT_FOUND,httpResponse.getCode());
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = "PERSISTENCE_UNIT_NAME_FOR_TESTS")
    public void test_Fail() throws Exception
    {
        HttpUriRequest request=new HttpGet( "http://localhost:"+TEST_PORT+"/hguhu");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

        // Then
        Assertions.assertEquals(HttpStatus.SC_OK,httpResponse.getCode());
    }
}
