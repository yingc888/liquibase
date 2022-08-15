package liquibase.command;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;

public class LiquibaseEventsPreprocessor implements CommandPreprocessor {

    private static Map<CommandStep, OutputStream> outputs = new LinkedHashMap<>();

    @Override
    public void before(CommandStep command, CommandResultsBuilder resultsBuilder) {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        CommandDefinition command1 = commandScope.getCommand();
        SortedMap<String, CommandArgumentDefinition<?>> arguments = command1.getArguments();
        // todo get argument values
        OutputStream os = new ByteArrayOutputStream();
        outputs.put(command, os);
        commandScope.setOutput(os);
    }
}
