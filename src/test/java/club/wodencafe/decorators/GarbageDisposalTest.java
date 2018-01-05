
package club.wodencafe.decorators;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

import com.jayway.awaitility.Awaitility;

import club.wodencafe.decorators.GarbageDisposal;

public class GarbageDisposalTest
{

	private boolean consumed = false;

	@Test
	public void test() throws InterruptedException
	{
		garbage();
		System.gc();
		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> consumed);
	}

	private void garbage()
	{
		GarbageDisposal.decorate(new Object(), () -> consumed = true);
	}
}
