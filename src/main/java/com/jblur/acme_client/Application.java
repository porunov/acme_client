package com.jblur.acme_client;

/**
 * Created by alex on 9/1/16.
 */

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import com.beust.jcommander.JCommander;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    private static final String LOGBACK_CONF = "logback_pattern.xml";

    private static void configureLogger(String logDir, String logLevel, String logbackConf) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            context.putProperty("LOG_DIR", logDir);
            context.putProperty("LOG_LEVEL", logLevel);
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream(logbackConf);
            configurator.doConfigure(is);
        } catch (JoranException je) {
            LOG.warn("Can not configure logger. Continue to execute the command.", je);
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
        JCommander jCommander = new JCommander(parameters, args);
        jCommander.setProgramName("java -jar acme_client.jar --command <command>");

        if (parameters.isHelp()) {
            StringBuilder usage = new StringBuilder();
            jCommander.usage(usage);
            System.out.println(usage.toString());
            String format = "%10s%n";
            System.out.format(format, Parameters.MAIN_USAGE.toString());
            return;
        }

        if (!IOManager.isDirectoryExists(parameters.getLogDir())) {
            LOG.info("Your log dir isn't exists: " + parameters.getLogDir() +
                    "\nTrying to create the directory for log files");
            try {
                IOManager.createDirectories(parameters.getLogDir());
            } catch (IOException e) {
                LOG.error("Can not create log dir: " + parameters.getLogDir() + "\n . Please check permissions", e);
                return;
            }
            LOG.info("Log directory " + parameters.getLogDir() + " is successfully created");
        }


        configureLogger(parameters.getLogDir(),
                (checkLogLevel(parameters.getLogLevel())) ? parameters.getLogLevel() : "WARN",
                LOGBACK_CONF);


        if (!parameters.verifyRequirements()) {
            return;
        }

        new CommandExecutor(parameters).execute();
    }

}