package ch.nerdin.minecraft;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
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
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jgit.api.Git;

/**
 * Goal sends a patch to the running minecraft server so that it can be hot deployed.
 */
@Mojo(name = "sync", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class SyncMojo extends AbstractMojo {

    /**
     * Location of the sync server.
     */
    @Parameter(required = true)
    private URI serverUri;

    @Parameter(defaultValue = ".", required = true)
    private File repository;

    @Override
    public void execute() throws MojoExecutionException {
        WebSocketClient client = new WebSocketClient();
        SyncSocket socket = new SyncSocket();

        try (Git git = Git.open(repository)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            git.diff().setOutputStream(out).call();
            String diff = out.toString();

            socket.add(diff);

            client.start();
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, serverUri, request);
        } catch (Exception e) {
            throw new MojoExecutionException("could not sync with server due to exception", e);
        }

        if (!socket.awaitClose(20, TimeUnit.SECONDS)) {
            throw new MojoExecutionException("could not sync, server reply timed out");
        }
        try {
            client.stop();
        } catch (Exception e) {
            // Shut up.
        }
    }
}
