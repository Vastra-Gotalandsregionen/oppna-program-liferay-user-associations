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
package se.vgregion.userassociations.matcher;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.GroupLocalService;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.service.UserLocalService;
import com.liferay.portal.service.UserLocalServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserScreenNameGroupMatcher implements Matcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserScreenNameGroupMatcher.class);

    private long companyId;
    private String valueRegExp;
    private List<Long> groupIds = new ArrayList<Long>();
    private List<String> groupNames;

    private UserLocalService userLocalService;
    private GroupLocalService groupLocalService;

    /**
     * Match group association (Community/Organization) from user screenname.
     *
     * @param companyId
     *              to separate portal-instance.
     * @param valueRegExp
     *              opt in regular expression.
     * @param groupNames
     *              the groups that should be associated.
     */
    public UserScreenNameGroupMatcher(long companyId, String valueRegExp, List<String> groupNames) {
        this.companyId = companyId;
        this.valueRegExp = valueRegExp;
        this.groupNames = groupNames;
    }

    /**
     * The matcher signature take the Liferay User as parameter.
     *
     * @param user
     *              Liferay user.
     */
    public void process(User user) {
        if (companyId != user.getCompanyId()) {
            return;
        }

        if (user.getScreenName().matches(valueRegExp)) {
            LOGGER.debug("Checking [" + user.getScreenName() + "] in [" + groupIds + "]");

            long[] memberIn = null;
            try {
                memberIn = user.getGroupIds();

                for (long groupId : groupIds) {
                    if (alreadyMember(groupId, memberIn)) {
                        continue;
                    }

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

    /**
     * Initialization method, used by Spring.
     *
     * This cannot be run in the constructor since the GroupLocalService has to have time to get initialized.
     */
    public void initGroupIds() {
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
        for (long member : memberIn) {
            if (member == groupId) {
                return true;
            }
        }
        return false;
    }

    /*
     * This cannot be autowired because, when UserLocalServiceUtil is called, the UserLocalService
     * instance hasn't been created yet.
     *
     * @return the UserLocalService.
     */
    private UserLocalService getUserLocalService() {
        if (userLocalService == null) {
            userLocalService = UserLocalServiceUtil.getService();
        }
        return userLocalService;
    }

    /*
     * This cannot be autowired because, when GroupLocalServiceUtil is called, the GroupLocalService
     * instance hasn't been created yet.
     *
     * @return the UserLocalService.
     */
    private GroupLocalService getGroupLocalService() {
        if (groupLocalService == null) {
            groupLocalService = GroupLocalServiceUtil.getService();
        }
        return groupLocalService;
    }
}
