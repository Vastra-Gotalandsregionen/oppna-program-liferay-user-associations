package se.vgregion.userupdate.svc;

import com.liferay.portal.model.Contact;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ContactLocalService;
import com.liferay.portal.service.UserLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.vgregion.userupdate.domain.PersonIdentityNumber;
import se.vgregion.userupdate.domain.UserLdapAttributes;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-28 15:16
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserPropertyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPropertyService.class);

    @Autowired
    private ContactLocalService contactLocalService;

    @Autowired
    private UserLocalService userLocalService;

    /**
     * Updates the birthday of a Liferay user with value from a LDAP catalog. If no user is found in the LDAP
     * catalog or person identity number is not set, birthday will be unmodified.
     *
     * @param user the Liferay user to update
     */
    public void updateBirthday(User user, UserLdapAttributes userLdapAttributes) {
        PersonIdentityNumber personIdentityNumber = userLdapAttributes.getPersonIdentityNumber();
        if (personIdentityNumber != null) {
            try {
                Contact contact = user.getContact();
                contact.setBirthday(personIdentityNumber.getBirthday());
                contactLocalService.updateContact(contact);
            } catch (Exception e) {
                String msg = String.format("Failed to update birthday for [%s]", user.getScreenName());
                log(msg, e);
            }
        }
    }

    /**
     * Updates the gender of a Liferay user with value from a LDAP catalog. If no user is found in the LDAP catalog
     * or person identity number is not set, gender will be unmodified.
     *
     * @param user the Liferay user to update
     */
    public void updateGender(User user, UserLdapAttributes userLdapAttributes) {
        PersonIdentityNumber personIdentityNumber = userLdapAttributes.getPersonIdentityNumber();
        if (personIdentityNumber != null) {
            try {
                Contact contact = user.getContact();
                contact.setMale(personIdentityNumber.getGender() == PersonIdentityNumber.Gender.MALE);
                contactLocalService.updateContact(contact);
            } catch (Exception e) {
                String msg = String.format("Failed to update gender for [%s]", user.getScreenName());
                log(msg, e);
            }
        } else {
            String msg = String.format("Failed to update gender for [%s]", user.getScreenName());
            LOGGER.info(msg);
        }
    }

    public void updateEmail(User user, UserLdapAttributes userLdapAttributes) {
        try {
            user.setEmailAddress(userLdapAttributes.getMail());
            userLocalService.updateUser(user);
        } catch (Exception e) {
            String msg = String.format("Failed to update email [%s] for [%s]",
                    userLdapAttributes.getMail(), user.getScreenName());
            log(msg, e);
        }
    }

    public void updateFullName(User user, UserLdapAttributes userLdapAttributes) {
        // Can be improved if UserLdapAttributes.Type is considered
        String fullName = userLdapAttributes.getDisplayName();
        if (fullName == null) {
            fullName = userLdapAttributes.getFullName();
        }
        try {

            Contact contact = user.getContact();
            contact.setUserName(fullName);
            contactLocalService.updateContact(contact);
        } catch (Exception e) {
            String msg = String.format("Failed to update fullName [%s] for [%s]",
                    fullName, user.getScreenName());
            log(msg, e);
        }
    }

    public void updateGivenName(User user, UserLdapAttributes userLdapAttributes) {
        try {
            // Can be improved if UserLdapAttributes.Type is considered
            user.setFirstName(userLdapAttributes.getGivenName());
            userLocalService.updateUser(user);
        } catch (Exception e) {
            String msg = String.format("Failed to update GivenName [%s] for [%s]",
                    userLdapAttributes.getGivenName(), user.getScreenName());
            log(msg, e);
        }
    }

    public void updateLastName(User user, UserLdapAttributes userLdapAttributes) {
        try {
            // Can be improved if UserLdapAttributes.Type is considered
            user.setLastName(userLdapAttributes.getSn());
            userLocalService.updateUser(user);
        } catch (Exception e) {
            String msg = String.format("Failed to update LastName [%s] for [%s]",
                    userLdapAttributes.getSn(), user.getScreenName());
            log(msg, e);
        }
    }

    public void updateTitle(User user, UserLdapAttributes userLdapAttributes) {
        String title = userLdapAttributes.getHsaTitle();
        if (title == null) {
            title = userLdapAttributes.getTitle();
        }
        try {
            user.setJobTitle(title);
            userLocalService.updateUser(user);
        } catch (Exception e) {
            String msg = String.format("Failed to update Title [%s] for [%s]",
                    title, user.getScreenName());
            log(msg, e);
        }
    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }
}
