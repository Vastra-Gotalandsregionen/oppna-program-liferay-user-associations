package se.vgregion.userupdate.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.vgregion.portal.core.domain.liferayuser.LiferayUser;
import se.vgregion.portal.core.domain.liferayuser.LiferayUserRepository;
import se.vgregion.portal.liferay.group.UserGroupUpdateService;
import se.vgregion.portal.myprofile.mysystems.domain.system.ItSystem;
import se.vgregion.portal.myprofile.mysystems.logic.SystemService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.List;

/**
 * This action do that and that, if it has something special it is.
 * 
 * @author <a href="mailto:david.rosell@redpill-linpro.com">David Rosell</a>
 */
public class UserDefaultMySystemAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDefaultMySystemAction.class);

    private UserGroupUpdateService userGroupUpdateService;

    private SystemService systemService;

    private LiferayUserRepository userRepository;

    @Override
    public void run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ActionException {
        init();

        Long userId = getCurrentUserId(httpServletRequest);
        LiferayUser user;
        try {
            user = userRepository.find(userId);
        } catch (Exception e) {
            LOGGER.warn("User " + userId + " is not an Liferay user - update cannot be performed.", e);
            return;
        }

        List<ItSystem> myItSystemList = systemService.getAllVisibleItSystemsByUserId(user.getId());

        for (ItSystem itSystem : myItSystemList) {
            systemService.addUserMemberToGroups(user, itSystem);
        }

        List<ItSystem> allItSystemList = systemService.getAllSystems();
        allItSystemList.removeAll(myItSystemList);
        for (ItSystem itSystem : allItSystemList) {
            systemService.removeUserMemberFromGroups(user, itSystem);
        }
    }

    private Long getCurrentUserId(HttpServletRequest request) throws ActionException {
        final Principal userPrincipal = request.getUserPrincipal();
        try {
            String userIdStr = userPrincipal.getName();
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            LOGGER.warn("No user session exists.");
            throw new ActionException("No user session exists.", e);
        }
    }

    private void init() {
        if (userGroupUpdateService == null) {
            userGroupUpdateService = (UserGroupUpdateService) getApplicationContext().getBean(
                    "userGroupUpdateService");
        }

        if (systemService == null) {
            systemService = (SystemService) getApplicationContext().getBean("systemService");
        }

        if (userRepository == null) {
            userRepository = (LiferayUserRepository) getApplicationContext().getBean("userRepository");
        }
    }

    private ApplicationContext ctx = null;

    private ApplicationContext getApplicationContext() {
        if (ctx == null) {
            ctx = new ClassPathXmlApplicationContext("applicationContextDefaultSystems.xml");
        }
        return ctx;
    }

}
