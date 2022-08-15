package liquibase.command;

public interface CommandPostprocessor {
    void after(CommandStep command);
}
