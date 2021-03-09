package nl.finalist.liferay.oidc;

import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @see LibAutoLogin
 */
public class OpenIDConnectAutoLogin implements AutoLogin {

    private LibAutoLogin libAutologin;
//
//    public OpenIDConnectAutoLogin() {
//
//        super();
//        System.out.println("HHERE 222222");
//
//        libAutologin = new LibAutoLogin(new Liferay62Adapter());
//    }

    @Override
    public String[] login(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws AutoLoginException {
        libAutologin = new LibAutoLogin(new Liferay62Adapter());

        return libAutologin.doLogin(httpServletRequest, httpServletResponse);
    }
}
