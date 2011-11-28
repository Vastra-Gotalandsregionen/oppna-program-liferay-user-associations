package se.vgregion.userupdate.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.vgregion.userupdate.domain.UserLdapAttributes;
import se.vgregion.userupdate.ldap.UserLdapAttributesDao;
import se.vgregion.userupdate.svc.UserPropertyService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-22 23:37
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserUpdateAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUpdateAction.class);

    private ApplicationContext applicationContext;
    private UserLocalService userLocalService;
    private UserLdapAttributesDao userLdapAttributesDao;
    private UserPropertyService userPropertyService;

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        init();
        User user = lookupUser(request);
        if (user == null) {
            return;
        }

        UserLdapAttributes userLdapAttributes = lookupInLdap(user.getScreenName());

        if (!user.getScreenName().equals(userLdapAttributes.getUid())) {
            String msg = String.format("Ldap användaren har felaktigt uid [%s] - [%s]", user.getScreenName(),
                    userLdapAttributes.getUid());
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }

        userPropertyService.updateBirthday(user, userLdapAttributes);
        userPropertyService.updateGender(user, userLdapAttributes);
        userPropertyService.updateEmail(user, userLdapAttributes);
        userPropertyService.updateFullName(user, userLdapAttributes);
        userPropertyService.updateGivenName(user, userLdapAttributes);
        userPropertyService.updateLastName(user, userLdapAttributes);
        userPropertyService.updateTitle(user, userLdapAttributes);

        String apa = "apa";
    }

    private UserLdapAttributes lookupInLdap(String uid) {
        List<UserLdapAttributes> userLdapAttributesList = userLdapAttributesDao.resolve(uid);
        switch (userLdapAttributesList.size()) {
            case 0: {
                String msg = String.format("Användaren [%s] hittades inte i LDAP", uid);
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }
            case 1:
                return userLdapAttributesList.get(0);
            default: {
                String msg = String.format("Mer än en användaren [%s] hittades i LDAP", uid);
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }
        }

    }

    private User lookupUser(HttpServletRequest request) {
        try {
            Long userId = Long.parseLong(request.getRemoteUser());

            User user = userLocalService.getUser(userId);
            if (user == null) {
                throw new Exception("Användaren hittades inte");
            }
            return user;
        } catch (Exception ex) {
            String msg = String.format("Användaren med id [%s] finns inte i Liferays användar databas",
                    request.getRemoteUser());
            log(msg, ex);
        }
        return null;
    }

    private void init() {
        if (userLocalService == null) {
            userLocalService = (UserLocalService) getApplicationContext().getBean("userLocalService");
        }
        if (userLdapAttributesDao == null) {
            userLdapAttributesDao = (UserLdapAttributesDao) getApplicationContext().getBean("userLdapAttributesDao");
        }
        if (userPropertyService == null) {
            userPropertyService = (UserPropertyService) getApplicationContext().
                    getBean("userPropertyService");
        }

    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }

    private ApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        }
        return applicationContext;
    }
}
