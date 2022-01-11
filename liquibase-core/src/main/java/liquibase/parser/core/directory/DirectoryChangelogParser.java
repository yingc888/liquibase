package liquibase.parser.core.directory;

import liquibase.change.core.RawSQLChange;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.ChangeLogParseException;
import liquibase.exception.SetupException;
import liquibase.parser.ChangeLogParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.FileUtil;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class DirectoryChangelogParser implements ChangeLogParser {
    @Override
    public DatabaseChangeLog parse(String physicalChangeLogLocation, ChangeLogParameters changeLogParameters, ResourceAccessor resourceAccessor) throws ChangeLogParseException {
        DatabaseChangeLog databaseChangeLog = new DatabaseChangeLog();
        try {
            databaseChangeLog.includeAll(physicalChangeLogLocation, false, null, true, databaseChangeLog.getStandardChangeLogComparator(), resourceAccessor, null, null, false);
        } catch (SetupException e) {
            System.out.println(e);
        }
        return databaseChangeLog;
    }

    // Copied from init project code
    private static String getExtension(String changelogFilePath) {
        if (changelogFilePath.isEmpty()) {
            return null;
        }
        String[] parts = changelogFilePath.split("\\.");
        if (parts.length == 1) {
            return null;
        }
        return parts[parts.length-1].toLowerCase();
    }

    @Override
    public boolean supports(String changeLogFile, ResourceAccessor resourceAccessor) {
        return new File(changeLogFile).isDirectory();
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
}
