package se.vgregion.userassociations.matcher;

import com.liferay.portal.model.User;

/**
 * Created by IntelliJ IDEA.
 * User: pabe, David Rosell
 * Date: 2011-06-15
 * Time: 09:20
 */
public interface Matcher {

    void process(User user);

}
