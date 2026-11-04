package rs.raf.eventbooker.config;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.repository.CategoryRepository;
import rs.raf.eventbooker.repository.impl.CategoryRepositoryJdbcImpl;
import rs.raf.eventbooker.service.CategoryService;
import rs.raf.eventbooker.service.impl.CategoryServiceImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("[AppInitializer] init start");
            Db.initAndMigrate();
            CategoryRepository catRepo = new CategoryRepositoryJdbcImpl();
            CategoryService catService = new CategoryServiceImpl(catRepo);
            sce.getServletContext().setAttribute("categoryService", catService);
            System.out.println("[AppInitializer] init OK");
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("App init failed", t);
        }
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[AppInitializer] close start");

        try {
            Db.shutdown();
        } catch (Exception ignore) {}

        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        try {
            Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                Driver d = drivers.nextElement();
                DriverManager.deregisterDriver(d);
                System.out.println("[AppInitializer] deregistered driver: " + d);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }


}
