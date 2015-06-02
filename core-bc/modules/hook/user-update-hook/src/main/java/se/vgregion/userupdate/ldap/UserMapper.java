package se.vgregion.userupdate.ldap;

import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.simple.ParameterizedContextMapper;
import se.vgregion.userupdate.domain.UserLdapAttributes;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-11-28 11:05
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserMapper implements ParameterizedContextMapper<UserLdapAttributes> {
    @Override
    public UserLdapAttributes mapFromContext(Object context) {
        DirContextAdapter ctx = (DirContextAdapter) context;

        // Change from ldap-poc to AD.
        UserLdapAttributes attrs = new UserLdapAttributes();
        attrs.setDn(ctx.getDn());
        attrs.setUid(ctx.getStringAttribute("cn"));
        attrs.setCn(ctx.getStringAttribute("cn"));
        attrs.setSn(ctx.getStringAttribute("sn"));
        attrs.setGivenName(ctx.getStringAttribute("givenName"));
        attrs.setDisplayName(ctx.getStringAttribute("displayName"));
        String initials = ctx.getStringAttribute("initials");
        if (initials != null && !initials.isEmpty()) {
            attrs.setFullName(attrs.getGivenName() + " " + initials + " " + attrs.getSn()); // ctx.getStringAttribute("fullName")
        } else {
            attrs.setFullName(attrs.getGivenName() + " " + attrs.getSn()); // ctx.getStringAttribute("fullName")
        }
        attrs.setMail(ctx.getStringAttribute("mail"));
        attrs.setHsaTitle(ctx.getStringAttribute("hsaTitle"));
        attrs.setTitle(ctx.getStringAttribute("title"));
        //attrs.setHsaPersonIdentityNumber(ctx.getStringAttribute("hsaPersonIdentityNumber")); // Saknas?
        //attrs.setHsaPersonPrescriptionCode(ctx.getStringAttribute("hsaPersonPrescriptionCode")); // Saknas?
        //attrs.setVgrAdminType(ctx.getStringAttribute("vgrAdminType"));  // Saknas?
        //attrs.setVgrLabeledURI(ctx.getStringAttributes("labeledURI"));  // Saknas?
        attrs.setVgrStrukturPerson(ctx.getStringAttributes("vgrStrukturPerson"));
        attrs.setVgrStrukturPersonDN(ctx.getStringAttributes("vgrStrukturPerson")); // vgrStrukturPersonDN
        attrs.setStrukturGrupp(ctx.getStringAttributes("StrukturGrupp")); // Saknas StrukturGrupp

        String type = ctx.getDn().toString();
        if (type.contains("ou=personal")) {
            attrs.setType(UserLdapAttributes.Type.PERSONAL);
        } else if (type.contains("ou=externa")) {
            attrs.setType(UserLdapAttributes.Type.EXTERNA);
        } else if (type.contains("ou=medborgare")) {
            attrs.setType(UserLdapAttributes.Type.MEDBORGARE);
        } else if (type.contains("ou=politiker")) {
            attrs.setType(UserLdapAttributes.Type.POLITIKER);
        } else if (type.contains("ou=admin")) {
            attrs.setType(UserLdapAttributes.Type.TMG);
        } else {
            attrs.setType(UserLdapAttributes.Type.UNKNOWN);
        }

        return attrs;
    }
}
