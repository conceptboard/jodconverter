package org.artofsolving.jodconverter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Utilites for logging the output on the standard streams of a running process
 * 
 * this basically has to create two threads which will consume the InputStreams of the process and log them to a logger, whenever a new line was written
 */
public class ProcessLoggingUtils {

	public static void logProcessOutput(final Process process, final long pid, final Logger logger) {
		ThreadGroup threadGroup = new ThreadGroup(String.format("loggingthreads for process [ %s ]", pid)) {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.warn("exception on loggingthread for process [ " + pid + " ]", e);
				super.uncaughtException(t, e);
			}
		};
		threadGroup.setDaemon(true);

		Thread stdOutLoggerThread = createLoggingThread(threadGroup, pid, process.getInputStream(), "stdout", logger);
		Thread stdErrLoggerThread = createLoggingThread(threadGroup, pid, process.getErrorStream(), "stderr", logger);

		start(stdOutLoggerThread);
		start(stdErrLoggerThread);
	}

	private static void start(Thread t) {
		if (t != null) {
			t.start();
		}
	}

	private static Thread createLoggingThread(final ThreadGroup threadGroup, final long pid, final InputStream stream, final String name,
	        final Logger logger) {
		if (stream == null) {
			return null;
		}
		Runnable r = createLoggingRunnable(pid, name, stream, logger);
		Thread t = new Thread(threadGroup, r, String.format("loggingthread for %s of process [ %s ]", name, pid));
		t.setDaemon(true);
		return t;
	}

	private static Runnable createLoggingRunnable(final long pid, final String name, final InputStream stream, final Logger logger) {
		if (stream == null) {
			return null;
		}
		String prefix = String.format("%s[%s]: ", name, pid);
		return new LoggingRunnable(stream, logger, prefix);
	}

	private static final class LoggingRunnable implements Runnable {
		private final BufferedReader reader;
		private final String prefix;
		private final Logger logger;

		private LoggingRunnable(InputStream stream, Logger logger, String prefix) {
			this.reader = new BufferedReader(new InputStreamReader(stream));
			this.logger = logger;
			this.prefix = prefix;
		}

		@Override
		public void run() {
			String line;
			try {
				while ((line = reader.readLine()) != null) {
					this.logger.debug(prefix + line);
				}
			} catch (IOException e) {
				this.logger.error(prefix + "error reading line", e);
			}
		}
	}
}
