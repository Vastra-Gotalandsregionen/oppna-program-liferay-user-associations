package se.vgregion.userassociations.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 13/6-11
 * Time: 15:47
 */
public class UserLoginAction extends Action {
    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        try {
            HttpSession session = request.getSession();
            User user = UserLocalServiceUtil.getUser(PortalUtil.getUserId(request));
            Group firstUserGroup = null;

            resolveAssociations(user);

//            // First look for User's Private Pages
//            if (user.hasPrivateLayouts()) {
//                // Redirect to User's Private Pages
//                lastPath = new LastPath(request.getContextPath(), PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_USER_SERVLET_MAPPING) + user.getGroup().getFriendlyURL());
//            }

//            if (lastPath == null) {
//                // Look for Organizations
//                List<Organization> organizations = user.getOrganizations();
//                for (Organization organization : organizations) {
//                    Group group = organization.getGroup();
//                    if (firstUserGroup == null)
//                        firstUserGroup = group;
//                    if (group.hasPrivateLayouts())
//                        lastPath = new LastPath(request.getContextPath(), PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) + group.getFriendlyURL());
//                    if (lastPath != null)
//                        break;
//                }
//            }

            LastPath lastPath = (LastPath) session.getAttribute(WebKeys.LAST_PATH);

            if (lastPath == null) {
                // Look for Communities
                List<Group> groups = user.getGroups();
                for (Group group : groups) {
                    if (firstUserGroup == null)
                        firstUserGroup = group;
                    if (group.hasPrivateLayouts())
                        lastPath = new LastPath(request.getContextPath(), PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) + group.getFriendlyURL());
                    if (lastPath != null)
                        break;
                }
            }

            if (lastPath != null) {
                session.setAttribute(WebKeys.LAST_PATH, lastPath);
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }

    private void resolveAssociations(User user) {
        //repository fetches all matchers

        //for each matcher, run each matcher
        //each matcher adds appropriate groups to the user

    }
}
