/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package com.google.cloud.tools.libraries;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

public class LibrariesTest {

  @Test
  public void testWellFormed() throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder parser = factory.newDocumentBuilder();
    File in = new File("src/main/java/com/google/cloud/tools/libraries/libraries.xml");
    parser.parse(in);
  }
  
  @Test
  public void testJson() throws FileNotFoundException, URISyntaxException {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    InputStream in =
        new FileInputStream("src/main/java/com/google/cloud/tools/libraries/libraries.json");
    JsonReader reader = factory.createReader(in); 
    Iterator<JsonValue> apis = reader.readArray().iterator();
    Assert.assertTrue(apis.hasNext());
    for (JsonObject api = (JsonObject) apis.next(); apis.hasNext(); api = (JsonObject) apis.next()) {
      verifyApi(api);
    }
  }

  private static final String[] statuses = {"alpha", "beta", "GA"};

  private static void verifyApi(JsonObject api) throws URISyntaxException {
    Assert.assertFalse(api.getString("name").isEmpty());
    Assert.assertFalse(api.getString("description").isEmpty());
    String transport = api.getString("transport");
    Assert.assertTrue("http".equals(transport) || "grpc".equals(transport));
    new URI(api.getString("documentation"));
    new URI(api.getString("site"));
    if (api.getString("icon") != null) {
      new URI(api.getString("icon"));
    }
    JsonArray clients = api.getJsonArray("clients");
    Assert.assertFalse(clients.isEmpty());
    for (int i = 0; i < clients.size(); i++) {
      JsonObject client = (JsonObject) clients.get(i);
      String status = client.getString("status");
      
      Assert.assertThat(statuses, hasItemInArray(status));
      new URI(client.getString("apireference"));
      Assert.assertTrue("1.7.0".equals(client.getString("languageLevel")));
      Assert.assertFalse(client.getString("version").isEmpty());  // todo regex
      Assert.assertNotNull(client.getJsonObject("mavenCoordinates"));
      if (client.getString("source") != null) {
        new URI(client.getString("source"));
      }
    }
  }
  
  @Test
  public void testMavenCoordinates() throws FileNotFoundException, URISyntaxException {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    InputStream in =
        new FileInputStream("src/main/java/com/google/cloud/tools/libraries/libraries.json");
    JsonReader reader = factory.createReader(in); 
    Iterator<JsonValue> apis = reader.readArray().iterator();
    Assert.assertTrue(apis.hasNext());
    
    Map<String, MavenCoordinates> map = new HashMap<>();
    for (JsonObject api = (JsonObject) apis.next(); apis.hasNext(); api = (JsonObject) apis.next()) {
      String name = api.getString("name");
      if (map.containsKey(name)) {
        Assert.fail(name + " is defined twice");
      }
      JsonObject coordinates =
          ((JsonObject) api.getJsonArray("clients").get(0)).getJsonObject("mavenCoordinates");
      MavenCoordinates mavenCoordinates = new MavenCoordinates.Builder()
          .setGroupId(coordinates.getString("groupId"))
          .setArtifactId(coordinates.getString("artifactId"))
          .setVersion(coordinates.getString("version"))
          .build();
      if (map.containsValue(mavenCoordinates)) {
        Assert.fail(mavenCoordinates + " is defined twice");
      }
      map.put(name, mavenCoordinates);
    }
  }

}
