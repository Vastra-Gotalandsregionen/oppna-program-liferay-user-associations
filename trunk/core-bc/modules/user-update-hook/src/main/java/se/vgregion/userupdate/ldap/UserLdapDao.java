package se.vgregion.userupdate.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-28 11:30
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserLdapDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<UserLdapAttributes> resolve(String uid) {
        String base = "ou=anv,o=vgr";
        String filter = String.format("(&(objectClass=person)(uid=%s))", uid);
        return ldapTemplate.search(base, filter, new UserMapper());
    }

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
