package ignite;

import java.lang.reflect.Constructor;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.bitwalker.useragentutils.UserAgent;
import ignite.data.IpStats;
import ignite.data.LogEntry;
import ignite.reporter.BaseReporter;

public class BaseIgniteExample {

	protected final static Logger LOG = LoggerFactory.getLogger(SimpleIgniteExample.class);

	protected Ignite ignite;
	protected IgniteCache<String, IpStats> cache;
	protected Class<? extends BaseReporter> reporterClass;

	protected boolean CLIENT_MODE = true;

	protected String IGNITE_CONFIG = "config/example-cache.xml";
	protected String DATA_FILE = "data/access_logs";

	private String CACHE_NAME = "ipStatsCache";

	public BaseIgniteExample(Class<? extends BaseReporter> reporterClass) {
		this.reporterClass = reporterClass;
	}

	public void run(String[] args) throws Exception {
		try {
			String dataFile = DATA_FILE;
			if (args.length > 0) {
				dataFile = args[0];
			}
			LOG.info("Using data file: " + dataFile);

			//			List<LogEntry> logEntries = readLogEntries(dataFile);
			LOG.info("Http logs readed from file " + dataFile);
			//			Map<String, IpStats> ipStatistic = calculateStatistics(logEntries);
			LOG.info("Statistic calculated");

			this.ignite = getIgnite();
			LOG.info("Got Ignite instance");
			this.cache = ignite.getOrCreateCache(CACHE_NAME);
			LOG.info("Got Ignite cache");

			//			putStatisticsInCache(ipStatistic);
			LOG.info("IpStatistic is in cache");
			//			validate(ipStatistic);
			LOG.info("Validation succeeded.");

			
			createReporter().printReport();

			//cache.destroy();
			//cache.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (ignite != null) {
				ignite.close();
			}
		}
	}

	
	
	private BaseReporter createReporter() throws Exception {
		Constructor<? extends BaseReporter> ctor = reporterClass.getConstructor(IgniteCache.class);
		return ctor.newInstance(cache);
	}

	
	@SuppressWarnings("unused")
	private void validate(Map<String, IpStats> ipStatistic) throws Exception {
		// iterate through local cache
		for (Entry<String, IpStats> entry : ipStatistic.entrySet()) {
			IpStats cacheValue = cache.get(entry.getKey());
			IpStats localValue = entry.getValue();
			if (!localValue.equals(cacheValue)) {
				throw new Exception(String.format("Validation failed. \nLocal value:%s, \nRemote value: %s ",
						localValue, cacheValue));
			}
		}
	}

	protected void putStatisticsInCache(Map<String, IpStats> ipStatistic) {
		try {
			IgniteTransactions txs = ignite.transactions();
			Transaction tx = txs.txStart();
			for (Entry<String, IpStats> entry : ipStatistic.entrySet()) {
				String ip = entry.getKey();
				IpStats newEntry = entry.getValue();

				IpStats cacheEntry = cache.get(ip);
				// if cache contains such entry, summarize all values in new entry
				if (cacheEntry != null) {
					newEntry.incrementCounters(cacheEntry.getBytesAmount(), cacheEntry.getRequestCount());
					newEntry.getBrowsersUsed().addAll(cacheEntry.getBrowsersUsed());
				}
				cache.put(ip, newEntry);
			}
			tx.commit();
		} catch (Exception e) {
			LOG.error("Unable to perform transaction. " + e);
		}
	}

	private Map<String, IpStats> calculateStatistics(List<LogEntry> logEntries) {
		Map<String, IpStats> result = new HashMap<String, IpStats>();
		for (LogEntry entry : logEntries) {
			String ip = entry.getIp();
			IpStats ipStats = new IpStats(ip);
			IpStats oldStats = result.putIfAbsent(ip, ipStats);
			if (oldStats != null) {
				ipStats = oldStats;
			}
			ipStats.incrementCounters(entry.getSize(), 1L);
			ipStats.getBrowsersUsed().add(entry.getBrowser());
		}
		return result;
	}

	protected List<LogEntry> readLogEntries(String dataFile) throws Exception {
		ArrayList<LogEntry> result = new ArrayList<LogEntry>();

		List<String> logLines = Files.readAllLines(Paths.get(dataFile));
		LOG.info("Http log has been readed");
		for (String line : logLines) {
			LogEntry entry = parseLogString(line);
			if (entry != null) {
				result.add(entry);
			}
		}
		return result;
	}

	protected LogEntry parseLogString(String line) {
		try {
			// convert this:
			// ip238 - - [24/Apr/2011:17:31:32 -0400] "GET /sun_ss5/ss5_jumpers.jpg HTTP/1.1" 200 39884 "http://host2/sun_ss5/" "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.16) Gecko/20110323 Linux Mint/9 (Isadora) Firefox/3.6.16"
			// to this :
			// ip238	39884	"Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.16) Gecko/20110323 Linux Mint/9 (Isadora) Firefox/3.6.16"
			String s = line.replaceAll("([^ ]*) [^\"]*\"[^\"]*\" [^ ]+ ([^ ]+) [^\"]*\"[^\"]*\" \"([^\"]*)\".*",
					"$1\t$2\t$3");

			String[] fields = s.split("\t");

			String ip = fields[0];
			long size = Integer.parseInt("-".equals(fields[1]) ? "0" : fields[1]);
			String browser = new UserAgent(fields[2]).getBrowser().getGroup().toString();
			return new LogEntry(ip, browser, size);
		} catch (Exception e) {
			LOG.error("Unable to parse log string: " + line);
			return null;
		}
	}

	private Ignite getIgnite() {
		IgniteConfiguration cfg = Ignition.loadSpringBean(IGNITE_CONFIG, "ignite.cfg");
		CacheConfiguration<String, IpStats> cacheCfg = new CacheConfiguration<>(CACHE_NAME);
		cacheCfg.setBackups(2);
		cacheCfg.setIndexedTypes(String.class, IpStats.class);
		cfg.setCacheConfiguration(cacheCfg);
		cfg.setClientMode(CLIENT_MODE);
		return Ignition.start(cfg);
	}

}
