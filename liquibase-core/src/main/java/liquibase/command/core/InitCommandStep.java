package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.configuration.core.ScopeValueProvider;
import liquibase.exception.CommandExecutionException;
import liquibase.hub.HubConfiguration;
import liquibase.hub.HubService;
import liquibase.hub.HubServiceFactory;
import liquibase.hub.HubUpdater;
import liquibase.ui.UIService;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

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

            //
            // Do not try to register if
            //   1.  We have a key already OR
            //
            if (StringUtil.isNotEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue())) {
                Scope.getCurrentScope().getUI().sendMessage("Liquibase Hub API Key already exists.");
                return;
            }

            //send hub code and get hub Api Key

            String hubCode = commandScope.getArgumentValue(HUB_CODE_ARG);
            String hubApiKey = "2Ff6MfjymN300dM7CieBIeAf7yBx7ZMX0IO1NwsGeto";

            //copy liquibase properties from LIQUIBASE_HOME/examples/liquibase.properties to the current working directory

            final HubService service = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
            String liquibaseInstallPath = System.getenv("LIQUIBASE_HOME");
            File examplePropertiesFile = new File(liquibaseInstallPath+"/examples/xml/liquibase.properties");
            File liquibasePropertiesFile = new File("liquibase.properties");
            Files.copy(examplePropertiesFile.toPath(), liquibasePropertiesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            //append hubApiKey to liquibase.properties

            HubUpdater.writeToPropertiesFile(liquibasePropertiesFile, "liquibase.hub.apiKey=" + hubApiKey + System.lineSeparator());
            HubUpdater.writeToPropertiesFile(liquibasePropertiesFile, "liquibase.hub.hubMode=all" + System.lineSeparator());

            //copy sample.changelog.xml from LIQUIBASE_HOME/examples/sameple.changelog.xml to the current working directory

            File exampleChangelogXml= new File(liquibaseInstallPath+"/examples/xml/sample.changelog.xml");
            File changelogXmlFile = new File("sample.changelog.xml");
            Files.copy(exampleChangelogXml.toPath(), changelogXmlFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            //
            // Parse the changelog
            //
            DatabaseChangeLog changeLog = parseChangeLogFile(changelogXmlFile.getName());

            //
            // Register the changelog
            //
            Map<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(HubConfiguration.LIQUIBASE_HUB_API_KEY.getKey(), hubApiKey);
            Scope.child(scopeValues, new Scope.ScopedRunner() {
                @Override
                public void run() throws Exception {
                    HubUpdater.registerChangeLog(null, changeLog, changelogXmlFile.getName());
                }
            });
        }
    }
}