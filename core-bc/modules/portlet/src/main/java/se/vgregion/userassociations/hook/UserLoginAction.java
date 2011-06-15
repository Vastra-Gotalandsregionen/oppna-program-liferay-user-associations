package se.vgregion.userassociations.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.struts.LastPath;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Group;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.vgregion.userassociations.matcher.Matcher;

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

    private List<Matcher> matcherList;

    public UserLoginAction() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

        matcherList = (List<Matcher>) ctx.getBean("matcherList");
    }

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

            if (noLanding(user)) {
                //lastPath = new LastPath(request.getContextPath(), "/web/guest/home");
            } else {
                if (lastPath.getPath() == null || lastPath.getPath().equals("/")) {
                    // Look for Communities
                    List<Group> groups = user.getGroups();
                    for (Group group : groups) {
                        if (group.hasPrivateLayouts())
                            lastPath = new LastPath(request.getContextPath(), PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) + group.getFriendlyURL());
                        if (lastPath != null)
                            break;
                    }
                }
            }

            if (lastPath != null) {
                session.setAttribute(WebKeys.LAST_PATH, lastPath);
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }

    private boolean noLanding(User user) {
        try {
            for (Role role : user.getRoles()) {
                if (role.getName().equals("Administrator")) return true;
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }

        return user.isDefaultUser() || !user.isActive() || user.isLockout();
    }

    private void resolveAssociations(User user) {
        for (Matcher matcher : matcherList) {
            matcher.process(user);
        }
    }

}
