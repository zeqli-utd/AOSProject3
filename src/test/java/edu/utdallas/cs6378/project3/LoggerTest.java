package edu.utdallas.cs6378.project3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.utdallas.project3.server.MutexServer;

public class LoggerTest {
	private static Logger logger = null;
	private static final Marker CRITICAL_SECTION_MARKER = MarkerManager.getMarker("CRITICAL_SECTION");
	
	@BeforeClass
	public static void setLogger() {
		logger = LogManager.getLogger(MutexServer.class.getName());
	}

	@Test
	public void testLogger() {
		logger.debug("Debug");
		logger.info("Info");
		logger.error("Error");
		
		// Critical Section Message
		logger.info(CRITICAL_SECTION_MARKER, "[Node{}] Enter critical section", 0);
		logger.info(CRITICAL_SECTION_MARKER, "[Node{}] Leave critical section", 0);
	}
	

}
