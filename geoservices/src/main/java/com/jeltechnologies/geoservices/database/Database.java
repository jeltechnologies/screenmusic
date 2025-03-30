package com.jeltechnologies.geoservices.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datasources.Location;
import com.jeltechnologies.geoservices.datasources.house.GeoCoordinates;
import com.jeltechnologies.geoservices.datasources.house.GeoFile;
import com.jeltechnologies.geoservices.datasources.house.GeoHouse;

public class Database implements HouseDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private final static String JNDI_DATASOURCE_NAME = "java:/comp/env/jdbc/geoservices";

    private Connection connection;
    
    protected Database() {
	try {
	    InitialContext cxt = new InitialContext();
	    DataSource ds = (DataSource) cxt.lookup(JNDI_DATASOURCE_NAME);
	    connection = ds.getConnection();
	    connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("Connected to JNDI data source: " + JNDI_DATASOURCE_NAME);
	    }
	} catch (Exception e) {
	    throw new IllegalStateException("Cannot connect to database", e);
	}
    }

    @Override
    public void close() {
	if (connection != null) {
	    try {
		connection.close();
	    } catch (SQLException e) {
		LOGGER.error("Error while closing database connection", e);
	    }
	}
    }

    @Override
    public void commit() {
	try {
	    if (connection != null) {
		connection.commit();
	    }
	} catch (SQLException e) {
	    LOGGER.error("Cannot commit transaction", e);
	}
    }

    public void rollback() {
	try {
	    if (connection != null) {
		connection.rollback();
	    }
	} catch (SQLException e) {
	    LOGGER.error("Cannot rollback transaction", e);
	}
    }

    private PreparedStatement getStatement(String sql) throws SQLException {
	if (LOGGER.isTraceEnabled()) {
	    LOGGER.trace("getStatement sql: " + sql);
	}
	PreparedStatement statement = connection.prepareStatement(sql);
	statement.clearParameters();
	return statement;
    }

    private void close(Statement statement) {
	try {
	    if (statement != null) {
		statement.close();
	    }
	} catch (SQLException e) {
	    LOGGER.warn("Cannot commit statement", e);
	}
    }

    private void close(ResultSet resultSet) {
	try {
	    if (resultSet != null) {
		resultSet.close();
	    }
	} catch (SQLException e) {
	    LOGGER.warn("Cannot commit resultset", e);
	}
    }

    private void executeSQL(String sql) throws SQLException {
	PreparedStatement statement = null;
	try {
	    statement = getStatement(sql);
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.info(sql);
	    }
	    statement.execute();
	} finally {
	    close(statement);
	}
    }

    @Override
    public void initDatabase(boolean dropTables) throws SQLException {
	if (dropTables) {
	    dropTables();
	}
	createTables();
    }

    private void dropTables() throws SQLException {
	executeSQL("DROP TABLE IF EXISTS Coordinates;");
	executeSQL("DROP TABLE IF EXISTS Houses;");
	executeSQL("DROP TABLE IF EXISTS Files;");
    }

    private void createTables() throws SQLException {
	String createCountries = """
		CREATE TABLE IF NOT EXISTS Files
		(
		  id SERIAL PRIMARY KEY,
		  filename TEXT UNIQUE,
		  countrycode TEXT,
		  countryname TEXT
		);""";
	executeSQL(createCountries);

	String createFiles = """
		CREATE TABLE IF NOT EXISTS Houses
		(
		   id SERIAL PRIMARY KEY,
		   files_id INT,
		   latitude NUMERIC(20,16),
		   longitude NUMERIC(20,16),
		   postal_code TEXT,
		   city	TEXT,
		   street TEXT,
		   house_number	TEXT,
		   countrycode TEXT,
		   FOREIGN KEY(files_id) REFERENCES Files(id)
		);""";
	executeSQL(createFiles);

	String createCoordinateTable = """
		CREATE TABLE IF NOT EXISTS Coordinates
		(
		   latitude NUMERIC(20,16),
		   longitude NUMERIC(20,16),
		   contents_id INT,
		   PRIMARY KEY(latitude, longitude, contents_id),
		   FOREIGN KEY(contents_id) REFERENCES Houses(id)
		);""";
	executeSQL(createCoordinateTable);
    }

    @Override
    public void insertFile(String fileName, Country country) throws SQLException {
	PreparedStatement st = null;
	try {
	    String sql = "INSERT INTO files(filename, countrycode, countryname) VALUES (?,?,?)";
	    st = getStatement(sql);
	    st.setString(1, fileName);
	    st.setString(2, country.code());
	    st.setString(3, country.name());
	    st.executeUpdate();
	} finally {
	    close(st);
	}
    }

    @Override
    public GeoFile getFile(String fileName) throws SQLException {
	PreparedStatement st = null;
	ResultSet rs = null;
	try {
	    String sql = "SELECT id, countrycode, countryname FROM files WHERE filename=?";
	    st = getStatement(sql);
	    st.setString(1, fileName);
	    GeoFile file = null;
	    rs = st.executeQuery();
	    if (rs.next()) {
		int id = rs.getInt(1);
		String countryCode = rs.getString(2);
		String countryName = rs.getString(3);
		file = new GeoFile(id, fileName, new Country(countryCode, countryName));
	    }
	    return file;
	} finally {
	    close(rs);
	    close(st);
	}
    }

    @Override
    public void insertHouse(int fileId, Coordinates coordinates, String postalCode, String city, String street, String houseNumber, String countryCode)
	    throws SQLException {
	PreparedStatement st = null;
	ResultSet rs = null;
	try {

	    String sql = "INSERT INTO houses(files_id, latitude, longitude, postal_code,city,street,house_number,countrycode) VALUES (?,?,?,?,?,?,?,?) RETURNING id";
	    st = getStatement(sql);

//	files_id, 
	    st.setInt(1, fileId);
//	latitude, 
	    st.setDouble(2, coordinates.latitude());
//	longitude, 
	    st.setDouble(3, coordinates.longitude());
//	postal_code,
	    st.setString(4, postalCode);
//	city,
	    st.setString(5, city);
//	street,
	    st.setString(6, street);
//	house_number,
	    st.setString(7, houseNumber);
//	country
	    st.setString(8, countryCode);

	    rs = st.executeQuery();
	    rs.next();
	} finally {
	    close(rs);
	    close(st);
	}
    }

    @Override
    public List<Location> getCoordinates(Country country) throws SQLException, InterruptedException {
	PreparedStatement st = null;
	ResultSet rs = null;
	try {
	    String sql = "SELECT id, latitude, longitude FROM houses WHERE files_id = (SELECT id FROM files WHERE countrycode = ?)";
	    st = getStatement(sql);
	    st.setString(1, country.code());
	    List<Location> result = new ArrayList<Location>();
	    rs = st.executeQuery();
	    while (rs.next()) {
		if (Thread.interrupted()) {
		    throw new InterruptedException();
		}
		GeoCoordinates geoCoordinates = new GeoCoordinates(rs.getInt(1), rs.getDouble(2), rs.getDouble(3));
		result.add(geoCoordinates);
	    }
	    if (LOGGER.isTraceEnabled()) {
		LOGGER.trace("getCoordinates('" + country.code() + "') => " + result.size());
	    }
	    return result;
	} finally {
	    close(rs);
	    close(st);
	}

    }

    @Override
    public GeoHouse getGeoHouse(int id) throws SQLException {
	PreparedStatement st = null;
	ResultSet rs = null;
	try {
	    String sql = "SELECT id, files_id, latitude, longitude, postal_code, city, street, house_number, countrycode FROM houses WHERE id=?";
	    st = getStatement(sql);
	    st.setInt(1, id);
	    GeoHouse result = null;
	    rs = st.executeQuery();
	    if (rs.next()) {
		Coordinates c = new Coordinates(rs.getDouble(3), rs.getDouble(4));
		result = new GeoHouse(rs.getInt(1), rs.getInt(2), c, rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9));
	    }
	    return result;
	} finally {
	    close(rs);
	    close(st);
	}
    }

}
