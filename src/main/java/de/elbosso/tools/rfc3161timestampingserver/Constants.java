package de.elbosso.tools.rfc3161timestampingserver;

public interface Constants
{
    java.lang.String INCLUDE_FULL_CHAIN="de.elbosso.tools.rfc3161timestampingserver.App.includeFullChain";
    java.lang.String INCLUDE_CRLS="de.elbosso.tools.rfc3161timestampingserver.App.includeCRLs";
    java.lang.String JDBC_PASSWORD="javax.persistence.jdbc.password";
    java.lang.String JDBC_URL="javax.persistence.jdbc.url";
    java.lang.String JDBC_USER="javax.persistence.jdbc.user";
    java.lang.String JDBC_DEFAULT_URL="jdbc:postgresql://postgresqlserver/jdbctest";
    java.lang.String JDBC_PASSWORD_FILE="javax.persistence.jdbc.password_FILE";
    java.lang.String PERSISTENCE_UNIT_NAME="de.elbosso.tools.rfc3161timestampingserver.App.PersistenceUnitName";
    java.lang.String PERSISTENCE_UNIT_NAME_DEFAULT="rfc3161timestampingserver";
    java.lang.String PERSISTENCE_UNIT_NAME_FOR_TESTS="rfc3161timestampingserver_test";

    java.lang.String ADMIN_PASSWORD_FILE = "admin.password_FILE";
    java.lang.String ADMIN_PASSWORD = "admin.password";
}
