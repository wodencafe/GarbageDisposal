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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

import club.wodencafe.decorators.GarbageDisposal;

public class GarbageDisposalTest
{

	private boolean consumed = false;
	private int identityHashCode = -1;

	@Before
	public void before()
	{
		consumed = false;
		identityHashCode = -1;
	}

	@Test
	public void testRegular() throws InterruptedException
	{
		garbage();
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> consumed);
	}

	@Test
	public void testHashCode() throws InterruptedException
	{
		int identityHashCode = garbageHash();
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> this.identityHashCode == identityHashCode);
	}

	@Test
	public void testHashCodeWithExecutorService() throws InterruptedException
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		int identityHashCode = garbageHashExecutor(executor);
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> this.identityHashCode == identityHashCode);
	}

	@Test
	public void testWithExecutorService() throws InterruptedException
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		garbageExecutor(executor);
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> consumed);
	}

	@Test
	public void testCompletableFuture()
	{
		garbageFuture();
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> consumed);

	}

	@Test
	public void testCompletableFutureWithExecutorService()
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		garbageFutureExecutor(executor);
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> consumed);

	}

	@Test
	public void testCompletableFutureHashCode()
	{
		int identityHashCode = garbageFutureHash();
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> this.identityHashCode == identityHashCode);

	}

	@Test
	public void testCompletableFutureHashCodeWithExecutorService()
	{
		ExecutorService executor = Executors.newSingleThreadExecutor();
		int identityHashCode = garbageFutureHashExecutor(executor);
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> this.identityHashCode == identityHashCode);

	}

	private void garbageFuture()
	{
		GarbageDisposal.decorateAsync(new Object()).thenRunAsync(() ->
		{
			consumed = true;
		});
	}

	private void garbageFutureExecutor(ExecutorService service)
	{
		GarbageDisposal.decorateAsync(new Object(), service).thenRunAsync(() ->
		{
			consumed = true;
		});
	}

	private int garbageFutureHash()
	{
		Object o = new Object();
		int identityHashCode = System.identityHashCode(o);
		GarbageDisposal.decorateAsync(new Object()).thenRunAsync(() ->
		{
			this.identityHashCode = identityHashCode;
		});
		return identityHashCode;
	}

	private int garbageFutureHashExecutor(ExecutorService service)
	{
		Object o = new Object();
		int identityHashCode = System.identityHashCode(o);
		GarbageDisposal.decorateAsync(new Object(), service).thenRunAsync(() ->
		{
			this.identityHashCode = identityHashCode;
		});
		return identityHashCode;
	}

	private int garbageHash()
	{
		Object o = new Object();
		int identityHashCode = System.identityHashCode(o);
		GarbageDisposal.decorate(o, () -> this.identityHashCode = identityHashCode);
		return identityHashCode;
	}

	private int garbageHashExecutor(ExecutorService service)
	{
		Object o = new Object();
		int identityHashCode = System.identityHashCode(o);
		GarbageDisposal.decorate(o, () -> this.identityHashCode = identityHashCode, service);
		return identityHashCode;
	}

	private void garbage()
	{
		GarbageDisposal.decorate(new Object(), () -> consumed = true);
	}

	private void garbageExecutor(ExecutorService service)
	{

		GarbageDisposal.decorate(new Object(), () -> consumed = true, service);
	}
}
