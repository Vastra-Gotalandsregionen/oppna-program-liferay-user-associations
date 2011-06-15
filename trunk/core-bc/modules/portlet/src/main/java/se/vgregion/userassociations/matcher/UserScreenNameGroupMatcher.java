package se.vgregion.userassociations.matcher;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalService;

/**
 * User: pabe
 * Date: 2011-06-15
 * Time: 09:21
 */
public class UserScreenNameGroupMatcher implements Matcher {

    private String valueRegExp;
    private long[] groupIds;


    private UserLocalService userLocalService;

    public UserScreenNameGroupMatcher(String valueRegExp, long[] groupIds) {
        this.valueRegExp = valueRegExp;
        this.groupIds = groupIds;
    }

    public void process(User user) {
        if (user.getScreenName().matches(valueRegExp)) {
            for (long groupId : groupIds) {
                try {
                    userLocalService.addGroupUsers(groupId, new long[]{user.getUserId()});
                } catch (PortalException e) {
                    e.printStackTrace();
                } catch (SystemException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
