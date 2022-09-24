package de.elbosso.tools.rfc3161timestampingserver;

import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.mockito.Mockito.*;

public class TestGetChain
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
    public void GET_to_chain() throws Exception
    {
        when(ctx.ip()).thenReturn("127.0.0.1");
        when(ctx.host()).thenReturn("localhost");
        handlers.handleGetChain(ctx);
        verify(ctx).status(201);
        verify(ctx).contentType("application/pkcs7-mime");
    }

}
