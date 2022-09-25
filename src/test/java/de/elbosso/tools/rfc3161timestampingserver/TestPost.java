package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.http.UploadedFile;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.io.IOException;
import java.security.Security;

import static org.mockito.Mockito.*;

public class TestPost
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private EntityManager em = mock(EntityManager.class);
    private Query findYoungestByMsgImprintBase64=mock(Query.class);
    private Query findYoungestByMsgDigestAndImprintBase64=mock(Query.class);
    private Query findYoungestByMsgImprintHex=mock(Query.class);
    private EntityTransaction entityTransaction=mock(EntityTransaction.class);
    private CryptoResourceManager cryptoResourceManager=mock(CryptoResourceManager.class);
    private CryptoResourceManager defCryptoResourceManager=new DefaultCryptoResourceManager();
    private Handlers handlers;
    private byte[] tsq;
    private byte[] tsq_nocert;
    private byte[] notsq;

    @BeforeAll
    static void setup()
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    @BeforeEach
    void init() throws IOException
    {
        handlers=new Handlers(em,cryptoResourceManager);
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        java.io.InputStream is=de.netsysit.util.ResourceLoader.getResourceAsStream("example.tsq");
        de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
        tsq=baos.toByteArray();
        baos=new java.io.ByteArrayOutputStream();
        is=de.netsysit.util.ResourceLoader.getResourceAsStream("example_nocert.tsq");
        de.elbosso.util.Utilities.copyBetweenStreams(is,baos,true);
        tsq_nocert=baos.toByteArray();
        notsq="huhu".getBytes();
    }

    @Test
    public void test_POST_unsupportedContentType() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form");
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
    }
    @Test
    public void test_POST_body() throws Exception
    {
        when(ctx.contentType()).thenReturn("application/timestamp-query");
        when(ctx.bodyAsBytes()).thenReturn(tsq);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        when(cryptoResourceManager.getTsaCert()).thenReturn(defCryptoResourceManager.getTsaCert());
        when(cryptoResourceManager.getChainPem()).thenReturn(defCryptoResourceManager.getChainPem());
        when(cryptoResourceManager.getPrivateKey()).thenReturn(defCryptoResourceManager.getPrivateKey());
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");

        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(entityTransaction).commit();
    }
    @Test
    public void test_POST_body_invalidRequest() throws Exception
    {
        when(ctx.contentType()).thenReturn("application/timestamp-query");
        when(ctx.bodyAsBytes()).thenReturn(notsq);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
        verify(entityTransaction).rollback();
    }

    @Test
    public void test_POST_formData_noFile() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        when(ctx.uploadedFile("tsq")).thenReturn(null);

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
    }
    @Test
    public void test_POST_formData() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query",tsq.length,"example.tsq",".tsq");
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(em.getTransaction()).thenReturn(entityTransaction);
        when(cryptoResourceManager.getTsaCert()).thenReturn(defCryptoResourceManager.getTsaCert());
        when(cryptoResourceManager.getChainPem()).thenReturn(defCryptoResourceManager.getChainPem());
        when(cryptoResourceManager.getPrivateKey()).thenReturn(defCryptoResourceManager.getPrivateKey());
        doNothing().when(entityTransaction).begin();
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(entityTransaction).commit();
    }
    @Test
    public void test_POST_formData_invalid_request() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(notsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query",notsq.length,"example.tsq",".tsq");
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
        verify(entityTransaction).rollback();
    }
    @Test
    public void test_POST_formData_nocert() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq_nocert);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query",tsq_nocert.length,"example_nocert.tsq",".tsq");
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        when(cryptoResourceManager.getTsaCert()).thenReturn(defCryptoResourceManager.getTsaCert());
        when(cryptoResourceManager.getChainPem()).thenReturn(defCryptoResourceManager.getChainPem());
        when(cryptoResourceManager.getPrivateKey()).thenReturn(defCryptoResourceManager.getPrivateKey());
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(entityTransaction).commit();
    }
    @Test
    @SetEnvironmentVariable(key = Constants.INCLUDE_CRLS, value = "true")
    public void test_POST_formData_includeCRLs() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query",tsq.length,"example.tsq",".tsq");
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        when(cryptoResourceManager.getTsaCert()).thenReturn(defCryptoResourceManager.getTsaCert());
        when(cryptoResourceManager.getChainPem()).thenReturn(defCryptoResourceManager.getChainPem());
        when(cryptoResourceManager.getPrivateKey()).thenReturn(defCryptoResourceManager.getPrivateKey());
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(entityTransaction).commit();
    }
    @Test
    @SetEnvironmentVariable(key = Constants.INCLUDE_FULL_CHAIN, value = "true")
    public void test_POST_formData_includeFullChain() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query",tsq.length,"example.tsq",".tsq");
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(em.getTransaction()).thenReturn(entityTransaction);
        doNothing().when(entityTransaction).begin();
        when(cryptoResourceManager.getTsaCert()).thenReturn(defCryptoResourceManager.getTsaCert());
        when(cryptoResourceManager.getChainPem()).thenReturn(defCryptoResourceManager.getChainPem());
        when(cryptoResourceManager.getPrivateKey()).thenReturn(defCryptoResourceManager.getPrivateKey());
        doAnswer(invocation -> {
            Rfc3161Timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigInteger.valueOf(1));
            return null;
        }).when(em).persist(any(Rfc3161Timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(entityTransaction).commit();
    }
}
