/*
 * Copyright 2017 Oliver Heger.
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
package de.javamagazin.react.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * <p>
 * Simple verticle that demonstrates reading a file using the Vertx API.
 * </p>
 * <p>
 * The class listens on an address on the message bus for incoming requests
 * to read specific paths. The files are then read (and simply dumped to the
 * console).
 * </p>
 */
public class ReadFileVerticle extends AbstractVerticle {
    /**
     * Event bus address this verticle listens on.
     */
    public static final String ADDR_READ_FILE = "react.file.read";

    /**
     * The logger.
     */
    private static Logger LOG = LoggerFactory.getLogger(ReadFileVerticle.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting ReadFileVerticle.");
        vertx.eventBus().consumer(ADDR_READ_FILE, this::handleReadMessage);
        startFuture.complete();
    }

    /**
     * Handles a message to read a file. The message is interpreted as path
     * name of a file on the local file system.
     *
     * @param msg the message to be handled
     */
    private void handleReadMessage(Message<Object> msg) {
        String path = String.valueOf(msg.body());
        LOG.info("Reading file " + path);
        readFile(path);
    }

    /**
     * Reads and prints a file.
     *
     * @param path the path to the file to be read
     */
    private void readFile(String path) {
        final long startTime = System.currentTimeMillis();
        vertx.fileSystem().readFile(path, res -> {
            final long duration = System.currentTimeMillis() - startTime;
            if (res.succeeded()) {
                String content = res.result().toString();
                LOG.info(content);
                LOG.info("Read " + content.length() + " bytes in " + duration + " ms.");
            } else {
                LOG.error("Reading file failed!", res.cause());
            }
        });
    }
}
