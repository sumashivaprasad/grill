package com.inmobi.grill.rdd.rdd;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hive.hcatalog.data.HCatRecord;
import org.apache.spark.api.java.function.Function;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

public class HCatRecordToObjectListMapper implements Function<Tuple2<WritableComparable, HCatRecord>, List<Object>> {
  @Override
  public List<Object> call(Tuple2<WritableComparable, HCatRecord> hcatTuple) throws Exception {
    HCatRecord record = hcatTuple._2();

    if (record == null) {
      return null;
    }

    List<Object> row = new ArrayList<Object>(record.size());
    for (int i = 0; i < record.size(); i++) {
      row.add(record.get(i));
    }
    return row;
  }
}