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
package de.javamagazin.react.progmodel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * Class for reading the content of a text file in a single operation using
 * Java's {@code AsynchronousFileChannel}.
 * </p>
 */
public class AsyncFileReader {
    /**
     * Buffer size for read operations.
     */
    private static final int BUF_SIZE = 1024;

    /**
     * Initial size of the content buffer.
     */
    private static final int CONTENT_BUFFER = 8192;

    /**
     * The completion handler used by this instance.
     */
    private final CompletionHandler<Integer, ReadContext> handler = createHandler();

    public CompletableFuture<String> readFile(Path path) {
        CompletableFuture<String> future = new CompletableFuture<>();
        try {
            AsynchronousFileChannel channel = AsynchronousFileChannel.open(path,
                    StandardOpenOption.READ);
            ReadContext context = new ReadContext(channel, future);
            readBlock(context);
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    /**
     * Triggers another read operation for a chunk of data.
     *
     * @param context the read context
     */
    private void readBlock(ReadContext context) {
        context.buffer.clear();
        context.channel.read(context.buffer, context.position, context, handler);
    }

    /**
     * Creates a handler to process the results of a read operation.
     *
     * @return the handler
     */
    private CompletionHandler<Integer, ReadContext> createHandler() {
        return new CompletionHandler<Integer, ReadContext>() {
            @Override
            public void completed(Integer count, ReadContext context) {
                System.out.print('.');
                if (count < 0) {
                    context.close();
                    context.future.complete(context.content.toString());
                } else {
                    context.buffer.flip();
                    byte[] data = new byte[count];
                    context.buffer.get(data);
                    context.content.append(new String(data));
                    context.position += count;
                    readBlock(context);
                }
            }

            @Override
            public void failed(Throwable exc, ReadContext context) {
                context.fail(exc);
            }
        };
    }

    /**
     * Internal data class holding context information for a file read
     * operation.
     */
    private static class ReadContext {
        /**
         * The channel for the read operation.
         */
        private final AsynchronousFileChannel channel;

        /**
         * The resulting future.
         */
        private final CompletableFuture<String> future;

        /**
         * Buffer for read operations.
         */
        private final ByteBuffer buffer;

        /**
         * The aggregated text content.
         */
        private final StringBuilder content;

        /**
         * The current read position.
         */
        private int position;

        public ReadContext(AsynchronousFileChannel c, CompletableFuture<String> f) {
            channel = c;
            future = f;
            content = new StringBuilder(CONTENT_BUFFER);
            buffer = ByteBuffer.allocate(BUF_SIZE);
        }

        /**
         * Marks this read operation as failed. The context is also closed.
         *
         * @param ex the exception
         */
        public void fail(Throwable ex) {
            future.completeExceptionally(ex);
            close();
        }

        /**
         * Closes the context. Especially the channel is closed.
         */
        public void close() {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
