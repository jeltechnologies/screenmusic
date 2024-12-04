package com.jeltechnologies.screenmusic.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ConfigurationLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);
    
    private Configuration config;

    /**
     * This method must be only called once from the single thread in SheetMusicListener
     */
    public void load(Path yaml) throws IOException {
	ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
	mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	mapper.findAndRegisterModules();
	try {
	    File yamlFile = yaml.toFile();
	    LOGGER.info("Reading configuration from " + yamlFile);
	    config = (Configuration) mapper.readValue(yamlFile, Configuration.class);
	    if (LOGGER.isInfoEnabled()) {
		LOGGER.info("Using configuration: " + config);
	    }
	    String error = validateLoad();
	    if (error != null) {
		throw new IllegalStateException(error);
	    }
	    setupFolders();
	} catch (Exception e) {
	    throw new IOException("Could not load config " + yaml + " because " + e.getMessage(), e);
	}
    }

    private String validateLoad() {
	String error = null;
	if (error == null) {
	    File cache = new File(config.storage().cache());
	    if (cache == null || !cache.isDirectory()) {
		error = "Storage cache is not a directory";
	    }
	}
	return error;
    }

    private void setupFolders() {
	StorageConfiguration storage = config.storage();
	createFolderIfNotExists(storage.temp());
	createFolderIfNotExists(storage.cache());
	createFolderIfNotExists(storage.cacheExtracted());
	createFolderIfNotExists(storage.cacheDeleted());
    }

    private void createFolderIfNotExists(String folderName) {
	Path folder = Path.of(folderName);
	LOGGER.trace(folderName + " is directory: " + Files.isDirectory(folder));
	if (!Files.isDirectory(folder)) {
	    try {
		Files.createDirectories(folder);
		LOGGER.info("Created folder " + folder.toString());
	    } catch (IOException e) {
		throw new IllegalArgumentException("Cannot create folder " + folderName);
	    }
	}
    }

}
