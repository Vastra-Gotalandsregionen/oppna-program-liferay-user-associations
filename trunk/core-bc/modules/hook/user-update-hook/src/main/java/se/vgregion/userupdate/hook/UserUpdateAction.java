package se.vgregion.userupdate.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;
import se.vgregion.userupdate.ldap.UserLdapDao;
import se.vgregion.userupdate.svc.UserUpdateService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
        User user = null;
        try {
            init();

            user = lookupUser(request);
            if (user == null) {
                return;
            }

            // First do things which does not need ldap attributes
            userUpdateService.updateVegaGroup(user);

            // Then do things which need the ldap attributes
            UserLdapAttributes userLdapAttributes = lookupInLdap(user.getScreenName());

            if (!user.getScreenName().equals(userLdapAttributes.getUid())) {
                String msg = String.format("Ldap användaren har felaktigt uid [%s] - [%s]", user.getScreenName(),
                        userLdapAttributes.getUid());
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
            userUpdateService.updateIsTandvard(user, userLdapAttributes);
            userUpdateService.updateOrganization(user, userLdapAttributes);

            List<UnitLdapAttributes> unitLdapAttributesList = lookupUserOrganizations(userLdapAttributes);
            userUpdateService.updateVgrLabeledURI(user, unitLdapAttributesList);
            userUpdateService.updateIsPrimarvard(user, unitLdapAttributesList);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        } finally {
            // Access level check - has to be done last
            userUpdateService.processAccessLevel(user, request);
        }
    }

    private List<UnitLdapAttributes> lookupUserOrganizations(UserLdapAttributes userLdapAttributes) {
        List<UnitLdapAttributes> unitLdapAttributesList = null;
        try {
            unitLdapAttributesList = userLdapDao.resolve(userLdapAttributes);
        } catch (Exception ex) {
            String msg = String.format("Failed to lookup user organizations for [%s]",
                    userLdapAttributes.getUid());
            log(msg, ex);
        }
        return unitLdapAttributesList;
    }

    private UserLdapAttributes lookupInLdap(String uid) {
        List<UserLdapAttributes> userLdapAttributesList = userLdapDao.resolve(uid);
        String msg;
        switch (userLdapAttributesList.size()) {
            case 0:
                msg = String.format("Användaren [%s] hittades inte i LDAP", uid);
                throw new RuntimeException(msg);
            case 1:
                return userLdapAttributesList.get(0);
            default:
                msg = String.format("Mer än en användaren [%s] hittades i LDAP", uid);
                throw new RuntimeException(msg);
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
