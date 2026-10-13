package org.opencdmp.service.maintenance;

import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.data.query.QueryFactory;
import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.authorization.Permission;
import org.opencdmp.data.InstallationEntity;
import org.opencdmp.data.TenantEntityManagerFactory;
import org.opencdmp.model.maintenance.InstallationInfo;
import org.opencdmp.model.maintenance.InstallationNotice;
import org.opencdmp.model.persist.InstallationPersist;
import org.opencdmp.query.InstallationQuery;
import org.opencdmp.service.updatecheck.UpdateCheckProperties;
import org.opencdmp.service.updatecheck.UpdateCheckTask;
import org.opencdmp.service.updatecheck.UpdateFeed;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.management.InvalidApplicationException;
import java.util.ArrayList;
import java.util.List;

@Service
public class InstallationServiceImpl implements InstallationService {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(InstallationServiceImpl.class));

    private final AuthorizationService authorizationService;
    private final QueryFactory queryFactory;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final UpdateCheckProperties updateCheckProperties;
    private final UpdateCheckTask updateCheckTask;
    private final TenantEntityManagerFactory tenantEntityManagerFactory;

    public InstallationServiceImpl(AuthorizationService authorizationService, QueryFactory queryFactory, ObjectProvider<BuildProperties> buildProperties, UpdateCheckProperties updateCheckProperties, UpdateCheckTask updateCheckTask, TenantEntityManagerFactory tenantEntityManagerFactory) {
        this.authorizationService = authorizationService;
        this.queryFactory = queryFactory;
        this.buildProperties = buildProperties;
        this.updateCheckProperties = updateCheckProperties;
        this.updateCheckTask = updateCheckTask;
        this.tenantEntityManagerFactory = tenantEntityManagerFactory;
    }

    @Override
    public InstallationInfo info() {
        logger.debug("installation info");
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        InstallationEntity installation = this.queryFactory.query(InstallationQuery.class).first();
        InstallationInfo model = new InstallationInfo();
        model.setId(installation.getId());
        // Missing when the app runs from an IDE build that skipped the build-info goal
        BuildProperties build = this.buildProperties.getIfAvailable();
        String version = build == null ? null : build.getVersion();
        model.setVersion(version);
        model.setUpdateCheckEnabled(this.updateCheckProperties.getEnabled());
        model.setShareUsage(installation.getShareUsage());

        UpdateFeed feed = this.updateCheckTask.getFeed();
        if (feed == null) return model;

        model.setCheckedAt(this.updateCheckTask.getCheckedAt());
        model.setLatestVersion(feed.getLatestVersion());
        model.setReleaseUrl(feed.getReleaseUrl());
        model.setUpdateAvailable(version != null && feed.getLatestVersion() != null && compareVersions(version, feed.getLatestVersion()) < 0);

        List<InstallationNotice> notices = new ArrayList<>();
        if (feed.getNotices() != null) {
            for (InstallationNotice notice : feed.getNotices()) {
                if (notice.getFromVersion() != null && (version == null || compareVersions(version, notice.getFromVersion()) < 0)) continue;
                if (notice.getBeforeVersion() != null && (version == null || compareVersions(version, notice.getBeforeVersion()) >= 0)) continue;
                notices.add(notice);
            }
        }
        model.setNotices(notices);
        return model;
    }

    @Override
    public InstallationInfo persist(InstallationPersist model) throws InvalidApplicationException {
        logger.debug(new MapLogEntry("persisting installation").And("model", model));
        this.authorizationService.authorizeForce(Permission.ManageQueueEvents);

        InstallationEntity data = this.queryFactory.query(InstallationQuery.class).first();
        data.setShareUsage(model.getShareUsage());
        this.tenantEntityManagerFactory.getInstance().merge(data);
        this.tenantEntityManagerFactory.getInstance().flush();

        return this.info();
    }

    // Compares the numeric parts; a suffix ("-dev", "-SNAPSHOT") sorts before the release it leads up to.
    private static int compareVersions(String left, String right) {
        String[] leftParts = left.split("-", 2);
        String[] rightParts = right.split("-", 2);
        String[] leftNumbers = leftParts[0].split("\\.");
        String[] rightNumbers = rightParts[0].split("\\.");
        for (int i = 0; i < Math.max(leftNumbers.length, rightNumbers.length); i++) {
            int leftNumber = i < leftNumbers.length ? versionNumber(leftNumbers[i]) : 0;
            int rightNumber = i < rightNumbers.length ? versionNumber(rightNumbers[i]) : 0;
            if (leftNumber != rightNumber) return Integer.compare(leftNumber, rightNumber);
        }
        return Integer.compare(rightParts.length, leftParts.length);
    }

    private static int versionNumber(String part) {
        String digits = part.replaceAll("\\D", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }
}
