package com.inmobi.grill.server.ml;

/**
 * Directly return input when it is known to be double
 */
public class DoubleValueMapper extends FeatureValueMapper {
  @Override
  public final Double call(Object input) {
    return input == null ? 0d : (Double) input;
  }
}