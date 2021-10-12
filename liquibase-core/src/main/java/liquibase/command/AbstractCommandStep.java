package liquibase.command;

import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.exception.CommandValidationException;
import liquibase.exception.LiquibaseException;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.Arrays;

/**
 * Convenience base class for {@link CommandStep} implementations.
 */
public abstract class AbstractCommandStep implements CommandStep {

    /**
     * @return {@link #ORDER_DEFAULT} if the command scope's name matches {@link #defineCommandNames()}. Otherwise {@link #ORDER_NOT_APPLICABLE}
     */
    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        final String[][] definedCommandNames = defineCommandNames();
        if (definedCommandNames != null) {
            for (String[] thisCommandName : definedCommandNames) {
                if ((thisCommandName != null) && StringUtil.join(Arrays.asList(thisCommandName), " ").equalsIgnoreCase(StringUtil.join(Arrays.asList(commandDefinition.getName()), " "))) {
                    return ORDER_DEFAULT;
                }
            }
        }
        return ORDER_NOT_APPLICABLE;
    }

    /**
     * Default implementation does no additional validation.
     */
    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
    }

    /**
     * Default implementation makes no changes
     */
    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {

    }

    protected DatabaseChangeLog parseChangeLogFile(String changeLogFile) throws LiquibaseException {
        return parseChangeLogFile(changeLogFile, null);
    }

    protected DatabaseChangeLog parseChangeLogFile(String changeLogFile, ChangeLogParameters changeLogParameters) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        if (changeLogParameters == null) {
            changeLogParameters = new ChangeLogParameters();
        }
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }
}
