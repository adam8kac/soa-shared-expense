package si.um.feri.soa.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import jakarta.annotation.PreDestroy;

@Configuration
public class FirestoreConfig {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Firestore firestoreInstance;

    @Bean(destroyMethod = "")
    public Firestore firestore() {
        try {
            ClassPathResource resource = new ClassPathResource("firestore.json");

            if (!resource.exists()) {
                throw new RuntimeException(
                        "firestore.json not found in classpath. Make sure the file exists in src/main/resources/");
            }

            if (FirebaseApp.getApps().isEmpty()) {
                try (InputStream serviceAccount = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    FirebaseApp.initializeApp(options);
                }
            }

            firestoreInstance = FirestoreClient.getFirestore();

            if (firestoreInstance == null) {
                throw new RuntimeException("Failed to get Firestore instance");
            }

            logger.info("Firestore client initialized successfully");
            return firestoreInstance;
        } catch (IOException e) {
            logger.severe("Failed to initialize Firestore: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Application shutting down - Firestore client will be cleaned up by Firebase SDK");
    }
}
