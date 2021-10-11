package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.ui.UIService;

public class RegisterChangelogCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"init"};

    public static final CommandArgumentDefinition<String> HUB_CODE_ARG;


    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        HUB_CODE_ARG = builder.argument("hubCode", String.class).required()
                .description("Hub Initialization Code").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        try (PrintWriter output = new PrintWriter(resultsBuilder.getOutputStream())) {

            final UIService ui = Scope.getCurrentScope().getUI();
            CommandScope commandScope = resultsBuilder.getCommandScope();
            final HubServiceFactory hubServiceFactory = Scope.getCurrentScope().getSingleton(HubServiceFactory.class);
            if (!hubServiceFactory.isOnline()) {
                throw new CommandExecutionException("The command registerChangeLog requires communication with Liquibase Hub, \nwhich is prevented by liquibase.hub.mode='off'. \nPlease set to 'all' or 'meta' and try again.  \nLearn more at https://hub.liquibase.com");
            }

            String hubCode = commandScope.getArgumentValue(HUB_CODE_ARG);

            final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();


        }
    }
}