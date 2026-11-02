package eric.estudos.repository;

import eric.estudos.model.Paciente;
import eric.estudos.config.DatabaseConfig;
import eric.estudos.config.NamedSql;
import org.flywaydb.core.Flyway;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;
import java.util.Map;

public class PacienteRepository {

    public PacienteRepository() {
        try {
            Files.createDirectories(Path.of("data"));
            Flyway.configure()
                    .dataSource(DatabaseConfig.getUrl(), DatabaseConfig.getUser(), DatabaseConfig.getPassword())
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load()
                    .migrate();
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível preparar o banco de dados.", e);
        }
    }

    public void salvar(Paciente paciente) throws SQLException {

        String sql = "INSERT INTO paciente (nome, cpf, data_nascimento, telefone) " +
                "VALUES (:nome, :cpf, :dataNascimento, :telefone)";

        try (Connection connection = DatabaseConfig.getConnection();

             PreparedStatement statement = NamedSql.prepare(connection, sql, Map.of(
                     "nome", paciente.getNome(),
                     "cpf", paciente.getCpf(),
                     "dataNascimento", Date.valueOf(paciente.getDataNascimento()),
                     "telefone", paciente.getTelefone()))) {
            statement.executeUpdate();
        }
    }
}
