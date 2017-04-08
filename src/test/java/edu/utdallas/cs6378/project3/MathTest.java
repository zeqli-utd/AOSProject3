package edu.utdallas.cs6378.project3;

import java.util.Arrays;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MathTest {
    private static final Logger logger = LogManager.getLogger();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testExponentialDistributionRandomVariable() {
        double mean = 10.0;
        ExponentialDistribution ed = new ExponentialDistribution(mean);
        double[] samples = new double[1000];
        for (int i = 0; i < 1000; i++){
            samples[i] = ed.sample();
        }
        Arrays.sort(samples);
    }

}
