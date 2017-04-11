package edu.utdallas.project3;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import edu.utdallas.project3.tools.MutexConfig;

public class ConfigurationLoadingTest {
    
    private static final Logger logger = LogManager.getLogger();

    @Test
    public void testLoadingConfiguration() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String configPath;
            configPath = classLoader.getResource("config.txt").toURI().getPath().substring(1);
            MutexConfig config = MutexConfig.loadFromConfigurationFile(configPath, 0);    
            int numberOfNodes = config.getNumberOfNodes();
            int meanCSExecution = config.getMeanCSExecution();
            int meanInterRequestDelay = config.getMeanInterRequestDelay();
            int numberofRequest = config.getNumberOfRequest();
            
            assertEquals(numberOfNodes, 5);
            assertEquals(meanInterRequestDelay, 20);
            assertEquals(meanCSExecution, 10);
            assertEquals(numberofRequest, 1000);
            logger.info(config.getNeighbors().toString());
            
            
        } catch (URISyntaxException e) {  }    
    }

    @Test
    public void testLoadingConfigurationFromInputStream() {
        logger.info("---------- Test load resource from inputstream ----------");
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream in = classLoader.getResourceAsStream("config.txt");
        MutexConfig config = MutexConfig.loadFromConfigurationFile(in, "config.txt", 0);    
        int numberOfNodes = config.getNumberOfNodes();
        int meanCSExecution = config.getMeanCSExecution();
        int meanInterRequestDelay = config.getMeanInterRequestDelay();
        int numberofRequest = config.getNumberOfRequest();
        
        assertEquals(numberOfNodes, 5);
        assertEquals(meanInterRequestDelay, 20);
        assertEquals(meanCSExecution, 10);
        assertEquals(numberofRequest, 1000);
        logger.info(config.getNeighbors().toString());
    
    }
}
