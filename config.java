import java.io.FileInputStream; 
import java.io.IOException;
import java.util.Properties;
public class config {
    private static Properties properties = new Properties();

    static {
        try (FileInputStream input = new FileInputStream("config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration properties.");
        }
    }

    public static String getApiKey() {
        return properties.getProperty("API_KEY");
    }
}
