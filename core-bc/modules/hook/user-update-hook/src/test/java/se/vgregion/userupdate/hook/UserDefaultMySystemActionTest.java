package se.vgregion.userupdate.hook;

import com.liferay.portal.kernel.events.ActionException;
import com.sun.security.auth.UserPrincipal;
import org.apache.log4j.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.portal.core.domain.liferayuser.LiferayUser;
import se.vgregion.portal.core.domain.liferayuser.LiferayUserRepository;
import se.vgregion.portal.liferay.group.UserGroupUpdateService;
import se.vgregion.portal.myprofile.mysystems.domain.system.ItSystem;
import se.vgregion.portal.myprofile.mysystems.domain.systemarea.SystemArea;
import se.vgregion.portal.myprofile.mysystems.logic.SystemService;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * This action do that and that, if it has something special it is.
 * 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */

public class UserDefaultMySystemActionTest {

    private UserDefaultMySystemAction action;
    private MockHttpServletRequest request;
    private HttpServletResponse response = null;
    private Principal principal;

    private static final String EOL = System.getProperty("line.separator");

    private LiferayUserRepository userRepository;
    private SystemService systemService;
    private UserGroupUpdateService userGroupUpdateService;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();

        action = new UserDefaultMySystemAction();
        userRepository = mock(LiferayUserRepository.class);
        ReflectionTestUtils.setField(action, "userRepository", userRepository);

        systemService = mock(SystemService.class);
        ReflectionTestUtils.setField(action, "systemService", systemService);

        userGroupUpdateService = mock(UserGroupUpdateService.class);
        ReflectionTestUtils.setField(action, "userGroupUpdateService", userGroupUpdateService);
    }

    @Test
    public void testRun_NoLiferayUser() throws Exception {
        final StringWriter writer = setupLogger(UserDefaultMySystemAction.class, Level.WARN);

        principal = new UserPrincipal("-3");
        request.setUserPrincipal(principal);

        // when
        when(userRepository.find(-3L)).thenThrow(new NoResultException());
        action.run(request, response);

        // then
        String[] logMessages = writer.toString().split(EOL);
        assertEquals("WARN - User -3 is not an Liferay user - update cannot be performed.", logMessages[0]);
    }

    @Test
    public void testRun_ExceptionWhenUserDoesNotExist() throws Exception {
        final StringWriter writer = setupLogger(UserDefaultMySystemAction.class, Level.WARN);

        principal = new UserPrincipal("");
        request.setUserPrincipal(principal);

        try {
            action.run(request, response);
        } catch (ActionException ae) {
            // then
            String[] logMessages = writer.toString().split(EOL);
            assertEquals("WARN - No user session exists.", logMessages[0]);
            return;
        }
        assertTrue("ActionException should have been thrown", false);
    }

    @Test
    public void testRun_AddUserToGroup() throws Exception {
        principal = new UserPrincipal("1");
        request.setUserPrincipal(principal);

        // when
        LiferayUser user = mock(LiferayUser.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.find(1L)).thenReturn(user);

        ItSystem itSystem = new ItSystem("test", new SystemArea("testArea"));
        List<ItSystem> itSystemList = new ArrayList<ItSystem>();
        itSystemList.add(itSystem);
        when(systemService.getAllVisibleItSystemsByUserId(1L)).thenReturn(itSystemList);

        when(systemService.getAllSystems()).thenReturn(itSystemList);

        action.run(request, response);

        verify(systemService).addUserMemberToGroups(user, itSystem);

        verify(systemService, never()).removeUserMemberFromGroups(user, itSystem);
    }

    @Test
    public void testRun_RemoveUserFromGroup() throws Exception {
        principal = new UserPrincipal("1");
        request.setUserPrincipal(principal);

        // when
        LiferayUser user = mock(LiferayUser.class);
        when(user.getId()).thenReturn(1L);
        when(userRepository.find(1L)).thenReturn(user);

        ItSystem itSystem = new ItSystem("test", new SystemArea("testArea"));
        List<ItSystem> emptyItSystemList = new ArrayList<ItSystem>();
        when(systemService.getAllVisibleItSystemsByUserId(1L)).thenReturn(emptyItSystemList);

        List<ItSystem> itSystemList = new ArrayList<ItSystem>();
        itSystemList.add(itSystem);
        when(systemService.getAllSystems()).thenReturn(itSystemList);

        action.run(request, response);

        verify(systemService, never()).addUserMemberToGroups(user, itSystem);

        verify(systemService).removeUserMemberFromGroups(user, itSystem);
    }

    @Test
    public void testRun_init_nullServiceRefs() {
        ReflectionTestUtils.setField(action, "userRepository", null);
        ReflectionTestUtils.setField(action, "systemService", null);
        ReflectionTestUtils.setField(action, "userGroupUpdateService", null);

        ApplicationContext ctx = mock(ApplicationContext.class);
        ReflectionTestUtils.setField(action, "ctx", ctx);

        principal = new UserPrincipal("");
        request.setUserPrincipal(principal);

        try {
            action.run(request, response);
        } catch (ActionException ae) {
        }

        verify(ctx, times(3)).getBean(anyString());
    }

    private StringWriter setupLogger(Class<?> loggerClass, Level level) {
        Logger logger = Logger.getLogger(loggerClass);
        logger.setLevel(level);

        final StringWriter writer = new StringWriter();
        Appender appender = new WriterAppender(new SimpleLayout(), writer);
        logger.addAppender(appender);
        return writer;
    }
}
