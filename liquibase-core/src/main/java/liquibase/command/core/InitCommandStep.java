package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.ui.UIService;
import liquibase.util.FileUtil;

import java.io.PrintWriter;
import java.io.File;
import java.nio.file.*;
import java.io.FileWriter;

public class InitCommandStep extends AbstractCommandStep {

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

            //send hub code and get hub Api Key

            String hubCode = commandScope.getArgumentValue(HUB_CODE_ARG);
            String hubApiKey = "";

            //copy liquibase properties from LIQUIBASE_HOME/examples/liquibase.properties to the current working directory

            final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            String liquibaseInstallPath = System.getenv("LIQUIBASE_HOME");
            File examplePropertiesFile = new File(liquibaseInstallPath+"/examples/liquibase.properties");
            File liquibasePropertiesFile = new File("liquibase.properties");
            Files.copy(examplePropertiesFile.toPath(), liquibasePropertiesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            //append hubApiKey to liquibase.properties

            File file = new File("liquibase.properties");
            FileWriter fr = new FileWriter(file, true);
            fr.write("apiKey="+hubApiKey);
            fr.close();

            //copy sample.changelog.xml from LIQUIBASE_HOME/examples/sameple.changelog.xml to the current working directory

            File exampleChangelogXml= new File(liquibaseInstallPath+"/examples/sample.changelog.xml");
            File changelogXmlFile = new File("sample.changelog.xml");
            Files.copy(exampleChangelogXml.toPath(), changelogXmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // register changelog

        }
    }
}