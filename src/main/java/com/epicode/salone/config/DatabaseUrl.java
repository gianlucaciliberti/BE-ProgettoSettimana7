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
 * dell'Environment, prima che il resto del contesto Spring venga creato
 * (e prima che il logging sia configurato: per questo qui sotto si usa
 * System.out invece di un Logger, altrimenti in certi ambienti i
 * messaggi andrebbero persi).
 */
public class DatabaseUrl implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String raw = environment.getProperty("DATABASE_URL");
        System.out.println("[DatabaseUrl] DATABASE_URL presente: " + (raw != null && !raw.isBlank())
                + (raw != null ? " (lunghezza " + raw.length() + ")" : ""));

        if (raw == null || raw.isBlank()) {
            System.out.println("[DatabaseUrl] Nessuna DATABASE_URL: restano i default di application.yml.");
            return;
        }

        try {
            URI uri = new URI(raw);
            String userInfo = uri.getUserInfo();
            System.out.println("[DatabaseUrl] parsata: scheme=" + uri.getScheme() + " host=" + uri.getHost()
                    + " port=" + uri.getPort() + " path=" + uri.getPath() + " userInfoPresente=" + (userInfo != null));

            if (userInfo == null || uri.getHost() == null) {
                throw new IllegalStateException(
                        "DATABASE_URL non ha il formato atteso utente:password@host:porta/nome (userInfo o host mancanti)");
            }

            String[] credenziali = userInfo.split(":", 2);
            String utente = credenziali[0];
            String password = credenziali.length > 1 ? credenziali[1] : "";
            int porta = uri.getPort() != -1 ? uri.getPort() : 5432;
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + porta + uri.getPath();

            Map<String, Object> proprieta = new HashMap<>();
            proprieta.put("spring.datasource.url", jdbcUrl);
            proprieta.put("spring.datasource.username", utente);
            proprieta.put("spring.datasource.password", password);

            environment.getPropertySources().addFirst(new MapPropertySource("databaseUrl", proprieta));
            System.out.println("[DatabaseUrl] spring.datasource.url impostata a: " + jdbcUrl);
        } catch (URISyntaxException e) {
            // Non si logga il valore grezzo: potrebbe contenere la password del DB.
            throw new IllegalStateException("DATABASE_URL non valida, formato atteso postgres://utente:password@host:porta/nome", e);
        }
    }
}
