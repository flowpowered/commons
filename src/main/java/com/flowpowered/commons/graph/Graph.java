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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class Graph<C> {
    private final Map<String, Node<C>> nodes = new HashMap<>();
    private final Set<Stage> stages = new TreeSet<>();
    private final Map<String, Links> nodeLinks = new HashMap<>();

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
        // Generate graph stages
        stages.clear();
        final Set<Node<C>> toBuild = new HashSet<>(nodes.values());
        final Set<Node<C>> previous = new HashSet<>();
        int i = 0;
        Stage current = new Stage(i++);
        while (true) {
            for (Iterator<Node<C>> iterator = toBuild.iterator(); iterator.hasNext(); ) {
                final Node<C> node = iterator.next();
                if (previous.containsAll(node.getParents().values())) {
                    current.addNode(node);
                    iterator.remove();
                }
            }
            final Set<Node<C>> currentNodes = current.getNodes();
            if (currentNodes.isEmpty()) {
                break;
            }
            previous.addAll(currentNodes);
            stages.add(current);
            if (toBuild.isEmpty()) {
                break;
            }
            current = new Stage(i++);
        }
        // Disconnect removed nodes
        for (Iterator<Entry<String, Links>> iterator = nodeLinks.entrySet().iterator(); iterator.hasNext(); ) {
            final Entry<String, Links> linksEntry = iterator.next();
            if (!nodes.containsKey(linksEntry.getKey())) {
                // Remove old node links
                iterator.remove();
                final Links links = linksEntry.getValue();
                final Map<String, String> outputs = links.getLinks();
                final Node<C> node = links.getNode();
                // Disconnect all broken links
                for (Entry<String, Node<C>> parent : links.getParents().entrySet()) {
                    final String input = parent.getKey();
                    node.disconnect(parent.getValue(), outputs.get(input), input);
                }
            }
        }
        // Update new and retained node connections
        for (Node<C> node : nodes.values()) {
            Links links = nodeLinks.get(node.getName());
            if (links == null) {
                // Create new node links
                links = new Links(node);
                nodeLinks.put(node.getName(), links);
                // Connect all new links
                for (String parent : links.getParents().keySet()) {
                    node.connect(parent);
                }
                continue;
            }
            // Update links
            final Map<String, String> currentLinks = node.getLinks();
            final Map<String, String> oldLinks = links.getLinks();
            final Map<String, Node<C>> currentParents = node.getParents();
            final Map<String, Node<C>> oldParents = links.getParents();
            // Disconnect all broken links
            for (String input : oldLinks.keySet()) {
                if (!currentLinks.containsKey(input)) {
                    node.disconnect(oldParents.get(input), oldLinks.get(input), input);
                }
            }
            // Connect all new links
            for (String input : currentLinks.keySet()) {
                if (!oldLinks.containsKey(input)) {
                    node.connect(input);
                }
            }
            // Update modified links
            for (Entry<String, Node<C>> entry : currentParents.entrySet()) {
                final String input = entry.getKey();
                final Node<C> parent = entry.getValue();
                if (!oldParents.get(input).equals(parent)) {
                    node.disconnect(oldParents.get(input), oldLinks.get(input), input);
                    node.connect(input);
                }
            }
            // Store current links state
            links.update();
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

    private class Links {
        private final Node<C> node;
        private final Map<String, String> links = new HashMap<>();
        private final Map<String, Node<C>> parents = new HashMap<>();

        private Links(Node<C> node) {
            this.node = node;
            update();
        }

        private void update() {
            links.clear();
            links.putAll(node.getLinks());
            parents.clear();
            parents.putAll(node.getParents());
        }

        private Node<C> getNode() {
            return node;
        }

        private Map<String, String> getLinks() {
            return links;
        }

        private Map<String, Node<C>> getParents() {
            return parents;
        }
    }

    private class Stage implements Comparable<Stage> {
        private final Set<Node<C>> nodes = new HashSet<>();
        private final int number;

        private Stage(int number) {
            this.number = number;
        }

        private void addNode(Node<C> node) {
            nodes.add(node);
        }

        public Set<Node<C>> getNodes() {
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
