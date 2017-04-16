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

import java.util.HashMap;
import java.util.Map;

public class Graph {

    private final Map<String, Node> nodes = new HashMap<>();

    public void addNode(Node node) { nodes.put(node.getName(), node); }

    public Node getNode(String name) { return nodes.get(name); }

    public boolean hasNode(String name) { return nodes.containsKey(name); }

    public boolean hasNode(Node node) { return nodes.containsKey(node); }

    public void removeNode(String name) {
        if(hasNode(name)) {
            nodes.remove(name);
        }
    }
    public void removeNode(Node node) {
        if(hasNode(node)) {
            nodes.remove(node.getName());
        }
    }

    public void  link(Node parent, Object output, Node child, Object input) { }

    public void build() { }

    public void execute() { }

    public void  set(Node nodeName, String name, Object value) { }
    public void  setAll(String name, Object value) { }
}
