package net.ttddyy.dsproxy.proxy;

import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.transform.QueryTransformer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

/**
 * Proxy InvocationHandler for {@link java.sql.PreparedStatement}.
 *
 * @author Tadaya Tsuyukubo
 */
public class PreparedStatementInvocationHandler implements InvocationHandler {

    private PreparedStatementProxyLogic delegate;

    public PreparedStatementInvocationHandler(PreparedStatement ps, String query) {
        delegate = new PreparedStatementProxyLogic(ps, query, new InterceptorHolder(QueryExecutionListener.DEFAULT, QueryTransformer.DEFAULT), "", JdbcProxyFactory.DEFAULT);
    }

    @Deprecated
    public PreparedStatementInvocationHandler(PreparedStatement ps, String query, QueryExecutionListener listener) {
        delegate = new PreparedStatementProxyLogic(ps, query, new InterceptorHolder(listener, QueryTransformer.DEFAULT), "", JdbcProxyFactory.DEFAULT);
    }

    @Deprecated
    public PreparedStatementInvocationHandler(
            PreparedStatement ps, String query, QueryExecutionListener listener, String dataSourceName, JdbcProxyFactory jdbcProxyFactory) {
        delegate = new PreparedStatementProxyLogic(ps, query, new InterceptorHolder(listener, QueryTransformer.DEFAULT), dataSourceName, jdbcProxyFactory);
    }

    public PreparedStatementInvocationHandler(
            PreparedStatement ps, String query, InterceptorHolder interceptorHolder, String dataSourceName, JdbcProxyFactory jdbcProxyFactory) {
        delegate = new PreparedStatementProxyLogic(ps, query, interceptorHolder, dataSourceName, jdbcProxyFactory);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return delegate.invoke(proxy, method, args);
    }

}
