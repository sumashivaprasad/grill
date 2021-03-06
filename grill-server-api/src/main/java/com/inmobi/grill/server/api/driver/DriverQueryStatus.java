package com.inmobi.grill.server.api.driver;

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

import java.io.Serializable;

import com.inmobi.grill.api.query.QueryStatus;

import lombok.Getter;
import lombok.Setter;

public class DriverQueryStatus implements Serializable {
  
  private static final long serialVersionUID = 1L;

  public enum DriverQueryState {
    NEW,
    INITIALIZED,
    PENDING,
    RUNNING,
    SUCCESSFUL,
    FAILED,
    CANCELED,
    CLOSED
  }
  
  @Getter @Setter private double progress = 0.0f;
  @Getter @Setter private DriverQueryState state = DriverQueryState.NEW;
  @Getter @Setter private String statusMessage;
  @Getter @Setter private boolean isResultSetAvailable = false;
  @Getter @Setter private String progressMessage;
  @Getter @Setter private String errorMessage;
  @Getter @Setter private Long driverStartTime = 0L;
  @Getter @Setter private Long driverFinishTime = 0L;
  
  public QueryStatus toQueryStatus() {
    QueryStatus.Status qstate = null;
    switch (state) {
    case NEW:
    case INITIALIZED:
    case PENDING:
      qstate = QueryStatus.Status.LAUNCHED;
      break;
    case RUNNING:
      qstate = QueryStatus.Status.RUNNING;
      break;
    case SUCCESSFUL:
      qstate = QueryStatus.Status.EXECUTED;
      break;      
    case FAILED:
      qstate = QueryStatus.Status.FAILED;
      break;
    case CANCELED:
      qstate = QueryStatus.Status.CANCELED;
      break;
    case CLOSED:
      qstate = QueryStatus.Status.CLOSED;
      break;
    }
    
    return new QueryStatus(progress, qstate, statusMessage, isResultSetAvailable, progressMessage, errorMessage);
  }

  public static QueryStatus createQueryStatus(QueryStatus.Status state,
      DriverQueryStatus dstatus) {
    return new QueryStatus(dstatus.progress, state, dstatus.statusMessage,
        dstatus.isResultSetAvailable, dstatus.progressMessage,
        dstatus.errorMessage);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder(state.toString()).append(':').
    append(statusMessage);
    if (state.equals(DriverQueryState.RUNNING)) {
      str.append(" - Progress:").append(progress).append(":").append(progressMessage);
    }
    if (state.equals(DriverQueryState.SUCCESSFUL)) {
      if (isResultSetAvailable) {
        str.append(" - Result Available");
      } else {
        str.append(" - Result Not Available");
      }
    }
    if (state.equals(DriverQueryState.FAILED)) {
      str.append(" - Cause:").append(errorMessage);
    }
    return str.toString();
  }

  public boolean isFinished() {
    return state.equals(DriverQueryState.SUCCESSFUL) || state.equals(DriverQueryState.FAILED) ||
        state.equals(DriverQueryState.CANCELED) || state.equals(DriverQueryState.CLOSED);
  }

}
