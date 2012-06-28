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

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {

        if (externalAccessRule(request)) {
            LOGGER.info("Is external user, will send redirect");
            try {
                PropertiesBean propertiesBean =
                        (PropertiesBean) getApplicationContext().getBean("propertiesBean");
                response.sendRedirect(propertiesBean.getExternalUserRedirectUrl());
            } catch (IOException e) {
                log(e.getMessage(), e);
            }
        }

    }

    private boolean externalAccessRule(HttpServletRequest request) {
        String header = request.getHeader("x-forwarded-for");
        PropertiesBean propertiesBean = (PropertiesBean) getApplicationContext().getBean("propertiesBean");
        if (header != null && header.contains(propertiesBean.getIpForExternalAccess())) { // there may be a
                                                                                          // comma-separated list
                                                                                          // of IPs
            return true;
        } else {
            return false;
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
