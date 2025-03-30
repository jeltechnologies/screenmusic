package com.jeltechnologies.geoservices.database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datasources.Location;
import com.jeltechnologies.geoservices.datasources.house.GeoCoordinates;
import com.jeltechnologies.geoservices.datasources.house.GeoFile;
import com.jeltechnologies.geoservices.datasources.house.GeoHouse;

public class InMemoryHouseDatasource implements HouseDataSource {

    private List<GeoFile> geoFiles;

    private List<GeoHouse> geoHouses;

    private Map<String, List<Location>> geoCoordinates;
    
    @Override
    public void initDatabase(boolean dropTables) throws SQLException {
	geoFiles = new ArrayList<GeoFile>();
	geoHouses = new ArrayList<GeoHouse>();
	geoCoordinates = new HashMap<String, List<Location>>();
    }

    @Override
    public void insertFile(String fileName, Country country) throws SQLException {
	synchronized (geoFiles) {
	    int index = geoFiles.size();
	    geoFiles.add(new GeoFile(index, fileName, country));
	}
    }

    @Override
    public GeoFile getFile(String fileName) throws SQLException {
	synchronized (geoFiles) {
	    GeoFile found = null;
	    for (int i = 0; i < geoFiles.size() && found == null; i++) {
		GeoFile current = geoFiles.get(i);
		if (current.filename().equals(fileName)) {
		    found = current;
		}
	    }
	    return found;
	}
    }

    @Override
    public synchronized void insertHouse(int fileId, Coordinates coordinates, String postalCode, String city, String street, String houseNumber,
	    String countryCode)
	    throws SQLException {
	GeoFile geoFile = null;
	geoFile = geoFiles.get(fileId);
	if (geoFile == null) {
	    throw new SQLException("No file with fileId " + fileId);
	}
	int index = geoHouses.size();
	GeoHouse geoHouse = new GeoHouse(index, fileId, coordinates, postalCode, city, street, houseNumber, countryCode);
	geoHouses.add(geoHouse);

	List<Location> geoLocations = this.geoCoordinates.get(countryCode);
	if (geoLocations == null) {
	    geoLocations = new ArrayList<Location>();
	    this.geoCoordinates.put(countryCode, geoLocations);
	}
	geoLocations.add(new GeoCoordinates(index, coordinates.latitude(), coordinates.longitude()));
    }

    @Override
    public synchronized List<Location> getCoordinates(Country country) throws SQLException, InterruptedException {
	return this.geoCoordinates.get(country.code());
    }

    @Override
    public synchronized GeoHouse getGeoHouse(int id) throws SQLException {
	return this.geoHouses.get(id);
    }

    @Override
    public void close() {
    }

    @Override
    public void commit() {
    }

    @Override
    public void rollback() {
    }

}
