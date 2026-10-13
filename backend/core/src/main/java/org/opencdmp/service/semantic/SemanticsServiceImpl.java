package org.opencdmp.service.semantic;


import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.enums.PluginType;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.query.lookup.SemanticsLookup;
import org.opencdmp.service.deposit.DepositService;
import org.opencdmp.service.evaluator.EvaluatorService;
import org.opencdmp.service.filetransformer.FileTransformerService;
import org.opencdmp.service.storage.StorageFileService;
import gr.cite.commons.web.authz.service.AuthorizationService;
import gr.cite.tools.logging.LoggerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SemanticsServiceImpl implements SemanticsService {

    private List<Semantic> semantics = null;

    private static final LoggerService logger = new LoggerService(LoggerFactory.getLogger(SemanticsServiceImpl.class));


    private final AuthorizationService authorizationService;

    private final StorageFileService storageFileService;

    private final JsonHandlingService jsonHandlingService;

    private final FileTransformerService fileTransformerService;

    private final DepositService depositService;

    private final EvaluatorService evaluatorService;


    @Autowired
    public SemanticsServiceImpl(
            AuthorizationService authorizationService,
            StorageFileService storageFileService,
            JsonHandlingService jsonHandlingService,
            FileTransformerService fileTransformerService,
            DepositService depositService,
            EvaluatorService evaluatorService) {
        this.authorizationService = authorizationService;
        this.storageFileService = storageFileService;
        this.jsonHandlingService = jsonHandlingService;
        this.fileTransformerService = fileTransformerService;
        this.depositService = depositService;
        this.evaluatorService = evaluatorService;
    }


    @Override
    public List<String> getSemantics(SemanticsLookup lookup) throws IOException {
        List<Semantic> semantics = this.getSemantics();
        List<String> filteredSemantics = semantics.stream().map(Semantic::getName).collect(Collectors.toList());
        String like = lookup.getLike();
        if (like != null && !like.isEmpty()) {
            filteredSemantics = semantics.stream().filter(x -> x.getCategory().contains(like.replaceAll("%", "")) || x.getName().contains(like.replaceAll("%", ""))).map(Semantic::getName).collect(Collectors.toList());
        }
        return filteredSemantics;
    }

    @Override
    public List<Semantic> getSemantics() throws IOException {
        this.authorizationService.authorizeForce(Permission.BrowseDescriptionTemplate);

        if (semantics == null) {
            semantics = new ArrayList<>();
            this.loadSemantics();
        }
        return semantics;
    }

    @Override
    public List<PluginSemantics> getPluginSemantics() {
        this.authorizationService.authorizeForce(Permission.BrowseDescriptionTemplate);

        // a plugin family that cannot be reached leaves the others listed
        List<PluginSemantics> result = new ArrayList<>();
        try {
            result.addAll(this.fileTransformerService.getSemanticsManifests());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            result.addAll(this.depositService.getSemanticsManifests());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
            this.evaluatorService.getAvailableEvaluators().stream()
                    .filter(x -> x != null && x.getSemantics() != null)
                    .map(x -> new PluginSemantics(PluginType.Evaluation, x.getEvaluatorId(), x.getSemantics()))
                    .forEach(result::add);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    private void loadSemantics() throws IOException {
        byte[] bytes = this.storageFileService.getSemanticsFile();
        if (bytes != null) {
            try {
                String json = new String(bytes, StandardCharsets.UTF_8);
                semantics = List.of(jsonHandlingService.fromJson(Semantic[].class, json));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}

