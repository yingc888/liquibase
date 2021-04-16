package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class SubscriptionBody {
    private int connectionCount = 5;
    private int subscriptionDuration = 12;
    private String subscriptionDurationUnit = "MONTHS";
    private String licenseType = "TRIAL";

    public int getConnectionCount() {
        return connectionCount;
    }

    public void setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
    }

    public int getSubscriptionDuration() {
        return subscriptionDuration;
    }

    public void setSubscriptionDuration(int subscriptionDuration) {
        this.subscriptionDuration = subscriptionDuration;
    }

    public String getSubscriptionDurationUnit() {
        return subscriptionDurationUnit;
    }

    public void setSubscriptionDurationUnit(String subscriptionDurationUnit) {
        this.subscriptionDurationUnit = subscriptionDurationUnit;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }
}