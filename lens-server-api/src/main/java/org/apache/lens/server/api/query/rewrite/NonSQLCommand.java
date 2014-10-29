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
package org.apache.lens.server.api.query.rewrite;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;

/**
 * NonSQL Commands that is rewritten to Driver specific HQLCommand
 */
public abstract class NonSQLCommand extends QueryCommand {

  protected NonSQLCommand(String input, String userName, Configuration conf) {
    super(input, userName, conf);
  }

  protected NonSQLCommand() {
  }

  @Override
  public Type getType() {
    return Type.NONSQL;
  }

  @Override
  public boolean matches(String cmd) {
   return StringUtils.startsWithIgnoreCase(cmd, "add") ||
        StringUtils.startsWithIgnoreCase(cmd, "set");
  }

  public static NonSQLCommand get(String input, String userName, Configuration conf) {
    return new NonSQLCommand(input, userName, conf) {
      @Override
      public boolean matches(String line) {
        return false;
      }

      @Override
      public HQLCommand rewrite() throws RewriteException {
        return null;
      }
    };
  }
}