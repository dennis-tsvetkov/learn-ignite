package ignite;

import ignite.reporter.BaseReporter;
import ignite.reporter.SimpleReporter;

public class SimpleIgniteExample extends BaseIgniteExample{

	public SimpleIgniteExample(Class<? extends BaseReporter> reporterClass) {
		super(reporterClass);
	}

	public static void main(String[] args) throws Exception {
		new SimpleIgniteExample(SimpleReporter.class).run(args);
	}



}
