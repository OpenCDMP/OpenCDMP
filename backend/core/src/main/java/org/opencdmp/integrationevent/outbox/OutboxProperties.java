package org.opencdmp.integrationevent.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "queue.task.publisher.options")
public class OutboxProperties {

	private final String exchange;
	private final int handleAckRetries;
	private final int handleNackRetries;
	private final int handleAckWaitInMilliSeconds;
	private final int handleNackWaitInMilliSeconds;
	private final String tenantDefaultLocaleRemovalTopic;
	private final String tenantDefaultLocaleTouchedTopic;

	private final String tenantTouchTopic;

	private final String tenantRemovalTopic;

	private final String tenantReactivationTopic;

	private final String tenantUserInviteTopic;

	private final String userRemovalTopic;

	private final String userTouchTopic;

	private final String annotationEntitiesTouchTopic;
	private final String annotationEntitiesRemovalTopic;

	private final String notifyTopic;

	private final String forgetMeCompletedTopic;

	private final String whatYouKnowAboutMeCompletedTopic;

	private final String accountingEntryCreatedTopic;

	private final String indicatorPointTopic;

	private final String indicatorTopic;

	private final String indicatorResetTopic;

	private final String indicatorAccessTopic;

	private final String generateFileTopic;

	public OutboxProperties(String exchange, int handleAckRetries, int handleNackRetries, int handleAckWaitInMilliSeconds, int handleNackWaitInMilliSeconds,
                            String tenantDefaultLocaleRemovalTopic,
                            String tenantDefaultLocaleTouchedTopic,
                            String tenantTouchTopic,
                            String tenantRemovalTopic,
                            String tenantReactivationTopic,
                            String tenantUserInviteTopic,
                            String userRemovalTopic,
                            String userTouchTopic,
                            String annotationEntitiesTouchTopic,
                            String annotationEntitiesRemovalTopic,
                            String notifyTopic,
                            String forgetMeCompletedTopic,
                            String whatYouKnowAboutMeCompletedTopic,
                            String accountingEntryCreatedTopic, String indicatorPointTopic, String indicatorTopic, String indicatorResetTopic, String indicatorAccessTopic, String generateFileTopic
	) {
		this.exchange = exchange;
		this.handleAckRetries = handleAckRetries;
		this.handleNackRetries = handleNackRetries;
		this.handleAckWaitInMilliSeconds = handleAckWaitInMilliSeconds;
		this.handleNackWaitInMilliSeconds = handleNackWaitInMilliSeconds;
		this.tenantDefaultLocaleRemovalTopic = tenantDefaultLocaleRemovalTopic;
		this.tenantDefaultLocaleTouchedTopic = tenantDefaultLocaleTouchedTopic;
		this.tenantTouchTopic = tenantTouchTopic;
		this.tenantRemovalTopic = tenantRemovalTopic;
		this.tenantReactivationTopic = tenantReactivationTopic;
		this.tenantUserInviteTopic = tenantUserInviteTopic;
        this.userRemovalTopic = userRemovalTopic;
        this.userTouchTopic = userTouchTopic;
        this.annotationEntitiesTouchTopic = annotationEntitiesTouchTopic;
		this.annotationEntitiesRemovalTopic = annotationEntitiesRemovalTopic;
		this.notifyTopic = notifyTopic;
		this.forgetMeCompletedTopic = forgetMeCompletedTopic;
		this.whatYouKnowAboutMeCompletedTopic = whatYouKnowAboutMeCompletedTopic;
		this.accountingEntryCreatedTopic = accountingEntryCreatedTopic;
        this.indicatorPointTopic = indicatorPointTopic;
        this.indicatorTopic = indicatorTopic;
        this.indicatorResetTopic = indicatorResetTopic;
        this.indicatorAccessTopic = indicatorAccessTopic;
        this.generateFileTopic = generateFileTopic;
	}

	public String getExchange() {
		return this.exchange;
	}

	public int getHandleAckRetries() {
		return this.handleAckRetries;
	}

	public int getHandleNackRetries() {
		return this.handleNackRetries;
	}

	public int getHandleAckWaitInMilliSeconds() {
		return this.handleAckWaitInMilliSeconds;
	}

	public int getHandleNackWaitInMilliSeconds() {
		return this.handleNackWaitInMilliSeconds;
	}

	public String getTenantDefaultLocaleRemovalTopic() {
		return this.tenantDefaultLocaleRemovalTopic;
	}

	public String getTenantDefaultLocaleTouchedTopic() {
		return this.tenantDefaultLocaleTouchedTopic;
	}

	public String getTenantTouchTopic() {
		return this.tenantTouchTopic;
	}

	public String getTenantRemovalTopic() {
		return this.tenantRemovalTopic;
	}

	public String getTenantReactivationTopic() {
		return this.tenantReactivationTopic;
	}

	public String getTenantUserInviteTopic() {
		return this.tenantUserInviteTopic;
	}

	public String getUserRemovalTopic() {
		return this.userRemovalTopic;
	}

	public String getUserTouchTopic() {
		return this.userTouchTopic;
	}

	public String getAnnotationEntitiesTouchTopic() {
		return this.annotationEntitiesTouchTopic;
	}

	public String getAnnotationEntitiesRemovalTopic() {
		return this.annotationEntitiesRemovalTopic;
	}

	public String getNotifyTopic() {
		return this.notifyTopic;
	}

	public String getForgetMeCompletedTopic() {
		return this.forgetMeCompletedTopic;
	}

	public String getWhatYouKnowAboutMeCompletedTopic() {
		return this.whatYouKnowAboutMeCompletedTopic;
	}

	public String getAccountingEntryCreatedTopic() {
		return accountingEntryCreatedTopic;
	}

	public String getGenerateFileTopic() {
		return this.generateFileTopic;
	}

	public String getIndicatorPointTopic() {
		return indicatorPointTopic;
	}

	public String getIndicatorTopic() {
		return indicatorTopic;
	}

	public String getIndicatorResetTopic() {
		return indicatorResetTopic;
	}

	public String getIndicatorAccessTopic() {
		return indicatorAccessTopic;
	}
}
