package se.vgregion.userassociations.matcher;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class UserScreenNameGroupMatcher implements Matcher {

    private long companyId;
    private String valueRegExp;
    private List<Long> groupIds = new ArrayList<Long>();
    private List<String> groupNames;

    private UserLocalService userLocalService;
    private GroupLocalService groupLocalService;

    public UserScreenNameGroupMatcher(long companyId, String valueRegExp, List<String> groupNames) {
        this.companyId = companyId;
        this.valueRegExp = valueRegExp;
        this.groupNames = groupNames;
    }

    public void process(User user) {
        if (companyId != user.getCompanyId()) return;

        if (user.getScreenName().matches(valueRegExp)) {
            long[] memberIn = null;
            try {
                memberIn = user.getGroupIds();

                for (long groupId : groupIds) {
                    if (alreadyMember(groupId, memberIn)) continue;

                    try {
                        getUserLocalService().addGroupUsers(groupId, new long[]{user.getUserId()});
                    } catch (PortalException e) {
                        e.printStackTrace();
                    } catch (SystemException e) {
                        e.printStackTrace();
                    }
                }
            } catch (PortalException e) {
                e.printStackTrace();
            } catch (SystemException e) {
                e.printStackTrace();
            }
        }
    }

    private void initGroupIds() {
        for (String groupName : groupNames) {
            try {
                groupIds.add(getGroupLocalService().getGroup(companyId, groupName).getGroupId());
            } catch (Exception e) {
                // Group not available in this context
                e.printStackTrace();
            }
        }
    }

    private boolean alreadyMember(long groupId, long[] memberIn) {
        for (long member: memberIn) {
            if (member == groupId) return true;
        }
        return false;
    }

    /*
     * This cannot be autowired because then UserLocalServiceUtil would be called before the UserLocalService
     * instance has been created.
     *
     * @return
     */
    private UserLocalService getUserLocalService() {
        if (userLocalService == null) {
            userLocalService = UserLocalServiceUtil.getService();
        }
        return userLocalService;
    }

    private GroupLocalService getGroupLocalService() {
        if (groupLocalService == null) {
            groupLocalService = GroupLocalServiceUtil.getService();
        }
        return groupLocalService;
    }
}
