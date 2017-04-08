package edu.utdallas.cs6378.project3;

import static org.junit.Assert.*;

import java.net.URISyntaxException;

import org.junit.Test;

import edu.utdallas.project3.tools.MutexConfig;

public class ConfigurationLoadingTest {

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
            
        } catch (URISyntaxException e) {  }    
    }

}
