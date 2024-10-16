package org.opencdmp.service.externalfetcher.config.entities;


import java.util.List;

public interface Static <Option extends StaticOption>{
    List<Option> getOptions();
}
