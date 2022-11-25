package de.elbosso.tools.rfc3161timestampingserver;

import io.javalin.core.security.Role;

public enum Roles implements Role
{
    ANYONE,
    ADMIN;
}
