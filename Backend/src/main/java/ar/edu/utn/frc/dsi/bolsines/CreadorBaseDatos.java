package ar.edu.utn.frc.dsi.bolsines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Crea la base de PostgreSQL de spring.datasource.url si todavía no existe, antes de que Hibernate se conecte.
 * Se conecta a la base administrativa "postgres" del mismo servidor con las credenciales del .env.
 */
class CreadorBaseDatos implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger log = LoggerFactory.getLogger(CreadorBaseDatos.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();
        String url = env.getRequiredProperty("spring.datasource.url");   // jdbc:postgresql://host:puerto/bolsines
        int barra = url.lastIndexOf('/');
        String nombre = url.substring(barra + 1).split("\\?")[0];
        if (!nombre.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalStateException("Nombre de base inválido en DB_NAME: " + nombre);
        }
        String urlAdmin = url.substring(0, barra + 1) + "postgres";
        try (Connection c = DriverManager.getConnection(urlAdmin,
                env.getProperty("spring.datasource.username"), env.getProperty("spring.datasource.password"));
             PreparedStatement existe = c.prepareStatement("select 1 from pg_database where datname = ?")) {
            existe.setString(1, nombre);
            if (existe.executeQuery().next()) return;
            try (Statement st = c.createStatement()) {
                st.execute("CREATE DATABASE " + nombre);
            }
            log.info("Base de datos '{}' creada", nombre);
        } catch (SQLException e) {
            // En hostings con PostgreSQL administrado (ej. Render) la base ya existe y puede no haber acceso a "postgres":
            // se sigue y, si la base realmente no está disponible, falla la conexión de Hibernate con su propio error.
            log.warn("No se pudo verificar/crear la base '{}' desde {} ({}). Se asume que ya existe; si no, revise "
                    + "que PostgreSQL esté iniciado y los valores DB_* de Backend/.env", nombre, urlAdmin, e.getMessage());
        }
    }
}
