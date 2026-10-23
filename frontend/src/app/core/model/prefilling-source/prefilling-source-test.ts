import { Guid } from "@common/types/guid";
import { ExternalFetcherBaseSourceConfigurationPersist } from "../external-fetcher/external-fetcher";

export interface PrefillingTestRequest {
    like: string;
    key: string;
    sources: ExternalFetcherBaseSourceConfigurationPersist[];
    prefillingSourceId?: Guid;
}
