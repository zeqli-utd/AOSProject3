package edu.utdallas.cs6378.project3;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.utdallas.project3.protocol.DirectClock;

public class UtilsTest {

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
    public void testDirectClock() {
        DirectClock v = new DirectClock(5, 0);
        v.receiveAction(1, 5);    // 6
        v.receiveAction(2, 4);    // 7
        v.receiveAction(3, 3);    // 8
        v.receiveAction(4, 4);    // 9
        assertEquals(9, v.getValue(0));
        assertTrue(v.isSmallest(2));
        assertFalse(v.isSmallest(3));
    }

}
