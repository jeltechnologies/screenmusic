package com.jeltechnologies.geoservices.database;

import java.sql.SQLException;
import java.util.List;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datasources.Location;
import com.jeltechnologies.geoservices.datasources.house.GeoFile;
import com.jeltechnologies.geoservices.datasources.house.GeoHouse;

public interface HouseDataSource {

    void close();

    void commit();
    
    void rollback();

    void initDatabase(boolean dropTables) throws SQLException;

    void insertFile(String fileName, Country country) throws SQLException;

    GeoFile getFile(String fileName) throws SQLException;

    void insertHouse(int fileId, Coordinates coordinates, String postalCode, String city, String street, String houseNumber, String countryCode)
	    throws SQLException;

    List<Location> getCoordinates(Country country) throws SQLException, InterruptedException;

    GeoHouse getGeoHouse(int id) throws SQLException;

}