package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.core.security.RouteRole;

public enum Roles implements RouteRole
{
    ANYONE,
    ADMIN;
}
