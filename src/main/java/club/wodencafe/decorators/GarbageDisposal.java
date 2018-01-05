
package club.wodencafe.decorators;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

/**
 * Utility decorator class to assist a developer with knowing when an object is
 * eligible for Garbage Collection.
 * 
 * 
 * @author Christopher Bryan Boyd
 *
 */
public class GarbageDisposal
{

	private static final GarbageDisposal gc = new GarbageDisposal();
	private static final ExecutorService cachedExecutor = Executors.newCachedThreadPool();
	private final ReferenceQueue<Object> q;
	private final Set<Runnable> set;
	private final GarbageCollectorCloser service;

	private GarbageDisposal()
	{
		set = new HashSet<>();
		q = new ReferenceQueue<>();
		service = new GarbageCollectorCloser();
		service.startAsync();
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
			PhantomRunnable<?> g = (PhantomRunnable<?>) gc.q.remove();
			g.run();
		}

	}

	public static final <T> void decorate(T object, Runnable runnable)
	{
		gc.set.add(new PhantomRunnable<>(object, runnable));
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
			this.executor = Optional.of(executor.orElse(cachedExecutor));
		}

		@Override
		public void run()
		{
			gc.set.remove(this);
			CompletableFuture.runAsync(() -> runnable.run(), executor.get());
		}

	}
}
