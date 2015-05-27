# User Associations Hook #


## Intro ##

When the user login a number of attributes and role associations has to be updated.

Information are fetched mainly from the LDAP catalog, user data and organization data, but also from access gate to the system. This information update is done as an integral part of the login process and will be left untouched on logout to allow an administrator to review if problems arise.


## Description ##


When a user sign in to the portal the User-association-hook intercept and retrieves the information from the LDPA catalog  and sets this new information for the user. It Also adds and removes memberships in user-groups.



![http://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/liferay-user-asociassion.png](http://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/liferay-user-asociassion.png)



### Sequence diagram ###

This sequence diagram describes what happen when a user sign in using the sign in portlet in the portal. The hook intercept the login process and retrieves information from LDAP using a service in Commons project. Then it uses another service in Common to set the values to Liferay. When this is done an if everything succeeded the user will be signed in.

![https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/UserUpdate-Hook-seq.png](https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/UserUpdate-Hook-seq.png)

### Internal access ###
The portal in this case uses user-groups to delegate permissions. The user-groups have user-group roles assigned to them and the roles have the permissions. Depending on if a user accesses the portal from the internal or external network the user shall have different permissions.

Their is a collection of groups the have ”_internal\_only” sufix on their name. This groups do not have any roles assigned to them. They are only used to tell that the user shall be added to the original group, the group named without ”_internal\_only”. If a user is accessing the portal from the internal network and have a membership in a ”_internal\_only”-group._

For example if a user is a member of the BFR\_internal\_only group and sing in from the internal network. UserUpdate-Hook will add the user to the BFR group. Now when the user is a member of BFR he have inherited the roll that gives the permission to the BFR application.

If a user then sing in from an external network all the memberships in the groups that have   a membership in corresponding ”_internal\_only” group will be removed from the original group. In our example the user will be removed from the BFR group, and no more have the permissions to the application BFR._

![http://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/Internal-only-access.png](http://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/Internal-only-access.png)