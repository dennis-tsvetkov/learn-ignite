package ignite;

import ignite.reporter.SimpleReporter;

public class SimpleIgniteExample {

	public static void main(String[] args) throws Exception {
		new BaseIgniteExample(SimpleReporter.class).run(args);
	}

}
