import authentication.AuthenticationEnforcer;
import authentication.AuthenticationProvider;
import config.ConfigLoader;
import config.filters.AllFilterConfig;
import filter.AuthFilterChain;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.JarResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

//    public static void main(String[] args) throws LifecycleException {
//        // start tomcat
//        Tomcat tomcat = new Tomcat();
//        tomcat.getConnector().setAttribute("skipTldScanning", true);
//        tomcat.setPort(Integer.getInteger("port", 8080));
//        tomcat.getConnector();
//        // create WebApp
//
//        ClassLoader classLoader = Main.class.getClassLoader();
//        InputStream inputStream = classLoader.getResourceAsStream("webapp");
//        File file =
//
//        Context context = tomcat.addWebapp("", new File(get).getAbsolutePath());
//        WebResourceRoot resources = new StandardRoot(context);
//        resources.addPreResources(
//                new DirResourceSet(resources, "/WEB-INF/classes",
//                        new File("target/classes").getAbsolutePath(), "/"));
//        context.setResources(resources);
//
//        tomcat.start();
//        tomcat.getServer().await();
//    }

    public static void main(String[] args) throws LifecycleException, IOException, URISyntaxException {


        // start  tomcat
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.getInteger("port", 8080));
        tomcat.getConnector();

        // build temp directory
        Path tempDir = Files.createTempDirectory("embedded-tomcat");
        tempDir.toFile().deleteOnExit();

        // set base directory
        tomcat.setBaseDir(tempDir.toString());

        // build webapp context
        StandardContext context = (StandardContext) tomcat.addWebapp("", tempDir.toString());

        // config webapp context
        WebResourceRoot resources = new StandardRoot(context);

        // check if running in JAR file
        if (isRunningInJar()) {
            // get JAR file location
            resources.addJarResources(new JarResourceSet(resources, "/",
                    getJarLocation(), "/META-INF/resources"));

            // add WEB-INF/classes resources
            resources.addJarResources(new JarResourceSet(resources, "/WEB-INF/classes",
                    getJarLocation(), "/"));
        } else {
            // dev mode, add resources from src/main/resources and target/classes
            File resourcesDir = new File("src/main/resources");
            resources.addPreResources(new DirResourceSet(resources, "/",
                    resourcesDir.getAbsolutePath(), "/"));

            // add WEB-INF/classes resources
            File classesDir = new File("target/classes");
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                    classesDir.getAbsolutePath(), "/"));
        }

        context.setResources(resources);

        // start tomcat
        tomcat.start();
        System.out.println("Tomcat started on port(s): " + tomcat.getConnector().getPort() + " (http)");
        tomcat.getServer().await();


    }

    /**
     * check if running in JAR file
     */
    private static boolean isRunningInJar() {
        String protocol = Main.class.getResource("Main.class").getProtocol();
        return "jar".equals(protocol);
    }

    /**
     * get JAR file location
     */
    private static String getJarLocation() throws URISyntaxException {
        // get Main
        return Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
    }
}
