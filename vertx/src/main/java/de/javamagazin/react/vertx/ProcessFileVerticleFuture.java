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
import java.io.IOException;

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
 * This class performs the same operations as {@link ProcessFileVerticleCB},
 * but this time futures are used to chain the steps rather than nested
 * callbacks.
 * </p>
 */
public class ProcessFileVerticleFuture extends AbstractVerticle {
    /**
     * Event bus address this verticle listens on.
     */
    public static final String ADDR_PROCESS_FILE = "react.file.process.future";

    /**
     * The logger.
     */
    private static Logger LOG = LoggerFactory.getLogger(ProcessFileVerticleFuture.class);

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        LOG.info("Starting ProcessFileVerticleFuture");
        vertx.eventBus().consumer(ADDR_PROCESS_FILE, this::processFile);
        startFuture.complete();
    }

    private void processFile(Message<Object> msg) {
        String path = String.valueOf(msg.body());
        LOG.info("Processing file " + path);
        String outPath = path + ".processed";

        existsFile(outPath)
                .compose(res -> !res ? Future.succeededFuture() :
                        Future.failedFuture(new IOException("File already exists")))
                .compose(v -> readFile(path))
                .map(b -> Buffer.buffer(DatatypeConverter.printHexBinary(b.getBytes())))
                .compose(buf -> writeFile(outPath, buf))
                .setHandler(res -> sendResponse(msg, res.succeeded(),
                        res.succeeded() ? "Generated " + outPath : res.cause().getMessage()));
    }

    /**
     * Checks whether a file exists and returns a Future with the result.
     *
     * @param path the path of the file
     * @return A future with the result of the exists check
     */
    private Future<Boolean> existsFile(String path) {
        Future<Boolean> result = Future.future();
        vertx.fileSystem().exists(path, result);
        return result;
    }

    /**
     * Reads a file and returns a Future with the result.
     *
     * @param path the path of the file
     * @return a Future with the data read from the file
     */
    private Future<Buffer> readFile(String path) {
        Future<Buffer> result = Future.future();
        vertx.fileSystem().readFile(path, result);
        return result;
    }

    /**
     * Writes data to a file and returns a Future with the result.
     *
     * @param outPath the path of the file
     * @param data    the data to write
     * @return a Future with the write result
     */
    private Future<Void> writeFile(String outPath, Buffer data) {
        Future<Void> result = Future.future();
        vertx.fileSystem().writeFile(outPath, data, result);
        return result;
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
