package se.vgregion.userupdate.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import se.vgregion.userupdate.domain.UnitLdapAttributes;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-29 14:28
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UnitContextMapper implements ParameterizedContextMapper<UnitLdapAttributes> {
    @Override
    public UnitLdapAttributes mapFromContext(Object ctx) {
        DirContextAdapter adapter = (DirContextAdapter) ctx;

        UnitLdapAttributes unit = new UnitLdapAttributes();
        unit.setDn(adapter.getDn());
        unit.setVgrVardVal(adapter.getStringAttribute("vgrVardval"));

        return unit;
    }
}
