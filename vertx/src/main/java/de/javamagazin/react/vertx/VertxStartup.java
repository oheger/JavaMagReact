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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.ext.shell.ShellService;
import io.vertx.ext.shell.ShellServiceOptions;
import io.vertx.ext.shell.term.TelnetTermOptions;

/**
 * Main class starting up Vertx and deploying the demo verticles.
 */
public class VertxStartup {
    public static void main(String[] args) throws InterruptedException {
        Vertx vertx = Vertx.vertx();
        int port = (args.length > 0) ? Integer.parseInt(args[0]) : 4000;
        ShellService service = ShellService.create(vertx,
                new ShellServiceOptions()
                        .setTelnetOptions(new TelnetTermOptions()
                                .setHost("localhost")
                                .setPort(port)));
        service.start();
        System.out.println("Vertx shell available on telnet port " + port);

        System.out.println("Deploying test verticles.");
        BlockingQueue<AsyncResult<CompositeFuture>> queue = new SynchronousQueue<>();
        Future<String> res1 = deployVerticle(vertx, ReadFileVerticle.class);
        Future<String> res2 = deployVerticle(vertx, ProcessFileVerticleCB.class);
        CompositeFuture.all(res1, res2).setHandler(queue::offer);
        AsyncResult<CompositeFuture> deployResult = queue.poll(10, TimeUnit.SECONDS);
        if (deployResult == null) {
            System.out.println("Timeout on deployment!");
            vertx.close(VertxStartup::closeHandler);
        } else {
            if (deployResult.failed()) {
                deployResult.cause().printStackTrace();
                System.out.println("Deployment failed!");
                vertx.close(VertxStartup::closeHandler);
            } else {
                System.out.println("Deployment completed.");
            }
        }
    }

    /**
     * Deploys a verticle and returns a future for the result.
     *
     * @param vert    the {@code Vertx} instance
     * @param vertCls the class of the verticle to be deployed
     * @param <T>     the type
     * @return a future with the deployment result
     */
    private static <T extends Verticle> Future<String> deployVerticle(Vertx vert, Class<T> vertCls) {
        System.out.println("Deploying verticle " + vertCls.getName());
        Future<String> future = Future.future();
        vert.deployVerticle(vertCls.getName(), future);
        return future;
    }

    /**
     * Handler for closing Vertx.
     *
     * @param result the result of the close operation
     */
    private static void closeHandler(AsyncResult<Void> result) {
        if (result.succeeded()) {
            System.out.println("Closed Vertx");
        } else {
            result.cause().printStackTrace();
            System.out.println("Could not close Vertx!");
        }
    }
}
