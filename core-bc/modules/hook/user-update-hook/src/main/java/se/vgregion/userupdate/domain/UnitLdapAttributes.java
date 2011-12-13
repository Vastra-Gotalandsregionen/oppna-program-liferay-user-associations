package se.vgregion.userupdate.domain;

import javax.naming.Name;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-29 14:36
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UnitLdapAttributes {
    private Name dn = null;
    private String vgrVardVal;
    private String labeledURI;
    private String vgrLabeledURI;

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

    public String getLabeledURI() {
        return labeledURI;
    }

    public void setLabeledURI(String labeledURI) {
        this.labeledURI = labeledURI;
    }

    public String getVgrLabeledURI() {
        return vgrLabeledURI;
    }

    public void setVgrLabeledURI(String vgrLabeledURI) {
        this.vgrLabeledURI = vgrLabeledURI;
    }
}
