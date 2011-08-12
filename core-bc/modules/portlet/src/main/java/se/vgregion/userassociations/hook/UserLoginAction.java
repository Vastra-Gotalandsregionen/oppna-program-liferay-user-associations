package se.vgregion.userassociations.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import com.liferay.portal.kernel.exception.PortalException;
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

            resolveAssociations(user);

            LastPath lastPath = (LastPath) session.getAttribute(WebKeys.LAST_PATH);

            if (lastPath == null || lastPath.getPath() == null || lastPath.getPath().equals("/")) {
                // Look for Communities - No path form initial call
                lastPath = computeLastPath(request.getContextPath(), user);
            }

            if (lastPath != null) {
                session.setAttribute(WebKeys.LAST_PATH, lastPath);
            }
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }

    private LastPath computeLastPath(String contextPath, User user) throws PortalException, SystemException {
        List<Group> groups = user.getGroups();
        Group vgregion = lookupVgregion(groups);
        if (vgregion != null) {
            if (vgregion.hasPrivateLayouts()) {
                return new LastPath(contextPath,
                        PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) +
                                vgregion.getFriendlyURL());
            }
        } else {
            for (Group group : groups) {
                if (group.hasPrivateLayouts()) {
                    return new LastPath(contextPath,
                            PropsUtil.get(PropsKeys.LAYOUT_FRIENDLY_URL_PRIVATE_GROUP_SERVLET_MAPPING) +
                                    group.getFriendlyURL());
                }
            }
        }
        return null;
    }

    private Group lookupVgregion(List<Group> groups) {
        Group vgregion = null;
        for (Group group : groups) {
            if (group.getName().equalsIgnoreCase("vgregion")) {
                vgregion = group;
                break;
            }
        }
        return vgregion;
    }

    private void resolveAssociations(User user) {
        for (Matcher matcher : matcherList) {
            matcher.process(user);
        }
    }

}
