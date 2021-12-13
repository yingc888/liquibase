package liquibase.ui.interactive;

public enum InteractivePromptResponseEnum {
    /**
     * The user selected yes, indicating that they wish to take all the default values.
     */
    yes_with_defaults,
    /**
     * The user selected no, indicating that they wish to exit without doing anything.
     */
    no,
    /**
     * The user wishes to go through an interactive prompting sessions to choose values for each parameter.
     */
    customize
}