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

    public Type type;
    public Name dn = null;
    public String uid;
    public String cn;
    public String sn;
    public String givenName;
    public String displayName;
    public String fullName;
    public String mail;
    public String hsaTitle;
    public String title;

    public String hsaPersonIdentityNumber;
    public String hsaPersonPrescriptionCode;
    public String vgrAdminType;
    public String[] vgrLabeledURI;
    public String[] vgrStrukturPerson;
    public String[] vgrStrukturPersonDN;
    public String[] StrukturGrupp;

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
        return vgrLabeledURI;
    }

    public void setVgrLabeledURI(String[] vgrLabeledURI) {
        this.vgrLabeledURI = vgrLabeledURI;
    }

    public String[] getVgrStrukturPerson() {
        return vgrStrukturPerson;
    }

    public void setVgrStrukturPerson(String[] vgrStrukturPerson) {
        this.vgrStrukturPerson = vgrStrukturPerson;
    }

    public String[] getVgrStrukturPersonDN() {
        return vgrStrukturPersonDN;
    }

    public void setVgrStrukturPersonDN(String[] vgrStrukturPersonDN) {
        this.vgrStrukturPersonDN = vgrStrukturPersonDN;
    }

    public String[] getStrukturGrupp() {
        return StrukturGrupp;
    }

    public void setStrukturGrupp(String[] strukturGrupp) {
        StrukturGrupp = strukturGrupp;
    }

    private void log(String msg, Throwable ex) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.warn(msg, ex);
        } else {
            LOGGER.warn(msg);
        }
    }
}
