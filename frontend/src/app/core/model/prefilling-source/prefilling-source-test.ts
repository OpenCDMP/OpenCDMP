import { ExternalFetcherBaseSourceConfigurationPersist } from "../external-fetcher/external-fetcher";

export interface PrefillingTestRequest {
    like: string;
    key: string;
    sources: ExternalFetcherBaseSourceConfigurationPersist[];
}
