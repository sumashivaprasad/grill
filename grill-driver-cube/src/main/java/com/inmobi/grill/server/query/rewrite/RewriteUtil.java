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

import java.util.Collection;
import java.util.Map;
import com.inmobi.grill.api.GrillException;
import com.inmobi.grill.server.api.query.*;
import com.inmobi.grill.server.api.query.rewrite.HQLCommand;
import com.inmobi.grill.server.api.query.rewrite.QueryCommand;
import org.apache.hadoop.conf.Configuration;
import com.inmobi.grill.server.api.driver.GrillDriver;

public class RewriteUtil {

  public static Map<GrillDriver, HQLCommand> rewriteQuery(QueryContext ctx, Collection<GrillDriver> drivers) throws GrillException {
    DriverSpecificQueryRewrite rewriter = new DriverSpecificQueryRewriterImpl(ctx);
    return rewrite(rewriter, ctx.getUserQuery(), ctx.getSubmittedUser(), ctx.getConf(), drivers);
  }

  public static Map<GrillDriver, HQLCommand> rewriteQuery(PreparedQueryContext ctx, Collection<GrillDriver> drivers) throws GrillException {
    DriverSpecificQueryRewrite rewriter = new DriverSpecificQueryRewriterImpl(ctx);
    return rewrite(rewriter, ctx.getUserQuery(), ctx.getPreparedUser(), ctx.getConf(), drivers);
  }

  public static  Map<GrillDriver, HQLCommand> rewriteQuery(String q1, String userName, Configuration conf, Collection<GrillDriver> drivers) throws GrillException {
    DriverSpecificQueryRewrite rewriter = new DriverSpecificQueryRewriterImpl();
    return rewrite(rewriter, q1, userName, conf, drivers);
  }

  private static Map<GrillDriver,HQLCommand> rewrite(DriverSpecificQueryRewrite rewriter, String q1, String userName, Configuration conf, Collection<GrillDriver> drivers) throws GrillException {
    final QueryCommand queryCommand = QueryCommands.get(q1, userName, conf);
    return rewriter.rewrite(queryCommand, drivers);
  }
}
