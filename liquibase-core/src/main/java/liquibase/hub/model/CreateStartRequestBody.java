package liquibase.hub.model;

public class CreateStartRequestBody {

    private String jdbcUrl;
    private String changelogName;

    public CreateStartRequestBody() {
    }

    public CreateStartRequestBody(String jdbcUrl, String changelogName) {
        this.jdbcUrl = jdbcUrl;
        this.changelogName = changelogName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getChangelogName() {
        return changelogName;
    }

    public void setChangelogName(String changelogName) {
        this.changelogName = changelogName;
    }

}
