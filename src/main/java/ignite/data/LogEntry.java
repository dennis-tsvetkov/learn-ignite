package ignite.data;

public class LogEntry {
	protected String ip;
	protected String browser;
	protected Long size;

	public LogEntry(String ip, String browser, Long size) {
		this.ip = ip;
		this.browser = browser;
		this.size = size;
	}

	@Override
	public String toString() {
		return String.format("%s:	%s	%s", ip, browser, size);
	}
	
	public String getIp() {
		return ip;
	}

	public String getBrowser() {
		return browser;
	}

	public Long getSize() {
		return size;
	}

}
