package authentication;

import DAO.DriverDAO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.filters.FilterConfig;
import config.filters.FilterConstant;
import filter.AuthFilterChain;
import lombok.AllArgsConstructor;
import filter.AuthFilter;
import org.checkerframework.checker.units.qual.A;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//The container class for Filters
public class AuthenticationProvider {

    private static Map<String, Entry> filtersMapping = new ConcurrentHashMap<>();

    private static volatile AuthenticationProvider INSTANCE;
    public AuthenticationProvider() {}
    public static AuthenticationProvider getInstance() {
        if (INSTANCE == null) {
            synchronized (DriverDAO.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthenticationProvider();
                }
            }
        }
        return INSTANCE;
    }


    //The container class for Filters

    public void loadFilters(List<FilterConfig> filters) throws Exception {

        for (FilterConfig node : filters) {

            String className = node.getClassName();
            int order = node.getOrder();
            String name = node.getName();

            Class<?> clazz = Class.forName(className);
            AuthFilter filter = (AuthFilter) clazz.getDeclaredConstructor().newInstance();

            register(name, filter, order);
        }

    }


    //Get the filter chain for the given path
    public AuthFilterChain buildChain (String path){

        //load rule config
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(FilterConstant.CONFIG_NAME)) {

            if (is == null) {
                System.out.println("[Warning] Failed to find filters config:"+ FilterConstant.CONFIG_NAME);
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);

            AuthFilterChain chain = new AuthFilterChain();
            List<Entry> filters = new ArrayList<>();

            JsonNode lists = root.get(path);
            if(lists==null)
                return null;

            for (JsonNode node : lists) {
                //get filter from mapping
                String name = node.get("name").asText();
                Entry filter = filtersMapping.get(name);
                if (filter == null){
                    System.out.println("[Warning]Filter not found: " + name);
                    continue;
                }
                filters.add(filter);
            }

            //sort filters by order
            chain.setFilters(filters.stream()
                    .sorted(Comparator.comparingInt(e -> e.order))
                    .map(e -> e.filter)
                    .toList());

            return chain;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load filters config", e);
        }

    }

    public AuthFilterChain buildChain (List<String> filterNames){

        //load rule config
        try {
            List<Entry> filters = new ArrayList<>();
            AuthFilterChain chain = new AuthFilterChain();

            for (String name : filterNames) {
                //get filter from mapping
                Entry filter = filtersMapping.get(name);
                if (filter == null){
                    System.out.println("[Warning]Filter not found: " + name);
                    continue;
                }
                filters.add(filter);
            }

            //sort filters by order
            chain.setFilters(filters.stream()
                    .sorted(Comparator.comparingInt(e -> e.order))
                    .map(e -> e.filter)
                    .toList());

            return chain;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load filters config", e);
        }

    }

    @AllArgsConstructor
    private static class Entry {
        String name;
        AuthFilter filter;
        int order;
    }
    private static void register(String name, AuthFilter filter, int order){
        Entry e = new Entry(name, filter, order);
        filtersMapping.put(name, e);
    }


}
