package us.jbec.lct.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Configuration class for Google Firebase
 */
@Profile("!dev")
@Configuration
public class FirebaseConfig {

    final Logger LOG = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.serviced-account-json-path}")
    private String servicedAccountJsonPath;


    /**
     * Get FirebaseAuth instance
     * @return FirebaseAuth instance
     */
    @Bean
    public FirebaseAuth firebaseAuth() {
        return FirebaseAuth.getInstance();
    }

    /**
     * Initialize Firebase with serviced account information.
     */
    @PostConstruct
    public void init() {
        try (FileInputStream serviceAccount = new FileInputStream(servicedAccountJsonPath)) {
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
            FirebaseApp.initializeApp(options);
            LOG.info("FirebaseApp successfully initialized");
        } catch (FileNotFoundException e) {
            LOG.error("Could not find serviced account JSON. Check the path in applications.properties.", e);
        } catch (IOException e) {
            LOG.error("Failed to build FirebaseOptions.", e);
        }

    }
}
