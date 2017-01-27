package ignite.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

public class IpStats implements Serializable {
	
	private static final long serialVersionUID = 6310338468284684248L;
	@QuerySqlField(index = true)
	protected final String ip;
    @QuerySqlField(index = true)
	protected Long bytesAmount = 0L;
    @QuerySqlField
	protected Long requestsCounter = 0L;
    @QuerySqlField
	protected Set<String> browsersUsed = new HashSet<String>();

	public IpStats(String ip) {
		if (ip == null) {
			throw new IllegalArgumentException("ip cannot be null");
		}
		this.ip = ip;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof IpStats)) {
			return false;
		}
		IpStats other = (IpStats) obj;

		return (this.ip.equals(other.ip) && this.bytesAmount == other.bytesAmount
				&& this.requestsCounter == other.requestsCounter && browsersUsed.equals(other.browsersUsed));
	}

	@Override
	public String toString() {
		return String.format("ip=[%s], bytes=%d, requests=%d, browsers=%s", ip, bytesAmount, requestsCounter, browsersUsed);
	}
	
	public void incrementCounters(Long bytes, Long requests) {
		this.bytesAmount += bytes;
		this.requestsCounter += requests;
	}

	public String getIp() {
		return ip;
	}

	public Long getBytesAmount() {
		return bytesAmount;
	}

	public Long getRequestCount() {
		return requestsCounter;
	}

	public Set<String> getBrowsersUsed() {
		return browsersUsed;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Long getRequestsCounter() {
		return requestsCounter;
	}

}
