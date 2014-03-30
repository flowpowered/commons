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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.flowpowered.commons.graph.Graph.Input;
import com.flowpowered.commons.graph.Graph.InputConnect;
import com.flowpowered.commons.graph.Graph.InputLink;
import com.flowpowered.commons.graph.Graph.Output;
import com.flowpowered.commons.graph.Graph.OutputConnect;
import com.flowpowered.commons.graph.Graph.OutputLink;
import com.flowpowered.commons.graph.Graph.Setting;

public abstract class Node<C> {
    private final String name;
    // Key is output, value is input
    private final Map<String, String> inputsToOutputs = new HashMap<>();
    // Keys are input/output name
    private final Map<String, Node<C>> parents = new HashMap<>();
    private final Map<String, Node<C>> children = new HashMap<>();
    private final Map<String, Method> inputs = new HashMap<>();
    private final Map<String, Method> outputs = new HashMap<>();
    private final Map<String, Method> inputLinks = new HashMap<>();
    private final Map<String, Method> inputConnects = new HashMap<>();
    private final Map<String, Method> outputLinks = new HashMap<>();
    private final Map<String, Method> outputConnects = new HashMap<>();
    // Keys are setting name
    private final Map<String, Method> settings = new HashMap<>();
    private final Map<String, Class<?>> settingTypes = new HashMap<>();

    public Node(Class<C> channelClass, String name) {
        this.name = name;
        final Method[] methods = getClass().getMethods();
        findInputsAndOutputs(methods, channelClass);
        findEventMethods(methods, channelClass);
        findSettingMethods(methods);
    }

    public String getName() {
        return name;
    }

    public Node<C> getParent(String channel) {
        return parents.get(channel);
    }

    public Collection<Node<C>> getParents() {
        return parents.values();
    }

    public Node<C> getChild(String channel) {
        return children.get(channel);
    }

    public Collection<Node<C>> getChildren() {
        return children.values();
    }

    public void link(Node<C> parent, String output, String input) {
        final C channel = parent.getOutput(output);
        setInput(input, channel);
        parents.put(input, parent);
        parent.children.put(output, this);
        inputsToOutputs.put(input, output);
        callEvent(outputLinks.get(output), this, channel);
        callEvent(inputLinks.get(input), parent, channel);
    }

    public void delink(String input) {
        setInput(input, null);
        final String output = inputsToOutputs.remove(input);
        parents.remove(input).children.remove(output);
        callEvent(outputLinks.get(output), null, null);
        callEvent(inputLinks.get(input), null, null);
    }

    public void set(String name, Object value) {
        final Class<?> type = settingTypes.get(name);
        if (type == null) {
            throw new IllegalArgumentException("No setting named \"" + name + "\"");
        }
        if (!type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Value is not of type: " + type.getCanonicalName());
        }
        try {
            settings.get(name).invoke(this, value);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to set value", ex);
        }
    }

    public abstract void execute();

    private void callEvent(Method event, Node<C> node, C channel) {
        if (event != null) {
            try {
                event.invoke(this, node, channel);
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to call node event", ex);
            }
        }
    }

    private void setInput(String name, C input) {
        final Method inputMethod = inputs.get(name);
        if (inputMethod == null) {
            throw new IllegalArgumentException("No input named \"" + name + "\"");
        }
        try {
            inputMethod.invoke(this, input);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to set node input", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private C getOutput(String name) {
        final Method outputMethod = outputs.get(name);
        if (outputMethod == null) {
            throw new IllegalArgumentException("No output named \"" + name + "\"");
        }
        try {
            return (C) outputMethod.invoke(this);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to get node output", ex);
        }
    }

    private void findInputsAndOutputs(Method[] methods, Class<C> channelClass) {
        for (Method method : methods) {
            method.setAccessible(true);
            final Input inputAnnotation = method.getAnnotation(Input.class);
            if (inputAnnotation != null) {
                validateInputMethod(method, channelClass);
                inputs.put(inputAnnotation.value(), method);
            }
            final Output outputAnnotation = method.getAnnotation(Output.class);
            if (outputAnnotation != null) {
                validateOutputMethod(method, channelClass);
                outputs.put(outputAnnotation.value(), method);
            }
        }
    }

    private void validateInputMethod(Method method, Class<C> channelClass) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !channelClass.isAssignableFrom(parameterTypes[0])) {
            throw new IllegalStateException("Input method must have one argument of type " + channelClass.getCanonicalName());
        }
    }

    private void validateOutputMethod(Method method, Class<C> channelClass) {
        if (method.getParameterTypes().length != 0 || !channelClass.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException("Input method must have no arguments and return type " + channelClass.getCanonicalName());
        }
    }

    private void findEventMethods(Method[] methods, Class<C> channelClass) {
        for (Method method : methods) {
            method.setAccessible(true);
            boolean validated = false;
            final InputLink inputLinkAnnotation = method.getAnnotation(InputLink.class);
            if (inputLinkAnnotation != null) {
                validateEventMethod(method, channelClass);
                validated = true;
                inputLinks.put(inputLinkAnnotation.value(), method);
            }
            final InputConnect inputConnectAnnotation = method.getAnnotation(InputConnect.class);
            if (inputConnectAnnotation != null) {
                if (!validated) {
                    validateEventMethod(method, channelClass);
                    validated = true;
                }
                inputConnects.put(inputConnectAnnotation.value(), method);
            }
            final OutputLink outputLinkAnnotation = method.getAnnotation(OutputLink.class);
            if (outputLinkAnnotation != null) {
                if (!validated) {
                    validateEventMethod(method, channelClass);
                    validated = true;
                }
                outputLinks.put(outputLinkAnnotation.value(), method);
            }
            final OutputConnect outputConnectAnnotation = method.getAnnotation(OutputConnect.class);
            if (outputConnectAnnotation != null) {
                if (!validated) {
                    validateEventMethod(method, channelClass);
                }
                outputConnects.put(outputConnectAnnotation.value(), method);
            }
        }
    }

    private void validateEventMethod(Method method, Class<C> channelClass) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 2 || !Node.class.isAssignableFrom(parameterTypes[0]) || !channelClass.isAssignableFrom(parameterTypes[1])) {
            throw new IllegalStateException("Event method must have two argument of types " + Node.class.getCanonicalName() + " and " + channelClass.getCanonicalName());
        }
    }

    private void findSettingMethods(Method[] methods) {
        for (Method method : methods) {
            method.setAccessible(true);
            final Setting settingAnnotation = method.getAnnotation(Setting.class);
            if (settingAnnotation != null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                validateSettingMethod(parameterTypes);
                final String name = settingAnnotation.value();
                settings.put(name, method);
                settingTypes.put(name, parameterTypes[0]);
            }
        }
    }

    private void validateSettingMethod(Class<?>[] parameterTypes) {
        if (parameterTypes.length != 1) {
            throw new IllegalStateException("Setting method must have only one argument");
        }
    }
}
