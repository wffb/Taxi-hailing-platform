package config.filters;



import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
public class FilterConfig {
    @JSONField(name = "name")
    private String name;

    @JSONField(name = "class")
    private String className;

    @JSONField(name = "order")
    private int order;

    @Override
    public String toString() {
        return "FilterConfig{" +
                "name='" + name + '\'' +
                ", className='" + className + '\'' +
                ", order=" + order +
                '}';
    }
}


