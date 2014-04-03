/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <http://www.spout.org/>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.flowpowered.commons.graph;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Graph<C> {
    private final Map<String, Node<C>> nodes = new HashMap<>();
    private final Set<Stage> stages = new TreeSet<>();

    public void addNode(Node<C> node) {
        nodes.put(node.getName(), node);
    }

    public Node<C> getNode(String name) {
        return nodes.get(name);
    }

    public boolean hasNode(String name) {
        return nodes.containsKey(name);
    }

    public boolean hasNode(Node<C> node) {
        return hasNode(node.getName());
    }

    public void removeNode(String name) {
        nodes.remove(name);
    }

    public void removeNode(Node<C> node) {
        removeNode(node.getName());
    }

    public void build() {
        stages.clear();
        final Set<Node<C>> toBuild = new HashSet<>(nodes.values());
        final Set<Node<C>> previous = new HashSet<>();
        int i = 0;
        Stage current = new Stage(i++);
        while (true) {
            for (Iterator<Node<C>> iterator = toBuild.iterator(); iterator.hasNext(); ) {
                final Node node = iterator.next();
                if (previous.containsAll(node.getParents())) {
                    current.addNode(node);
                    iterator.remove();
                }
            }
            if (current.getNodes().isEmpty()) {
                break;
            }
            previous.addAll(current.getNodes());
            stages.add(current);
            if (toBuild.isEmpty()) {
                break;
            }
            current = new Stage(i++);
        }
        for (Node<C> node : nodes.values()) {

        }
    }

    public void execute() {
        for (Stage stage : stages) {
            stage.execute();
        }
    }

    public <T> void set(Node nodeName, String name, T value) {
    }

    public <T> void setAll(String name, T value) {
    }

    private static class Stage implements Comparable<Stage> {
        private final Set<Node> nodes = new HashSet<>();
        private final int number;

        private Stage(int number) {
            this.number = number;
        }

        private void addNode(Node node) {
            nodes.add(node);
        }

        public Set<Node> getNodes() {
            return nodes;
        }

        private void execute() {
            for (Node node : nodes) {
                node.execute();
            }
        }

        private int getNumber() {
            return number;
        }

        @Override
        public int compareTo(Stage o) {
            return number - o.getNumber();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Input {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Output {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface InputLink {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface OutputLink {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface InputConnect {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface OutputConnect {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Setting {
        String value();
    }
}
