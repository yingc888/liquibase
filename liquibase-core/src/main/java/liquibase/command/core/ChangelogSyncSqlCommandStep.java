package liquibase.command.core;

import liquibase.command.*;
import liquibase.database.Database;
import liquibase.util.LoggingExecutorTextUtil;

import java.io.Writer;
import java.util.Arrays;
import java.util.List;

public class ChangelogSyncSqlCommandStep extends ChangelogSyncCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSyncSql"};

    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> OUTPUT_DEFAULT_CATALOG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        OUTPUT_DEFAULT_SCHEMA_ARG = builder.argument("outputDefaultSchema", Boolean.class)
                .description("Control whether names of objects in the default schema are fully qualified or not. If true they are. If false, only objects outside the default schema are fully qualified")
                .defaultValue(true).build();
        OUTPUT_DEFAULT_CATALOG_ARG = builder.argument("outputDefaultCatalog", Boolean.class)
                .description("Control whether names of objects in the default catalog are fully qualified or not. If true they are. If false, only objects outside the default catalog are fully qualified")
                .defaultValue(true).build();
        builder.addArgument(CHANGELOG_FILE_ARG).build();
        builder.addArgument(LABEL_FILTER_ARG).build();
        builder.addArgument(CONTEXTS_ARG).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, Writer.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changelogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        LoggingExecutorTextUtil.outputHeader("SQL to add all changesets to database history table", database, changelogFile);
        super.run(resultsBuilder);
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Output the raw SQL used by Liquibase when running changelogSync");
    }

}
