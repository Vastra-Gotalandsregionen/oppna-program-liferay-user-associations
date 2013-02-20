package se.vgregion.userupdate.hook;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.userupdate.domain.PropertiesBean;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;

/**
 * Created by IntelliJ IDEA. Created: 2011-11-22 23:37
 * 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class ExternalUserLogoutAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalUserLogoutAction.class);
    private ApplicationContext applicationContext;

    /**
     * As of now, we don't make any difference whether the client is located externally. We redirect to the logout-url
     * independently, except for when the client is targeting the Tomcat directly.
     * @param request
     * @param response
     * @throws ActionException
     */
    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {

        int serverPort = request.getServerPort();
        final int httpPort = 80;
        final int httpsPort = 443;
        if (serverPort == httpPort || serverPort == httpsPort) {
            // Means we are not targeting the tomcat directly, and instead going through a Siteminder. Then redirect.
            try {
                PropertiesBean propertiesBean = (PropertiesBean) getApplicationContext().getBean("propertiesBean");
                response.sendRedirect(propertiesBean.getExternalUserRedirectUrl());
            } catch (IOException e) {
                log(e.getMessage(), e);
            }
        }
    }

    private ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        }
        return applicationContext;
    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }
}
