//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
//    -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
//    -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ThreadFactory} that allows for custom thread names
 */
class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadIndex = new AtomicInteger(0);
	private static final Logger logger = LoggerFactory.getLogger(NamedThreadFactory.class);

    private final String baseName;
    private final boolean daemon;

    public NamedThreadFactory(String baseName) {
        this(baseName, true);
    }

    public NamedThreadFactory(String baseName, boolean daemon) {
        this.baseName = baseName;
        this.daemon = daemon;
    }

    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, baseName + "-" + threadIndex.getAndIncrement());
        thread.setDaemon(daemon);
		thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Failure in thread", e);
			}
		});
        return thread;
    }

}
