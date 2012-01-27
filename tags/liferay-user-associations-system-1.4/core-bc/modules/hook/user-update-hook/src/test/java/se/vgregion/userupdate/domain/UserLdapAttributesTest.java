package se.vgregion.userupdate.domain;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.vgregion.portal.patient.event.PersonNummer;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-12-07 23:21
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserLdapAttributesTest {
    UserLdapAttributes attr;

    @Before
    public void setup() {
       attr = new UserLdapAttributes();
       attr.setHsaPersonIdentityNumber("191212121212");
       attr.setUid("191311121212");

    }

    @Test
    public void testGetPersonIdentityNumber_Personal() throws Exception {
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals("19121212-1212", pNo.getFull());
    }

    @Test
    public void testGetPersonIdentityNumber_Medborgare() throws Exception {
        attr.setType(UserLdapAttributes.Type.MEDBORGARE);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals("19131112-1212", pNo.getFull());
    }

    @Test
    public void testGetPersonIdentityNumber_Politiker() throws Exception {
        attr.setType(UserLdapAttributes.Type.POLITIKER);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals("19131112-1212", pNo.getFull());
    }

    @Test
    public void testGetPersonIdentityNumber_Externa() throws Exception {
        attr.setType(UserLdapAttributes.Type.EXTERNA);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals(PersonNummer.Type.INVALID, pNo.getType());
    }

    @Test
    public void testGetPersonIdentityNumber_Tmg() throws Exception {
        attr.setType(UserLdapAttributes.Type.TMG);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals(PersonNummer.Type.INVALID, pNo.getType());
    }

    @Test
    public void testGetPersonIdentityNumber_Unknown() throws Exception {
        attr.setType(UserLdapAttributes.Type.UNKNOWN);
        PersonNummer pNo = attr.getPersonNummer();

        assertEquals(PersonNummer.Type.INVALID, pNo.getType());
    }
}
