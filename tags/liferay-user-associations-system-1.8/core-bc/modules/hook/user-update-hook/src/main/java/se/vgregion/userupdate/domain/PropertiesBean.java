/**
 * 
 */
package se.vgregion.userupdate.domain;

/**
 * Bean which holds properties.
 *
 * @author Simon Göransson - simon.goransson@monator.com - vgrid: simgo3
 */
public class PropertiesBean {

    private String[] ipForExternalAccess;
    private String externalUserRedirectUrl;

    public String[] getIpsForExternalAccess() {
        return ipForExternalAccess.clone();
    }

    public void setIpForExternalAccess(String ipForExternalAccess) {
        this.ipForExternalAccess = ipForExternalAccess.replaceAll(" ", "").split(",");
    }

    public String getExternalUserRedirectUrl() {
        return externalUserRedirectUrl;
    }

    public void setExternalUserRedirectUrl(String externalUserRedirectUrl) {
        this.externalUserRedirectUrl = externalUserRedirectUrl;
    }

}
