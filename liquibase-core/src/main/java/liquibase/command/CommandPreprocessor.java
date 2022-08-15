package liquibase.command;

public interface CommandPreprocessor {
    void before(CommandStep command, CommandResultsBuilder resultsBuilder);
}
