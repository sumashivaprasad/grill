package com.inmobi.grill.server.query.rewrite;

/*
 * #%L
 * Grill Cube Driver
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

import com.inmobi.grill.server.api.query.rewrite.HQLCommand;
import com.inmobi.grill.server.api.query.rewrite.QueryCommand;
import com.inmobi.grill.server.api.query.rewrite.RewriteException;
import org.apache.hadoop.conf.Configuration;

public class HQLCommandImpl extends HQLCommand {

  public HQLCommandImpl() {
  }

  public HQLCommandImpl(String command, String userName, Configuration conf) {
    super(command, userName, conf);
  }

  @Override
  public QueryCommand rewrite() throws RewriteException {
    return this;
  }

  public HQLCommandImpl(QueryCommand queryCommand) {
    super(queryCommand.getCommand(), queryCommand.getUserName(), queryCommand.getConf());
  }

}