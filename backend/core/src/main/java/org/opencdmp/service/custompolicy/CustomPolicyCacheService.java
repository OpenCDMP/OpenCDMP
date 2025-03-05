package org.opencdmp.service.custompolicy;


import gr.cite.tools.cache.CacheService;
import org.opencdmp.commons.types.descriptionstatus.DescriptionStatusDefinitionEntity;
import org.opencdmp.commons.types.planstatus.PlanStatusDefinitionEntity;
import org.opencdmp.event.DescriptionStatusTouchedEvent;
import org.opencdmp.event.PlanStatusTouchedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CustomPolicyCacheService extends CacheService<CustomPolicyCacheService.CustomPolicyCacheValue> {

    public static class CustomPolicyCacheValue {

        public CustomPolicyCacheValue() {}


        public CustomPolicyCacheValue(String tenantCode, Map<UUID, PlanStatusDefinitionEntity> planStatusDefinitionMap, Map<UUID, DescriptionStatusDefinitionEntity> descriptionStatusDefinitionMap) {
            this.tenantCode = tenantCode;
            this.planStatusDefinitionMap = planStatusDefinitionMap;
            this.descriptionStatusDefinitionMap = descriptionStatusDefinitionMap;
        }

        private String tenantCode;
        private Map<UUID, PlanStatusDefinitionEntity> planStatusDefinitionMap;
        private Map<UUID, DescriptionStatusDefinitionEntity> descriptionStatusDefinitionMap;

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public Map<UUID, PlanStatusDefinitionEntity> getPlanStatusDefinitionMap() {
            return planStatusDefinitionMap;
        }

        public void setPlanStatusDefinitionMap(Map<UUID, PlanStatusDefinitionEntity> planStatusDefinitionMap) {
            this.planStatusDefinitionMap = planStatusDefinitionMap;
        }

        public Map<UUID, DescriptionStatusDefinitionEntity> getDescriptionStatusDefinitionMap() {
            return descriptionStatusDefinitionMap;
        }

        public void setDescriptionStatusDefinitionMap(Map<UUID, DescriptionStatusDefinitionEntity> descriptionStatusDefinitionMap) {
            this.descriptionStatusDefinitionMap = descriptionStatusDefinitionMap;
        }
    }

    public void clearCache(PlanStatusTouchedEvent event) {
        this.evict(this.buildKey(event.getTenantCode()));
    }

    public void clearCache(DescriptionStatusTouchedEvent event) {
        this.evict(this.buildKey(event.getTenantCode()));
    }

    @Autowired
    public CustomPolicyCacheService(CustomPolicyCacheOptions options) {
        super(options);
    }

    @Override
    protected Class<CustomPolicyCacheValue> valueClass() {return CustomPolicyCacheValue.class;}


    public String keyOf(CustomPolicyCacheValue value) {
        return this.buildKey(value.getTenantCode());
    }

    public String buildKey(String tenantCode) {
        HashMap<String, String> keyParts = new HashMap<>();
        keyParts.put("$tenantCode$", tenantCode);
        return this.generateKey(keyParts);
    }

}
