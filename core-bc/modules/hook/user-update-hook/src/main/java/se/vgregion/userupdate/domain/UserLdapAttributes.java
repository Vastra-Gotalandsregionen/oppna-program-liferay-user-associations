package se.vgregion.userupdate.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.vgregion.portal.patient.event.PersonNummer;

import javax.naming.Name;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-22 23:45
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserLdapAttributes {
    public enum Type {
        PERSONAL, EXTERNA, MEDBORGARE, POLITIKER, TMG, UNKNOWN;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(UserLdapAttributes.class);

    private Type type;
    private Name dn = null;
    private String uid;
    private String cn;
    private String sn;
    private String givenName;
    private String displayName;
    private String fullName;
    private String mail;
    private String hsaTitle;
    private String title;

    private String hsaPersonIdentityNumber;
    private String hsaPersonPrescriptionCode;
    private String vgrAdminType;
    private String[] vgrLabeledURI;
    private String[] vgrStrukturPerson;
    private String[] vgrStrukturPersonDN;
    private String[] strukturGrupp;

    /**
     * Retrieve the personNummer for all types of individuals, i.e. personal, medborgare and politiker.
     *
     * @return the {@link PersonNummer}
     */
    public PersonNummer getPersonNummer() {
        String pNo = null;
        switch (getType()) {
            case PERSONAL:
                pNo = getHsaPersonIdentityNumber();
                break;
            case MEDBORGARE:
                pNo = getUid();
                break;
            case POLITIKER:
                pNo = getUid();
                break;
            default:
                String msg = String.format("Ldap user of type [%s] has no personIdentityNumber information",
                        getType());
                LOGGER.info(msg);
                pNo = null;
        }
        return PersonNummer.personummer(pNo);
    }


    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getHsaPersonIdentityNumber() {
        return hsaPersonIdentityNumber;
    }

    public void setHsaPersonIdentityNumber(String hsaPersonIdentityNumber) {
        this.hsaPersonIdentityNumber = hsaPersonIdentityNumber;
    }

    public String getHsaPersonPrescriptionCode() {
        return hsaPersonPrescriptionCode;
    }

    public void setHsaPersonPrescriptionCode(String hsaPersonPrescriptionCode) {
        this.hsaPersonPrescriptionCode = hsaPersonPrescriptionCode;
    }

    public String getHsaTitle() {
        return hsaTitle;
    }

    public void setHsaTitle(String hsaTitle) {
        this.hsaTitle = hsaTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getVgrAdminType() {
        return vgrAdminType;
    }

    public void setVgrAdminType(String vgrAdminType) {
        this.vgrAdminType = vgrAdminType;
    }

    public String[] getVgrLabeledURI() {
        if (vgrLabeledURI != null) {
            return vgrLabeledURI.clone();
        }
        return null;
    }

    public void setVgrLabeledURI(String[] vgrLabeledURI) {
        this.vgrLabeledURI = vgrLabeledURI.clone();
    }

    public String[] getVgrStrukturPerson() {
        if (vgrStrukturPerson != null) {
            return vgrStrukturPerson.clone();
        }
        return null;
    }

    public void setVgrStrukturPerson(String[] vgrStrukturPerson) {
        this.vgrStrukturPerson = vgrStrukturPerson.clone();
    }

    public String[] getVgrStrukturPersonDN() {
        if (vgrStrukturPersonDN != null) {
            return vgrStrukturPersonDN.clone();
        }
        return null;
    }

    public void setVgrStrukturPersonDN(String[] vgrStrukturPersonDN) {
        this.vgrStrukturPersonDN = vgrStrukturPersonDN.clone();
    }

    public String[] getStrukturGrupp() {
        if (strukturGrupp != null) {
            return strukturGrupp.clone();
        }
        return null;
    }

    public void setStrukturGrupp(String[] strukturGrupp) {
        this.strukturGrupp = strukturGrupp.clone();
    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }
}
