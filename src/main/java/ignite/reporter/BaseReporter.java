package ignite.reporter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import ignite.data.IpStats;

public abstract class BaseReporter {

	protected IgniteCache<String, IpStats> cache;	
	
	public BaseReporter(IgniteCache<String, IpStats> cache) {
		if (cache == null) {
			throw new IllegalArgumentException("cache cannot be null");
		}
		this.cache = cache;
	}

	protected List<List<?>> execQuery(String sql){
		SqlFieldsQuery query = new SqlFieldsQuery(sql);
		QueryCursor<List<?>> cursor = cache.query(query);
		return cursor.getAll();
	}
	
	
	protected void printBrowserStats(Map<String, Set<String>> browsersStats) {
		System.out.println("Browsers statistic:");
		for (Entry<String, Set<String>> entry: browsersStats.entrySet()){
			System.out.println(String.format("  %s	%d", entry.getKey(), entry.getValue().size()));
		}
	}

	protected void countBrowser(Map<String, Set<String>> browsersStats, String ip, Set<String> browsersUsed) {
		for (String browser : browsersUsed){
			Set<String> setOfIps = browsersStats.get(browser);
			if (setOfIps == null) {
				setOfIps = new HashSet<>();
			}
			setOfIps.add(ip);
			browsersStats.put(browser, setOfIps);
		}
	}	
	
	public abstract void printReport();
}