package com.inmobi.grill.server.ml.spark.models;

import org.apache.spark.mllib.classification.SVMModel;

public class SVMClassificationModel extends BaseSparkClassificationModel<SVMModel> {
  public SVMClassificationModel(String modelId, SVMModel model) {
    super(modelId, model);
  }
}
