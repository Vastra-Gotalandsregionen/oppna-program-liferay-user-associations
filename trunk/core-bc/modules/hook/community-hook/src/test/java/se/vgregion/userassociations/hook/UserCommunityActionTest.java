/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */
package se.vgregion.userassociations.hook;

import com.liferay.portal.kernel.util.Props;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.util.Portal;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 30/8-11
 * Time: 09:47
 */
public class UserCommunityActionTest {
    @Before
    public void setUp() throws Exception {
        GroupLocalService groupLocalService = mock(GroupLocalService.class);
        Group extern = mock(Group.class);
        Group vgr = mock(Group.class);
        when(groupLocalService.getGroup(anyLong(), eq("VGRegion"))).thenReturn(vgr);
        when(vgr.getGroupId()).thenReturn(1l);
        when(groupLocalService.getGroup(anyLong(), eq("Extern"))).thenReturn(extern);
        when(extern.getGroupId()).thenReturn(2l);

        Field service = GroupLocalServiceUtil.class.getDeclaredField("_service");
        service.setAccessible(true);
        service.set(null, groupLocalService);
        //new GroupLocalServiceUtil().setService(groupLocalService);

        PropsUtil.setProps(mock(Props.class));
    }

    @Test
    public void testConstructor() throws Exception {
        UserCommunityAction action = new UserCommunityAction();
        List<String> matchers = (List<String>) ReflectionTestUtils.getField(action, "matcherList");

        assertEquals(2, matchers.size());
    }

    @Test
    public void testRun() throws Exception {
        UserCommunityAction action = new UserCommunityAction();
        UserLocalService userLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(action, "userLocalService", userLocalService);
        Portal portal = mock(Portal.class);
        ReflectionTestUtils.setField(action, "portal", portal);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/");

        User user = getUser(0l, 12345l, "mockUser", new long[]{});
        Group vgr = mock(Group.class);
        when(vgr.getName()).thenReturn("VGRegion");
        when(vgr.hasPrivateLayouts()).thenReturn(true);
        when(user.getGroups()).thenReturn(Arrays.asList(vgr));
        when(portal.getUserId(req)).thenReturn(123l);
        when(userLocalService.getUser(123l)).thenReturn(user);


        action.run(req, res);

        verify(session).setAttribute(eq(WebKeys.LAST_PATH), anyString());
    }
    @Test
    public void testRun2() throws Exception {
        UserCommunityAction action = new UserCommunityAction();
        UserLocalService userLocalService = mock(UserLocalService.class);
        ReflectionTestUtils.setField(action, "userLocalService", userLocalService);
        Portal portal = mock(Portal.class);
        ReflectionTestUtils.setField(action, "portal", portal);

        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        HttpSession session = mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/");

        User user = getUser(0l, 12345l, "mockUser", new long[]{});
        Group extern = mock(Group.class);
        when(extern.getName()).thenReturn("Extern");
        when(extern.hasPrivateLayouts()).thenReturn(true);
        when(user.getGroups()).thenReturn(Arrays.asList(extern));
        when(portal.getUserId(req)).thenReturn(123l);
        when(userLocalService.getUser(123l)).thenReturn(user);


        action.run(req, res);

        verify(session).setAttribute(eq(WebKeys.LAST_PATH), anyString());
    }

    private User getUser(long companyId, long userId, String screenName, long[] currentGroups) throws Exception {
        User user = mock(User.class);
        when(user.getCompanyId()).thenReturn(companyId);
        when(user.getUserId()).thenReturn(userId);
        when(user.getScreenName()).thenReturn(screenName);
        when(user.getGroupIds()).thenReturn(currentGroups);

        return user;
    }
}
