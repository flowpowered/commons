/*
 * This file is part of Flow Commons, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 Spout LLC <https://spout.org/>
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
package com.flowpowered.commons.typechecker;

import java.util.Map;

public class MapTypeChecker<K, V, U extends Map<? extends K, ? extends V>> extends TypeChecker<U> {
    private final TypeChecker<? extends K> keyChecker;
    private final TypeChecker<? extends V> valueChecker;

    @SuppressWarnings ("unchecked")
    protected MapTypeChecker(Class<? super U> clazz, TypeChecker<? extends K> keyChecker, TypeChecker<? extends V> valueChecker) {
        super((Class<U>) clazz);

        this.keyChecker = keyChecker;
        this.valueChecker = valueChecker;
    }

    @Override
    public U check(Object object) {
        U map = super.check(object);

        for (Map.Entry<?, ?> element : map.entrySet()) {
            keyChecker.check(element.getKey());
            valueChecker.check(element.getValue());
        }

        return map;
    }
}
