package com.jeltechnologies.screenmusic.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.screenmusic.config.Admin;

public abstract class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private static final String DATASOURCE_NAME = "java:/comp/env/jdbc/screenmusic";
    private Connection connection;
    
    private Map<String, PreparedStatement> usedPreparedStatements = new HashMap<String, PreparedStatement>();
    
    public Database() {
	try {
	    InitialContext cxt = new InitialContext();
	    DataSource ds = (DataSource) cxt.lookup(DATASOURCE_NAME);
	    connection = ds.getConnection();
	    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Connected to JNDI data source: " + DATASOURCE_NAME);
	    }
	} catch (Exception e) {
	    throw new IllegalStateException("Cannot connect to database", e);
	}
    }
    
    
    protected void close(ResultSet rs) {
	if (rs != null) {
	    try {
		rs.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close ResultSet");
	    }
	}
    }
    
    protected void close(ResultSetIterator rs) {
	if (rs != null) {
	    try {
		rs.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close ResultSetIterator");
	    }
	}
    }

    public void commit() throws SQLException {
	connection.commit();
    }

    public void rollback() throws SQLException {
	connection.rollback();
    }

    public boolean isClosed() throws SQLException {
	return connection.isClosed();
    }

    public void prepareDatabase(Admin admin) throws SQLException {
	new DBCreateTables(connection).prepareDatabase(admin);
    }

    protected PreparedStatement getStatement(String sql) throws SQLException {
	PreparedStatement s = this.usedPreparedStatements.get(sql);
	if (s == null) {
	    s = connection.prepareStatement(sql);
	    this.usedPreparedStatements.put(sql, s);
	}
	return s;
    }

    public void close() {
	try {
	    for (String sql : this.usedPreparedStatements.keySet()) {
		try {
		    PreparedStatement p = this.usedPreparedStatements.get(sql);
		    p.close();
		}
		catch (SQLException e) {
		    LOGGER.error("Cannot close PrepardStatement " + sql);
		}
	    }
	    commit();
	} catch (SQLException e) {
	    LOGGER.error("Cannot commit to database", e);
	}
	if (connection != null) {
	    try {
		connection.close();
	    } catch (SQLException e) {
		LOGGER.error("Cannot close connection");
	    }
	}
    }

}
