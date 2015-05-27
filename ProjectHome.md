![https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/intro.png](https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/intro.png)


# [Community-Hook](Community_Hook.md) #
By using matching rules users can initially be associated to communities, organizations and user-groups allowing custom login landing page can be configured.

# [UserUpdate-Hook](UserUpdate_Hook.md) #
When the user login a number of attributes and role associations has to be updated.
Information are fetched mainly from the LDAP catalog, user data and organization data, but also from access gate to the system.
This information update is done as an integral part of the login process and will be left untouched on logout to allow an administrator to review if problems arise.
