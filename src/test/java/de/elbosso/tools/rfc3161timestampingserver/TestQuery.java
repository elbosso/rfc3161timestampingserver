package de.elbosso.tools.rfc3161timestampingserver;

import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.Collections;

import static org.mockito.Mockito.*;

public class TestQuery
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private EntityManager em = mock(EntityManager.class);
    private Query findYoungestByMsgImprintBase64=mock(Query.class);
    private Query findYoungestByMsgDigestAndImprintBase64=mock(Query.class);
    private Query findYoungestByMsgImprintHex=mock(Query.class);
    private Handlers handlers;

    @Before
    public void setup()
    {
        handlers=new Handlers(em);
    }

    @Test
    public void POST_to_query_unsupportedContentType() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form");
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(500);
    }
    @Test
    public void POST_to_query_no_algoId_nothingFoundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintBase64")).thenReturn(findYoungestByMsgImprintBase64);
        when(findYoungestByMsgImprintBase64.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgImprintBase64);
        when(findYoungestByMsgImprintBase64.setMaxResults(1)).thenReturn(findYoungestByMsgImprintBase64);
        when(findYoungestByMsgImprintBase64.getResultList()).thenReturn(Collections.EMPTY_LIST);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void POST_to_query_no_algoId_msgDigestBase64_foundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintBase64")).thenReturn(findYoungestByMsgImprintBase64);
        when(findYoungestByMsgImprintBase64.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgImprintBase64);
        when(findYoungestByMsgImprintBase64.setMaxResults(1)).thenReturn(findYoungestByMsgImprintBase64);
        java.util.List<Rfc3161Timestamp> results=new java.util.LinkedList();
        Rfc3161Timestamp rfc3161Timestamp=new Rfc3161Timestamp();
        rfc3161Timestamp.setTsrData(new byte[0]);
        results.add(rfc3161Timestamp);
        when(findYoungestByMsgImprintBase64.getResultList()).thenReturn(results);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }
    @Test
    public void POST_to_query_algoId_msgDigestBase64_nothingFoundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn("algoid");
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgDigestAndImprintBase64")).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setParameter(eq("Alg"),any(String.class))).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setMaxResults(1)).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.getResultList()).thenReturn(Collections.EMPTY_LIST);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void POST_to_query_algoId_msgDigestBase64_foundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn("algoid");
        when(ctx.formParam("msgDigestBase64")).thenReturn("msgDigestBase64");
        when(ctx.formParam("msgDigestHex")).thenReturn(null);
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgDigestAndImprintBase64")).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setParameter(eq("Alg"),any(String.class))).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        when(findYoungestByMsgDigestAndImprintBase64.setMaxResults(1)).thenReturn(findYoungestByMsgDigestAndImprintBase64);
        java.util.List<Rfc3161Timestamp> results=new java.util.LinkedList();
        Rfc3161Timestamp rfc3161Timestamp=new Rfc3161Timestamp();
        rfc3161Timestamp.setTsrData(new byte[0]);
        results.add(rfc3161Timestamp);
        when(findYoungestByMsgDigestAndImprintBase64.getResultList()).thenReturn(results);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }
    @Test
    public void POST_to_query_no_algoId_no_msgDigestBase64_no_msgDigestHex() throws Exception
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
    public void POST_to_query_msgDigestHex_nothingFoundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn(null);
        when(ctx.formParam("msgDigestHex")).thenReturn("");
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintHex")).thenReturn(findYoungestByMsgImprintHex);
        when(findYoungestByMsgImprintHex.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgImprintHex);
        when(findYoungestByMsgImprintHex.setMaxResults(1)).thenReturn(findYoungestByMsgImprintHex);
        when(findYoungestByMsgImprintHex.getResultList()).thenReturn(java.util.Collections.emptyList());
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(404);
    }
    @Test
    public void POST_to_query_msgDigestHex_foundInDatabase() throws Exception
    {
        when(ctx.contentType()).thenReturn("multipart/form-data");
        when(ctx.formParam("algoid")).thenReturn(null);
        when(ctx.formParam("msgDigestBase64")).thenReturn(null);
        when(ctx.formParam("msgDigestHex")).thenReturn("");
        when(em.createNamedQuery("Rfc3161Timestamp.findYoungestByMsgImprintHex")).thenReturn(findYoungestByMsgImprintHex);
        when(findYoungestByMsgImprintHex.setParameter(eq("Imprint"),any(String.class))).thenReturn(findYoungestByMsgImprintHex);
        when(findYoungestByMsgImprintHex.setMaxResults(1)).thenReturn(findYoungestByMsgImprintHex);
        java.util.List<Rfc3161Timestamp> results=new java.util.LinkedList();
        Rfc3161Timestamp rfc3161Timestamp=new Rfc3161Timestamp();
        rfc3161Timestamp.setTsrData(new byte[0]);
        results.add(rfc3161Timestamp);
        when(findYoungestByMsgImprintHex.getResultList()).thenReturn(results);
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handlePostQuery(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/timestamp-reply");
        verify(ctx).header("Content-Disposition","filename=\"queried.tsr\"");
        verify(ctx).result(any(java.io.InputStream.class));
    }

}
