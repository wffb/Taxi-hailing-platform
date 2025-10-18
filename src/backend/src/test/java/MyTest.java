import DAO.UserDAO;
import authentication.AuthenticationEnforcer;
import authentication.AuthenticationProvider;
import common.util.CacheUtil;
import config.filters.AllFilterConfig;
import filter.AuthFilterChain;
import service.UserService;
import service.WalletService;

import javax.servlet.FilterChain;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MyTest {


    private static void cacheTest() {

    }

    private static void filterTest() {
        try {
            //load config
            AllFilterConfig allFilterConfig = AllFilterConfig.loadConfig();
            //load filters
            AuthenticationProvider.getInstance().loadFilters(allFilterConfig.getFilters());
            //load chains
            for(Map.Entry<String, List<String>> entry : allFilterConfig.getFilterChains().entrySet()){
                AuthFilterChain chain = AuthenticationProvider.getInstance().buildChain(entry.getValue());
                AuthenticationEnforcer.getInstance().registerChain(entry.getKey(), chain);
            }
            //load chain mapping
            for(Map.Entry<String, String> entry : allFilterConfig.getPaths().entrySet()){
                AuthenticationEnforcer.getInstance().registerPath(entry.getKey(), entry.getValue());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void transferTest() {
        System.out.println("transfer test:"+
                WalletService.getInstance().transferWallet(12, 10, new BigDecimal("20"))
        );
    }


    public static void main(String[] args)  {
        transferTest();
        System.out.println("end");
    }
}
