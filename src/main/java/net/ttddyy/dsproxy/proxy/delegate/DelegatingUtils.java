package net.ttddyy.dsproxy.proxy.delegate;

import net.ttddyy.dsproxy.DataSourceProxyException;
import net.ttddyy.dsproxy.proxy.ProxyJdbcObject;

import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * Utility methods for delegating classes.
 *
 * @author Tadaya Tsuyukubo
 * @since 1.5
 */
public class DelegatingUtils {

    public static final Method GET_TARGET_METHOD;

    static {
        try {
            GET_TARGET_METHOD = ProxyJdbcObject.class.getMethod("getTarget");
        } catch (NoSuchMethodException e) {
            throw new DataSourceProxyException("Cannot find ProxyJdbcObject#getTarget()");
        }
    }


    public static Method getMethodIfAvailable(Class<?> clazz, String name, Class... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    public interface InvocationCallback {
        Object invoke() throws Throwable;
    }

    public static Object invoke(Method method, InvocationCallback callback) throws SQLException {

        if (method == null) {
            throw new DataSourceProxyException("Could not find the method");
        }

        try {
            return callback.invoke();
        } catch (Throwable throwable) {
            if (throwable instanceof SQLException) {
                throw (SQLException) throwable;
            }
            throw new DataSourceProxyException("Failed to invoke method:" + method, throwable);
        }
    }

}
