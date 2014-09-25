package com.inmobi.grill.server.query.rewrite.dsl;

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

import com.inmobi.grill.server.api.query.rewrite.QueryCommand;
import com.inmobi.grill.server.api.query.rewrite.dsl.DSL;
import com.inmobi.grill.server.api.query.rewrite.dsl.DSLCommand;
import com.inmobi.grill.server.api.query.rewrite.dsl.DSLSemanticException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.metadata.AuthorizationException;
import org.apache.hadoop.hive.ql.parse.ParseException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DSLCommandImpl extends DSLCommand {

  static Pattern domainPattern = Pattern.compile(".*DOMAIN\\sSELECT.*",
      Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
  static Matcher matcher = null;

  public static final Log LOG = LogFactory.getLog(DSLCommandImpl.class);

  public DSLCommandImpl() {
    super();
  }

  public DSLCommandImpl(String command, String submittedUser, Configuration conf) {
    super(command, submittedUser, conf);
  }

  @Override
  public boolean matches(String line) {
    if (matcher == null) {
      matcher = domainPattern.matcher(line);
    } else {
      matcher.reset(line);
    }
    return matcher.matches();
  }

  @Override
  public Type getType() {
    return Type.DOMAIN;
  }


  @Override
  public QueryCommand rewrite() throws DSLSemanticException {
    QueryCommand query = null;

    final Collection<DSL> DSLs = DSLRegistry.getInstance().getDSLs();
    DSLSemanticException dslException = new DSLSemanticException("No Domain accepted the query due to : ");
    for (DSL dsl : DSLs) {
      try {
        if (dsl.accept(this)) {
          query = dsl.rewrite(this);
          break;
        }
      } catch (ParseException pe) {
        LOG.warn("Domain could not parse the DSL : " + dsl.getName(), pe);
        dslException.addDSLRewriteError(pe.getLocalizedMessage());
      }  catch (AuthorizationException pe) {
        LOG.warn("Domain could not parse the DSL : " + dsl.getName(), pe);
        dslException.addDSLRewriteError(pe.getLocalizedMessage());
      }
    }
    if (query == null) {
      throw dslException;
    }
    return query;
  }
}