package cs.zju.eva.statistics;

import cs.zju.utils.Pair;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import java.util.ArrayList;
import java.util.List;

public class StatisticCalculator {
    public static double getMedian(double[] values){
        Median m = new Median();
        m.setData(values);
        return m.evaluate();
    }

    public static double getMean(double[] values){
        Mean m = new Mean();
        m.setData(values);
        return m.evaluate();
    }

    public static double getLowerMAD(double[] values){
        double median = getMedian(values);
        double[] tmpValues = new double[values.length];
        for (int i = 0; i < values.length; i++){
            tmpValues[i] = Math.abs(values[i] - median);
        }

        double median2 = getMedian(tmpValues);
        return median - median2;
    }

    public static RealVector vectorAddition(RealVector v1, RealVector v2){
        return v1.add(v2);
    }

    public static RealVector vectorSubtraction(RealVector v1, RealVector v2){
        return v1.subtract(v2);
    }

    public static RealVector vectorElementByElementDivide(RealVector v1, RealVector v2){
        for (double d: v2.toArray()){
            if (d == 0)
                throw new RuntimeException("Divide Zero Error");
        }
        return v1.ebeDivide(v2);
    }

    public static RealVector vectorElementByElementMultiply(RealVector v1, RealVector v2){
        return v1.ebeMultiply(v2);
    }

    public static List<Integer> nonZeroIndexes(RealVector v){
        List<Integer> indexes = new ArrayList<>();
        int idx = 0;
        for (double d: v.toArray()){
            if (d != 0)
                indexes.add(idx);
            idx ++;
        }
        return indexes;
    }

    public static RealVector nonZeroEntries(RealVector v){
        List<Double> nonZeros = new ArrayList<>();
        for (double d: v.toArray()){
            if (d != 0)
                nonZeros.add(d);
        }
        return new ArrayRealVector(nonZeros.toArray(new Double[nonZeros.size()]));
    }

    public static Pair<RealVector, RealVector> differentEntryVector(RealVector v1, RealVector v2){
        if (v1.getDimension() != v2.getDimension())
            throw new RuntimeException("cannot compare the two vectors");
        List<Double> tmp1 = new ArrayList<>();
        List<Double> tmp2 = new ArrayList<>();
        for (int i = 0; i < v1.getDimension(); i++){
            double val1 = v1.getEntry(i);
            double val2 = v2.getEntry(i);
            if (val1 == val2){
                tmp1.add(val1);
                tmp2.add(val2);
            }
        }
        RealVector tmpV1 = new ArrayRealVector(tmp1.toArray(new Double[tmp1.size()]));
        RealVector tmpV2 = new ArrayRealVector(tmp2.toArray(new Double[tmp2.size()]));
        return new Pair<>(tmpV1, tmpV2);
    }

    public static double sumOfRealVector(RealVector realVector){
        Sum s = new Sum();
        s.setData(realVector.toArray());
        return s.evaluate();
    }
}
