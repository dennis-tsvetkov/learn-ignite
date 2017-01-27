package ignite;

import ignite.reporter.BaseReporter;
import ignite.reporter.SqlReporter;

public class SqlIgniteExample extends BaseIgniteExample{

	public SqlIgniteExample(Class<? extends BaseReporter> reporterClass) {
		super(reporterClass);
	}

	public static void main(String[] args) throws Exception {
		new SqlIgniteExample(SqlReporter.class).run(args);
	}

}
