package com.inmobi.grill.server.api.query.rewrite.dsl;

/*
 * #%L
 * Grill API for server and extensions
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
import com.inmobi.grill.server.query.rewrite.ParseException;
import org.apache.hadoop.hive.ql.metadata.AuthorizationException;

/**
 *  Grill server can accept a registered Domain Specific Language.
 *  The Parsing and rewriting of the DSL is the responsibility of the DSL implementation
 *  and can be rewritten to CubeQL/HQL
 */
public interface DSL {

  /**
   *
   * @return The DSL identifier
   */
  String getName();

  /**
   *
   * @param command the query to be rewritten
   * @return false if DSL does not accept the query
   * @throws ParseException when DSL is not able to parse the given query
   * @throws AuthorizationException thrown when user is not authorized to submit the DSL query
   */
  boolean accept(DSLCommand command) throws ParseException, AuthorizationException;

  /**
   *
   * @param command the query to be rewritten
   * @return the rewritten query - CubeQL/HQL
   * @throws ParseException
   * @throws AuthorizationException thrown when user is not authorized to submit the DSL query
   */
   QueryCommand rewrite(DSLCommand command) throws DSLSemanticException;

}
