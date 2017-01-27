package ignite.reporter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.cache.Cache;

import org.apache.ignite.IgniteCache;

import ignite.data.IpStats;

public class SimpleReporter extends BaseReporter {

	public SimpleReporter(IgniteCache<String, IpStats> cache) {
		super(cache);
	}

	@Override
	public void printReport() {

		IpStats maxIpStats = null;
		IpStats minIpStats = null;
		Map<String, Set<String>> browsersStats = new HashMap<>();

		for (Iterator<Cache.Entry<String, IpStats>> iterator = cache.iterator(); iterator.hasNext();) {
			IpStats ipStats = iterator.next().getValue();

			System.out.println(String.format("  %s	avg=%.2f	total=%d", ipStats.getIp(),
					1.0f * ipStats.getBytesAmount() / ipStats.getRequestCount(), ipStats.getBytesAmount()));

			// max bytes
			if (maxIpStats == null || maxIpStats.getBytesAmount() < ipStats.getBytesAmount()) {
				maxIpStats = ipStats;
			}
			// min bytes
			if (minIpStats == null || minIpStats.getBytesAmount() > ipStats.getBytesAmount()) {
				minIpStats = ipStats;
			}

			countBrowser(browsersStats, ipStats.getIp(), ipStats.getBrowsersUsed());
		}

		System.out.println(String.format("Ip with MAXIMUM amount of bytes:\n  %s	%d", maxIpStats.getIp(),
				maxIpStats.getBytesAmount()));
		System.out.println(String.format("Ip with MINIMUM amount of bytes:\n  %s	%d", minIpStats.getIp(),
				minIpStats.getBytesAmount()));

		printBrowserStats(browsersStats);

		System.out.println(" === That was SIMPLE reporter ===");
	}

}
