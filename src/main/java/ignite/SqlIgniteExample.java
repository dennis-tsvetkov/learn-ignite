package ignite;

import ignite.reporter.SqlReporter;

public class SqlIgniteExample {

	public static void main(String[] args) throws Exception {
		new BaseIgniteExample(SqlReporter.class).run(args);
	}

}
