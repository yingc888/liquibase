package liquibase.hub.model;

import java.util.UUID;

public class CreateStartResponseBody {

    private UUID organizationId;
    private UUID projectId;
    private UUID changelogId;
    private UUID connectionId;
    private String apiKey;

    public CreateStartResponseBody() {
    }

    public CreateStartResponseBody(UUID organizationId, UUID projectId, UUID changelogId, UUID connectionId, String apiKey) {
        this.organizationId = organizationId;
        this.projectId = projectId;
        this.changelogId = changelogId;
        this.connectionId = connectionId;
        this.apiKey = apiKey;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getChangelogId() {
        return changelogId;
    }

    public void setChangelogId(UUID changelogId) {
        this.changelogId = changelogId;
    }

    public UUID getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(UUID connectionId) {
        this.connectionId = connectionId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
