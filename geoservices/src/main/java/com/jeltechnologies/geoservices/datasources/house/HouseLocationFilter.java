package com.jeltechnologies.geoservices.datasources.house;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.database.HouseDataSource;
import com.jeltechnologies.geoservices.database.HouseDataSourceFactory;
import com.jeltechnologies.geoservices.datamodel.AddressRequest;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datasources.AbstractLocationFilter;
import com.jeltechnologies.geoservices.datasources.NearestLocation;
import com.jeltechnologies.geoservices.service.CountryMap;
import com.jeltechnologies.geoservices.utils.StringUtils;

public class HouseLocationFilter extends AbstractLocationFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HouseLocationFilter.class);

    private final Country country;

    private final HouseDataSourceFactory houseDataSourceFactory;

    /**
     * Read all house coordinates from the database
     * 
     * @param
     * @throws SQLException
     * @throws IOException
     */
    public HouseLocationFilter(Country country, CountryMap countryMap, HouseDataSourceFactory houseDataSourceFactory)
	    throws SQLException, IOException, InterruptedException {
	super(countryMap);
	this.houseDataSourceFactory = houseDataSourceFactory;
	this.country = country;
	init();
    }

    private void init() throws SQLException, InterruptedException {
	HouseDataSource database = null;
	try {
	    database = houseDataSourceFactory.get();
	    super.locations = database.getCoordinates(country);
	    database.commit();
	} finally {
	    if (database != null) {
		database.close();
	    }
	}
    }

    private GeoHouse getHouse(int id) {
	HouseDataSource database = null;
	GeoHouse house = null;
	try {
	    database = houseDataSourceFactory.get();
	    house = database.getGeoHouse(id);
	    database.commit();
	} catch (SQLException e) {
	    LOGGER.warn("Cannot get house because of " + e.getMessage());
	} finally {
	    if (database != null) {
		database.commit();
		database.close();
	    }
	}
	return house;
    }

    @Override
    protected void makeUpdates(AddressRequest request) {
	NearestLocation nearest = getNearestLocation(request.query().coordinates());
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(" Nearest: " + nearest);
	    LOGGER.debug(" Current: " + request.answer().getDistanceFromQuery());
	}
	if (nearest.isCloserThan(request.answer().getDistanceFromQuery())) {
	    GeoCoordinates houseCoordinates = (GeoCoordinates) nearest.location();
	    GeoHouse houseAddress = getHouse(houseCoordinates.getId());
	    Coordinates coordinates = houseCoordinates.getCoordinates();
	    request.setStreet(houseAddress.street(), coordinates);
	    request.setNr(houseAddress.houseNumber(), coordinates);
	    request.setPostcalCode(houseAddress.postalCode(), coordinates);
	    String countryCode = houseAddress.countryCode();
	    request.setCountry(countries.getCountry(houseAddress.countryCode()), coordinates);
	    String city = houseAddress.city();
	    if (countryCode.equals("SE")) {
		city = StringUtils.stripAfter(city, "s kommun");
		city = StringUtils.stripAfterLast(city, " kommun");
	    }
	    if (!countryCode.equals("CN") && !countryCode.equals("TW")) {
		request.setPlace(city, coordinates);
	    }
	}
    }

    public Country getCountry() {
	return country;
    }

    @Override
    protected String getDescription() {
	return super.getDescription() + " (" + country.name() + ")";
    }

    @Override
    public String toString() {
	return StringUtils.formatNumber(super.locations.size()) + " addresses in " + country.name();
    }
}
