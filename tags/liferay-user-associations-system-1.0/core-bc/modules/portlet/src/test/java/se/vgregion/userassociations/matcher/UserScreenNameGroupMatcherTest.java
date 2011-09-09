package se.vgregion.userassociations.matcher;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Contact;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.UserLocalService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import se.vgregion.liferay.application.userproperty.ContactStub;
import se.vgregion.liferay.application.userproperty.UserStub;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 29/8-11
 * Time: 13:14
 */
public class UserScreenNameGroupMatcherTest {

    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void testProcessScreennameNotEx() throws Exception {
        long companyId = 0l;

        UserScreenNameGroupMatcher matcher = new UserScreenNameGroupMatcher(companyId, "^((?!^ex_.*).)*$",
                Arrays.asList("Apa", "Bepa", "Cepa"));

        GroupLocalService mockGroupLocalService = mock(GroupLocalService.class);
        ReflectionTestUtils.setField(matcher, "groupLocalService", mockGroupLocalService);

        UserLocalService mockUserLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(matcher, "userLocalService", mockUserLocalService);

        ReflectionTestUtils.setField(matcher, "groupIds", Arrays.asList(1l, 2l, 3l));

        User user = getUserStub(companyId, 12345l, "mockUser", new long[] {});

        matcher.process(user);

        verify(mockUserLocalService).addGroupUsers(1l, new long[]{12345l});
        verify(mockUserLocalService).addGroupUsers(2l, new long[]{12345l});
        verify(mockUserLocalService).addGroupUsers(3l, new long[]{12345l});
    }
    @Test
    public void testProcessScreennameNotEx2() throws Exception {
        long companyId = 0l;

        UserScreenNameGroupMatcher matcher = new UserScreenNameGroupMatcher(companyId, "ex_.*",
                Arrays.asList("Apa"));

        GroupLocalService mockGroupLocalService = mock(GroupLocalService.class);
        ReflectionTestUtils.setField(matcher, "groupLocalService", mockGroupLocalService);

        UserLocalService mockUserLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(matcher, "userLocalService", mockUserLocalService);

        ReflectionTestUtils.setField(matcher, "groupIds", Arrays.asList(1l));

        User user = getUserStub(companyId, 12345l, "mockUser", new long[] {});

        matcher.process(user);

        verify(mockUserLocalService, never()).addGroupUsers(1l, new long[]{12345l});
    }

    @Test
    public void testProcessScreennameAlreadyMember() throws Exception {
        long companyId = 0l;

        UserScreenNameGroupMatcher matcher = new UserScreenNameGroupMatcher(companyId, "ex_.*",
                Arrays.asList("Apa"));

        GroupLocalService mockGroupLocalService = mock(GroupLocalService.class);
        ReflectionTestUtils.setField(matcher, "groupLocalService", mockGroupLocalService);

        UserLocalService mockUserLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(matcher, "userLocalService", mockUserLocalService);

        ReflectionTestUtils.setField(matcher, "groupIds", Arrays.asList(1l));

        User user = getUserStub(companyId, 12345l, "ex_mockUser", new long[] {1l});

        matcher.process(user);

        verify(mockUserLocalService, never()).addGroupUsers(1l, new long[]{12345l});
    }

    @Test
    public void testProcessScreennameEx() throws Exception {
        long companyId = 0l;

        UserScreenNameGroupMatcher matcher = new UserScreenNameGroupMatcher(companyId, "^((?!^ex_.*).)*$",
                Arrays.asList("Apa"));

        GroupLocalService mockGroupLocalService = mock(GroupLocalService.class);
        ReflectionTestUtils.setField(matcher, "groupLocalService", mockGroupLocalService);

        UserLocalService mockUserLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(matcher, "userLocalService", mockUserLocalService);

        ReflectionTestUtils.setField(matcher, "groupIds", Arrays.asList(1l));

        User user = getUserStub(companyId, 12345l, "ex_mockUser", new long[] {});

        matcher.process(user);

        verify(mockUserLocalService, never()).addGroupUsers(1l, new long[]{12345l});
    }

    @Test
    public void testProcessScreennameEx2() throws Exception {
        long companyId = 0l;

        UserScreenNameGroupMatcher matcher = new UserScreenNameGroupMatcher(companyId, "ex_.*",
                Arrays.asList("Apa"));

        GroupLocalService mockGroupLocalService = mock(GroupLocalService.class);
        ReflectionTestUtils.setField(matcher, "groupLocalService", mockGroupLocalService);

        UserLocalService mockUserLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(matcher, "userLocalService", mockUserLocalService);

        ReflectionTestUtils.setField(matcher, "groupIds", Arrays.asList(1l));

        User user = getUserStub(companyId, 12345l, "ex_mockUser", new long[] {});

        matcher.process(user);

        verify(mockUserLocalService).addGroupUsers(1l, new long[]{12345l});
    }

    private User getUserStub(long companyId, long userId, String screenName, long[] currentGroups) {
        User user = mock(User.class);
        when(user.getCompanyId()).thenReturn(companyId);
        when(user.getUserId()).thenReturn(userId);
        when(user.getScreenName()).thenReturn(screenName);
        try {
            when(user.getGroupIds()).thenReturn(currentGroups);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

}
