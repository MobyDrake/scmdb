package com.onevizion.scmdb.vo;

import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DbCnnCredentials {
    private static final String JDBC_THIN_URL_PREFIX = "jdbc:oracle:thin:@";
    private static final String DB_CNN_STR_ERROR_MESSAGE = "You should specify db connection properties using one of following formats:"
            + " <username>/<password>@<host>:<port>:<SID> or <username>/<password>@//<host>:<port>/<service>";

    private String schemaName;
    private String password;
    private String connectionString;
    private String oracleUrl;
    private String schemaWithUrlBeforeDot;

    private DbCnnCredentials() {}

    public static DbCnnCredentials create(String ownerCnnStr) {
        if (!isCorrectConnectionString(ownerCnnStr)) {
            throw new IllegalArgumentException(DB_CNN_STR_ERROR_MESSAGE);
        }

        DbCnnCredentials cnnCredentials = new DbCnnCredentials();
        cnnCredentials.setConnectionString(ownerCnnStr);

        Pattern p = Pattern.compile("(.+?)/(.+?)@(.+)");
        Matcher m = p.matcher(ownerCnnStr);
        if (m.matches() && m.groupCount() == 3) {
            cnnCredentials.setSchemaName(m.group(1));
            cnnCredentials.setPassword(m.group(2));
            cnnCredentials.setOracleUrl(JDBC_THIN_URL_PREFIX + m.group(3));
        } else {
            throw new IllegalArgumentException(DB_CNN_STR_ERROR_MESSAGE);
        }
        return cnnCredentials;
    }

    public static boolean isCorrectConnectionString(String cnnStr) {
        Pattern p = Pattern.compile("(.+?)/(.+?)@(.+)");
        Matcher m = p.matcher(cnnStr);
        return m.matches() && m.groupCount() == 3;
    }

    public static boolean isCorrectSchemaCredentials(String credentialsString) {
        Pattern p = Pattern.compile("(.+?)/([^@]*)");
        Matcher m = p.matcher(credentialsString);
        return m.matches() && m.groupCount() == 2;
    }

    public static String genCnnStrForSchema(String ownerCnnStr, SchemaType schemaType) {
        String owner = ownerCnnStr.substring(0, ownerCnnStr.indexOf("/"));
        String schema = owner + schemaType.getSchemaPostfix();
        return schema + ownerCnnStr.substring(ownerCnnStr.indexOf("/"));
    }

    public static String genCnnStrForSchema(String ownerCnnStr, String schemaCredentials) {
        String ownerUrl = ownerCnnStr.substring(ownerCnnStr.indexOf("@"));
        return schemaCredentials + ownerUrl;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getOracleUrl() {
        return oracleUrl;
    }

    public void setOracleUrl(String oracleUrl) {
        this.oracleUrl = oracleUrl;
    }

    public String getSchemaWithUrlBeforeDot() {
        if (StringUtils.isEmpty(schemaWithUrlBeforeDot)) {
            schemaWithUrlBeforeDot = parseUrlToSchemaWithUrlBeforeDot();
        }

        return schemaWithUrlBeforeDot;
    }

    private String parseUrlToSchemaWithUrlBeforeDot() {
        String url = oracleUrl.replaceAll(JDBC_THIN_URL_PREFIX, "");
        int colonIndex = url.indexOf(':');
        if (colonIndex != -1) {
            url = url.substring(0, colonIndex);
        } else {
            int slashIndex = url.indexOf('/');
            url = url.substring(0, slashIndex);
        }

        int dotIndex = url.indexOf('.');
        if (dotIndex != -1) {
            url = url.substring(0, dotIndex);
        }

        return MessageFormat.format("{0}@{1}", schemaName, url);
    }
}
