package nl.koenhabets.yahtzeescore;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage {
    private final Queue<Double> Dataset = new LinkedList<>();
    private final int size;
    private double sum;

    public MovingAverage(int size)
    {
        this.size = size;
    }

    public void addData(double num)
    {
        sum += num;
        Dataset.add(num);

        if (Dataset.size() > size)
        {
            sum -= Dataset.remove();
        }
    }

    public double getMean()
    {
        return sum / size;
    }
}
