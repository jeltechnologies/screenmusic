package com.jeltechnologies.geoservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.geoservices.config.Configuration;
import com.jeltechnologies.geoservices.config.Environment;
import com.jeltechnologies.geoservices.database.HouseDataSource;
import com.jeltechnologies.geoservices.database.HouseDataSourceFactory;
import com.jeltechnologies.geoservices.utils.JMXUtils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ContextListener implements ServletContextListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
	HouseDataSource datasource = null;
	try {
	    Configuration config = Environment.getConfiguration();
	    HouseDataSourceFactory factory = HouseDataSourceFactory.getInstance(servletContextEvent.getServletContext());
	    factory.init(config);
	    datasource = factory.get();
	    datasource.initDatabase(config.refreshOpenStreetDataCSV());
	    datasource.commit();
	    new DataSourceEngine(servletContextEvent.getServletContext(), config);
	    LOGGER.info("Service deployed");
	} catch (Exception e) {
	    String message = "Cannot start web application because " + e.getMessage();
	    LOGGER.error(message, e);
	    throw new IllegalStateException(message, e);
	}
	finally {
	    if (datasource != null) {
		datasource.close();
	    }
	}
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
	DataSourceEngine.getDataSourceEngine(servletContextEvent.getServletContext()).shutdown();
	JMXUtils.getInstance().unregisterAllMBeans();
	LOGGER.info("Service undeployed");
    }
}
