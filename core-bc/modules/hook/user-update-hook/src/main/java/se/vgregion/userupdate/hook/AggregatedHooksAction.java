package se.vgregion.userupdate.hook;

import com.liferay.portal.kernel.events.Action;
import com.liferay.portal.kernel.events.ActionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Patrik Bergström
 */
public class AggregatedHooksAction extends Action {

    private UserDefaultMySystemAction userDefaultMySystemAction = new UserDefaultMySystemAction();
    private UserUpdateAction userUpdateAction = new UserUpdateAction();

    @Override
    public void run(HttpServletRequest request, HttpServletResponse httpServletResponse) throws ActionException {
        userDefaultMySystemAction.run(request, httpServletResponse);
        userUpdateAction.run(request, httpServletResponse);
    }
}
