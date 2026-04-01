package br.com.fiap.esg.mobilidade_sustentavel.config;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component  
@ConfigurationProperties(prefix = "app")
public class Co2EmissionConfig {

     
    private Map<String, BigDecimal> co2EmissionFactors = Collections.emptyMap();

    public Map<String, BigDecimal> getCo2EmissionFactors() {
        return co2EmissionFactors;
    }

    public void setCo2EmissionFactors(Map<String, BigDecimal> co2EmissionFactors) {
        this.co2EmissionFactors = co2EmissionFactors != null ? co2EmissionFactors : Collections.emptyMap();
    }

    public BigDecimal getFactorForTransport(String transportType) {
        if (transportType == null) {
            return getDefaultFactor();
        }
         
        String normalizedType = transportType.strip().toUpperCase(); 
         
        
        return co2EmissionFactors.getOrDefault(normalizedType, getDefaultFactor());
    }

    public BigDecimal getDefaultFactor() {
         
        return co2EmissionFactors.getOrDefault("OUTRO", new BigDecimal("0.100"));
    }
} 