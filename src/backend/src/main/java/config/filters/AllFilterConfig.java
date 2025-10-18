package config.filters;

import authentication.AuthenticationProvider;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class AllFilterConfig {
    @JSONField(name = "filters")
    private List<FilterConfig> filters;

    @JSONField(name = "filterChains")
    private Map<String, List<String>> filterChains;

    @JSONField(name = "paths")
    private Map<String, String> paths;



    @Override
    public String toString() {
        return "Config{" +
                "filters=" + filters +
                ", filterChains=" + filterChains +
                ", paths=" + paths +
                '}';
    }
    
    public static AllFilterConfig loadConfig() throws IOException {

        try (InputStream inputStream = AllFilterConfig.class.getClassLoader().getResourceAsStream(FilterConstant.CONFIG_NAME)) {
            if (inputStream == null) {
                System.err.println("文件未找到: filters.json");
                return null;
            }

            // read json file and parse to object
            String jsonContent =  new String(inputStream.readAllBytes());
            AllFilterConfig config = JSON.parseObject(jsonContent, AllFilterConfig.class);

            return config;

        } catch (IOException e) {
            throw e;
        }

    }
}
