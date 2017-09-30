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

import javax.xml.bind.DatatypeConverter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * <p>
 * A demo verticle that simulates file processing.
 * </p>
 * <p>
 * This class listens on the message bus for requests to process a file. The
 * specified file is read in a block, base-64 encoded, and stored in an output
 * path. (If the output file already exists, the operation fails.) The caller
 * then gets a response with the status of the operation.
 * </p>
 * <p>
 * This example shows the nesting of multiple callbacks.
 * </p>
 */
public class ProcessFileVerticleCB extends AbstractVerticle {
    /**
     * Event bus address this verticle listens on.
     */
    public static final String ADDR_PROCESS_FILE = "react.file.process";

    /**
     * The logger.
     */
    private static Logger LOG = LoggerFactory.getLogger(ProcessFileVerticleCB.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting ProcessFileVerticleCB");
        vertx.eventBus().consumer(ADDR_PROCESS_FILE, this::processFile);
        startFuture.complete();
    }

    private void processFile(Message<Object> msg) {
        String path = String.valueOf(msg.body());
        LOG.info("Processing file " + path);
        String outPath = path + ".processed";

        vertx.fileSystem().exists(outPath, rEx -> {
            if (rEx.failed()) {
                sendResponse(msg, false, "Exists check failed");
            } else if (rEx.result()) {
                sendResponse(msg, false, "File already exists");
            } else {
                vertx.fileSystem().readFile(path, rRead -> {
                    if (rRead.failed()) {
                        sendResponse(msg, false, "Read failed: " + rRead.cause());
                    } else {
                        byte[] content = rRead.result().getBytes();
                        String encoded = DatatypeConverter.printBase64Binary(content);
                        vertx.fileSystem().writeFile(outPath, Buffer.buffer(encoded), rWrt -> {
                            if (rWrt.succeeded()) {
                                sendResponse(msg, true, "Generated " + outPath);
                            } else {
                                sendResponse(msg, false, "Write failed: " + rWrt.cause());
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Sends a response message to a caller. A JSON result based on the given
     * parameters is passed to the message.
     *
     * @param msg     the message to be answered
     * @param success the success flag
     * @param txt     the status text
     */
    private void sendResponse(Message<?> msg, boolean success, String txt) {
        JsonObject result = new JsonObject()
                .put("result", success)
                .put("message", txt);
        String msgStr = result.encodePrettily();
        LOG.info("Sending response: " + msgStr);
        msg.reply(msgStr);
    }
}
