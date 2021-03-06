package com.inmobi.grill.lib.query;

/*
 * #%L
 * Grill Query Library
 * %%
 * Copyright (C) 2014 Inmobi
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.FieldSchema;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hive.service.cli.ColumnDescriptor;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.inmobi.grill.server.api.GrillConfConstants;
import com.inmobi.grill.server.api.driver.GrillResultSetMetadata;
import com.inmobi.grill.server.api.query.QueryContext;

public abstract class TestAbstractFileFormatter {

  protected WrappedFileFormatter formatter;

  @AfterMethod
  public void cleanup() throws IOException {
    if (formatter != null) {
      FileSystem fs = new Path(formatter.getFinalOutputPath()).getFileSystem(new Configuration());
      fs.delete(new Path(formatter.getFinalOutputPath()), true);
    }
  }

  @Test
  public void testFormatter() throws IOException {
    Configuration conf = new Configuration();
    setConf(conf);
    testFormatter(conf, "UTF8",
        GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR_DEFAULT, ".csv");
    // validate rows
    Assert.assertEquals(readFinalOutputFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-8"), getExpectedCSVRows());
  }

  @Test
  public void testCompression() throws IOException {
    Configuration conf = new Configuration();
    conf.setBoolean(GrillConfConstants.QUERY_OUTPUT_ENABLE_COMPRESSION, true);
    setConf(conf);
    testFormatter(conf, "UTF8",
        GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR_DEFAULT, ".csv.gz");
    // validate rows
    Assert.assertEquals(readCompressedFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-8"), getExpectedCSVRows());
  }

  @Test
  public void testCustomCompression() throws IOException {
    Configuration conf = new Configuration();
    conf.setBoolean(GrillConfConstants.QUERY_OUTPUT_ENABLE_COMPRESSION, true);
    conf.set(GrillConfConstants.QUERY_OUTPUT_COMPRESSION_CODEC,
        org.apache.hadoop.io.compress.DefaultCodec.class.getCanonicalName());
    setConf(conf);
    testFormatter(conf, "UTF8",
        GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR_DEFAULT, ".csv.deflate");
    // validate rows
    Assert.assertEquals(readCompressedFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-8"), getExpectedCSVRows());
  }

  @Test
  public void testEncoding() throws IOException {
    Configuration conf = new Configuration();
    conf.set(GrillConfConstants.QUERY_OUTPUT_CHARSET_ENCODING, "UTF-16LE");
    setConf(conf);
    testFormatter(conf, "UnicodeLittleUnmarked",
        GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR_DEFAULT, ".csv");
    // validate rows
    Assert.assertEquals(readFinalOutputFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-16LE"), getExpectedCSVRows());
  }

  @Test
  public void testCompressionAndEncoding() throws IOException {
    Configuration conf = new Configuration();
    conf.set(GrillConfConstants.QUERY_OUTPUT_CHARSET_ENCODING, "UTF-16LE");
    conf.setBoolean(GrillConfConstants.QUERY_OUTPUT_ENABLE_COMPRESSION, true);
    setConf(conf);
    testFormatter(conf, "UnicodeLittleUnmarked",
        GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR_DEFAULT, ".csv.gz");
    // validate rows
    Assert.assertEquals(readCompressedFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-16LE"), getExpectedCSVRows());
  }

  @Test
  public void testOutputPath() throws IOException {
    Configuration conf = new Configuration();
    String outputParent = "target/" + getClass().getSimpleName();
    conf.set(GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR, outputParent);
    setConf(conf);
    testFormatter(conf, "UTF8", outputParent, ".csv");
    // validate rows
    Assert.assertEquals(readFinalOutputFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-8"), getExpectedCSVRows());
  }

  @Test
  public void testCompressionWithCustomOutputPath() throws IOException {
    Configuration conf = new Configuration();
    String outputParent = "target/" + getClass().getSimpleName();
    conf.set(GrillConfConstants.GRILL_RESULT_SET_PARENT_DIR, outputParent);
    conf.setBoolean(GrillConfConstants.QUERY_OUTPUT_ENABLE_COMPRESSION, true);
    setConf(conf);
    testFormatter(conf, "UTF8", outputParent, ".csv.gz");
    // validate rows
    Assert.assertEquals(readCompressedFile(
        new Path(formatter.getFinalOutputPath()), conf, "UTF-8"), getExpectedCSVRows());
  }

  protected abstract WrappedFileFormatter createFormatter();

  protected abstract void writeAllRows(Configuration conf) throws IOException;

  protected void setConf(Configuration conf) {
  }

  protected void testFormatter(Configuration conf, String charsetEncoding,
      String outputParentDir,
      String fileExtn) throws IOException {
    QueryContext ctx = new QueryContext("test writer query", "testuser", null, conf);
    formatter = createFormatter();

    formatter.init(ctx, getMockedResultSet());

    // check output spec
    Assert.assertEquals(formatter.getEncoding(), charsetEncoding);
    Path tmpPath = formatter.getTmpPath();
    Path expectedTmpPath = new Path (outputParentDir,
        ctx.getQueryHandle() + ".tmp" + fileExtn);
    Assert.assertEquals(tmpPath, expectedTmpPath);

    // write header, rows and footer; 
    formatter.writeHeader();
    writeAllRows(conf);
    formatter.writeFooter();
    FileSystem fs = expectedTmpPath.getFileSystem(conf);
    Assert.assertTrue(fs.exists(tmpPath));

    //commit and close
    formatter.commit();
    formatter.close();
    Assert.assertFalse(fs.exists(tmpPath));
    Path finalPath = new Path(formatter.getFinalOutputPath());
    Path expectedFinalPath = new Path (outputParentDir,
        ctx.getQueryHandle() + fileExtn).makeQualified(fs);
    Assert.assertEquals(finalPath, expectedFinalPath);
    Assert.assertTrue(fs.exists(finalPath));
  }

  protected List<String> readFinalOutputFile(Path finalPath, Configuration conf,
      String encoding) throws IOException {
    FileSystem fs = finalPath.getFileSystem(conf);
    return readFromStream(new InputStreamReader(fs.open(finalPath), encoding));
  }

  protected List<String> readCompressedFile(Path finalPath, Configuration conf,
      String encoding) throws IOException {
    CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(conf);
    compressionCodecs = new CompressionCodecFactory(conf);
    final CompressionCodec codec = compressionCodecs.getCodec(finalPath);
    FileSystem fs = finalPath.getFileSystem(conf);
    return readFromStream(new InputStreamReader(
        codec.createInputStream(fs.open(finalPath)), encoding));
  }

  protected List<String> readFromStream(InputStreamReader ir) throws IOException {
    List<String> result = new ArrayList<String>();
    BufferedReader reader = new BufferedReader(ir);
    String line = reader.readLine();
    while (line != null) {
      result.add(line);
      line = reader.readLine();
    }
    reader.close();
    return result;

  }
  private GrillResultSetMetadata getMockedResultSet() {
    return new GrillResultSetMetadata() {

      @Override
      public List<ColumnDescriptor> getColumns() {
        List<ColumnDescriptor> columns = new ArrayList<ColumnDescriptor>();
        columns.add(new ColumnDescriptor(new FieldSchema("firstcol", "int", ""), 0));
        columns.add(new ColumnDescriptor(new FieldSchema("secondcol", "string", ""), 1));
        columns.add(new ColumnDescriptor(new FieldSchema("thirdcol", "varchar(20)", ""), 2));
        columns.add(new ColumnDescriptor(new FieldSchema("fourthcol", "char(15)", ""), 3));
        columns.add(new ColumnDescriptor(new FieldSchema("fifthcol", "array<tinyint>", ""), 4));
        columns.add(new ColumnDescriptor(new FieldSchema("sixthcol", "struct<a:int,b:varchar(10)>", ""), 5));
        columns.add(new ColumnDescriptor(new FieldSchema("seventhcol", "map<int,char(10)>", ""), 6));
        return columns;
      }
    };
  }

  protected List<String> getExpectedCSVRows() {
    List<String> csvRows = new ArrayList<String>();
    csvRows.add("\"firstcol\",\"secondcol\",\"thirdcol\",\"fourthcol\",\"fifthcol\",\"sixthcol\",\"seventhcol\"");
    csvRows.add("\"1\",\"one\",\"one\",\"one\",\"1\",\"1:one\",\"1=one\"");
    csvRows.add("\"2\",\"two\",\"two\",\"two\",\"1,2\",\"2:two\",\"1=one,2=two\"");
    csvRows.add("\"NULL\",\"three\",\"three\",\"three\",\"1,2,NULL\",\"NULL:three\",\"1=one,2=two,NULL=three\"");
    csvRows.add("\"4\",\"NULL\",\"NULL\",\"NULL\",\"1,2,NULL,4\",\"4:NULL\",\"1=one,2=two,NULL=three,4=NULL\"");
    csvRows.add("\"NULL\",\"NULL\",\"NULL\",\"NULL\",\"1,2,NULL,4,NULL\",\"NULL:NULL\",\"1=one,2=two,NULL=three,4=NULL,5=NULL\"");
    csvRows.add("Total rows:5");
    return csvRows;
  }

  protected List<String> getExpectedTextRows() {
    List<String> txtRows = new ArrayList<String>();
    txtRows.add("firstcolsecondcolthirdcolfourthcolfifthcolsixthcolseventhcol");
    txtRows.add("1oneoneone            11one1one       ");
    txtRows.add("2twotwotwo            122two1one       2two       ");
    txtRows.add("\\Nthreethreethree          12\\N\\Nthree1one       2two       \\Nthree     ");
    txtRows.add("4\\N\\N\\N12\\N44\\N1one       2two       \\Nthree     4\\N");
    txtRows.add("\\N\\N\\N\\N12\\N4\\N\\N\\N1one       2two       \\Nthree     4\\N5\\N");
    txtRows.add("Total rows:5");
    return txtRows;
  }

  protected List<String> readZipOutputFile(Path finalPath, Configuration conf,
      String encoding) throws IOException {
    FileSystem fs = finalPath.getFileSystem(conf);
    List<String> result = new ArrayList<String>();
    ZipEntry ze = null; 
    ZipInputStream zin = new ZipInputStream(fs.open(finalPath)); 
    while ((ze = zin.getNextEntry()) != null) { 
      BufferedReader reader = new BufferedReader(new InputStreamReader(zin, encoding));
      String line = reader.readLine();
      while (line != null) {
        result.add(line);
        line = reader.readLine();
      }
      zin.closeEntry(); 
    }
    zin.close();
    return result;
  }

  protected List<String> getExpectedCSVRowsWithMultiple() {
    List<String> csvRows = new ArrayList<String>();
    csvRows.add("\"firstcol\",\"secondcol\",\"thirdcol\",\"fourthcol\",\"fifthcol\",\"sixthcol\",\"seventhcol\"");
    csvRows.add("\"1\",\"one\",\"one\",\"one\",\"1\",\"1:one\",\"1=one\"");
    csvRows.add("\"2\",\"two\",\"two\",\"two\",\"1,2\",\"2:two\",\"1=one,2=two\"");
    csvRows.add("\"firstcol\",\"secondcol\",\"thirdcol\",\"fourthcol\",\"fifthcol\",\"sixthcol\",\"seventhcol\"");
    csvRows.add("\"NULL\",\"three\",\"three\",\"three\",\"1,2,NULL\",\"NULL:three\",\"1=one,2=two,NULL=three\"");
    csvRows.add("\"4\",\"NULL\",\"NULL\",\"NULL\",\"1,2,NULL,4\",\"4:NULL\",\"1=one,2=two,NULL=three,4=NULL\"");
    csvRows.add("\"firstcol\",\"secondcol\",\"thirdcol\",\"fourthcol\",\"fifthcol\",\"sixthcol\",\"seventhcol\"");
    csvRows.add("\"NULL\",\"NULL\",\"NULL\",\"NULL\",\"1,2,NULL,4,NULL\",\"NULL:NULL\",\"1=one,2=two,NULL=three,4=NULL,5=NULL\"");
    csvRows.add("Total rows:5");
    return csvRows;
  }

  protected List<String> getExpectedTextRowsWithMultiple() {
    List<String> txtRows = new ArrayList<String>();
    txtRows.add("firstcolsecondcolthirdcolfourthcolfifthcolsixthcolseventhcol");
    txtRows.add("1oneoneone            11one1one       ");
    txtRows.add("2twotwotwo            122two1one       2two       ");
    txtRows.add("firstcolsecondcolthirdcolfourthcolfifthcolsixthcolseventhcol");
    txtRows.add("\\Nthreethreethree          12\\N\\Nthree1one       2two       \\Nthree     ");
    txtRows.add("4\\N\\N\\N12\\N44\\N1one       2two       \\Nthree     4\\N");
    txtRows.add("firstcolsecondcolthirdcolfourthcolfifthcolsixthcolseventhcol");
    txtRows.add("\\N\\N\\N\\N12\\N4\\N\\N\\N1one       2two       \\Nthree     4\\N5\\N");
    txtRows.add("Total rows:5");
    return txtRows;
  }

}
