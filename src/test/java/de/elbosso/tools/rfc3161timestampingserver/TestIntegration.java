package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.Javalin;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.entity.EntityBuilder;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.security.Security;

//see: https://www.baeldung.com/integration-testing-a-rest-api

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
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessMultiPart() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);
        builder.addBinaryBody("tsq", url.openStream(), ContentType.create("application/timestamp-query"), "query.tsq");
//
        HttpEntity entity = builder.build();
        post.setEntity(entity);

        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());
    }
    @Test
    @SetEnvironmentVariable(key = Constants.JDBC_URL, value = "jdbc:h2:mem:test")
    @SetEnvironmentVariable(key = Constants.JDBC_PASSWORD, value = "")
    @SetEnvironmentVariable(key = Constants.JDBC_USER, value = "sa")
    @SetEnvironmentVariable(key = Constants.PERSISTENCE_UNIT_NAME, value = Constants.PERSISTENCE_UNIT_NAME_FOR_TESTS)
    public void test_SuccessBody() throws Exception
    {
        HttpPost post = new HttpPost("http://localhost:"+TEST_PORT+"/");
        java.net.URL url=TestIntegration.class.getClassLoader().getResource("example.tsq");
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        de.elbosso.util.Utilities.copyBetweenStreams(url.openStream(),baos,true);
        post.setHeader("Content-type", "application/timestamp-query");

        post.setEntity(EntityBuilder.create().setBinary(baos.toByteArray()).build());


        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( post );
        // Then
        Assertions.assertEquals(HttpStatus.SC_CREATED,httpResponse.getCode());
    }
}
