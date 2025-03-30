package com.jeltechnologies.geoservices.datasources.house;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.database.HouseDataSource;
import com.jeltechnologies.geoservices.database.HouseDataSourceFactory;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.service.CountryMap;
import com.jeltechnologies.geoservices.utils.StringUtils;

public class HouseDataSourceFetcher implements Callable<HouseLocationFilter> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HouseDataSourceFetcher.class);
    private final Country country;
    private final CountryMap countries;
    private final File file;
    private HouseDataSource db = null;
    private final HouseDataSourceFactory datasourceFactory;

    public HouseDataSourceFetcher(Country country, CountryMap countries, File inputFile, HouseDataSourceFactory datasourceFactory) {
	this.country = country;
	this.countries = countries;
	this.file = inputFile;
	this.datasourceFactory = datasourceFactory;
    }

    @Override
    public HouseLocationFilter call() throws Exception {
	if (Thread.interrupted()) {
	    throw new InterruptedException();
	}
	try {
	    LOGGER.info("Loading addresses in " + country.name() + "...");
	    db = datasourceFactory.get();
	    GeoFile geoFile = persistCsvWhenNeeded();
	    Country country = geoFile.country();
	    HouseLocationFilter datasource;
	    datasource = new HouseLocationFilter(country, countries, datasourceFactory);
	    LOGGER.info("  Finished loading " + StringUtils.formatNumber(datasource.size()) + " addresses in " + country.name());
	    return datasource;
	} catch (Exception e) {
	    db.rollback();
	    throw e;
	} finally {
	    if (db != null) {
		db.close();
	    }
	}
    }

    private GeoFile persistCsvWhenNeeded() throws SQLException, FileNotFoundException, IOException {
	GeoFile geoFile = db.getFile(file.getName());
	if (geoFile != null) {
	    LOGGER.trace(file.getName() + " already in the database");
	} else {
	    persistCsvFileInDatabase(file);
	    db.commit();
	}
	geoFile = db.getFile(file.getName());
	return geoFile;
    }

    private void persistCsvFileInDatabase(File file) throws SQLException, FileNotFoundException, IOException {
	String countryName = country.name();
	FileInputStream in = new FileInputStream(file);
	LOGGER.info("Loading " + countryName + " from " + file.getName() + "...");

	String fileName = file.getName();
	db.insertFile(file.getName(), country);
	GeoFile geoFile = db.getFile(fileName);

	Scanner scanner = null;
	try {
	    scanner = new Scanner(in, "UTF-8");
	    int i = 0;
	    while (scanner.hasNext()) {
		i++;
		String line = scanner.nextLine();
		if (i > 1) {
		    try {
			addHouse(geoFile, i, line);
		    } catch (Exception e) {
			LOGGER.error(e.getMessage());
		    }
		}
	    }
	} finally {
	    if (scanner != null) {
		scanner.close();
	    }
	    if (in != null) {
		in.close();
	    }
	}
    }

    private void addHouse(GeoFile file, int lineNumber, String line) throws SQLException {
	String[] parts = line.split("\t");
	if (parts.length >= 7) {
	    String postalCode = parts[0];
	    String city = parts[1];
	    String street = parts[2];
	    String houseNumber = parts[3];
	    String x = parts[4];
	    String y = parts[5];
	    String countryCode = parts[6];
	    if (!countryCode.equals(file.country().code())) {
		LOGGER.warn("Data file inconsistencies, expected " + file.country().code() + " but got " + countryCode);
	    }
	    try {
		Coordinates c = new Coordinates(Float.parseFloat(y), Float.parseFloat(x));
		db.insertHouse(file.id(), c, postalCode, city, street, houseNumber, countryCode);
	    } catch (NumberFormatException nfe) {
		LOGGER.info("Could not parse geolocation in line " + lineNumber + " for x=" + x + " and y=" + y);
	    }
	}
    }

}
