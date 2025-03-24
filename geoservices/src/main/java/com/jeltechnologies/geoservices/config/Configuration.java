package com.jeltechnologies.geoservices.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Configuration(
	String dataFolder,
	Boolean useDatabase,
	int threadPool,
	Boolean searchAllHouses,
	Boolean refreshOpenStreetDataCSV,
	CacheConfiguraton cache,
	@JsonProperty("limit-countries-with-addresses")
	String limitCountriesWithAddresses) {

    public Configuration {
	if (dataFolder == null) {
	    throw new IllegalArgumentException("dataFolder must be set");
	}
	final boolean USE_DATABASE = false; 
	final boolean USE_CACHE = true;
	final boolean SEARCH_ALL_HOUSES = false;
	final int MAX_CACHE_SIZE = 100000;
	final int EXPIRY_TIME_MINUTES = 1;
	final int SCHEDULE_CACHE_CLEAN_MINUTES = 2;
	final boolean REFRESH_OPENSTREETDATA_CSV = false;
	final int THREADPOOL = 5;

	if (useDatabase == null) {
	    useDatabase = USE_DATABASE;
	}
	
	if (searchAllHouses == null) {
	    searchAllHouses = SEARCH_ALL_HOUSES;
	}
	if (refreshOpenStreetDataCSV == null) {
	    refreshOpenStreetDataCSV = REFRESH_OPENSTREETDATA_CSV;
	}
	if (threadPool < 1) {
	    threadPool = THREADPOOL;
	}
	if (cache == null) {
	    cache = new CacheConfiguraton(USE_CACHE, MAX_CACHE_SIZE, EXPIRY_TIME_MINUTES, SCHEDULE_CACHE_CLEAN_MINUTES);
	}
    }
}
