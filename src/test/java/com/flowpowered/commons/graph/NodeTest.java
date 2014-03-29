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

import com.flowpowered.commons.graph.Graph.Input;
import com.flowpowered.commons.graph.Graph.InputConnect;
import com.flowpowered.commons.graph.Graph.InputLink;
import com.flowpowered.commons.graph.Graph.Output;
import com.flowpowered.commons.graph.Graph.OutputConnect;
import com.flowpowered.commons.graph.Graph.OutputLink;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class NodeTest {
    private static StringNode expectedParentNode = null;
    private static StringNode expectedChildNode = null;
    private static String expectChannel = null;

    @Test
    public void test() {
        final StringNode guy = new StringNode("guy", "cool");
        final StringNode bob = new StringNode("bob", "story");
        final StringNode joe = new StringNode("joe", "bro");
        final StringNode pal = new StringNode("pal", "...");

        /*
          Generates this graph:

               bob       1: guy to bob, channel is cool
               / \       2: guy to joe, channel is cool
              1   3      3: bob to pal, channel is story
             /     \
          guy       pal
             \
              2
               \
               joe
         */

        expectedParentNode = guy;
        expectChannel = "cool";

        expectedChildNode = bob;
        bob.link(guy, "out", "in");

        expectedChildNode = joe;
        joe.link(guy, "out", "in");

        expectedChildNode = pal;

        expectedParentNode = bob;
        expectChannel = "story";
        pal.link(bob, "out", "in");
    }

    private final class StringNode extends Node<String> {
        private final String out;
        private String in;

        public StringNode(String name, String value) {
            super(String.class, name);
            this.out = value;
        }

        @Override
        public void execute() {
        }

        @Input("in")
        public void setInput(String in) {
            this.in = in;
        }

        @Output("out")
        public String getOutput() {
            return out;
        }

        @InputLink("in")
        public void onInputLink(StringNode node, String channel) {
            Assert.assertEquals(expectedParentNode, node);
            Assert.assertEquals(expectChannel, channel);
        }

        @InputConnect("in")
        public void onInputConnect(StringNode node, String channel) {
            Assert.assertEquals(expectedParentNode, node);
            Assert.assertEquals(expectChannel, channel);
        }

        @OutputLink("out")
        public void onOutputLink(StringNode node, String channel) {
            Assert.assertEquals(expectedChildNode, node);
            Assert.assertEquals(expectChannel, channel);
        }

        @OutputConnect("out")
        public void onOuputConnect(StringNode node, String channel) {
            Assert.assertEquals(expectedChildNode, node);
            Assert.assertEquals(expectChannel, channel);
        }
    }
}
