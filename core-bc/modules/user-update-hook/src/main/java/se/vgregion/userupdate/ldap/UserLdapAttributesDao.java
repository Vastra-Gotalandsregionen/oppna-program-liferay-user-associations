package se.vgregion.userupdate.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import se.vgregion.userupdate.domain.UserLdapAttributes;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-28 11:30
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserLdapAttributesDao {

    @Autowired
    private LdapTemplate ldapTemplate;

    public List<UserLdapAttributes> resolve(String uid) {
        String base = "ou=anv";
        String filter = String.format("(&(objectClass=person)(uid=%s))", uid);
        return ldapTemplate.search(base, filter, new UserLdapAttributesMapper());
    }
}
