package se.vgregion.userassociations.matcher;

import com.liferay.portal.model.User;

/**
 * Created by IntelliJ IDEA.
 * User: pabe, David Rosell
 * Date: 2011-06-15
 * Time: 09:20
 */
public interface Matcher {

    /**
     * The matcher signature take the Liferay User as parameter.
     *
     * @param user, Liferay user.
     */
    void process(User user);

}
