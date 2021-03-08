package nl.finalist.liferay.oidc;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.PwdGenerator;

import java.util.Calendar;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

public class Liferay62Adapter implements LiferayAdapter {

    private static final Log LOG = LogFactoryUtil.getLog(Liferay62Adapter.class);

    @Override
    public OIDCConfiguration getOIDCConfiguration(long companyId) {
        return new OpenIDConnectPortalPropsConfiguration(companyId);
    }

    @Override
    public String getPortalProperty(String propertyKey) {
        return PropsUtil.get(propertyKey);
    }

    @Override
    public String getPortalProperty(String propertyKey, String defaultString) {
        return GetterUtil.getString(PropsUtil.get(propertyKey), defaultString);
    }

    @Override
    public boolean getPortalProperty(String propertyKey, boolean defaultBoolean) {
        return GetterUtil.getBoolean(PropsUtil.get(propertyKey), defaultBoolean);
    }

    @Override
    public void trace(String s) {
        LOG.trace(s);
    }

    @Override
    public void info(String s) {
        LOG.info(s);
    }

    @Override
    public void debug(String s) {
        LOG.debug(s);
    }

    @Override
    public void warn(String s) {
        LOG.warn(s);
    }

    @Override
    public String getCurrentCompleteURL(HttpServletRequest request) {
        return PortalUtil.getCurrentCompleteURL(request);
    }

    @Override
    public boolean isUserLoggedIn(HttpServletRequest request) {
        try {
            return PortalUtil.getUser(request) != null;
        } catch (PortalException | SystemException e) {
            return false;
        }
    }

    @Override
    public long getCompanyId(HttpServletRequest request) {
        return PortalUtil.getCompanyId(request);
    }

    @Override
    public void error(String s) {
        LOG.error(s);
    }

    @Override
    public String createOrUpdateUser(long companyId, String screenName, String mobile, String nationalCode, String personalCode, String emailAddress, String firstName, String lastName) {

        try {
            User user = null;
            try {
                user = UserLocalServiceUtil.getUserByScreenName(companyId, screenName);
            } catch (NoSuchUserException nsue) {
            }

            if (user == null) {
                LOG.debug("No Liferay user found with email address " + screenName + ", will create one.");
                user = addUser(companyId, screenName, mobile, nationalCode, personalCode, emailAddress, firstName, lastName);
            } else {
                LOG.debug("User found, updating name details with info from userinfo");
                updateUser(user, firstName, lastName);
            }
            return String.valueOf(user.getUserId());

        } catch (SystemException | PortalException e) {
            throw new RuntimeException(e);
        }
    }


    // Copied from OpenSSOAutoLogin.java
    protected User addUser(
            long companyId, String screenName, String mobile, String nationalCode, String personalCode, String emailAddress, String firstName, String lastName)
            throws SystemException, PortalException {

        Locale locale = LocaleUtil.getDefault();
        long creatorUserId = 0;
        boolean autoPassword = false;
        String password1 = PwdGenerator.getPassword();
        String password2 = password1;
        boolean autoScreenName = false;
        long facebookId = 0;
        String openId = StringPool.BLANK;
        String middleName = StringPool.BLANK;
        int prefixId = 0;
        int suffixId = 0;
        boolean male = true;
        int birthdayMonth = Calendar.JANUARY;
        int birthdayDay = 1;
        int birthdayYear = 1970;
        String jobTitle = StringPool.BLANK;
        long[] groupIds = null;
        long[] organizationIds = null;
        long[] roleIds = null;
        long[] userGroupIds = null;
        boolean sendEmail = false;
        ServiceContext serviceContext = new ServiceContext();

        User user = UserLocalServiceUtil.addUser(
                creatorUserId, companyId, autoPassword, password1, password2,
                autoScreenName, screenName, emailAddress, facebookId, openId,
                locale, firstName, middleName, lastName, personalCode, nationalCode, mobile, prefixId, suffixId, male,
                birthdayMonth, birthdayDay, birthdayYear, jobTitle, groupIds,
                organizationIds, roleIds, userGroupIds, sendEmail, serviceContext);

        // No password
        user.setPasswordReset(false);
        // No reminder query at first login.
        user.setReminderQueryQuestion("x");
        user.setReminderQueryAnswer("y");
        UserLocalServiceUtil.updateUser(user);
        return user;
    }


    private void updateUser(User user, String firstName, String lastName) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        try {
            UserLocalServiceUtil.updateUser(user);
        } catch (SystemException e) {
            LOG.error("Could not update user with new name attributes", e);
        }
    }
}
