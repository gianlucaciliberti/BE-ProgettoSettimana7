package com.epicode.salone.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Render fornisce la connessione Postgres come DATABASE_URL nel formato
 * postgres://utente:password@host:porta/nome (stile Heroku), non nel
 * formato jdbc:postgresql://... che si aspetta Spring. Se la variabile
 * c'è, la converte e la espone come spring.datasource.url/username/password
 * con priorità più alta di application.yml. In locale, dove DATABASE_URL
 * non è impostata, non fa nulla e restano i default di application.yml.
 * <p>
 * Registrata in META-INF/spring.factories: gira nella fase di preparazione
 * dell'Environment, prima che il resto del contesto Spring venga creato.
 */
public class DatabaseUrl implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = environment.getProperty("DATABASE_URL");
        if (raw == null || raw.isBlank()) {
            return;
        }

        try {
            URI uri = new URI(raw);
            String[] credenziali = uri.getUserInfo().split(":", 2);
            String utente = credenziali[0];
            String password = credenziali.length > 1 ? credenziali[1] : "";
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();

            Map<String, Object> proprieta = new HashMap<>();
            proprieta.put("spring.datasource.url", jdbcUrl);
            proprieta.put("spring.datasource.username", utente);
            proprieta.put("spring.datasource.password", password);

            environment.getPropertySources().addFirst(new MapPropertySource("databaseUrl", proprieta));
        } catch (URISyntaxException e) {
            // Non si logga il valore grezzo: potrebbe contenere la password del DB.
            throw new IllegalStateException("DATABASE_URL non valida, formato atteso postgres://utente:password@host:porta/nome", e);
        }
    }
}
