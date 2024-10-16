package org.opencdmp.service.metrics;

import io.prometheus.client.Gauge;

import javax.management.InvalidApplicationException;
import java.util.Map;

public interface MetricsService {
	void calculate(Map<String, Gauge> gauges) throws InvalidApplicationException;
	Map<String, Gauge> gaugesBuild();
}
