package org.opencdmp.configurations.swagger;

public class SwaggerGroupConfig {

    private Group legacyApi;

    private Group currentApi;

    public static class Group {
        private String group;

        private String displayName;

        private String packagesToScan;

        private String packagesToExclude;

        private String pathsToMatch;

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPackagesToScan() {
            return packagesToScan;
        }

        public void setPackagesToScan(String packagesToScan) {
            this.packagesToScan = packagesToScan;
        }

        public String getPackagesToExclude() {
            return packagesToExclude;
        }

        public void setPackagesToExclude(String packagesToExclude) {
            this.packagesToExclude = packagesToExclude;
        }

        public String getPathsToMatch() {
            return pathsToMatch;
        }

        public void setPathsToMatch(String pathsToMatch) {
            this.pathsToMatch = pathsToMatch;
        }
    }

    public Group getLegacyApi() {
        return legacyApi;
    }

    public void setLegacyApi(Group legacyApi) {
        this.legacyApi = legacyApi;
    }

    public Group getCurrentApi() {
        return currentApi;
    }

    public void setCurrentApi(Group currentApi) {
        this.currentApi = currentApi;
    }
}
