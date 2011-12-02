package se.vgregion.userupdate.domain;

import javax.naming.Name;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-29 14:36
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UnitLdapAttributes {
    public Name dn = null;
    public String vgrVardVal;

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getVgrVardVal() {
        return vgrVardVal;
    }

    public void setVgrVardVal(String vgrVardVal) {
        this.vgrVardVal = vgrVardVal;
    }
}
