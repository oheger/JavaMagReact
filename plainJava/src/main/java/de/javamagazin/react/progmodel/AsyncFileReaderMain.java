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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Main class for reading a file using {@link AsyncFileReader}. The path to be
 * read is expected to be passed as single command line argument.
 */
public class AsyncFileReaderMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: AsyncFileReaderMain <path>");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        System.out.println("Reading file " + path);

        AsyncFileReader reader = new AsyncFileReader();
        long startTime = System.currentTimeMillis();
        CompletableFuture<String> future = reader.readFile(path);
        System.out.println("Read in progress...");
        // Block to get the result of this test driver.
        // This is of course no reactive style!
        String s = future.get(30, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\n" + s);
        System.out.println("Read " + s.length() + " bytes in " + duration + " ms.");
    }
}
