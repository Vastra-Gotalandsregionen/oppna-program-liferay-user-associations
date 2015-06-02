package se.vgregion.userupdate.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access object using LDAP.
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
@SuppressWarnings("unchecked")
public class UserLdapDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    /**
     * Retrieve {@link UserLdapAttributes}s for a uid.
     *
     * @param uid the uid
     * @return a {@link List} of {@link UserLdapAttributes}
     */
    public List<UserLdapAttributes> resolve(String uid) {
        /*String base = "ou=anv,o=vgr";
        String filter = String.format("(&(objectClass=person)(uid=%s))", uid);*/
        //String base = "OU=usr-Pers,DC=vgregion,DC=se";
        String filter = String.format("(CN=%s)", uid);
        //List result = ldapTemplate.search(base, filter, new UserMapper());
        List result = ldapTemplate.search("", filter, new UserMapper());
        return result;
    }

    /**
     * Retrieve all {@link UnitLdapAttributes} which are associated with a certain {@link UserLdapAttributes}.
     *
     * @param userLdapAttributes the {@link UserLdapAttributes}
     * @return a {@link List} of {@link UnitLdapAttributes}
     */
    public List<UnitLdapAttributes> resolve(UserLdapAttributes userLdapAttributes) {
        List<UnitLdapAttributes> unitList = new ArrayList<UnitLdapAttributes>();
        if (userLdapAttributes.getVgrStrukturPersonDN() != null) {
            for (String orgDn : userLdapAttributes.getVgrStrukturPersonDN()) {
                Object result = ldapTemplate.lookup(orgDn, new UnitMapper());
                if (result != null && (result instanceof UnitLdapAttributes)) {
                    unitList.add((UnitLdapAttributes) result);
                }
            }
        }
        return unitList;
    }
}
