package com.inmobi.grill.cli.commands;

/*
 * #%L
 * Grill CLI
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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.inmobi.grill.api.APIResult;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class GrillStorageCommands extends  BaseGrillCommand implements CommandMarker {

  @CliCommand(value = "show storages", help = "list storages")
  public String getStorages() {
    List<String> storages = getClient().getAllStorages();
    if(storages == null || storages.isEmpty()) {
      return "No storages found";
    }
    return Joiner.on("\n").join(storages);
  }

  @CliCommand(value = "create storage", help = "Create a new Storage")
  public String createStorage(@CliOption(key = {"", "storage"},
      mandatory = true, help = "<path to storage-spec>") String storageSpec) {
    File f = new File(storageSpec);
    if (!f.exists()) {
      return "cube spec path"
          + f.getAbsolutePath()
          + " does not exist. Please check the path";
    }
    APIResult result = getClient().createStorage(storageSpec);
    return result.getMessage();
  }

  @CliCommand(value = "drop storage", help = "drop storage")
  public String dropStorage(@CliOption(key = {"", "storage"},
      mandatory = true, help = "storage name to be dropped") String storage) {
    APIResult result = getClient().dropStorage(storage);
    if (result.getStatus() == APIResult.Status.SUCCEEDED) {
      return "Successfully dropped " + storage + "!!!";
    } else {
      return "Dropping storage failed";
    }
  }

  @CliCommand(value = "update storage", help = "update storage")
  public String updateStorage(@CliOption(key = {"", "storage"}, mandatory = true, help = "<storage-name> <path to storage-spec>") String specPair) {
    Iterable<String> parts = Splitter.on(' ')
        .trimResults()
        .omitEmptyStrings()
        .split(specPair);
    String[] pair = Iterables.toArray(parts, String.class);
    if (pair.length != 2) {
      return "Syntax error, please try in following " +
          "format. create fact <fact spec path> <storage spec path>";
    }

    File f = new File(pair[1]);

    if (!f.exists()) {
      return "Fact spec path"
          + f.getAbsolutePath()
          + " does not exist. Please check the path";
    }

    APIResult result = getClient().updateStorage(pair[0], pair[1]);
    if (result.getStatus() == APIResult.Status.SUCCEEDED) {
      return "Update of " + pair[0] + " succeeded";
    } else {
      return "Update of " + pair[0] + " failed";
    }
  }

  @CliCommand(value = "describe storage", help = "describe storage schema")
  public String describeStorage(@CliOption(key = {"", "storage"},
      mandatory = true, help = "<storage-name> to be described") String storage) {
    try {
      return formatJson(mapper.writer(pp).writeValueAsString(
          getClient().getStorage(storage)));
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
