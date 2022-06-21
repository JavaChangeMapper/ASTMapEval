package cs.zju.eva.statistics;

import cs.zju.utils.CsvOperationsUtil;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDataExtractor {
    private String filePath;
    private List<String[]> records;
    private String[] headers;
    private Map<String, Integer> colNameIdxMap;

    public CsvDataExtractor(String filePath, String[] headers) throws Exception {
        this.filePath = filePath;
        this.headers = headers;
        colNameIdxMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++){
            colNameIdxMap.put(headers[i], i);
        }
        records = CsvOperationsUtil.getCSVData(filePath, headers);
    }

    public CsvDataExtractor(List<String[]> records, String[] headers){
        this.headers = headers;
        colNameIdxMap = new HashMap<>();
        for (int i = 0; i < headers.length; i++){
            colNameIdxMap.put(headers[i], i);
        }
        this.records = records;
    }

    public String getColVal(String[] record, String colName){
        return record[colNameIdxMap.get(colName)];
    }

    public double getColDoubleVal(String[] record, String colName){
        return Double.parseDouble(getColVal(record, colName));
    }

    public List<String[]> getRecords() {
        return records;
    }

    public List<String> getColValues(String colName){
        if (!colNameIdxMap.containsKey(colName))
            throw new RuntimeException("the csv does not contain the column");
        int index = colNameIdxMap.get(colName);
        List<String> colVals = new ArrayList<>();
        for (String[] record: records){
            colVals.add(record[index]);
        }
        return colVals;
    }

    public List<Double> getColValuesConvertToReal(String colName){
        List<String> colVals = getColValues(colName);
        List<Double> ret = new ArrayList<>();
        for (String colVal: colVals){
            ret.add(Double.parseDouble(colVal));
        }
        return ret;
    }

    public RealVector getColValuesVector(String colName){
        List<Double> colValues = getColValuesConvertToReal(colName);
        Double[] array = colValues.toArray(new Double[colValues.size()]);
        RealVector vector = new ArrayRealVector(array);
        return vector;
    }
}
