package se.vgregion.userupdate.hook;

import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;
import org.apache.log4j.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.userupdate.domain.UnitLdapAttributes;
import se.vgregion.userupdate.domain.UserLdapAttributes;
import se.vgregion.userupdate.ldap.UserLdapDao;
import se.vgregion.userupdate.svc.UserUpdateService;

import javax.servlet.http.HttpServletRequest;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by IntelliJ IDEA.
 * Created: 2011-12-08 16:46
 *
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserUpdateActionTest {
    private static final String EOL = System.getProperty("line.separator");

    UserUpdateAction action;

    @Mock
    UserLdapDao userLdapDao;
    @Mock
    UserLocalService userLocalService;
    @Mock
    UserUpdateService userUpdateService;
    @Mock
    HttpServletRequest request;
    @Mock
    User user;

    @Before
    public void setUp() throws Exception {
        action = new UserUpdateAction();
        initMocks(this);
        ReflectionTestUtils.setField(action, "userLocalService", userLocalService);
        ReflectionTestUtils.setField(action, "userLdapDao", userLdapDao);
        ReflectionTestUtils.setField(action, "userUpdateService", userUpdateService);
    }

    @Test
    public void testRun() throws Exception {
        when(request.getRemoteUser()).thenReturn("1");
        when(userLocalService.getUser(1L)).thenReturn(user);
        when(user.getScreenName()).thenReturn("test");

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setUid("test");
        when(userLdapDao.resolve("test")).thenReturn(Arrays.asList(attr));

        List<UnitLdapAttributes> uAttr = Arrays.asList(new UnitLdapAttributes());
        when(userLdapDao.resolve(attr)).thenReturn(uAttr);

        action.run(request, null);
        // User
        verify(userUpdateService).updateBirthday(user, attr);
        verify(userUpdateService).updateGender(user, attr);
        verify(userUpdateService).updateEmail(user, attr);
        verify(userUpdateService).updateFullName(user, attr);
        verify(userUpdateService).updateGivenName(user, attr);
        verify(userUpdateService).updateLastName(user, attr);
        verify(userUpdateService).updateTitle(user, attr);
        verify(userUpdateService).updateHsaTitle(user, attr);
        verify(userUpdateService).updatePrescriptionCode(user, attr);
        verify(userUpdateService).updateIsDominoUser(user, attr);
        verify(userUpdateService).updateVgrAdmin(user, attr);
        verify(userUpdateService).updateIsTandvard(user, attr);
        verify(userUpdateService).updateOrganization(user, attr);
        // Organization
        verify(userUpdateService).updateVgrLabeledURI(user, uAttr);
        verify(userUpdateService).updateIsPrimarvard(user, uAttr);
        // Request
        verify(userUpdateService).processAccessLevel(user, request);
    }

    @Test
    public void testRun_noUserInLiferay() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateAction.class, Level.WARN);

        when(request.getRemoteUser()).thenReturn("1");
        when(userLocalService.getUser(1L)).thenReturn(null);

        action.run(request, null);
        // User
        verify(userUpdateService, never()).updateBirthday(Matchers.<User>any(), Matchers.<UserLdapAttributes>any());
        // Request
        verify(userUpdateService, never()).processAccessLevel(user, request);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Användaren med id [1] finns inte i Liferays användar databas", logMessages[0]);
    }

    @Test
    public void testRun_multiUserInLdap() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateAction.class, Level.WARN);

        when(request.getRemoteUser()).thenReturn("1");
        when(userLocalService.getUser(1L)).thenReturn(user);
        when(user.getScreenName()).thenReturn("test");

        UserLdapAttributes attr1 = new UserLdapAttributes();
        UserLdapAttributes attr2 = new UserLdapAttributes();
        when(userLdapDao.resolve("test")).thenReturn(Arrays.asList(attr1, attr2));

        action.run(request, null);
        // User
        verify(userUpdateService, never()).updateBirthday(Matchers.<User>any(), Matchers.<UserLdapAttributes>any());
        // Request
        verify(userUpdateService).processAccessLevel(user, request);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Mer än en användaren [test] hittades i LDAP", logMessages[0]);
    }

    @Test
    public void testRun_noUserInLdap() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateAction.class, Level.WARN);

        when(request.getRemoteUser()).thenReturn("1");
        when(userLocalService.getUser(1L)).thenReturn(user);
        when(user.getScreenName()).thenReturn("test");

        when(userLdapDao.resolve("test")).thenReturn(Collections.<UserLdapAttributes>emptyList());

        action.run(request, null);
        // User
        verify(userUpdateService, never()).updateBirthday(Matchers.<User>any(), Matchers.<UserLdapAttributes>any());
        // Request
        verify(userUpdateService).processAccessLevel(user, request);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Användaren [test] hittades inte i LDAP", logMessages[0]);
    }

    @Test
    public void testRun_ldapDataError() throws Exception {
        final StringWriter writer = setupLogger(UserUpdateAction.class, Level.WARN);

        when(request.getRemoteUser()).thenReturn("1");
        when(userLocalService.getUser(1L)).thenReturn(user);
        when(user.getScreenName()).thenReturn("test");

        UserLdapAttributes attr = new UserLdapAttributes();
        attr.setUid("err");
        when(userLdapDao.resolve("test")).thenReturn(Arrays.asList(attr));

        action.run(request, null);
        // User
        verify(userUpdateService, never()).updateBirthday(Matchers.<User>any(), Matchers.<UserLdapAttributes>any());
        // Request
        verify(userUpdateService).processAccessLevel(user, request);

        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - Ldap användaren har felaktigt uid [test] - [err]", logMessages[0]);
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
