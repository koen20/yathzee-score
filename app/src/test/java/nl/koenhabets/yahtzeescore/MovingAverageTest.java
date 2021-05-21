package nl.koenhabets.yahtzeescore;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MovingAverageTest {
    @Test
    public void average() {
        MovingAverage movingAverage = new MovingAverage(4);
        movingAverage.addData(4);
        assertEquals(1 , movingAverage.getMean(), 0.1d);
        movingAverage.addData(4);
        movingAverage.addData(10);
        movingAverage.addData(10);
        assertEquals(7 , movingAverage.getMean(), 0.1d);
        movingAverage.addData(10);
        assertEquals(8.5 , movingAverage.getMean(), 0.1d);
    }
}
