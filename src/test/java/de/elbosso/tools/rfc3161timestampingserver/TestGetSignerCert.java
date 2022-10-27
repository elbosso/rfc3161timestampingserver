package de.elbosso.tools.rfc3161timestampingserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.*;

public class TestGetSignerCert
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private EntityManager em = mock(EntityManager.class);
    private CryptoResourceManager cryptoResourceManager=mock(CryptoResourceManager.class);

    private Handlers handlers;

    @BeforeEach
    void init()
    {
        handlers=new Handlers(em,cryptoResourceManager);
    }

    @Test
    public void test_GET_to_tsa_crt() throws Exception
    {
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        when(cryptoResourceManager.getTsaCert()).thenReturn(de.netsysit.util.ResourceLoader.getResource("crypto/tsa.crt"));
        handlers.handleGetSignerCert(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/pkix-cert");
    }

}