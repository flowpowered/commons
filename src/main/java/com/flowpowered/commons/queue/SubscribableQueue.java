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
package com.flowpowered.commons.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A concurrent queue to which multiple threads can subscribe. Each subscribed thread gets its own copy of the queue, but not of it contents. Items added to the queue (via either {@link #add(Object)},
 * {@link #addAll(java.util.Collection)} or {@link #offer(Object)}) will be added to all the thread queues, but any removal operation is confined to the thread's queue. A thread needs to subscribe to
 * the queue before being able to use it, using {@link #subscribe()}. The queue to use is selected from the calling thread, using {@link Thread#currentThread()}. Once done, a thread should unsubscribe
 * using {@link #unsubscribe()}.
 * <p/>
 * When a thread constructs a new {@link SubscribableQueue}, it can declared itself as the publisher thread, and the operation of the following methods is limited to that thread only: {@link
 * #unsubscribeAll()}. The following operations operate on all queues if called by the publisher, else on the thread's own queue: {@link #add(Object)}, {@link #addAll(java.util.Collection)}, {@link
 * #offer(Object)}.
 * <p/>
 * It's possible to add objects for only specific subscribers, as long as they have provided a non-null identifier object when using {@link #subscribe(Object)}, and that the publisher knows which
 * object corresponds to which thread. Calling {@link #add(Object, Object)}, {@link #addAll(java.util.Collection, Object)} or {@link #offer(Object, Object)} using a valid identifier will ensure that
 * the object is only passed to the desired subscriber.
 * <p/>
 * Calls to methods can be expensive because of the cost of {@link Thread#currentThread()} on certain platforms. Using mass removal or addition operations ({@link #removeAll(java.util.Collection)},
 * {@link #addAll(java.util.Collection)}) is recommended when multiple items need to be removed or added.
 */
public class SubscribableQueue<T> implements Queue<T> {
    private final Map<Long, Queue<T>> queues = new ConcurrentHashMap<>();
    private Map<Object, Long> subscriberIdentifiers;
    private final AtomicLong publisherThreadID = new AtomicLong();

    /**
     * Constructs a new subscribable queue, making the thread the publisher.
     */
    public SubscribableQueue() {
        this(true);
    }

    /**
     * Constructs a new subscribable queue.
     *
     * @param becomePublisher Whether or not to become the publisher
     */
    public SubscribableQueue(boolean becomePublisher) {
        if (becomePublisher) {
            publisherThreadID.set(Thread.currentThread().getId());
        } else {
            publisherThreadID.set(-1);
        }
    }

    /**
     * Attempts to make the thread the publisher, returning true if the attempt succeeded. This is only possible if there's no publisher.
     *
     * @return Whether or not the thread became the publisher
     */
    public boolean becomePublisher() {
        if (publisherThreadID.get() != -1) {
            return false;
        }
        publisherThreadID.set(Thread.currentThread().getId());
        return true;
    }

    /**
     * Makes the publisher thread not be the publisher any more. Can only be called by the publisher thread.
     */
    public void quitPublisher() {
        checkPublisherThread();
        publisherThreadID.set(-1);
    }

    /**
     * Subscribes the thread to the queue.
     */
    public void subscribe() {
        subscribe(null);
    }

    /**
     * Subscribes the thread to the queue, providing an identifier object to allow the publisher to recognize and target the subscriber (if not null).
     *
     * @param identifier The identifier object, can be null
     */
    public void subscribe(Object identifier) {
        final long id = Thread.currentThread().getId();
        queues.put(id, new ConcurrentLinkedQueue<T>());
        if (identifier != null) {
            if (subscriberIdentifiers == null) {
                subscriberIdentifiers = new ConcurrentHashMap<>();
            }
            subscriberIdentifiers.put(identifier, id);
        }
    }

    /**
     * Unsubscribes the thread from the queue.
     */
    public void unsubscribe() {
        final long id = Thread.currentThread().getId();
        queues.remove(id);
        if (subscriberIdentifiers != null) {
            for (Iterator<Entry<Object, Long>> iterator = subscriberIdentifiers.entrySet().iterator(); iterator.hasNext(); ) {
                if (id == iterator.next().getValue()) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    /**
     * Unsubscribes all threads from all queues, deleting all the content. Can only be performed by the publisher thread.
     */
    public void unsubscribeAll() {
        checkPublisherThread();
        queues.clear();
        if (subscriberIdentifiers != null) {
            subscriberIdentifiers.clear();
        }
    }

    @Override
    public boolean add(T t) {
        return add(t, null);
    }

    /**
     * Adds an object to the queue, targeting only the subscriber identified by the provided identifier object, unless said object is null. The added object will only be visible to the subscriber.
     *
     * @param t The object to add
     * @param identifier The identifier object
     * @return True if this queue changed as a result of the call
     * @see #add(Object)
     */
    public boolean add(T t, Object identifier) {
        checkNotNullArgument(t);
        if (isPublisherThread()) {
            boolean changed = false;
            if (identifier != null) {
                final Long id = subscriberIdentifiers.get(identifier);
                checkNotNullIdentifier(id);
                changed = queues.get(id).add(t);
            } else {
                for (Queue<T> queue : queues.values()) {
                    if (queue.add(t)) {
                        changed = true;
                    }
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().add(t);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return addAll(c, null);
    }

    /**
     * Adds a collection of objects to the queue, targeting only the subscriber identified by the provided identifier object, unless said object is null. The added objects will only be visible to the
     * subscriber.
     *
     * @param c The collection of objects to add
     * @param identifier The identifier object, can be null
     * @return True if this queue changed as a result of the call
     * @see #addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends T> c, Object identifier) {
        checkNotNullArgument(c);
        if (isPublisherThread()) {
            boolean changed = false;
            if (identifier != null) {
                final Long id = subscriberIdentifiers.get(identifier);
                checkNotNullIdentifier(id);
                changed = queues.get(id).addAll(c);
            } else {
                for (Queue<T> queue : queues.values()) {
                    if (queue.addAll(c)) {
                        changed = true;
                    }
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().addAll(c);
    }

    @Override
    public boolean offer(T t) {
        return offer(t, null);
    }

    /**
     * Offers an object to the queue, targeting only the subscriber identified by the provided identifier object, unless said object is null. The offered object will only be visible to the subscriber
     * (if added).
     *
     * @param t The offered object
     * @param identifier The identifier object, can be null
     * @return True if this queue changed as a result of the call
     * @see #offer(Object)
     */
    public boolean offer(T t, Object identifier) {
        checkNotNullArgument(t);
        if (isPublisherThread()) {
            boolean changed = false;
            if (identifier != null) {
                final Long id = subscriberIdentifiers.get(identifier);
                checkNotNullIdentifier(id);
                changed = queues.get(id).offer(t);
            } else {
                for (Queue<T> queue : queues.values()) {
                    if (queue.offer(t)) {
                        changed = true;
                    }
                }
            }
            return changed;
        }
        return getCurrentThreadQueue().offer(t);
    }

    @Override
    public T remove() {
        return getCurrentThreadQueue().remove();
    }

    @Override
    public T poll() {
        return getCurrentThreadQueue().poll();
    }

    @Override
    public T element() {
        return getCurrentThreadQueue().element();
    }

    @Override
    public T peek() {
        return getCurrentThreadQueue().peek();
    }

    @Override
    public int size() {
        return getCurrentThreadQueue().size();
    }

    @Override
    public boolean isEmpty() {
        return getCurrentThreadQueue().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return getCurrentThreadQueue().contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return getCurrentThreadQueue().iterator();
    }

    @Override
    public Object[] toArray() {
        return getCurrentThreadQueue().toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        checkNotNullArgument(a);
        return getCurrentThreadQueue().toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return getCurrentThreadQueue().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        checkNotNullArgument(c);
        return getCurrentThreadQueue().containsAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkNotNullArgument(c);
        return getCurrentThreadQueue().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkNotNullArgument(c);
        return getCurrentThreadQueue().retainAll(c);
    }

    @Override
    public void clear() {
        getCurrentThreadQueue().clear();
    }

    private Queue<T> getCurrentThreadQueue() {
        final Queue<T> queue = queues.get(Thread.currentThread().getId());
        if (queue == null) {
            throw new IllegalArgumentException("The calling thread is not subscribed to the queue");
        }
        return queue;
    }

    private void checkNotNullArgument(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }
    }

    private void checkNotNullIdentifier(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Identifier does not match any subscriber");
        }
    }

    private void checkPublisherThread() {
        if (!isPublisherThread()) {
            throw new IllegalStateException("This operation can only be performed by the publisher thread");
        }
    }

    private boolean isPublisherThread() {
        return Thread.currentThread().getId() == publisherThreadID.get();
    }
}
