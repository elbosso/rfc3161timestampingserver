package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.tools.rfc3161timestampingserver.dao.DaoFactory;
import de.elbosso.tools.rfc3161timestampingserver.dao.Rfc3161timestampDao;
import de.elbosso.tools.rfc3161timestampingserver.domain.Rfc3161timestamp;
import de.elbosso.tools.rfc3161timestampingserver.service.CryptoResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.Query;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class TestPostQuery
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private DaoFactory df=mock(DaoFactory.class);
    private Rfc3161timestampDao dao=mock(Rfc3161timestampDao.class);
    private Query findYoungestByMsgImprintBase64=mock(Query.class);
    private Query findYoungestByMsgDigestAndImprintBase64=mock(Query.class);
    private Query findYoungestByMsgImprintHex=mock(Query.class);
    private CryptoResourceManager cryptoResourceManager=mock(CryptoResourceManager.class);
    private Handlers handlers;

    @BeforeEach
    void init()
    {
        handlers=new Handlers(df,cryptoResourceManager);
    }

    @Test
    public void test_POST_to_query_unsupportedContentType() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form");
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(500);
    }
    @Test
    public void test_POST_to_query_no_algoId_nothingFoundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(dao.findYoungestByMsgImprintBase64(any(Rfc3161timestamp.class))).thenReturn(Optional.empty());
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void test_POST_to_query_no_algoId_msgDigestBase64_foundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        Rfc3161timestamp rfc3161Timestamp=new Rfc3161timestamp();
        rfc3161Timestamp.setTsr_data(new byte[0]);
        when(dao.findYoungestByMsgImprintBase64(any(Rfc3161timestamp.class))).thenReturn(Optional.of(rfc3161Timestamp));
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(200);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }
    @Test
    public void test_POST_to_query_algoId_msgDigestBase64_nothingFoundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn("algoid");
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(dao.findYoungestByMsgDigestAndImprintBase64(any(Rfc3161timestamp.class))).thenReturn(Optional.empty());
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void test_POST_to_query_algoId_msgDigestBase64_foundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn("algoid");
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        Rfc3161timestamp rfc3161Timestamp=new Rfc3161timestamp();
        rfc3161Timestamp.setTsr_data(new byte[0]);
        when(dao.findYoungestByMsgDigestAndImprintBase64(any(Rfc3161timestamp.class))).thenReturn(Optional.of(rfc3161Timestamp));
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(200);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }
    @Test
    public void test_POST_to_query_no_algoId_no_msgDigestBase64_no_msgDigestHex() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn(null);
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(500);
    }
    @Test
    public void test_POST_to_query_msgDigestHex_nothingFoundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn(null);
        when(ctx.formParam("msgDigestHex")).thenReturn("msgDigestHex");
        when(dao.findYoungestByMsgImprintHex(any(Rfc3161timestamp.class))).thenReturn(Optional.empty());
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void test_POST_to_query_msgDigestHex_foundInDatabase() throws Exception
    {
        when(df.createRfc3161timestampDao()).thenReturn(dao);
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn(null);
        when(ctx.formParam("msgDigestHex")).thenReturn("msgDigestHex");
        Rfc3161timestamp rfc3161Timestamp=new Rfc3161timestamp();
        rfc3161Timestamp.setTsr_data(new byte[0]);
        when(dao.findYoungestByMsgImprintHex(any(Rfc3161timestamp.class))).thenReturn(Optional.of(rfc3161Timestamp));
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(200);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }

}
