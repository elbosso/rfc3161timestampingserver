package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import de.elbosso.tools.rfc3161timestampingserver.dao.Rfc3161timestampDao;
import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.impl.DefaultCryptoResourceManager;
import de.elbosso.tools.rfc3161timestampingserver.service.CryptoResourceManager;
import io.javalin.http.UploadedFile;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.Security;

import static org.mockito.Mockito.*;

public class TestPost
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private DaoFactory df=mock(DaoFactory.class);
    private Rfc3161timestampDao dao=mock(Rfc3161timestampDao.class);
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
        handlers=new Handlers(df,cryptoResourceManager);
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        java.io.InputStream is=de.netsysit.util.ResourceLoader.getResourceAsStream("example.tsq");
        de.elbosso.util.io.Utilities.copyBetweenStreams(is,baos,true);
        tsq=baos.toByteArray();
        baos=new java.io.ByteArrayOutputStream();
        is=de.netsysit.util.ResourceLoader.getResourceAsStream("example_nocert.tsq");
        de.elbosso.util.io.Utilities.copyBetweenStreams(is,baos,true);
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
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("application/timestamp-query");
        when(ctx.bodyAsBytes()).thenReturn(tsq);
        when(cryptoResourceManager.getChainPem()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem"));
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(cryptoResourceManager.getPrivateKey()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key"));
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");

        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
    }
    @Test
    public void test_POST_body_invalidRequest() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("application/timestamp-query");
        when(ctx.bodyAsBytes()).thenReturn(notsq);
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
        verify(dao).rollbackTransaction();
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
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query","example.tsq",".tsq",tsq.length);
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(cryptoResourceManager.getChainPem()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem"));
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(cryptoResourceManager.getPrivateKey()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key"));
        doNothing().when(dao).beginTransaction();
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(dao).commitTransaction();
    }
    @Test
    public void test_POST_formData_invalid_request() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(notsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query","example.tsq",".tsq",notsq.length);
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        doNothing().when(dao).beginTransaction();
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(500);
        verify(dao).rollbackTransaction();
    }
    @Test
    public void test_POST_formData_nocert() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq_nocert);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query","example_nocert.tsq",".tsq",tsq_nocert.length);
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        doNothing().when(dao).beginTransaction();
        when(cryptoResourceManager.getChainPem()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem"));
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(cryptoResourceManager.getPrivateKey()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key"));
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(dao).commitTransaction();
    }
    @Test
    public void test_POST_formData_includeCRLs() throws Exception
    {
        System.setProperty(Constants.INCLUDE_CRLS,"true");
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query","example.tsq",".tsq",tsq.length);
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        doNothing().when(dao).beginTransaction();
        when(cryptoResourceManager.getChainPem()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem"));
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(cryptoResourceManager.getPrivateKey()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key"));
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(dao).commitTransaction();
    }
    @Test
    public void test_POST_formData_includeFullChain() throws Exception
    {
        System.setProperty(Constants.INCLUDE_FULL_CHAIN,"true");
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-dataxyp");
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(tsq);
        UploadedFile uploadedFile=new UploadedFile(bais,"application/timestamp-query","example.tsq",".tsq",tsq.length);
        when(ctx.uploadedFile("tsq")).thenReturn(uploadedFile);
        doNothing().when(dao).beginTransaction();
        when(cryptoResourceManager.getChainPem()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/chain.pem"));
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.crt"));
        when(cryptoResourceManager.getPrivateKey()).thenReturn(de.netsysit.util.ResourceLoader.getResource("rfc3161timestampingserver/priv/tsa.key"));
        doAnswer(invocation -> {
            Rfc3161timestamp arg0 = invocation.getArgument(0);
            arg0.setId(java.math.BigDecimal.valueOf(1));
            return null;
        }).when(dao).persist(any(Rfc3161timestamp.class));

        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePost(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"reply.tsr\"");
        verify(ctx).result(any(java.io.ByteArrayInputStream.class));
        verify(dao).commitTransaction();
    }
}
