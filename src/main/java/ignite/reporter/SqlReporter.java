package ignite.reporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ignite.IgniteCache;
import ignite.data.IpStats;

public class SqlReporter extends BaseReporter {

	public SqlReporter(IgniteCache<String, IpStats> cache) {
		super(cache);
	}

	@Override
	public void printReport() {
		List<List<?>> rows = null;
		// query for avg/total statistic per ip
		rows = execQuery("SELECT ip, CAST(bytesAmount AS DOUBLE)/requestsCounter, bytesAmount FROM ipStats");
		for (List<?> row : rows) {
			System.out.println(String.format("  %s	avg=%.2f	total=%d", row.get(0), row.get(1), row.get(2)));
		}
		// query for min bytes
		rows = execQuery("SELECT ip, bytesAmount FROM ipStats ORDER BY bytesAmount LIMIT 1");
		for (List<?> row : rows) {
			System.out.println(String.format("Ip with MINIMUM amount of bytes:\n  %s	%d", row.get(0), row.get(1)));
		}
		// query for min bytes
		rows = execQuery("SELECT ip, bytesAmount FROM ipStats ORDER BY bytesAmount DESC LIMIT 1");
		for (List<?> row : rows) {
			System.out.println(String.format("Ip with MAXIMUM amount of bytes:\n  %s	%d", row.get(0), row.get(1)));
		}
		// browsers statistics
		Map<String, Set<String>> browsersStats = new HashMap<>();
		rows = execQuery("SELECT _val FROM ipStats");
		for (List<?> row : rows) {
			IpStats ipStats = (IpStats) row.get(0);
			countBrowser(browsersStats, ipStats.getIp(), ipStats.getBrowsersUsed());		
		}
		printBrowserStats(browsersStats);
		
		System.out.println(" === That was SQL reporter ===");
	}
}
