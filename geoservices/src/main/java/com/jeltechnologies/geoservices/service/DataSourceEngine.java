package com.jeltechnologies.geoservices.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.config.Configuration;
import com.jeltechnologies.geoservices.database.HouseDataSourceFactory;
import com.jeltechnologies.geoservices.datamodel.AddressRequest;
import com.jeltechnologies.geoservices.datamodel.Answer;
import com.jeltechnologies.geoservices.datamodel.Coordinates;
import com.jeltechnologies.geoservices.datamodel.Country;
import com.jeltechnologies.geoservices.datamodel.Query;
import com.jeltechnologies.geoservices.datasources.LocationFilter;
import com.jeltechnologies.geoservices.datasources.city.CityLocationFilter;
import com.jeltechnologies.geoservices.datasources.house.HouseDataSourceFetcher;
import com.jeltechnologies.geoservices.datasources.house.HouseLocationFilter;
import com.jeltechnologies.geoservices.datasources.postalcode.PostalCodesLocationFilter;
import com.jeltechnologies.geoservices.utils.JMXUtils;
import com.jeltechnologies.geoservices.utils.StringUtils;

import jakarta.servlet.ServletContext;

public class DataSourceEngine implements DataSourceEngineMBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceEngine.class);
    
    private final String HOUSES_FILE_EXTENSION = "-houses.tsv";

    private final Configuration configuration;

    private final ExecutorService executor;

    private final GeoLocationCache locationCache;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private LocationFilter cities;

    private CountryMap countries;

    private LocationFilter postalCodes;

    private Map<Country, HouseLocationFilter> houses = new HashMap<Country, HouseLocationFilter>();

    private AtomicBoolean readyForService = new AtomicBoolean(false);

    private long handledRequests;

    private LocalDateTime lastRequest;

    private String lastAddress;

    public DataSourceEngine(ServletContext context, Configuration configuration) throws IOException {
	JMXUtils.getInstance().registerMBean("engine", "Address", this);
	this.configuration = configuration;
	this.executor = Executors.newFixedThreadPool(configuration.threadPool());
	context.setAttribute(DataSourceEngine.class.getName(), this);
	if (configuration.cache().useCache()) {
	    this.locationCache = new GeoLocationCache(configuration.cache(), scheduledExecutorService);
	} else {
	    this.locationCache = null;
	}
	HouseDataSourceFactory houseDataSourceFactory = HouseDataSourceFactory.getInstance(context);
	executor.execute(new Runnable() {
	    @Override
	    public void run() {
		try {
		    List<LocationFilter> datasources = init(houseDataSourceFactory);
		    LOGGER.info("Loading completed of: ");
		    int totalHouses = 0;
		    for (LocationFilter ds : datasources) {
			if (ds instanceof HouseLocationFilter) {
			    totalHouses += ds.size();
			} else {
			    LOGGER.info("  " + ds.toString());
			}
		    }
		    LOGGER.info("  " + StringUtils.formatNumber(totalHouses) + " adresses");
		    LOGGER.info("Ready for service");
		    readyForService.set(true);
		} catch (IOException e) {
		    LOGGER.info("Error reading data files", e.getMessage(), e);
		} catch (SQLException e) {
		    LOGGER.info("Error interacting with database", e.getMessage(), e);
		} catch (InterruptedException ie) {
		    LOGGER.info("Reading of datafiles was interrupted");
		}
	    }
	});
    }

    public AddressRequest getAddress(Coordinates coordinates) {
	lastRequest = LocalDateTime.now();
	AddressRequest location = null;
	if (this.locationCache != null) {
	    try {
		location = this.locationCache.fetch(coordinates);
	    } catch (Exception e) {
		LOGGER.trace("Cache size problem, fetch a new instance instead");
	    }
	}
	if (location == null) {
	    location = calculateLocation(coordinates);
	}
	if (this.locationCache != null) {
	    this.locationCache.add(location);
	}
	handledRequests++;
	lastAddress = location.answer().getAddress().toString();
	return location;
    }

    private AddressRequest calculateLocation(Coordinates coordinates) {
	Query query = new Query(coordinates);
	Answer answer = new Answer();
	AddressRequest request = new AddressRequest(query, answer);
	cities.updateLocation(request);
	Country country = request.answer().getAddress().getCountry();
	if (configuration.searchAllHouses()) {
	    for (Country c : houses.keySet()) {
		houses.get(c).updateLocation(request);
	    }
	} else {
	    HouseLocationFilter datasource = houses.get(country);
	    if (datasource != null) {
		datasource.updateLocation(request);
	    }
	}
	postalCodes.updateLocation(request);
	return request;
    }

    public boolean isReadyForService() {
	return readyForService.get();
    }

    public void shutdown() {
	executor.shutdown();
	scheduledExecutorService.shutdown();
	this.houses = new HashMap<Country, HouseLocationFilter>();
    }

    public static DataSourceEngine getDataSourceEngine(ServletContext context) {
	return (DataSourceEngine) context.getAttribute(DataSourceEngine.class.getName());
    }

    private InputStream getStreamFromFile(String name) throws IOException {
	File dataFolder = new File(configuration.dataFolder());
	File file = new File(dataFolder.getAbsolutePath() + "/" + name);
	FileInputStream in = new FileInputStream(file);
	return in;
    }

    private InputStream getStreamFromResource(String name) throws IOException {
	return DataSourceEngine.class.getResourceAsStream("/" + name);
    }

    private List<LocationFilter> init(HouseDataSourceFactory houseDataSourceFactory) throws SQLException, IOException, InterruptedException {
	countries = new CountryMap(getStreamFromResource("countrycodes.json"));
	List<LocationFilter> datasources = new ArrayList<LocationFilter>();

	// Download at https://public.opendatasoft.com/explore/dataset/geonames-all-cities-with-a-population-1000/table/?disjunctive.cou_name_en&sort=name
	cities = new CityLocationFilter(getStreamFromFile("geonames-all-cities-with-a-population-1000.csv"), countries);
	datasources.add(cities);

	// Download at https://public.opendatasoft.com/explore/dataset/geonames-postal-code/export/
	postalCodes = new PostalCodesLocationFilter(getStreamFromFile("geonames-postal-code.csv"), countries);
	datasources.add(postalCodes);
	addOpenStreetSources(houseDataSourceFactory);
	for (Country c : houses.keySet()) {
	    datasources.add(houses.get(c));
	}
	return datasources;
    }

    private void addOpenStreetSources(HouseDataSourceFactory houseDataSourceFactory) throws SQLException, IOException, InterruptedException {
	File dataFolder = new File(configuration.dataFolder());
	List<Future<HouseLocationFilter>> futureFetchers = new ArrayList<Future<HouseLocationFilter>>();

	String limitedCountries = configuration.limitCountriesWithAddresses();

	if (limitedCountries != null) {
	    List<String> countryCodes = new ArrayList<String>();
	    String[] parts = limitedCountries.split(",");
	    for (String part : parts) {
		countryCodes.add(part.trim().toUpperCase());
	    }
	    for (String countryCode : countryCodes) {
		File file = new File(dataFolder, countryCode + HOUSES_FILE_EXTENSION);
		Future<HouseLocationFilter> future = getFutureThatExtractsAddress(houseDataSourceFactory, file, countryCode);
		futureFetchers.add(future);
	    }
	} else {
	    File[] streetFiles = dataFolder.listFiles(new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
		    return name.endsWith(HOUSES_FILE_EXTENSION);
		}
	    });
	    for (File file : streetFiles) {
		String countryCode = file.getName().substring(0, 2).toUpperCase();
		Future<HouseLocationFilter> future = getFutureThatExtractsAddress(houseDataSourceFactory, file, countryCode);
		futureFetchers.add(future);
	    }
	}

	for (Future<HouseLocationFilter> fetcher : futureFetchers) {
	    try {
		HouseLocationFilter datasource = fetcher.get();
		houses.put(datasource.getCountry(), datasource);
	    } catch (ExecutionException e) {
		LOGGER.warn("Error loading houses data: ", e.getMessage(), e);
	    }
	}
	LOGGER.info("All house locations added");
    }

    private Future<HouseLocationFilter> getFutureThatExtractsAddress(HouseDataSourceFactory houseDataSourceFactory, File file, String countryCode) {
	Country country = countries.getCountry(countryCode);
	Future<HouseLocationFilter> future = executor.submit(new HouseDataSourceFetcher(country, countries, file, houseDataSourceFactory));
	return future;
    }

    @Override
    public int getNrOfCities() {
	return cities.size();
    }

    @Override
    public int getNrOfHouses() {
	int total = 0;
	for (Country c : houses.keySet()) {
	    total = total + houses.get(c).size();
	}
	return total;
    }

    @Override
    public int getNrOfPostalCodes() {
	return postalCodes.size();
    }

    @Override
    public long getHandledRequests() {
	return handledRequests;
    }

    @Override
    public String getLastRequest() {
	return lastRequest.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String getLastAddress() {
	return lastAddress;
    }
}
