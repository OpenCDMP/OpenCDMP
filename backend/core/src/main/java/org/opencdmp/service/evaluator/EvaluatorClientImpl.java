package org.opencdmp.service.evaluator;

import gr.cite.tools.logging.LoggerService;
import gr.cite.tools.logging.MapLogEntry;
import org.opencdmp.commonmodels.models.description.DescriptionModel;
import org.opencdmp.commonmodels.models.plan.PlanModel;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorClient;
import org.opencdmp.evaluatorbase.interfaces.EvaluatorConfiguration;
import org.opencdmp.evaluatorbase.models.misc.DescriptionEvaluationModel;
import org.opencdmp.evaluatorbase.models.misc.PlanEvaluationModel;
import org.opencdmp.evaluatorbase.models.misc.RankResultModel;
import org.opencdmp.service.filetransformer.FileTransformerRepository;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class EvaluatorClientImpl implements EvaluatorClient {

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(FileTransformerRepository.class));

    private final WebClient transformerClient;

    public EvaluatorClientImpl(WebClient transformerClient) {
        this.transformerClient = transformerClient;
    }

    @Override
    public RankResultModel rankPlan(PlanEvaluationModel planModel) {
        logger.debug(new MapLogEntry("rankPlan").And("planModel", planModel));

        return this.transformerClient.post().uri("/rank/plan").bodyValue(planModel)  // Send planModel in the body
                .exchangeToMono(mono -> mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(RankResultModel.class)).block();
    }

    @Override
    public RankResultModel rankDescription(DescriptionEvaluationModel descriptionModel) {
        logger.debug(new MapLogEntry("rankDescription").And("descriptionModel", descriptionModel));
        return this.transformerClient.post().uri("/rank/description").bodyValue(descriptionModel)  // Send descriptionModel in the body
                .exchangeToMono(mono -> mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(RankResultModel.class)).block();

    }

    @Override
    public EvaluatorConfiguration getConfiguration() {
        logger.debug(new MapLogEntry("getConfiguration"));
        return this.transformerClient.get().uri("/config")
                .exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(new ParameterizedTypeReference<EvaluatorConfiguration>() {})).block();
    }

    @Override
    public String getLogo() {
        logger.debug(new MapLogEntry("getLogo"));
        return this.transformerClient.get().uri("/logo").exchangeToMono(mono ->  mono.statusCode().isError() ? mono.createException().flatMap(Mono::error) : mono.bodyToMono(String.class)).block();
    }
}
