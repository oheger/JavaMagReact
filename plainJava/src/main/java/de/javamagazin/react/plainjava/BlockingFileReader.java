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
package de.javamagazin.react.plainjava;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Example class for reading a file in the traditional blocking style. The
 * path to be read is expected to be passed as single command line argument.
 */
public class BlockingFileReader {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: BlockingFileReader <path>");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        System.out.println("Reading file " + path);

        long startTime = System.currentTimeMillis();
        byte[] bytes = Files.readAllBytes(path);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println(new String(bytes));
        System.out.println("Read " + bytes.length + " bytes in " + duration + " ms.");
    }
}
