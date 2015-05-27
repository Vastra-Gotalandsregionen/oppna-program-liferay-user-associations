# Introduction #

By using matching rules, users can be associated to communities, organizations and user-groups, allowing a custom login landing page to be configured.

![https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/community-hook1.png](https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/community-hook1.png)


# Details #

  1. The Community hooks start processing when a user log into the portal.
  1. The hook works through all of the matchers one at a time. A matcher use a regexp as a filter to determinate if the matcher is valid for the user. On a match, the hook analyses if the user is a member of all the groups the matcher have in it's list of groups and if not is a member, the user will be added to that group.
  1. When all matchers has been processed, the hook determine the correct landing page by analyzing the groups the user is member of.
  1. And ends with setting the landing pages so the user later can be redirected there.

![https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/Community-hook.png](https://oppna-program-liferay-user-associations.googlecode.com/svn/wiki/images/Community-hook.png)