package se.vgregion.userupdate.svc;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.model.UserGroup;
import com.liferay.portal.service.ContactLocalService;
import com.liferay.portal.service.UserLocalService;
import org.apache.log4j.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.liferay.expando.UserExpandoHelper;
import se.vgregion.liferay.organization.OrganizationHelper;
import se.vgregion.liferay.usergroup.UserGroupHelper;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;



/**
 * Created by IntelliJ IDEA.
 * Created: 2011-12-07 11:10
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserUpdateServiceTest {
    private static final String EOL = System.getProperty("line.separator");

    UserUpdateService userUpdateService;

    @Mock
    ContactLocalService contactLocalService;
    @Mock
    UserLocalService userLocalService;
    @Mock
    UserExpandoHelper userExpandoHelper;
    @Mock
    UserGroupHelper userGroupHelper;
    @Mock
    OrganizationHelper organizationHelper;

    @Mock
    User user;

    @Before
    public void setUp() throws Exception {
        userUpdateService = new UserUpdateService();
        initMocks(this);
        ReflectionTestUtils.setField(userUpdateService, "contactLocalService", contactLocalService);
        ReflectionTestUtils.setField(userUpdateService, "userLocalService", userLocalService);
        ReflectionTestUtils.setField(userUpdateService, "userExpandoHelper", userExpandoHelper);
        ReflectionTestUtils.setField(userUpdateService, "userGroupHelper", userGroupHelper);
        ReflectionTestUtils.setField(userUpdateService, "organizationHelper", organizationHelper);
        ReflectionTestUtils.setField(userUpdateService, "internalAccessGateHosts", "1.1.1.1,2.2.2.2");
    }

    @Test
    public void testUpdateBirthday() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonIdentityNumber("191212121212");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getBirthday()).thenReturn(new Date());

        userUpdateService.updateBirthday(user, attr);

        verify(contact).setBirthday(Matchers.<Date>any());
        verify(contactLocalService).updateContact(eq(contact));
    }

    @Test
    public void testUpdateBirthday_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonIdentityNumber("191212121212");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getBirthday()).thenReturn(new Date());
        when(contactLocalService.updateContact(eq(contact))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateBirthday(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update birthday for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateBirthday_noData() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.INFO);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateBirthday(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("INFO - Failed to update birthday, no personIdentityNumber for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateGender() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonIdentityNumber("191212121212");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.isMale()).thenReturn(false);

        userUpdateService.updateGender(user, attr);

        verify(contact).setMale(eq(true));
        verify(contactLocalService).updateContact(eq(contact));
    }

    @Test
    public void testUpdateGender_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonIdentityNumber("191212121212");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.isMale()).thenReturn(false);
        when(contactLocalService.updateContact(eq(contact))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateGender(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update gender for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateGender_noData() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.INFO);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateGender(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("INFO - Failed to update gender, no personIdentityNumber for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateEmail() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setMail("apa@bepa.se");

        when(user.getEmailAddress()).thenReturn("cepa@bepa.se");

        userUpdateService.updateEmail(user, attr);

        verify(user).setEmailAddress(eq("apa@bepa.se"));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateEmail_guardEmptyMail() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        when(user.getEmailAddress()).thenReturn("cepa@bepa.se");

        userUpdateService.updateEmail(user, attr);

        verify(user).setEmailAddress(eq(""));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateEmail_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setMail("apa@bepa.se");

        when(user.getEmailAddress()).thenReturn("cepa@bepa.se");
        when(userLocalService.updateUser(eq(user))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateEmail(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update email [apa@bepa.se] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateFullName() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setDisplayName("Apa Cepa");
        attr.setFullName("Apa Bepa Cepa");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getUserName()).thenReturn("");

        userUpdateService.updateFullName(user, attr);

        verify(contact).setUserName(eq("Apa Cepa"));
        verify(contactLocalService).updateContact(eq(contact));
    }

    @Test
    public void testUpdateFullName_noDisplayName() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setFullName("Apa Bepa Cepa");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getUserName()).thenReturn("");

        userUpdateService.updateFullName(user, attr);

        verify(contact).setUserName(eq("Apa Bepa Cepa"));
        verify(contactLocalService).updateContact(eq(contact));
    }

    @Test
    public void testUpdateFullName_guardEmptyName() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getUserName()).thenReturn("Err");

        userUpdateService.updateFullName(user, attr);

        verify(contact).setUserName(eq(""));
        verify(contactLocalService).updateContact(eq(contact));
    }

    @Test
    public void testUpdateFullName_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setDisplayName("Apa Cepa");

        Contact contact = mock(Contact.class);
        when(user.getContact()).thenReturn(contact);
        when(contact.getUserName()).thenReturn("");
        when(contactLocalService.updateContact(eq(contact))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateFullName(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update fullName [Apa Cepa] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateGivenName() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setGivenName("Apa");

        when(user.getFirstName()).thenReturn("");

        userUpdateService.updateGivenName(user, attr);

        verify(user).setFirstName(eq("Apa"));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateGivenName_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        when(user.getFirstName()).thenReturn("Err");

        userUpdateService.updateGivenName(user, attr);

        verify(user).setFirstName(eq(""));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateGivenName_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setGivenName("Apa");

        when(user.getFirstName()).thenReturn("");
        when(userLocalService.updateUser(eq(user))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateGivenName(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update GivenName [Apa] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateLastName() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setSn("Cepa");

        when(user.getLastName()).thenReturn("");

        userUpdateService.updateLastName(user, attr);

        verify(user).setLastName(eq("Cepa"));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateLastName_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        when(user.getLastName()).thenReturn("Err");

        userUpdateService.updateLastName(user, attr);

        verify(user).setLastName(eq(""));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateLastName_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setSn("Cepa");

        when(user.getLastName()).thenReturn("");
        when(userLocalService.updateUser(eq(user))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateLastName(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update LastName [Cepa] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateTitle() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setTitle("Title");

        when(user.getJobTitle()).thenReturn("");

        userUpdateService.updateTitle(user, attr);

        verify(user).setJobTitle(eq("Title"));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateTitle_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        when(user.getJobTitle()).thenReturn("Err");

        userUpdateService.updateTitle(user, attr);

        verify(user).setJobTitle(eq(""));
        verify(userLocalService).updateUser(eq(user));
    }

    @Test
    public void testUpdateTitle_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setTitle("Title");

        when(user.getJobTitle()).thenReturn("");
        when(userLocalService.updateUser(eq(user))).thenThrow(new SystemException());
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateTitle(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update Title [Title] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateHsaTitle() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaTitle("HsaTitle");

        userUpdateService.updateHsaTitle(user, attr);

        verify(userExpandoHelper).set(eq("hsaTitle"), eq("HsaTitle"), eq(user));
    }

    @Test
    public void testUpdateHsaTitle_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updateHsaTitle(user, attr);

        verify(userExpandoHelper).set(eq("hsaTitle"), eq(""), eq(user));
    }

    @Test
    public void testUpdateHsaTitle_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaTitle("HsaTitle");

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateHsaTitle(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to set HsaTitle [HsaTitle] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdatePrescriptionCode() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonPrescriptionCode("abc123");

        userUpdateService.updatePrescriptionCode(user, attr);

        verify(userExpandoHelper).set(eq("hsaPrescriptionCode"), eq("abc123"), eq(user));
        verify(userGroupHelper).addUser(eq("PliUsers"), eq(user));
    }

    @Test
    public void testUpdatePrescriptionCode_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updatePrescriptionCode(user, attr);

        verify(userExpandoHelper).set(eq("hsaPrescriptionCode"), eq(""), eq(user));
        verify(userGroupHelper).removeUser(eq("PliUsers"), eq(user));
    }

    @Test
    public void testUpdateHsaPrescriptionCode_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setHsaPersonPrescriptionCode("abc123");

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updatePrescriptionCode(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to set HsaPersonPerscriptionCode [abc123] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateIsDominoUser() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setMail("apa@vgregion.se");

        userUpdateService.updateIsDominoUser(user, attr);

        verify(userExpandoHelper).set(eq("isDominoUser"), eq(true), eq(user));
        verify(userGroupHelper).addUser(eq("DominoUsers"), eq(user));
        verify(userGroupHelper).removeUser(eq("NotDominoUsers"), eq(user));
    }

    @Test
    public void testUpdateIsDominoUser_other() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setMail("apa@google.com");

        userUpdateService.updateIsDominoUser(user, attr);

        verify(userExpandoHelper).set(eq("isDominoUser"), eq(false), eq(user));
        verify(userGroupHelper).addUser(eq("NotDominoUsers"), eq(user));
        verify(userGroupHelper).removeUser(eq("DominoUsers"), eq(user));
    }

    @Test
    public void testUpdateIsDominoUser_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updateIsDominoUser(user, attr);

        verify(userExpandoHelper).set(eq("isDominoUser"), eq(false), eq(user));
        verify(userGroupHelper).addUser(eq("NotDominoUsers"), eq(user));
        verify(userGroupHelper).removeUser(eq("DominoUsers"), eq(user));
    }

    @Test
    public void testUpdateIsDominoUser_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setMail("abc123");

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateIsDominoUser(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update domino user state [false] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateVgrAdmin() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setVgrAdminType("A");

        userUpdateService.updateVgrAdmin(user, attr);

        verify(userExpandoHelper).set(eq("vgrAdminType"), eq("A"), eq(user));
        verify(userGroupHelper).addUser(eq("VgrAdminUsers"), eq(user));
    }

    @Test
    public void testUpdateVgrAdmin_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updateVgrAdmin(user, attr);

        verify(userExpandoHelper).set(eq("vgrAdminType"), eq(""), eq(user));
        verify(userGroupHelper).removeUser(eq("VgrAdminUsers"), eq(user));
    }

    @Test
    public void testUpdateVgrAdminType_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        attr.setVgrAdminType("AB");

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateVgrAdmin(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update vgrAdminType [AB] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateVgrLabeledURI() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] vgrLabeledURI = {"A", "B"};
        attr.setVgrLabeledURI(vgrLabeledURI);

        userUpdateService.updateVgrLabeledURI(user, attr);

        verify(userExpandoHelper).set(eq("vgrLabeledURI"), eq(vgrLabeledURI), eq(user));
    }

    @Test
    public void testUpdateVgrLabeledURI_guardEmpty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updateVgrLabeledURI(user, attr);

        verify(userExpandoHelper).set(eq("vgrLabeledURI"), eq(new String[]{"http://intra.vgregion.se/"}), eq(user));
    }

    @Test
    public void testUpdateVgrLabeledURI_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] vgrLabeledURI = {"A", "B"};
        attr.setVgrLabeledURI(vgrLabeledURI);

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateVgrLabeledURI(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update vgrLabeledURI [A, B] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateIsTandvard() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] strukturGrupp = {"A", "Tandvård", "B"};
        attr.setStrukturGrupp(strukturGrupp);

        userUpdateService.updateIsTandvard(user, attr);

        verify(userExpandoHelper).set(eq("isTandvard"), eq(true), eq(user));
        verify(userGroupHelper).addUser(eq("TandvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsTandvard_Folktandvården() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] strukturGrupp = {"A", "Tandhälsan", "Folktandvården Västra Götaland"};
        attr.setStrukturGrupp(strukturGrupp);

        userUpdateService.updateIsTandvard(user, attr);

        verify(userExpandoHelper).set(eq("isTandvard"), eq(true), eq(user));
        verify(userGroupHelper).addUser(eq("TandvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsTandvard_notTandvård() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] strukturGrupp = {"A", "Tandhälsan"};
        attr.setStrukturGrupp(strukturGrupp);

        userUpdateService.updateIsTandvard(user, attr);

        verify(userExpandoHelper).set(eq("isTandvard"), eq(false), eq(user));
        verify(userGroupHelper).removeUser(eq("TandvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsTandvard_empty() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        userUpdateService.updateIsTandvard(user, attr);

        verify(userExpandoHelper).set(eq("isTandvard"), eq(false), eq(user));
        verify(userGroupHelper).removeUser(eq("TandvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsTandvard_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateIsTandvard(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update isTandvard [false] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateIsPrimarvard() throws Exception {
        UnitLdapAttributes attr = new UnitLdapAttributes();
        attr.setVgrVardVal("vv");

        userUpdateService.updateIsPrimarvard(user, Arrays.asList(attr));

        verify(userExpandoHelper).set(eq("isPrimarvard"), eq(true), eq(user));
        verify(userGroupHelper).addUser(eq("VGPrimarvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsPrimarvard_multi() throws Exception {
        UnitLdapAttributes attr1 = new UnitLdapAttributes();
        UnitLdapAttributes attr2 = new UnitLdapAttributes();
        attr2.setVgrVardVal("vv");

        userUpdateService.updateIsPrimarvard(user, Arrays.asList(attr1, attr2));

        verify(userExpandoHelper).set(eq("isPrimarvard"), eq(true), eq(user));
        verify(userGroupHelper).addUser(eq("VGPrimarvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsPrimarvard_multi2() throws Exception {
        UnitLdapAttributes attr1 = new UnitLdapAttributes();
        UnitLdapAttributes attr2 = new UnitLdapAttributes();

        userUpdateService.updateIsPrimarvard(user, Arrays.asList(attr1, attr2));

        verify(userExpandoHelper).set(eq("isPrimarvard"), eq(false), eq(user));
        verify(userGroupHelper).removeUser(eq("VGPrimarvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsPrimarvard_empty() throws Exception {
        userUpdateService.updateIsPrimarvard(user, Collections.<UnitLdapAttributes>emptyList());

        verify(userExpandoHelper).set(eq("isPrimarvard"), eq(false), eq(user));
        verify(userGroupHelper).removeUser(eq("VGPrimarvardUsers"), eq(user));
    }

    @Test
    public void testUpdateIsPrimarvard_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateIsPrimarvard(user, Collections.<UnitLdapAttributes>emptyList());

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to update isPrimarvard [false] for [apa]", logMessages[0]);
    }

    @Test
    public void testUpdateOrganization_add() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] orgDn = {"ou=U-11,ou=U-1,ou=AAA,o=vgr"};
        attr.setVgrStrukturPersonDN(orgDn);

        when(user.getCompanyId()).thenReturn(1L);
        when(user.getOrganizations()).thenReturn(Collections.<Organization>emptyList());
        Organization organization = mock(Organization.class);
        when(organizationHelper.createIfNeeded(eq("U-1"), eq(1L))).thenReturn(organization);

        userUpdateService.updateOrganization(user, attr);

        verify(organizationHelper).addUser(eq(organization), eq(user));
        verify(organizationHelper, never()).removeUser(Matchers.<Organization>any(), eq(user));
    }

    @Test
    public void testUpdateOrganization_noChange() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] orgDn = {"ou=U-11,ou=U-1,ou=AAA,o=vgr"};
        attr.setVgrStrukturPersonDN(orgDn);

        when(user.getCompanyId()).thenReturn(1L);
        when(user.getOrganizations()).thenReturn(Collections.<Organization>emptyList());
        Organization organization = mock(Organization.class);
        when(organizationHelper.createIfNeeded(eq("U-1"), eq(1L))).thenReturn(organization);
        when(organizationHelper.isMember(eq("U-1"), eq(user))).thenReturn(true);

        userUpdateService.updateOrganization(user, attr);

        verify(organizationHelper, never()).addUser(Matchers.<Organization>any(), eq(user));
        verify(organizationHelper, never()).removeUser(Matchers.<Organization>any(), eq(user));
    }

    @Test
    public void testUpdateOrganization_remove() throws Exception {
        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] orgDn = {"ou=U-11,ou=U-1,ou=AAA,o=vgr"};
        attr.setVgrStrukturPersonDN(orgDn);

        when(user.getCompanyId()).thenReturn(1L);
        when(organizationHelper.isMember(eq("U-1"), eq(user))).thenReturn(true);
        Organization org1 = mock(Organization.class);
        when(org1.getName()).thenReturn("U-1");
        Organization org2 = mock(Organization.class);
        when(org2.getName()).thenReturn("U-2");
        when(user.getOrganizations()).thenReturn(Arrays.asList(org1, org2));
        when(organizationHelper.findByName(eq("U-2"), eq(1L))).thenReturn(org2);

        userUpdateService.updateOrganization(user, attr);

        verify(organizationHelper, never()).addUser(Matchers.<Organization>any(), eq(user));
        verify(organizationHelper).removeUser(eq(org2), eq(user));
    }

    @Test
    public void testUpdateOrganization_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setType(UserLdapAttributes.Type.PERSONAL);
        String[] orgDn = {"ou=U-11,ou=U-1,ou=AAA,o=vgr",
        "ou=U-21,ou=U-2,ou=AAA,o=vgr",
        "ouU-3,ou=AAA,o=vgr",
        };
        attr.setVgrStrukturPersonDN(orgDn);

        doThrow(new RuntimeException()).when(organizationHelper).isMember(anyString(), eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateOrganization(user, attr);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Strange organization name [ouU-3]", logMessages[0]);
        assertEquals("WARN - Failed to update organization membership [U-1, U-2] for [apa]", logMessages[1]);
    }

    @Test
    public void testUpdateInternalAccessOnly_internalAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteHost()).thenReturn("2.2.2.2");
        UserGroup ug1 = mock(UserGroup.class);
        when(ug1.getName()).thenReturn("ug1_internal_only");
        UserGroup ug2 = mock(UserGroup.class);
        when(ug2.getName()).thenReturn("ug2");
        when(user.getUserGroups()).thenReturn(Arrays.asList(ug1, ug2));

        userUpdateService.updateInternalAccessOnly(user, request);

        verify(userExpandoHelper).set("isInternalAccess", true, user);
        verify(userGroupHelper).addUser("ug1", user);
    }

    @Test
    public void testUpdateInternalAccessOnly_notInternalAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteHost()).thenReturn("3.3.3.3");
        UserGroup ug1 = mock(UserGroup.class);
        when(ug1.getName()).thenReturn("ug1_internal_only");
        UserGroup ug2 = mock(UserGroup.class);
        when(ug2.getName()).thenReturn("ug2");
        when(user.getUserGroups()).thenReturn(Arrays.asList(ug1, ug2));

        userUpdateService.updateInternalAccessOnly(user, request);

        verify(userExpandoHelper).set("isInternalAccess", false, user);
        verify(userGroupHelper).removeUser("ug1", user);
    }

    @Test
    public void testUpdateInternalAccessOnly_noGroups() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteHost()).thenReturn("2.2.2.2");

        userUpdateService.updateInternalAccessOnly(user, request);

        verify(userExpandoHelper).set("isInternalAccess", true, user);
    }

    @Test
    public void testUpdateInternalAccessOnly_updateFailed() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateService.class, Level.WARN);

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getRemoteHost()).thenReturn("2.2.2.2");

        doThrow(new RuntimeException()).when(userExpandoHelper).set(anyString(), eq(true),eq(user));
        when(user.getScreenName()).thenReturn("apa");

        userUpdateService.updateInternalAccessOnly(user, request);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Failed to process isInternalAccess [true] for [apa]", logMessages[0]);
    }

    private StringWriter setupLogger(Class loggerClass, Level level) {
        Logger logger = Logger.getLogger(loggerClass);
        logger.setLevel(level);

        final StringWriter writer = new StringWriter();
        Appender appender = new WriterAppender(new SimpleLayout(), writer);
        logger.addAppender(appender);
        return writer;
    }
}
