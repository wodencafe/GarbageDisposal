/* 
 * BSD 3-Clause License (https://opensource.org/licenses/BSD-3-Clause)
 *
 * Copyright (c) 2018, Christopher Bryan Boyd <wodencafe@gmail.com> All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 *    may be used to endorse or promote products derived from this software 
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package club.wodencafe.decorators;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * <h1>GarbageDisposal
 * <h1>GarbageDisposal is a utility decorator class, designed to assist you with
 * performing actions when a target object is Garbage Collected.
 * <p>
 * <strong>Example:</strong>
 * <p>
 * 
 * <pre>
 * {@code
 * Object objectToWatch = new Object();
 * GarbageDisposal.decorate(objectToWatch, 
 * () -> System.out.println("Object was Garbage Collected");
 * }
 * </pre>
 * 
 * This will print "Object was Garbage Collected" when the next <strong>Garbage
 * Collection Cycle</strong> occurs.
 * 
 * @author Christopher Bryan Boyd <wdodencafe@gmail.com>
 * @version 0.2
 * @since 2018-01-05
 * @see {@link java.lang.ref.PhantomReference}
 *
 */
public final class GarbageDisposal
{

	// Singleton
	private static final GarbageDisposal gc = new GarbageDisposal();

	// SLF4J Logger
	private static final Logger logger = LoggerFactory.getLogger(GarbageDisposal.class);

	// ExecutorService, defaults to a CachedExecutorService
	private static ExecutorService cachedExecutor = null;

	// Guava Cache for storing decorated objects and their callbacks
	private static final Cache<Object, PhantomRunnable<Object>> cache = CacheBuilder.newBuilder().weakKeys().build();

	// Reference Queue, populated with the callbacks of Garbage Collected
	// objects
	private final ReferenceQueue<Object> q = new ReferenceQueue<>();

	// Single-threaded Guava service for dequeueing entities from the
	// ReferenceQueue
	private final GarbageCollectorCloser service;

	private GarbageDisposal()
	{
		logger.debug("GarbageDisposal has been started.", this);
		service = new GarbageCollectorCloser();
		service.startAsync();
		Runtime.getRuntime().addShutdownHook(new Thread()
		{

			@Override
			public void run()
			{
				try
				{
					logger.debug("GarbageCollectorCloser is shutting down, waiting up "
							+ "to 10 seconds for running tasks to terminate.", service);
					service.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
					logger.debug("GarbageCollectorCloser was shut down.", service);
				}
				catch (TimeoutException e)
				{
					logger.error("Timed out while shutting down GarbageCollectorCloser.", e);
				}
				catch (Throwable th)
				{
					logger.error("Unexpected exception while shutting down GarbageCollectorCloser.", th);
				}
			}
		});
	}

	public static final GarbageDisposal getInstance()
	{
		return gc;
	}

	private static final class GarbageCollectorCloser extends AbstractExecutionThreadService
	{

		@Override
		protected void run() throws Exception
		{
			try
			{
				PhantomRunnable<?> g = (PhantomRunnable<?>) gc.q.remove();
				logger.debug("GarbageCollectorCloser dequeued PhantomRunnable<?> "
						+ String.valueOf(System.identityHashCode(g)));
				g.run();
			}
			catch (Throwable th)
			{
				logger.error("Unexpected exception while running GarbageCollectorCloser.", th);
			}
		}

	}

	/**
	 * If the specified <strong>Object</strong> is <i>decorated</i>, remove the
	 * <i>decoration</i>.
	 * 
	 * @param object
	 *            The <strong>Object</strong> to undecorate.
	 */
	public static final <T> void undecorate(T object)
	{
		Objects.requireNonNull(object, "Cannot undecorate a null object.");
		if (Objects.nonNull(cache.getIfPresent(object)))
		{
			logger.debug("Object " + String.valueOf(System.identityHashCode(object)) + " has been undecorated.");
			cache.invalidate(object);
		}
		else
		{
			logger.warn("Object " + String.valueOf(System.identityHashCode(object))
					+ " is not decorated, therefore it cannot be undecorated.");
		}
	}

