package de.elbosso.tools.rfc3161timestampingserver;

import de.elbosso.tools.rfc3161timestampingserver.util.DockerSecrets;
import io.javalin.core.security.BasicAuthCredentials;
import io.javalin.core.security.RouteRole;
import io.javalin.core.util.Header;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class AccessManager extends java.lang.Object implements io.javalin.core.security.AccessManager
{
    private static final String ADMINUSERNAME = "admin";

    @Override
    public void manage(@NotNull Handler handler, @NotNull Context ctx, @NotNull java.util.Set<RouteRole> permittedRoles) throws Exception
    {
        if(permittedRoles.contains(Roles.ANYONE))
            handler.handle(ctx);
        RouteRole userRole = getUserRole(ctx);
        if (permittedRoles.contains(userRole)) {
            handler.handle(ctx);
        } else {
            ctx.header(Header.WWW_AUTHENTICATE, "Basic");
            ctx.status(401).result("Unauthorized");
            throw new UnauthorizedResponse();
        }
    }
    RouteRole getUserRole(Context ctx) {
        RouteRole rv=Roles.ANYONE;
        try
        {
            BasicAuthCredentials basicAuthCredentials = ctx.basicAuthCredentials();
            if (basicAuthCredentials != null)
            {
                java.lang.String userName = basicAuthCredentials.getUsername();
                if ((userName != null) && (userName.equals(ADMINUSERNAME)))
                {
                    java.lang.String password = basicAuthCredentials.getPassword();
                    java.lang.String pw= DockerSecrets.readPassword(Constants.ADMIN_PASSWORD_FILE,Constants.ADMIN_PASSWORD,null);
                    if ((pw!=null)&&((password != null) && (password.equals(pw))))
                        rv = Roles.ADMIN;
                }
            }
        }
        catch(IllegalArgumentException exp)
        {
            //no auth header is ok for now
        }
        return rv;
    }
}
