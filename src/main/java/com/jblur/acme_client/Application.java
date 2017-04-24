package com.jblur.acme_client;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private static final String LOGBACK_CONF = "logback_pattern.xml";
    private static final String APPLICATION_PROPS = "application.properties";

    private static ClassLoader classloader = Thread.currentThread().getContextClassLoader();

    private static void configureLogger(String logDir, String logLevel, String logbackConf) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            if (!logDir.endsWith(File.separator))
                logDir+= File.separator;
            context.putProperty("LOG_DIR", logDir);
            context.putProperty("LOG_LEVEL", logLevel);

            InputStream is = classloader.getResourceAsStream(logbackConf);
            configurator.doConfigure(is);
        } catch (JoranException je) {
            LOG.warn("Cannot configure logger. Continue to execute the command.", je);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private static boolean checkLogLevel(String logLevel) {
        String[] logLevels = new String[]{"OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"};
        boolean valid = false;
        for (String strLogLevel : logLevels) {
            if (strLogLevel.equalsIgnoreCase(logLevel)) {
                valid = true;
                break;
            }
        }
        return valid;
    }

    public static void main(String[] args) throws URISyntaxException, IOException {
        Parameters parameters = new Parameters();
        JCommander jCommander;
        try {
            jCommander = new JCommander(parameters, args);
        } catch (Exception e) {
            LOG.error("An error occurred while parsing parameters.", e);
            System.out.print(CommandExecutor.RESULT_ERROR);
            return;
        }

        jCommander.setProgramName("java -jar acme_client.jar");

        if (parameters.isHelp()) {
            StringBuilder usage = new StringBuilder();
            jCommander.usage(usage);
            System.out.println(usage.toString());
            String format = "%10s%n";
            System.out.format(format, Parameters.MAIN_USAGE.toString());
            return;
        }

        if (parameters.isVersion()) {
            Properties prop = new Properties();
            try {
                prop.load(classloader.getResourceAsStream(APPLICATION_PROPS));
                System.out.println(prop.getProperty("version"));
            }
            catch (IOException ex) {
                LOG.error("Cannot get version information.", ex);
                System.out.println(CommandExecutor.RESULT_ERROR);
            }
            return;
        }

        if (!Files.isDirectory(Paths.get(parameters.getLogDir()))) {
            LOG.info("Specified log directory doesn't exist: " + parameters.getLogDir() +
                    "\nTrying to create the log directory.");
            try {
                Files.createDirectories(Paths.get(parameters.getLogDir()));
            } catch (IOException e) {
                LOG.error("Cannot create log directory: " + parameters.getLogDir() + ".\nPlease check filesystem permissions.", e);
                System.out.print(CommandExecutor.RESULT_ERROR);
                return;
            }
            LOG.info("Created log directory: " + parameters.getLogDir());
        }


        configureLogger(parameters.getLogDir(),
                (checkLogLevel(parameters.getLogLevel())) ? parameters.getLogLevel() : "WARN",
                LOGBACK_CONF);


        if (!parameters.verifyRequirements()) {
            System.out.print(CommandExecutor.RESULT_ERROR);
            return;
        }

        new CommandExecutor(parameters).execute();
    }

}