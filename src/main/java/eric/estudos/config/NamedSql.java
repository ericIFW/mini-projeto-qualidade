package eric.estudos.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NamedSql {
    private static final Pattern PARAMETER = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)");

    private NamedSql() {
    }

    public static PreparedStatement prepare(
            Connection connection,
            String sql,
            Map<String, ?> parameters) throws SQLException {

        Matcher matcher = PARAMETER.matcher(sql);
        StringBuffer jdbcSql = new StringBuffer();

        while (matcher.find()) {
            String name = matcher.group(1);
            if (!parameters.containsKey(name)) {
                throw new IllegalArgumentException("Parâmetro SQL não informado: " + name);
            }
            matcher.appendReplacement(jdbcSql, "?");
        }

        matcher.appendTail(jdbcSql);

        PreparedStatement statement = connection.prepareStatement(jdbcSql.toString());

        matcher.reset();

        int index = 1;

        while (matcher.find()) {
            statement.setObject(index++, parameters.get(matcher.group(1)));
        }
        return statement;
    }
}
