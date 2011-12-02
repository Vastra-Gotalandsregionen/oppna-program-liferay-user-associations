package se.vgregion.userupdate.hook;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;
import se.vgregion.userupdate.ldap.UserLdapDao;
import se.vgregion.userupdate.svc.UserUpdateService;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;

/**
 * Created by IntelliJ IDEA. Created: 2011-11-22 23:37
 * 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserUpdateAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUpdateAction.class);

    private ApplicationContext applicationContext;
    private UserLocalService userLocalService;
    private UserLdapDao userLdapDao;
    private UserUpdateService userUpdateService;

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        init();

        User user = lookupUser(request);
        if (user == null) {
            return;
        }

        try {
            UserLdapAttributes userLdapAttributes = lookupInLdap(user.getScreenName());

            if (!user.getScreenName().equals(userLdapAttributes.getUid())) {
                String msg = String.format("Ldap användaren har felaktigt uid [%s] - [%s]", user.getScreenName(),
                        userLdapAttributes.getUid());
                LOGGER.error(msg);
                throw new RuntimeException(msg);
            }

            userUpdateService.updateBirthday(user, userLdapAttributes);
            userUpdateService.updateGender(user, userLdapAttributes);
            userUpdateService.updateEmail(user, userLdapAttributes);
            userUpdateService.updateFullName(user, userLdapAttributes);
            userUpdateService.updateGivenName(user, userLdapAttributes);
            userUpdateService.updateLastName(user, userLdapAttributes);
            userUpdateService.updateTitle(user, userLdapAttributes);
            userUpdateService.updateHsaTitle(user, userLdapAttributes);
            userUpdateService.updatePrescriptionCode(user, userLdapAttributes);
            userUpdateService.updateIsDominoUser(user, userLdapAttributes);
            userUpdateService.updateVgrAdmin(user, userLdapAttributes);
            userUpdateService.updateVgrLabeledURI(user, userLdapAttributes);
            userUpdateService.updateIsTandvard(user, userLdapAttributes);
            userUpdateService.updateOrganization(user, userLdapAttributes);

            List<UnitLdapAttributes> unitLdapAttributesList = userLdapDao.resolve(userLdapAttributes);
            userUpdateService.updateIsPrimarvard(user, unitLdapAttributesList);
        } finally {
            // internal access only check - has to be done last
            userUpdateService.updateInternalAccessOnly(user, request);
        }
    }

    private UserLdapAttributes lookupInLdap(String uid) {
        List<UserLdapAttributes> userLdapAttributesList = userLdapDao.resolve(uid);
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
        if (userLdapDao == null) {
            userLdapDao = (UserLdapDao) getApplicationContext().getBean("userLdapDao");
        }
        if (userUpdateService == null) {
            userUpdateService = (UserUpdateService) getApplicationContext().getBean("userUpdateService");
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
