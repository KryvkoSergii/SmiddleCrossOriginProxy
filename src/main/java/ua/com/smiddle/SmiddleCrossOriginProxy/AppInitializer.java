package ua.com.smiddle.SmiddleCrossOriginProxy;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import ua.com.smiddle.SmiddleCrossOriginProxy.config.AppConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;


/**
 * Added by A.Osadchuk on 08.04.2016 at 11:07.
 * Project: SmiddleCampaignManager
 */
@SuppressWarnings("ALL")
public class AppInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        try {
            //Creating Spring context
            AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
            rootContext.register(AppConfig.class);
            //Binding MVC context to IoC
            rootContext.setServletContext(servletContext);
            //MVC Servlet-dispatcher registration
            DispatcherServlet servlet = new DispatcherServlet(rootContext);
            servlet.setThrowExceptionIfNoHandlerFound(true);
            ServletRegistration.Dynamic dispatcher = servletContext.addServlet("dispatcher", servlet);
            dispatcher.setLoadOnStartup(1);
            dispatcher.addMapping("/");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
