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

import java.util.Arrays;
import java.util.Collection;

import com.flowpowered.commons.graph.Graph.Input;
import com.flowpowered.commons.graph.Graph.InputConnect;
import com.flowpowered.commons.graph.Graph.InputLink;
import com.flowpowered.commons.graph.Graph.Output;
import com.flowpowered.commons.graph.Graph.OutputConnect;
import com.flowpowered.commons.graph.Graph.OutputLink;
import com.flowpowered.commons.graph.Graph.Setting;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class NodeTest {
    private static StringNode11 expectedParentNode = null;
    private static StringNode11 expectedChildNode = null;
    private static String expectChannel = null;

    @Test
    public void test() {
        final StringNode11 guy = new StringNode12("guy", "cool", "stuff");
        final StringNode11 bob = new StringNode11("bob", "story");
        final StringNode11 joe = new StringNode11("joe", "bro");
        final StringNode11 pal = new StringNode11("pal", "...");
        final StringNode11 ron = new StringNode21("ron", "idk");

        /*
          Generates this graph:

               bob       1: guy to bob, channel is cool
               / \       2: guy to joe, channel is stuff
              1   3      3: bob to pal, channel is story
             /     \     4: pal to ron, channel is ...
           guy     pal   5: joe to ron, channel is bro
             \       \
              2       4
               \       \
               joe -5- ron
         */

        guy.set("v1", 10);
        guy.set("v2", "heh");
        try {
            guy.set("v1", 10.0);
            Assert.fail();
        } catch (Exception ignored) {
        }

        expectedParentNode = guy;

        expectedChildNode = bob;
        bob.link(guy, "out", "in");

        expectedChildNode = joe;
        joe.link(guy, "out2", "in");

        expectedChildNode = pal;
        expectedParentNode = bob;
        pal.link(bob, "out", "in");

        expectedChildNode = ron;

        expectedParentNode = pal;
        ron.link(pal, "out", "in");

        expectedParentNode = joe;
        ron.link(joe, "out", "in2");

        validateParents(guy);
        validateParents(bob, guy);
        validateParents(pal, bob);
        validateParents(joe, guy);
        validateParents(ron, pal, joe);

        validateChildren(guy, bob, joe);
        validateChildren(bob, pal);
        validateChildren(pal, ron);
        validateChildren(joe, ron);
        validateChildren(ron);

        expectedParentNode = null;
        expectedChildNode = null;
        ron.delink("in");

        validateParents(ron, joe);
    }

    @SafeVarargs
    private static void validateParents(Node<String> node, Node<String>... parents) {
        final Collection<Node<String>> expected = Arrays.asList(parents);
        final Collection<Node<String>> actual = node.getParents();
        Assert.assertTrue(actual.containsAll(expected));
        Assert.assertTrue(expected.containsAll(actual));
    }

    @SafeVarargs
    private static void validateChildren(Node<String> node, Node<String>... children) {
        final Collection<Node<String>> expected = Arrays.asList(children);
        final Collection<Node<String>> actual = node.getChildren();
        Assert.assertTrue(actual.containsAll(expected));
        Assert.assertTrue(expected.containsAll(actual));
    }

    private static class StringNode11 extends Node<String> {
        private final String out;
        private String in;

        public StringNode11(String name, String value) {
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
        public void onInputLink(StringNode11 node) {
            validateParent(node);
        }

        @InputConnect("in")
        public void onInputConnect(StringNode11 node, String channel) {
        }

        @OutputLink("out")
        public void onOutputLink(StringNode11 node) {
            validateChild(node);
        }

        @OutputConnect("out")
        public void onOuputConnect(StringNode11 node, String channel) {
        }

        @Setting("v1")
        public void setV1(Integer v1) {
            Assert.assertEquals(10, v1.longValue());
        }

        @Setting("v2")
        public void setV2(CharSequence v2) {
            Assert.assertEquals("heh", v2);
        }
    }

    private static class StringNode12 extends StringNode11 {
        private String out2;

        private StringNode12(String name, String value, String value2) {
            super(name, value);
            out2 = value2;
        }

        @Output("out2")
        public String getOutput2() {
            return out2;
        }

        @OutputLink("out2")
        public void onOutput2Link(StringNode11 node) {
            validateChild(node);
        }

        @OutputConnect("out2")
        public void onOuput2Connect(StringNode11 node, String channel) {
        }
    }

    private static class StringNode21 extends StringNode11 {
        private String in2;

        private StringNode21(String name, String value) {
            super(name, value);
        }

        @Input("in2")
        public void setInput2(String in) {
            this.in2 = in;
        }

        @InputLink("in2")
        public void onInput2Link(StringNode11 node) {
            validateParent(node);
        }

        @InputConnect("in2")
        public void onInput2Connect(StringNode11 node, String channel) {
        }
    }

    private static void validateParent(Node<String> node) {
        Assert.assertEquals(expectedParentNode, node);
    }

    private static void validateChild(Node<String> node) {
        Assert.assertEquals(expectedChildNode, node);
    }
}
