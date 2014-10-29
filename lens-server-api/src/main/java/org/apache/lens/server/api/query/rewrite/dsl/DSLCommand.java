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
package org.apache.lens.server.api.query.rewrite.dsl;

import org.apache.lens.server.api.query.rewrite.QueryCommand;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;

public abstract class DSLCommand extends QueryCommand {

  public static final String DSL_PREFIX = "DOMAIN";

  protected DSLCommand(String input, String userName, Configuration conf) {
    super(input, userName, conf);
  }

  protected DSLCommand() {
  }

  @Override
  public Type getType() {
    return Type.DOMAIN;
  }

  public static DSLCommand get(String input, String userName, Configuration conf) {
     return new DSLCommand(input, userName, conf) {
       @Override
       public boolean matches(String line) {
         return true;
       }

       @Override
       public QueryCommand rewrite() throws DSLSemanticException {
         return null;
       }

     };
  }

  @Override
  public boolean matches(String line) {
    return StringUtils.startsWithIgnoreCase(line, DSL_PREFIX);
  }

}