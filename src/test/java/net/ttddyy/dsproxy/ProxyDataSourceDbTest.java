package net.ttddyy.dsproxy;

import net.ttddyy.dsproxy.listener.CallCheckMethodExecutionListener;
import net.ttddyy.dsproxy.listener.MethodExecutionContext;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * TODO: clean up & rewrite
 *
 * @author Tadaya Tsuyukubo
 */
@DatabaseTest
public class ProxyDataSourceDbTest {

    private ProxyDataSource proxyDataSource;
    private TestListener listener;
    private CallCheckMethodExecutionListener methodListener;

    private DataSource jdbcDataSource;
    private DbResourceCleaner cleaner;

    public ProxyDataSourceDbTest(DataSource jdbcDataSource, DbResourceCleaner cleaner) {
        this.jdbcDataSource = jdbcDataSource;
        this.cleaner = cleaner;
    }

    @BeforeEach
    public void setup() throws Exception {
        listener = new TestListener();
        methodListener = new CallCheckMethodExecutionListener();

        ProxyConfig proxyConfig = ProxyConfig.Builder.create()
                .listener(this.listener)
                .listener(this.methodListener)
                .build();

        proxyDataSource = new ProxyDataSource();
        proxyDataSource.setDataSource(this.jdbcDataSource);
        proxyDataSource.setProxyConfig(proxyConfig);
    }

    @Test
    public void testStatementWithExecuteUpdateQuery() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.executeUpdate("create table aa ( a varchar(5) primary key );");

