package com.jeltechnologies.screenmusic.opticalmusicrecognition;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jeltechnologies.screenmusic.User;
import com.jeltechnologies.screenmusic.config.AudiverisConfiguration;
import com.jeltechnologies.screenmusic.config.AudiverisDefaultOptions;
import com.jeltechnologies.screenmusic.config.AudiverisOption;
import com.jeltechnologies.screenmusic.config.Configuration;
import com.jeltechnologies.screenmusic.library.Library;
import com.jeltechnologies.screenmusic.pdf.PdfExtractor;
import com.jeltechnologies.screenmusic.servlet.ScreenMusicContext;
import com.jeltechnologies.utils.OperatingSystemCommand;
import com.jeltechnologies.utils.OperatingSystemCommandListener;
import com.jeltechnologies.utils.StringUtils;

public class Consumer implements Runnable, ConsumerMBean, OperatingSystemCommandListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    private final JobQueue queue;

    private Job job;

    private final int threadNumber;

    private String status;

    private volatile boolean shuttingDown = false;

    private volatile boolean consuming = false;

    private AudiverisSteps steps;

    private final User user;

    private final AudiverisConfiguration config;

    private final ScreenMusicContext context;

    private final static AudiverisDefaultOptions AUDIVERIS_DEFAULT_OPTIONS = new AudiverisDefaultOptions();

    public Consumer(User user, ScreenMusicContext context, AudiverisConfiguration config, JobQueue queue, int threadNumber) {
	this.queue = queue;
	this.threadNumber = threadNumber;
	this.user = user;
	this.config = config;
	this.context = context;
    }

    public void shutdown() {
	LOGGER.info("Shutdown");
	this.shuttingDown = true;
    }

    @Override
    public void run() {
	String threadName = "OmrJobConsumer-" + threadNumber;
	Thread.currentThread().setName(threadName);
	LOGGER.info("Started " + threadName);
	while (!shuttingDown) {
	    try {
		status = "Waiting for items in queue";
		consuming = false;
		job = queue.poll();
		if (job != null && !job.isCanceled()) {
		    status = "Processing file:" + job.getInputFile() + ", id: " + job.getId();
		    consuming = true;
		    steps = new AudiverisSteps(job.getPages());
		    consumeJob();
		}
	    } catch (InterruptedException e) {
		LOGGER.info("Job was interrupted");
		shuttingDown = true;
	    } catch (Exception e) {
		job.setStatus(JobStatus.ERROR);
		LOGGER.error("Cannot run Audiveris because of " + e.getMessage(), e);
	    }
	}
	status = "Ended";
	LOGGER.info("Ended " + threadName);
    }

    public boolean isConsuming() {
	return consuming;
    }

    private void consumeJob() throws InterruptedException, IOException {
	job.setStatus(JobStatus.PROCESSING);
	steps.getStep("EXPORT_PDF");
	File extractedPages = extractPagesFromPdf();
	if (extractedPages != null) {
	    doOpticalRecognition(extractedPages);
	} else {
	    job.setStatus(JobStatus.ERROR);
	}
    }

    private File extractPagesFromPdf() {
	LOGGER.info("User: " + user);
	File extractedPagesFile = null;
	try {
	    String outFileName = job.getId();
	    if (job.getFrom() != -1) {
		extractedPagesFile = new PdfExtractor().extractPdf(user, outFileName, job.getBook(), job.getFrom(), job.getTo());
	    } else {
		extractedPagesFile = user.getFile(job.getBook().getRelativeFileName());
	    }
	} catch (Exception e) {
	    LOGGER.warn("Cannot extract PDF for job " + job);
	}
	return extractedPagesFile;
    }

    private void doOpticalRecognition(File extractedPages) throws IOException, InterruptedException, FileNotFoundException {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("extractedPage: " + extractedPages.getAbsolutePath());
	}
	job.setInputFile(extractedPages);
	File outputFolder = Files.createTempDirectory("sheetmusic-audiveris-" + job.getId()).toFile();
	job.setOutputFolder(outputFolder);
	doAudiveris(outputFolder);
	File downloadFile = createDownloadFile(outputFolder);
	//extractedPages.delete();
	if (downloadFile == null) {
	    job.setStatus(JobStatus.ERROR);
	    job.setStep("");
	} else {
	    job.setDownloadFile(downloadFile);
	    job.setStatus(JobStatus.READY);
	    job.setStep("");
	}
    }

    private File createDownloadFile(File outputFolder) throws FileNotFoundException, IOException {
	String pageLabel = new Library(user, context).getPageLabel(job.getBook(), job.getFrom(), job.getTo());
	pageLabel = StringUtils.stripCharsNotIn(pageLabel, "abcdefghijklmnopqrstuvwxyzåöäABCDEFGHIJKLMNOPQRSTUVWXYZÅÖÄ .,0123456789-");
	String outputFileName = outputFolder.getAbsolutePath() + "/" + pageLabel;

	File[] outputFiles = outputFolder.listFiles(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return name.endsWith(".mxl");
	    }
	});

	File outputFile = null;
	if (outputFiles.length > 1) {
	    outputFile = new File(outputFileName + ".zip");
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Zipping to " + outputFile);
	    }
	    FileOutputStream fos = new FileOutputStream(outputFile);
	    ZipOutputStream zipOut = new ZipOutputStream(fos);
	    for (File file : outputFiles) {
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(file.getName());
		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
		    zipOut.write(bytes, 0, length);
		}
		fis.close();
	    }
	    zipOut.close();
	    fos.close();
	} else {
	    if (outputFiles.length == 1) {
		File mxlFile =  outputFiles[0];
		File renamed = new File(outputFileName + ".mxl");
		mxlFile.renameTo(renamed);
		outputFile = renamed;
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("OutputFile: " + outputFile);
		}
	    } else {
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("No output found");
		}
	    }
	}
	return outputFile;
    }

    private void doAudiveris(File outputFolder) throws IOException, InterruptedException {
	File exe;

	String javaHome = System.getenv("JAVA_HOME");
	if (javaHome == null) {
	    throw new IOException("Cannot start Audiveris because JAVA_HOME is not set");
	}
	String javaExe = javaHome + "/bin/java";

	boolean onWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
	if (onWindows) {
	    javaExe += ".exe";
	}
	exe = new File(javaExe);

	OperatingSystemCommand command = new OperatingSystemCommand(exe);

	File audiverisLibFolder = new File(config.getAudiveris_lib());
	if (!audiverisLibFolder.isDirectory()) {
	    throw new IOException("Cannot start Audiveris because audiverusLibFolder cannot be found or is not a folder. Check the YAML configuration");
	}
	command.setFolder(audiverisLibFolder);
	command.addArgument("-cp");
	command.addArgument("*");
	command.addArgument("Audiveris");
	command.addArgument("-batch");
	command.addArgument("-transcribe");
	command.addArgument("-export");
	command.addArgument("-save");
	command.addArgument("-output");
	command.addArgument(outputFolder.getAbsolutePath());
	command.addArgument(job.getInputFile().getAbsolutePath());

	addOptions(command);

	command.setEnvironmentVariable("TESSDATA_PREFIX", config.getTessdata_prefix());
	command.addListener(this);

	command.execute();
    }

    private void addOptions(OperatingSystemCommand command) {
	Map<String, String> map = new HashMap<String, String>();
	List<AudiverisOption> defaults = Configuration.getInstance().opticalmusicrecognition().audiveris().getDefaultOptions();
	for (AudiverisOption option : defaults) {
	    map.put(option.name(), option.value());
	}
	List<AudiverisOption> jobOptions = job.getJobData().getOptions();
	for (AudiverisOption option : jobOptions) {
	    map.put(option.name(), option.value());
	}
	if (!map.isEmpty()) {
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("Audiveris CLI options");
		LOGGER.debug("======================");
	    }
	    for (String name : map.keySet()) {
		if (verifyOption(name)) {
		    String value = map.get(name);
		    String cliOption = name + "=" + value;
		    if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("  -option " + cliOption);
		    }
		    command.addArgument("-option");
		    command.addArgument(cliOption);
		} else {
		    LOGGER.warn("Option not found in Audiveris: " + name);
		}
	    }
	    if (LOGGER.isDebugEnabled()) {
		LOGGER.debug("======================");
	    }
	}
    }

    private boolean verifyOption(String name) {
	AudiverisOption option = AUDIVERIS_DEFAULT_OPTIONS.getOption(name);
	return option != null;
    }

    public String getStatus() {
	return status;
    }

    @Override
    public void receivedLine(String line) {
	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug(line);
	}
	String afterPipe = StringUtils.findAfter(line, " | ").trim();
	if (afterPipe != null && !afterPipe.equals("")) {
	    String step = steps.getStep(afterPipe);
	    if (step != null && !step.isEmpty()) {
		int percentage = steps.getPercentageCompleted();
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug(percentage + "% completed");
		}
		job.setPercentageCompleted(percentage);
	    }
	}
    }
}
