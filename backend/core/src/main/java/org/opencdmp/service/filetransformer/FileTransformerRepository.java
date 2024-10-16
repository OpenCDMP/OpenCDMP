package org.opencdmp.service.filetransformer;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commonmodels.models.FileEnvelopeModel;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerClient;
import org.opencdmp.filetransformerbase.interfaces.FileTransformerConfiguration;
import org.opencdmp.filetransformerbase.models.misc.DescriptionImportModel;
import org.opencdmp.filetransformerbase.models.misc.PlanImportModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingDescriptionModel;
import org.opencdmp.filetransformerbase.models.misc.PreprocessingPlanModel;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class FileTransformerRepository implements FileTransformerClient {
    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FileTransformerRepository.class));

    private final WebClient transformerClient;

    public FileTransformerRepository(WebClient transformerClient) {
        this.transformerClient = transformerClient;
    }

    @Override
    public FileEnvelopeModel exportPlan(PlanModel planModel, String format) {
        logger.debug(new MapLogEntry("exportPlan").And("format", format).And("planModel", planModel));
        return this.transformerClient.post().uri("/export/plan", uriBuilder -> uriBuilder.queryParam("format", format).build()).bodyValue(planModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(FileEnvelopeModel.class)).block();
    }

    @Override
    public PlanModel importPlan(PlanImportModel planImportModel) {
        logger.debug(new MapLogEntry("importPlan").And("fileEnvelope", planImportModel.getFile()));
        return this.transformerClient.post().uri("/import/plan").bodyValue(planImportModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(PlanModel.class)).block();
    }

    @Override
    public FileEnvelopeModel exportDescription(DescriptionModel descriptionModel, String format) {
        logger.debug(new MapLogEntry("exportDescription").And("format", format).And("descriptionModel", descriptionModel));
        return this.transformerClient.post().uri("/export/description", uriBuilder -> uriBuilder.queryParam("format", format).build()).bodyValue(descriptionModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(FileEnvelopeModel.class)).block();
    }

    @Override
    public DescriptionModel importDescription(DescriptionImportModel descriptionImportModel) {
        logger.debug(new MapLogEntry("importDescription").And("fileEnvelope", descriptionImportModel.getFile()));
        return this.transformerClient.post().uri("/import/description").bodyValue(descriptionImportModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(DescriptionModel.class)).block();
    }

    @Override
    public FileTransformerConfiguration getConfiguration() {
        logger.debug(new MapLogEntry("getConfiguration"));
        return this.transformerClient.get().uri("/formats")
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<FileTransformerConfiguration>() {})).block();
    }

    @Override
    public PreprocessingPlanModel preprocessingPlan(FileEnvelopeModel fileEnvelopeModel) {
        logger.debug(new MapLogEntry("preprocessingPlan").And("fileEnvelope", fileEnvelopeModel));
        return this.transformerClient.post().uri("/preprocessing/plan").bodyValue(fileEnvelopeModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(PreprocessingPlanModel.class)).block();
    }

    @Override
    public PreprocessingDescriptionModel preprocessingDescription(FileEnvelopeModel fileEnvelopeModel) {
        logger.debug(new MapLogEntry("preprocessingDescription").And("fileEnvelope", fileEnvelopeModel));
        return this.transformerClient.post().uri("/preprocessing/description").bodyValue(fileEnvelopeModel)
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(PreprocessingDescriptionModel.class)).block();
    }


}
