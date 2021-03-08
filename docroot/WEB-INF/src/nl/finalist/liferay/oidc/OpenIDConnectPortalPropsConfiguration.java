package nl.finalist.liferay.oidc;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsUtil;

/**
 * Implementation of the OIDC configuration that uses Liferay's Portal properties.
 * As this implementation uses PrefsProps instead of plain Props, it is 'Virtual Instance-safe',
 * i.e. each virtual instance can have its own settings in portal-{companyId}.properties
 */
public class OpenIDConnectPortalPropsConfiguration implements OIDCConfiguration {

    private final long companyId;
    private final Liferay62Adapter liferay62Adapter;

    public OpenIDConnectPortalPropsConfiguration(long companyId) {
        this.companyId = companyId;
        this.liferay62Adapter = new Liferay62Adapter();
    }

    @Override
    public boolean isEnabled() {
        return liferay62Adapter.getPortalProperty(LibFilter.PROPKEY_ENABLE_OPEN_IDCONNECT, false);
    }

    @Override
    public String authorizationLocation() {
        return liferay62Adapter.getPortalProperty("openidconnect.authorization-location");
    }

    @Override
    public String tokenLocation() {
        return liferay62Adapter.getPortalProperty("openidconnect.token-location");
    }

    @Override
    public String profileUri() {
        return liferay62Adapter.getPortalProperty("openidconnect.profile-uri");
    }

    @Override
    public String ssoLogoutUri() {
        return liferay62Adapter.getPortalProperty("openidconnect.sso-logout-uri","");
    }

    @Override
    public String ssoLogoutParam() {
        return liferay62Adapter.getPortalProperty("openidconnect.sso-logout-param","");
    }

    @Override
    public String ssoLogoutValue() {
        return liferay62Adapter.getPortalProperty("openidconnect.sso-logout-value","");
    }

    @Override
    public String issuer() {
        return liferay62Adapter.getPortalProperty("openidconnect.issuer");
    }

    @Override
    public String clientId() {
        return liferay62Adapter.getPortalProperty("openidconnect.client-id");
    }

    @Override
    public String secret() {
        return liferay62Adapter.getPortalProperty("openidconnect.secret");
    }

    @Override
    public String scope() {
        return liferay62Adapter.getPortalProperty("openidconnect.scope");
    }

    @Override
    public String providerType() {
        return liferay62Adapter.getPortalProperty("openidconnect.provider", "generic");
    }
}
