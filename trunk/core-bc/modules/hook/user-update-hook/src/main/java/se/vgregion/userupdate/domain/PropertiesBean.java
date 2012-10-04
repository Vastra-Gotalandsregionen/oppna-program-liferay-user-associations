/**
 * 
 */
package se.vgregion.userupdate.domain;

/**
 * @author Simon Göransson - simon.goransson@monator.com - vgrid: simgo3
 * 
 */
public class PropertiesBean {

    private String[] ipForExternalAccess;
    private String externalUserRedirectUrl;

    public String[] getIpsForExternalAccess() {
        return ipForExternalAccess;
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