        assertThat(listener.getBeforeCount()).isEqualTo(1);
        assertThat(listener.getAfterCount()).isEqualTo(1);
    }

    @Test
    public void testStatementWithExecuteQuery() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.executeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES;");  // hsqldb system table

        assertThat(listener.getBeforeCount()).isEqualTo(1);
        assertThat(listener.getAfterCount()).isEqualTo(1);
    }

    @Test
    public void testUseStatement() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.executeQuery("select * from emp;");

        assertThat(listener.getBeforeCount()).isEqualTo(1);
        assertThat(listener.getAfterCount()).isEqualTo(1);
    }

    @Test
    public void testUsePreparedStatement() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        PreparedStatement st = conn.prepareStatement("select * from emp");
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.executeQuery();

        assertThat(listener.getBeforeCount()).isEqualTo(1);
        assertThat(listener.getAfterCount()).isEqualTo(1);
    }

    @Test
    public void testUsePrepareCall() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        CallableStatement st = conn.prepareCall("select * from emp");
        this.cleaner.add(conn);
        this.cleaner.add(st);
        st.execute();
    }

    @Test
    public void statementGetConnection() throws Exception {
        Connection proxyConn = proxyDataSource.getConnection();
        Statement st = proxyConn.createStatement();
        Connection conn = st.getConnection();
        this.cleaner.add(proxyConn);
        this.cleaner.add(conn);
        this.cleaner.add(st);

        assertThat(conn).isSameAs(proxyConn);
    }

    @Test
    public void preparedGetConnection() throws Exception {
        Connection proxyConn = proxyDataSource.getConnection();
        PreparedStatement ps = proxyConn.prepareStatement("select * from emp");
        Connection conn = ps.getConnection();
        this.cleaner.add(proxyConn);
        this.cleaner.add(conn);
        this.cleaner.add(ps);

        assertThat(conn).isSameAs(proxyConn);
    }

    @Test
    public void callableGetConnection() throws Exception {
        Connection proxyConn = proxyDataSource.getConnection();
        CallableStatement cs = proxyConn.prepareCall("select * from emp");
        Connection conn = cs.getConnection();
        this.cleaner.add(proxyConn);
        this.cleaner.add(conn);
        this.cleaner.add(cs);

        assertThat(conn).isSameAs(proxyConn);
    }

    @Test
    public void methodExecutionListener() throws Throwable {
        assertFalse(this.methodListener.isBeforeMethodCalled());
        assertFalse(this.methodListener.isAfterMethodCalled());

        Connection connection = proxyDataSource.getConnection();

        assertTrue(this.methodListener.isBeforeMethodCalled(), "methodListener should be called for getConnection");
        assertTrue(this.methodListener.isAfterMethodCalled(), "methodListener should be called for getConnection");

        MethodExecutionContext context = this.methodListener.getAfterMethodContext();
        assertThat(context.getTarget()).isSameAs(proxyDataSource);
        assertThat(context.getResult()).isSameAs(connection);
        assertThat(context.getMethod().getDeclaringClass()).isSameAs(DataSource.class);
        assertThat(context.getMethod().getName()).isEqualTo("getConnection");
        assertThat(context.getConnectionInfo()).isNotNull();

        // adding connection set in cleaner calls hashCode() method on connection, thus call it after verification
        this.cleaner.add(connection);

        this.methodListener.reset();

        String username = DbTestUtils.getUsername();
        String password = DbTestUtils.getPassword();
        connection = proxyDataSource.getConnection(username, password);

        assertTrue(this.methodListener.isBeforeMethodCalled(), "methodListener should be called for getConnection");
        assertTrue(this.methodListener.isAfterMethodCalled(), "methodListener should be called for getConnection");

        this.cleaner.add(connection);
        this.methodListener.reset();

        // for now, only getConnection is supported for method execution listener

        proxyDataSource.close();
        assertFalse(this.methodListener.isBeforeMethodCalled(), "methodListener should NOT be called for close");
        assertFalse(this.methodListener.isAfterMethodCalled(), "methodListener should NOT be called for close");

        this.methodListener.reset();

        proxyDataSource.getLoginTimeout();
        assertFalse(this.methodListener.isBeforeMethodCalled(), "methodListener should NOT be called for getLoginTimeout");
        assertFalse(this.methodListener.isAfterMethodCalled(), "methodListener should NOT be called for getLoginTimeout");

        this.methodListener.reset();

        proxyDataSource.setLoginTimeout(100);
        assertFalse(this.methodListener.isBeforeMethodCalled(), "methodListener should NOT be called for setLoginTimeout");
        assertFalse(this.methodListener.isAfterMethodCalled(), "methodListener should NOT be called for setLoginTimeout");

        this.methodListener.reset();

        PrintWriter writer = proxyDataSource.getLogWriter();
        assertFalse(this.methodListener.isBeforeMethodCalled(), "methodListener should NOT be called for getLogWriter");
        assertFalse(this.methodListener.isAfterMethodCalled(), "methodListener should NOT be called for getLogWriter");

        proxyDataSource.setLogWriter(writer);
        assertFalse(this.methodListener.isBeforeMethodCalled(), "methodListener should NOT be called for setLogWriter");
        assertFalse(this.methodListener.isAfterMethodCalled(), "methodListener should NOT be called for setLogWriter");
    }

    @Test
    public void connectionClose() throws Exception {
        ConnectionIdManager connIdManager = proxyDataSource.getConnectionIdManager();
        Connection conn = proxyDataSource.getConnection();
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        ConnectionInfo connInfo = this.methodListener.getBeforeMethodContext().getConnectionInfo();
        assertThat(connInfo.isClosed()).isFalse();
        assertThat(connIdManager.getOpenConnectionIds()).containsOnly(connInfo.getConnectionId());

        st.close();
        assertThat(connInfo.isClosed()).isFalse();
        assertThat(connIdManager.getOpenConnectionIds()).containsOnly(connInfo.getConnectionId());

        conn.close();
        assertThat(connInfo.isClosed()).isTrue();
        assertThat(connIdManager.getOpenConnectionIds()).isEmpty();
    }

    @Test
    public void commitAndRollbackCount() throws Exception {
        Connection conn = proxyDataSource.getConnection();
        conn.setAutoCommit(false);
        Statement st = conn.createStatement();
        this.cleaner.add(conn);
        this.cleaner.add(st);

        ConnectionInfo connInfo = this.methodListener.getBeforeMethodContext().getConnectionInfo();

        st.close();
        conn.commit();
        assertThat(connInfo.getCommitCount()).isEqualTo(1);
        assertThat(connInfo.getRollbackCount()).isEqualTo(0);

        conn.commit();
        assertThat(connInfo.getCommitCount()).isEqualTo(2);
        assertThat(connInfo.getRollbackCount()).isEqualTo(0);

        conn.rollback();
        assertThat(connInfo.getCommitCount()).isEqualTo(2);
        assertThat(connInfo.getRollbackCount()).isEqualTo(1);

        conn.rollback();
        assertThat(connInfo.getCommitCount()).isEqualTo(2);
        assertThat(connInfo.getRollbackCount()).isEqualTo(2);
    }

}
