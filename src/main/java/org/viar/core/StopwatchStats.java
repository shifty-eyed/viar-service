package org.viar.core;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class StopwatchStats {

	private static class StatItem {
		long runningDuration;
		long srartTime;
		int frameCount;
		long avgTime;
	}

	@Getter
	private final Map<String, StatItem> stats = new HashMap<>();

	public void start(String key) {
		stats.putIfAbsent(key, new StatItem());
		stats.get(key).srartTime = System.currentTimeMillis();
	}

	public void stop(String key) {
		StatItem item = stats.get(key);
		item.runningDuration += System.currentTimeMillis() - item.srartTime;
		item.frameCount++;
		item.avgTime = item.runningDuration / item.frameCount;
		if (item.frameCount > 30) {
			item.runningDuration = 0;
			item.frameCount = 0;
		}
	}

	public String getStats() {
		StringBuilder sb = new StringBuilder();
		stats.forEach((k, v) -> sb.append(k).append(": ").append(v.avgTime).append("ms\n"));
		return sb.toString();
	}


}
