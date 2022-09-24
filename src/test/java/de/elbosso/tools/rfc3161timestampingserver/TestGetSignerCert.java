package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.http.util.ContextUtil;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class TestGetSignerCert
{
    private io.javalin.http.Context ctx = mock(io.javalin.http.Context.class); // javalin 2.1.0 or before: "mock-maker-inline" must be enabled
    private EntityManager em = mock(EntityManager.class);

    private Handlers handlers;

    @Before
    public void setup()
    {
        handlers=new Handlers(em);
    }

    @Test
    public void GET_to_tsa_crt() throws Exception
    {
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handleGetSignerCert(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/pkix-cert");
    }

}
