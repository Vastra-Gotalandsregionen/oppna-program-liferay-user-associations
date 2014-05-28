package se.vgregion.userupdate.svc;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.ContactLocalService;
import com.liferay.portal.service.UserLocalService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.vgregion.liferay.expando.UserExpandoHelper;
import se.vgregion.liferay.organization.OrganizationHelper;
import se.vgregion.liferay.usergroup.UserGroupHelper;
import se.vgregion.portal.patient.event.PersonNummer;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;

/**
 * Created by IntelliJ IDEA. Created: 2011-11-28 15:16
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserUpdateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserUpdateService.class);
    private final DateFormat df = new SimpleDateFormat("yyyyMMdd");
    private final Lock dfLock = new ReentrantLock();

    @Autowired
    private ContactLocalService contactLocalService;

    @Autowired
    private UserLocalService userLocalService;

    @Autowired
    private UserExpandoHelper userExpandoHelper;

    @Autowired
    private UserGroupHelper userGroupHelper;

    @Autowired
    private OrganizationHelper organizationHelper;

    @Value("${ip_for_external_access}")
    private String ipForExternalAccess;

    private static final String POSTFIX_INTERNAL_ONLY = "_internal_only";

    /**
     * Updates the birthday of a Liferay user with value from a LDAP catalog. If no user is found in the LDAP
     * catalog or person identity number is not set, birthday will be unmodified.
     *
     * @param user               the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes}
     */
    public void updateBirthday(User user, UserLdapAttributes userLdapAttributes) {
        PersonNummer personNummer = userLdapAttributes.getPersonNummer();
        if (personNummer.getType() != PersonNummer.Type.INVALID) {
            try {
                Contact contact = user.getContact();
                final int beginIndex = 0;
                final int endIndex = 8;
                dfLock.lock();
                Date pNoDate;
                try {
                    pNoDate = df.parse(personNummer.getFull().substring(beginIndex, endIndex));
                } finally {
                    dfLock.unlock();
                }
                Date contactDate = contact.getBirthday();
                if (!pNoDate.equals(contactDate)) {
                    contact.setBirthday(pNoDate);
                    contactLocalService.updateContact(contact);
                }
            } catch (Exception e) {
                String msg = String.format("Failed to update birthday for [%s]", user.getScreenName());
                log(msg, e);
            }
        } else {
            String msg = String.format("Failed to update birthday, no personIdentityNumber for [%s]",
                    user.getScreenName());
            LOGGER.info(msg);
        }
    }

    /**
     * Updates the gender of a Liferay user with value from a LDAP catalog. If no user is found in the LDAP catalog
     * or person identity number is not set, gender will be unmodified.
     *
     * @param user the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes}
     */
    public void updateGender(User user, UserLdapAttributes userLdapAttributes) {
        PersonNummer personNummer = userLdapAttributes.getPersonNummer();
        if (personNummer.getType() != PersonNummer.Type.INVALID) {
            boolean isMale = personNummer.getGender() == PersonNummer.Gender.MALE;
            try {
                Contact contact = user.getContact();
                if (isMale != contact.isMale()) {
                    contact.setMale(isMale);
                    contactLocalService.updateContact(contact);
                }
            } catch (Exception e) {
                String msg = String.format("Failed to update gender for [%s]", user.getScreenName());
                log(msg, e);
            }
        } else {
            String msg = String.format("Failed to update gender, no personIdentityNumber for [%s]",
                    user.getScreenName());
            LOGGER.info(msg);
        }
    }

    /**
     * Update a {@link User}'s email.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the email address
     */
    public void updateEmail(User user, UserLdapAttributes userLdapAttributes) {
        String email = userLdapAttributes.getMail();
        if (email == null) {
            email = "";
        }
        try {
            if (!email.equals(user.getEmailAddress())) {
                user.setEmailAddress(email);
                userLocalService.updateUser(user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update email [%s] for [%s]", userLdapAttributes.getMail(),
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s full name.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the full name
     */
    public void updateFullName(User user, UserLdapAttributes userLdapAttributes) {
        // Can be improved if UserLdapAttributes.Type is considered
        String fullName = userLdapAttributes.getDisplayName();
        if (fullName == null) {
            fullName = userLdapAttributes.getFullName();
        }
        if (fullName == null) {
            fullName = "";
        }
        try {
            Contact contact = user.getContact();
            if (!fullName.equals(contact.getUserName())) {
                contact.setUserName(fullName);
                contactLocalService.updateContact(contact);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update fullName [%s] for [%s]", fullName, user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s given name.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the given name
     */
    public void updateGivenName(User user, UserLdapAttributes userLdapAttributes) {
        // Can be improved if UserLdapAttributes.Type is considered
        String givenName = userLdapAttributes.getGivenName();
        if (givenName == null) {
            givenName = "";
        }
        try {
            if (!givenName.equals(user.getFirstName())) {
                user.setFirstName(givenName);
                userLocalService.updateUser(user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update GivenName [%s] for [%s]",
                    userLdapAttributes.getGivenName(), user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s last name.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the last name
     */
    public void updateLastName(User user, UserLdapAttributes userLdapAttributes) {
        // Can be improved if UserLdapAttributes.Type is considered
        String lastName = userLdapAttributes.getSn();
        if (lastName == null) {
            lastName = "";
        }
        try {
            if (!lastName.equals(user.getLastName())) {
                user.setLastName(lastName);
                userLocalService.updateUser(user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update LastName [%s] for [%s]", userLdapAttributes.getSn(),
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s title.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the title
     */
    public void updateTitle(User user, UserLdapAttributes userLdapAttributes) {
        String title = userLdapAttributes.getTitle();
        if (title == null) {
            title = "";
        }
        try {
            if (!title.equals(user.getJobTitle())) {
                user.setJobTitle(title);
                userLocalService.updateUser(user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update Title [%s] for [%s]", userLdapAttributes.getTitle(),
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s HSA title.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the HSA title
     */
    public void updateHsaTitle(User user, UserLdapAttributes userLdapAttributes) {
        String hsaTitle = userLdapAttributes.getHsaTitle();
        if (hsaTitle == null) {
            hsaTitle = "";
        }
        try {
            userExpandoHelper.set("hsaTitle", hsaTitle, user);
        } catch (Exception e) {
            String msg = String.format("Failed to set HsaTitle [%s] for [%s]", userLdapAttributes.getHsaTitle(),
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Updates the prescription code of a Liferay user with value from a LDAP catalog. If no user is found in LDAP
     * catalog prescription code will be cleared.
     *
     * @param user the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the prescription code
     */
    public void updatePrescriptionCode(User user, UserLdapAttributes userLdapAttributes) {
        String prescriptionCode = userLdapAttributes.getHsaPersonPrescriptionCode();
        if (prescriptionCode == null) {
            prescriptionCode = "";
        }
        try {
            userExpandoHelper.set("hsaPrescriptionCode", prescriptionCode, user);

            String groupName = resolveGroupName(user, "PliUsers");
            if (StringUtils.isBlank(prescriptionCode)) {
                userGroupHelper.removeUser(groupName, user);
            } else {
                userGroupHelper.addUser(groupName, user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to set HsaPersonPerscriptionCode [%s] for [%s]", prescriptionCode,
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Sets the Domino user flag on a Liferay user if the users has Domino access according to the LDAP catalog. If
     * no user is found in LDAP the Domino user flag is set to false.
     *
     * @param user the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the isDomino attribute
     */
    public void updateIsDominoUser(User user, UserLdapAttributes userLdapAttributes) {
        boolean isDominoUser = false;
        if (StringUtils.isNotBlank(userLdapAttributes.getMail())
                && userLdapAttributes.getMail().endsWith("@vgregion.se")) {
            isDominoUser = true;
        }
        try {
            userExpandoHelper.set("isDominoUser", isDominoUser, user);

            String dominoGroupName = resolveGroupName(user, "DominoUsers");
            String notDominoGroupName = resolveGroupName(user, "NotDominoUsers");
            if (isDominoUser) {
                userGroupHelper.addUser(dominoGroupName, user);
                userGroupHelper.removeUser(notDominoGroupName, user);
            } else {
                userGroupHelper.addUser(notDominoGroupName, user);
                userGroupHelper.removeUser(dominoGroupName, user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update domino user state [%s] for [%s]", isDominoUser,
                    user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Updates the prescription code of a Liferay user with value from a LDAP catalog. If no user is found in LDAP
     * catalog prescription code will be cleared.
     *
     * @param user the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes} which should contain the VGR admin type attribute
     */
    public void updateVgrAdmin(User user, UserLdapAttributes userLdapAttributes) {
        String vgrAdmin = userLdapAttributes.getVgrAdminType();
        if (vgrAdmin == null) {
            vgrAdmin = "";
        }

        try {
            userExpandoHelper.set("vgrAdminType", vgrAdmin, user);

            String groupName = resolveGroupName(user, "VgrKivAdminUsers");
            if (StringUtils.isNotBlank(vgrAdmin)) {
                userGroupHelper.addUser(groupName, user);
            } else {
                userGroupHelper.removeUser(groupName, user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update vgrAdminType [%s] for [%s]", vgrAdmin,
                    user.getScreenName());
            log(msg, e);
        }
    }

    private String resolveGroupName(User user, String groupName) {
        userGroupHelper.createIfNeeded(groupName, user.getCompanyId());
        if (userGroupHelper.findByName(groupName + POSTFIX_INTERNAL_ONLY, user.getCompanyId()) != null) {
            groupName += POSTFIX_INTERNAL_ONLY;
        }
        return groupName;
    }

    /**
     * Update a {@link User}'s isTandvard attribute.
     *
     * @param user the {@link User}
     * @param userLdapAttributes the {@link UserLdapAttributes} of the corresponding LDAP user
     */
    public void updateIsTandvard(User user, UserLdapAttributes userLdapAttributes) {
        boolean isTandvard = lookupIsTandvard(userLdapAttributes);
        try {
            userExpandoHelper.set("isTandvard", isTandvard, user);

            String groupName = resolveGroupName(user, "TandvardUsers");
            if (isTandvard) {
                userGroupHelper.addUser(groupName, user);
            } else {
                userGroupHelper.removeUser(groupName, user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update isTandvard [%s] for [%s]", isTandvard,
                    user.getScreenName());
            LOGGER.error(msg, e);
        }
    }

    private boolean lookupIsTandvard(UserLdapAttributes userLdapAttributes) {
        boolean tandvard = false;

        String[] strukturGrupp = userLdapAttributes.getStrukturGrupp();
        if (strukturGrupp != null) {
            List<String> strukturGrupps = Arrays.asList(strukturGrupp);
            List<String> tandvardNames = Arrays.asList("Tandvård", "Folktandvården Västra Götaland");
            outer:
            for (String grupp : strukturGrupps) {
                for (String name : tandvardNames) {
                    if (grupp.toLowerCase().contains(name.toLowerCase())) {
                        tandvard = true;
                        break outer;
                    }
                }
            }
        }
        return tandvard;
    }

    /**
     * Update a {@link User}'s vgrLabeledURI attribute.
     *
     * @param user the Liferay user to update
     * @param userOrganizations the {@link UnitLdapAttributes} of the user's organizations
     */
    public void updateVgrLabeledURI(User user, List<UnitLdapAttributes> userOrganizations) {
        List<String> uriList = new ArrayList<String>();
        if (userOrganizations == null) {
            return;
        }

        if (userOrganizations.size() <= 0) {
            uriList.add("http://intra.vgregion.se/");
        }

        for (UnitLdapAttributes unit : userOrganizations) {
            String vgrLabeledURI = unit.getVgrLabeledURI();
            if (StringUtils.isBlank(vgrLabeledURI)) {
                vgrLabeledURI = unit.getLabeledURI();
            }
            if (StringUtils.isNotBlank(vgrLabeledURI)) {
                uriList.add(vgrLabeledURI);
            }
        }

        try {
            userExpandoHelper.set("vgrLabeledURI", uriList.toArray(new String[uriList.size()]), user);
        } catch (Exception e) {
            String msg = String.format("Failed to update vgrLabeledURI %s for [%s]",
                    uriList.toString(), user.getScreenName());
            log(msg, e);
        }
    }

    /**
     * Update a {@link User}'s isPrimarvard attribute.
     *
     * @param user the Liferay user to update
     * @param userOrganizations the {@link UnitLdapAttributes} of the user's organizations.
     */
    public void updateIsPrimarvard(User user, List<UnitLdapAttributes> userOrganizations) {
        if (userOrganizations == null) {
            return;
        }
        boolean isPrimarvard = lookupIsPrimarvard(userOrganizations);

        try {
            userExpandoHelper.set("isPrimarvard", isPrimarvard, user);

            String groupName = resolveGroupName(user, "VGPrimarvardUsers");
            if (isPrimarvard) {
                userGroupHelper.addUser(groupName, user);
            } else {
                userGroupHelper.removeUser(groupName, user);
            }
        } catch (Exception e) {
            String msg = String.format("Failed to update isPrimarvard [%s] for [%s]", isPrimarvard,
                    user.getScreenName());
            log(msg, e);
        }
    }

    private boolean lookupIsPrimarvard(List<UnitLdapAttributes> userOrganizations) {
        boolean isPrimarvard = false;
        for (UnitLdapAttributes unit : userOrganizations) {
            if (StringUtils.isNotBlank(unit.getVgrVardVal())) {
                isPrimarvard = true;
                break;
            }
        }
        return isPrimarvard;
    }

    /**
     * Update a {@link User}'s organizations.
     *
     * @param user the Liferay user to update
     * @param userLdapAttributes the {@link UserLdapAttributes} containing the vgrStrukturPersonDN attribute
     */
    public void updateOrganization(User user, UserLdapAttributes userLdapAttributes) {
        List<String> organizationNames = lookupOrganizationName(userLdapAttributes);
        long companyId = user.getCompanyId();
        try {
            // add to organizations
            List<String> addUserToOrganization = new ArrayList<String>();
            for (String organizationName : organizationNames) {
                if (!organizationHelper.isMember(organizationName, user)) {
                    addUserToOrganization.add(organizationName);
                }
            }
            // remove from organizations
            List<String> removeUserFromOrganization = new ArrayList<String>();
            List<Organization> oldUserOrganizations = user.getOrganizations();
            for (Organization userOrganization : oldUserOrganizations) {
                if (!organizationNames.contains(userOrganization.getName())) {
                    removeUserFromOrganization.add(userOrganization.getName());
                }
            }

            for (String organizationName : addUserToOrganization) {
                Organization organization = organizationHelper.findByName(organizationName, companyId);
                if (organization == null) {
                    organization = organizationHelper.createIfNeeded(organizationName, companyId);
                }
                organizationHelper.addUser(organization, user);
            }

            for (String organizationName : removeUserFromOrganization) {
                Organization organization = organizationHelper.findByName(organizationName, companyId);
                organizationHelper.removeUser(organization, user);
            }

        } catch (Exception e) {
            String msg = String.format("Failed to update organization membership %s for [%s]",
                    organizationNames.toString(), user.getScreenName());
            log(msg, e);
        }

    }

    private List<String> lookupOrganizationName(UserLdapAttributes userLdapAttributes) {
        List<String> organizations = new ArrayList<String>();
        String[] unitDNs = userLdapAttributes.getVgrStrukturPersonDN();

        if (unitDNs != null) {
            for (String unitDN : unitDNs) {
                String orgName = extractOrganization(unitDN);
                if (StringUtils.isNotBlank(orgName)) {
                    organizations.add(orgName);
                }
            }
        }
        return organizations;
    }

    private String extractOrganization(String unitDN) {
        String[] units = unitDN.split(",");
        final int three = 3;
        if (units.length - three >= 0) {
            String[] namePart = units[units.length - three].split("=");
            if (namePart.length == 2) {
                return namePart[1];
            } else {
                String msg = String.format("Strange organization name [%s]", units[units.length - three]);
                LOGGER.warn(msg);
            }
        }
        return null;
    }

    /**
     * Update a {@link User}'s isInternalAccess attribute.
     *
     * @param user the Liferay user to update
     * @param request the {@link HttpServletRequest} which holds information of the access level.
     */
    public void processAccessLevel(User user, HttpServletRequest request) {
        boolean internalAccess = false;
        boolean externalSithsAccess = false;
        try {
            internalAccess = internalAccessRule(request, user);

            if (internalAccess) {
                externalSithsAccess = false;
            } else {
                String smAuthenticationlevel = request.getHeader("sm_authenticationlevel");
                if ("8 or whatever it is".equals(smAuthenticationlevel)) { // TODO gör rätt
                    externalSithsAccess = true;
                }
            }

            userExpandoHelper.set("isInternalAccess", internalAccess, user);
            userExpandoHelper.set("isExternalSithsAccess", externalSithsAccess, user); // TODO ska kolla http headers istället

            userGroupHelper.processInternalAccessOnly(user);
            userGroupHelper.processExternallySithsOnlyAccess(user);
        } catch (Exception e) {
            String msg = String.format("Failed to process isInternalAccess [%s] for [%s]", internalAccess,
                    user.getScreenName());
            log(msg, e);
            request.getSession().invalidate(); // Better to invalidate than risking anyone getting too much access
        }
    }

    private boolean internalAccessRule(HttpServletRequest request, User user) {
        String header = request.getHeader("x-forwarded-for");
        String[] ipsForExternalAccess = ipForExternalAccess.replaceAll(" ", "").split(",");
        if (header != null) {
            // Iterate over the ip:s. We'll find a match if the user is located externally.
            for (String ip : ipsForExternalAccess) {
                if (header.contains(ip)) { // String.contains(...) since the header value may be a comma-separated list.
                    LOGGER.info("User " + user.getScreenName() + " logged in externally.");
                    return false;
                }
            }
        }
        LOGGER.info("User " + user.getScreenName() + " logged in internally.");
        return true;
    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }

    public void updateVegaGroup(User user) {
        try {
            List<Group> groups = user.getGroups(); // A group is a community

            boolean vegaGroupFound = false;
            for (Group group : groups) {
                if (group.getName().equalsIgnoreCase("vega")) {
                    vegaGroupFound = true;
                    break;
                }
            }

            List<UserGroup> userGroups = user.getUserGroups();

            boolean vegaUserGroupFound = false;
            for (UserGroup userGroup : userGroups) {
                if (userGroup.getName().equalsIgnoreCase("Administration-Vega")) {
                    vegaUserGroupFound = true;
                    break;
                }
            }

            if (vegaGroupFound && !vegaUserGroupFound) {
                userGroupHelper.addUser("Administration-Vega", user);
            } else if (!vegaGroupFound && vegaUserGroupFound) {
                userGroupHelper.removeUser("Administration-Vega", user);
            }
        } catch (SystemException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