	/**
	 * When the specified <strong>Object</strong> is <i>Garbage Collected</i>,
	 * run the specified {@link java.lang.Runnable} callback.
	 * 
	 * @param object
	 *            The <strong>Object</strong> to decorate.
	 * @param runnable
	 *            The callback to run when the <strong>Object</strong> is
	 *            <i>Garbage Collected</i>.
	 */
	public static final <T> void decorate(T object, Runnable runnable)
	{
		Objects.requireNonNull(object, "Cannot decorate a null object.");
		Objects.requireNonNull(runnable, "Cannot call a null callback, did you intend to call undecorate?");
		if (Objects.nonNull(cache.getIfPresent(object)))
		{
			logger.warn("Object " + String.valueOf(System.identityHashCode(object))
					+ " is already decorated, existing decoration will be replaced.");
		}
		cache.put(object, new PhantomRunnable<>(object, runnable));
		logger.debug("Object " + String.valueOf(System.identityHashCode(object)) + " has been decorated.");
	}

	/**
	 * When the specified <strong>Object</strong> is <i>Garbage Collected</i>,
	 * run the specified {@link java.lang.Runnable} callback on the desired
	 * {@link java.util.concurrent.ExecutorService}.
	 * 
	 * @param object
	 *            The <strong>Object</strong> to decorate.
	 * @param runnable
	 *            The callback to run when the <strong>Object</strong> is
	 *            <i>Garbage Collected</i>.
	 * @param executorService
	 *            The {@link java.util.concurrent.ExecutorService} to run the
	 *            callback on.
	 */
	public static final <T> void decorate(T object, Runnable runnable, ExecutorService executorService)
	{
		Objects.requireNonNull(object, "Cannot decorate a null object.");
		Objects.requireNonNull(runnable, "Cannot call a null callback, did you intend to call undecorate?");
		if (Objects.nonNull(cache.getIfPresent(object)))
		{
			logger.warn("Object " + String.valueOf(System.identityHashCode(object))
					+ " is already decorated, existing decoration will be replaced.");
		}
		if (Objects.isNull(executorService))
		{

			logger.warn("Specified ExecutorService is null, the default will be used instead.");
		}
		cache.put(object, new PhantomRunnable<>(object, runnable, Optional.ofNullable(executorService)));
		logger.debug("Object " + String.valueOf(System.identityHashCode(object)) + " has been decorated.");
	}

	private static final class PhantomRunnable<T> extends PhantomReference<T> implements Runnable
	{

		private final Runnable runnable;

		private final Optional<ExecutorService> executor;

		public PhantomRunnable(T referent,
				Runnable runnable)

		{
			this(referent, runnable, Optional.empty());
		}

		public PhantomRunnable(T referent,
				Runnable runnable,
				Optional<ExecutorService> executor)

		{
			super(referent, gc.q);
			this.runnable = runnable;
			this.executor = Optional.of(executor.orElse(getCachcedExecutor()));
		}

		@Override
		public void run()
		{
			// Probably Not Necessary if the keys are weak.
			/*
			 * Optional<Object> object =
			 * cache.asMap().entrySet().stream().filter(x -> x.getValue() ==
			 * this).map(x -> x.getKey()).findAny(); if (object.isPresent()) {
			 * gc.set.remove(this); }
			 */
			CompletableFuture.runAsync(() -> runnable.run(), executor.get());
		}

	}

	private static final ExecutorService getCachcedExecutor()
	{
		if (Objects.isNull(cachedExecutor))
		{
			cachedExecutor = Executors.newCachedThreadPool();
			Runtime.getRuntime().addShutdownHook(new Thread()
			{

				@Override
				public void run()
				{
					try
					{

						logger.debug("Default ExecutorService is shutting down, waiting up "
								+ "to 10 seconds for running tasks to terminate.", cachedExecutor);
						cachedExecutor.shutdown();
						cachedExecutor.awaitTermination(10, TimeUnit.SECONDS);
						logger.debug("Default ExecutorService was shut down.", cachedExecutor);
					}
					catch (InterruptedException e)
					{
						logger.error("Interrupted while shutting down the Default ExecutorService.", e);
					}
					catch (Throwable th)
					{
						logger.error("Unexpected exception while shutting down the Default ExecutorService.", th);
					}
				}
			});

		}
		return cachedExecutor;
	}
}
