/*
 * Copyright 2012 selendroid committers.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openqa.selendroid.server;

import org.openqa.selendroid.ServerInstrumentation;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;

public class AndroidServer {
  private int driverPort = 8080;
  private WebServer webServer;

  /** for testing only */
  protected AndroidServer(int port, ServerInstrumentation androidInstrumentation) {
    this.driverPort = port;
    init(androidInstrumentation);
  }

  public AndroidServer(ServerInstrumentation androidInstrumentation) {
    init(androidInstrumentation);
  }

  protected void init(ServerInstrumentation androidInstrumentation) {
    webServer = WebServers.createWebServer(driverPort);
    webServer.add(new AndroidServlet(createAndroidDriver(androidInstrumentation)));
  }

  protected AndroidDriver createAndroidDriver(ServerInstrumentation androidInstrumentation) {
    return new AndroidNativeDriver(androidInstrumentation);
  }

  public void start() {
    webServer.start();
  }

  public void stop() {
    webServer.stop();
  }

  public int getPort() {
    return webServer.getPort();
  }
}