package se.vgregion.userassociations.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: 8/9-11
 * Time: 16:25
 */
public class UserLogoutAction extends Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserLogoutAction.class);

    @Override
    public void run(HttpServletRequest request, HttpServletResponse response) throws ActionException {
        LOGGER.info("user logout action");
    }
}
