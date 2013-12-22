/*
 * This file is part of Spout LLC, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2013 ${organization} <http://www.spout.org/>
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
package com.flowpowered.commons;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class SimpleFuture<T> implements Future<T> {

	private static Object THROWABLE = new Object();
	private static Object CANCEL = new Object();
	private static Object NULL = new Object();

	private AtomicReference<T> resultRef = new AtomicReference<>(null);
	private AtomicReference<Throwable> throwable = new AtomicReference<>(null);

	@SuppressWarnings("unchecked")
	public boolean setThrowable(Throwable t) {
		if (!throwable.compareAndSet(null, t)) {
			return false;
		}

		if (!resultRef.compareAndSet(null, (T)THROWABLE)) {
			return false;
		}

		synchronized (resultRef) {
			resultRef.notifyAll();
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean setResult(T result) {
		if (result == null) {
			result = (T)NULL;
		}
		
		if (!resultRef.compareAndSet(null, result)) {
			return false;
		}
		
		synchronized (resultRef) {
			resultRef.notifyAll();
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return resultRef.compareAndSet(null, (T)CANCEL);
	}

	@Override
	public boolean isCancelled() {
		return resultRef.get() == CANCEL;
	}

	@Override
	public boolean isDone() {
		return resultRef.get() != null;
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		try {
			return get(0, TimeUnit.MILLISECONDS);
		} catch (TimeoutException toe) {
			throw new IllegalStateException("Attempting to get with an infinite timeout should not cause a timeout exception", toe);
		}
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		boolean noTimeout = timeout <= 0;

		long timeoutInMS = unit.toMillis(timeout);

		if (!noTimeout && timeoutInMS <= 0) {
			timeoutInMS = 1;
		}
		long currentTime = System.currentTimeMillis();
		long endTime = currentTime + timeoutInMS;

		while (noTimeout || currentTime < endTime) {
			synchronized (resultRef) {
				T result = resultRef.get();
				if (result != null) {
					if (result == NULL || result == CANCEL) {
						return null;
					}

					if (result == THROWABLE) {
						Throwable t = throwable.get();
						throw new ExecutionException("Exception occured when trying to retrieve the result of this future", t);
					}

					return result;
				}

				if (noTimeout) {
					resultRef.wait();
				} else {
					resultRef.wait(endTime - currentTime);
				}
			}
			currentTime = System.currentTimeMillis();
		}
		throw new TimeoutException("Wait duration of " + (currentTime - (endTime - timeoutInMS)) + "ms exceeds timeout of " + timeout + unit.toString());
	}
}
