/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.lens.server.query.rewrite;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.cube.parse.CubeQueryRewriter;
import org.apache.lens.driver.cube.MockDriver;
import org.apache.lens.server.api.LensConfConstants;
import org.apache.lens.server.api.driver.LensDriver;
import org.apache.lens.server.api.query.rewrite.QueryCommand;
import org.apache.lens.server.query.rewrite.dsl.DSLRegistry;
import org.apache.lens.server.query.rewrite.dsl.TestDSL;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@PrepareForTest({CubeQLCommandImpl.class, DriverSpecificQueryRewriterImpl.class})
@PowerMockIgnore({"org.apache.log4j.*", "javax.management.*", "javax.xml.*",
    "com.sun.org.apache.xerces.internal.jaxp.*", "ch.qos.logback.*", "org.slf4j.*", "org.w3c.dom*"})
public class TestDriverSpecificRewriter {

  DriverSpecificQueryRewrite rewriter;
  List<LensDriver> drivers = new ArrayList<LensDriver>();
  HiveConf conf = new HiveConf();
  MockDriver driver = new MockDriver();
  DSLRegistry registry;

  /**
   * We need a special {@link org.testng.IObjectFactory}.
   *
   * @return {@link org.powermock.modules.testng.PowerMockObjectFactory}.
   */
  @ObjectFactory
  public IObjectFactory getObjectFactory() {
    return new org.powermock.modules.testng.PowerMockObjectFactory();
  }

  @BeforeTest
  public void beforeTest() throws Exception {
    driver.configure(conf);
    drivers.add(driver);
    conf.set(LensConfConstants.QUERY_REWRITER, DriverSpecificQueryRewriterImpl.class.getCanonicalName());
    conf.set(LensConfConstants.QUERY_DSLS, "test");
    conf.set("grill.query.test.dsl.impl", TestDSL.class.getCanonicalName());
    registry = DSLRegistry.getInstance();

  }


  @Test
  public void testNonSQLRewrite() throws Exception {
    rewriter = new DriverSpecificQueryRewriterImpl();
    final String TEST_COMMAND="add test.jar";
    final QueryCommand queryCommand = QueryCommands.get(TEST_COMMAND, null, conf);
    Assert.assertEquals(queryCommand.getType(), QueryCommand.Type.NONSQL);

    final Map<LensDriver, QueryCommand> rewrittenQuery = rewriter.rewrite(queryCommand, drivers);
    Assert.assertEquals(rewrittenQuery.get(driver).getCommand(), TEST_COMMAND);
  }

  @Test
  public void testDSLRewrite() throws Exception {

    CubeQueryRewriter mockWriter = TestRewriting.getMockedRewriter();
    final ClassLoader mockClassLoader = mockWriter.getClass().getClassLoader();
    PowerMockito.stub(PowerMockito.method(CubeQLCommandImpl.class, "getRewriter")).toReturn(mockWriter);
    registry.init(conf, mockClassLoader);
    rewriter = RewriteUtil.getQueryRewriter(conf, mockClassLoader);

    final String TEST_COMMAND="domain select * from domain_entity";
    final String REWRITTEN_COMMAND="select name from table";
    final QueryCommand queryCommand = QueryCommands.get(TEST_COMMAND, null, conf);
    Assert.assertEquals(queryCommand.getType(), QueryCommand.Type.DOMAIN);

    final Map<LensDriver, QueryCommand> rewrittenQuery = rewriter.rewrite(queryCommand, drivers);
    Assert.assertEquals(StringUtils.trim(rewrittenQuery.get(driver).getCommand()), REWRITTEN_COMMAND);
  }

  @Test
  public void testCubeRewrite() throws Exception {

    CubeQueryRewriter mockWriter = TestRewriting.getMockedRewriter();
    final ClassLoader mockClassLoader = mockWriter.getClass().getClassLoader();
    PowerMockito.stub(PowerMockito.method(CubeQLCommandImpl.class, "getRewriter")).toReturn(mockWriter);
    registry.init(conf, mockClassLoader);
    rewriter = RewriteUtil.getQueryRewriter(conf, mockClassLoader);

    final String TEST_COMMAND="cube select * from table";
    final String REWRITTEN_COMMAND="select * from table";
    final QueryCommand queryCommand = QueryCommands.get(TEST_COMMAND, null, conf);
    Assert.assertEquals(queryCommand.getType(), QueryCommand.Type.CUBE);

    final Map<LensDriver, QueryCommand> rewrittenQuery = rewriter.rewrite(queryCommand, drivers);
    Assert.assertEquals(StringUtils.trim(rewrittenQuery.get(driver).getCommand()), REWRITTEN_COMMAND);
  }

  /**
   *  HQL rewrite is a No-op.
   *
   */

  @Test
  public void testHQLRewrite() throws Exception {
    rewriter = new DriverSpecificQueryRewriterImpl();
    final String TEST_COMMAND="select * from table";
    final String REWRITTEN_COMMAND="select * from table";
    final QueryCommand queryCommand = QueryCommands.get(TEST_COMMAND, null, conf);
    Assert.assertEquals(queryCommand.getType(), QueryCommand.Type.HQL);

    final Map<LensDriver, QueryCommand> rewrittenQuery = rewriter.rewrite(queryCommand, drivers);
    Assert.assertEquals(rewrittenQuery.get(driver).getCommand(), REWRITTEN_COMMAND);
  }

}