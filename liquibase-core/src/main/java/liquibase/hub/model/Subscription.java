package liquibase.hub.model;

import java.util.Date;
import java.util.UUID;

public class Subscription implements HubModel {
    private UUID id;
    private Organization organization;
    private LicenseKey licenseKey;
    private String subscriptionStatus;
    private String status;
    private Date createDate;
    private Date expirationDate;

    @Override
    public UUID getId() {
        return null;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public LicenseKey getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(LicenseKey licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }
}