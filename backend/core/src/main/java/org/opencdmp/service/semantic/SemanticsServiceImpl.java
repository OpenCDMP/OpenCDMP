package org.opencdmp.service.semantic;


import org.opencdmp.authorization.Permission;
import org.opencdmp.commons.JsonHandlingService;
import org.opencdmp.query.lookup.SemanticsLookup;
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


    @Autowired
    public SemanticsServiceImpl(
            AuthorizationService authorizationService,
            StorageFileService storageFileService,
            JsonHandlingService jsonHandlingService) {
        this.authorizationService = authorizationService;
        this.storageFileService = storageFileService;
        this.jsonHandlingService = jsonHandlingService;
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

    private void loadSemantics() throws IOException {
        byte[] bytes = this.storageFileService.getSemanticsFile();
        if (bytes != null) {
            try {
                String json = new String(bytes, StandardCharsets.UTF_8);
                semantics = List.of(jsonHandlingService.fromJson(Semantic[].class, json));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}

