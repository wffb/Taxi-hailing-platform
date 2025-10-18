package config;

import DAO.DriverDAO;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigLoader {

    private static volatile ConfigLoader INSTANCE;
    public static ConfigLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ConfigLoader();
                }
            }
        }
        return INSTANCE;
    }

    public String loadTomcatConfig() {
        return this.getClass().getClassLoader().getResource("webapp").getPath();
    }
}
