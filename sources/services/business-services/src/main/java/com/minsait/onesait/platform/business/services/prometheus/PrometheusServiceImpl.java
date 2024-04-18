/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2022 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.business.services.prometheus;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.minsait.onesait.platform.resources.service.IntegrationResourcesService;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.Module;
import com.minsait.onesait.platform.resources.service.IntegrationResourcesServiceImpl.ServiceUrl;

import edu.emory.mathcs.backport.java.util.Collections;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PrometheusServiceImpl implements PrometheusService {

	@Autowired
	IntegrationResourcesService resourcesService;

	private final String PROMETHEUS_API_URL = "https://jailbreak.onesaitplatform.com/api/v1/query";

	private final String QUERY_ZEPPELIN = "container_memory_usage_bytes{pod=~'zeppelin.*'}[1h]";

	private final String QUERY_MEMORY_USAGE = "container_memory_usage_bytes{namespace=\"$namespace\"}[1h]";

	private final String QUERY_MACHINE_MEMORY = "sum(machine_memory_bytes{})";

	private final String QUERY_CPU_USAGE = "container_cpu_usage_seconds_total{namespace=\"$namespace\"}[1h]";

	private final String QUERY_NUM_CPUS = "count without(cpu, mode) (node_cpu_seconds_total{mode=\"idle\"})";

	private RestTemplate restTemplate;

	@PostConstruct
	public void init() {
		this.restTemplate = new RestTemplate();

	}

	@Override
	public String testPrometheus() {
		try {
			String fooResourceUrl = "http://localhost:8080/spring-rest/foos";
			ResponseEntity<String> response = restTemplate.getForEntity(fooResourceUrl + "/1", String.class);
			if (!response.getStatusCode().equals(HttpStatus.OK)) {
				return ("ERROR");
			} else {
				return response.getBody();
			}
		} catch (HttpServerErrorException e) {
			return ("ERROR");
		}
	}

	private String invokePrometheusAPI(String prometheusQuery) {

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString(resourcesService.getUrl(Module.PROMETHEUS, ServiceUrl.BASE))
				.queryParam("query", prometheusQuery);
		ResponseEntity<String> response = restTemplate.getForEntity(builder.build().toUri(), String.class);

		if (!response.getStatusCode().equals(HttpStatus.OK)) {
			return ("ERROR");
		} else {
			return response.getBody();
		}
	}

	@Override
	public String getMemStats(String namespace) {
		String responseUsage = invokePrometheusAPI(QUERY_MEMORY_USAGE.replace("$namespace", namespace));
		String responseTotMem = invokePrometheusAPI(QUERY_MACHINE_MEMORY);

		JSONArray usageResults = getResult(responseUsage);
		JSONArray totMemResults = getResult(responseTotMem);

		double totalMem = 0;
		for (Object json : totMemResults) {
			JSONArray totalMemory = ((JSONObject) json).getJSONArray("value");
			totalMem = totalMemory.getDouble(1);
		}

		ArrayList<PodStat> podMemStatArray = toPodMemUseStatArray(usageResults);

		JSONArray sumvalues = sumMemUsageValues(podMemStatArray, totalMem);

		Double memoryPercent = ((JSONArray) sumvalues.get(sumvalues.length() - 1)).getDouble(1);
		Double memoryUsed = (((JSONArray) sumvalues.get(sumvalues.length() - 1)).getDouble(1) * totalMem) / 100;

		DecimalFormat df = new DecimalFormat("#.#");
		String memoryPercentString = df.format(memoryPercent) + " %";
		String memoryUsedString = df.format(memoryUsed / 1000000000) + " of " + df.format(totalMem / 1000000000)
				+ "GB Used";

		JSONArray sumPodValues = sumPodMemUsageValues(podMemStatArray);

		JSONObject result = new JSONObject();
		result.put("memorygraph", sumvalues);
		result.put("memoryPercent", memoryPercentString);
		result.put("memoryUsed", memoryUsedString);
		result.put("memorypodsvalues", sumPodValues);
		result.put("totalMemory", df.format(totalMem / 1000000000));
		return result.toString();
	}

	private ArrayList<PodStat> toPodMemUseStatArray(JSONArray usageResults) {
		ArrayList<PodStat> podUseStatArray = new ArrayList<PodStat>();
		for (int i = 0; i < usageResults.length(); i++) {
			JSONObject result = usageResults.getJSONObject(i);
			if (result.getJSONObject("metric").has("container_name")
					&& !result.getJSONObject("metric").getString("container_name").equals("POD")) {
				String containerName = "";
				// containerName = result.getJSONObject("metric").getString("container_name");

				String podName = result.getJSONObject("metric").getString("pod");

				HashMap<Long, Long> useValues = calculateUseValues(result.getJSONArray("values"));

				PodStat podCPUStat = new PodStat(containerName, podName, useValues);
				podUseStatArray.add(podCPUStat);
			}
		}
		return podUseStatArray;
	}

	private JSONArray sumMemUsageValues(ArrayList<PodStat> podMemStatArray, double totalMem) {
		HashMap<Long, Long> sumvalues = new HashMap<Long, Long>();

		for (PodStat podStat : podMemStatArray) {
			for (Long usageTime : podStat.getPodUse().keySet()) {
				if (sumvalues.get(usageTime) != null) {
					sumvalues.put(usageTime, sumvalues.get(usageTime) + podStat.getPodUse().get(usageTime));
				} else {
					sumvalues.put(usageTime, podStat.getPodUse().get(usageTime));
				}
			}
		}
		List<Entry<Long, Long>> sumvaluesList = sumvalues.entrySet().stream().collect(Collectors.toList());
		Collections.sort(sumvaluesList, new Comparator<Entry<Long, Long>>() {
			@Override
			public int compare(final Entry<Long, Long> o1, final Entry<Long, Long> o2) {
				if (o1.getKey() < o2.getKey()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		return (toMemUse(sumvaluesList, totalMem));
	}

	private JSONArray toMemUse(List<Entry<Long, Long>> sumvaluesList, double totalMem) {
		JSONArray memValues = new JSONArray();
		for (int i = 1; i < sumvaluesList.size(); i++) {
			JSONArray valuearray = new JSONArray();
			valuearray.put(new Double(sumvaluesList.get(i).getKey() / 1000));
			valuearray.put(new Double((sumvaluesList.get(i).getValue())) * 100 / totalMem);
			memValues.put(valuearray);
		}
		return memValues;
	}

	private JSONArray sumPodMemUsageValues(ArrayList<PodStat> podMemStatArray) {
		HashMap<String, PodStat> podStatMap = new HashMap<String, PodStat>();

		for (PodStat podStat : podMemStatArray) {
			if (podStatMap.get(podStat.getPod()) == null) {
				PodStat podStatMemsum = new PodStat(null, podStat.getPod(), new HashMap<Long, Long>());
				for (PodStat podStatIt : podMemStatArray) {
					if (podStatMemsum.getPod().equals(podStatIt.getPod())) {
						for (Long usageTime : podStatIt.getPodUse().keySet()) {
							if (podStatMemsum.getPodUse().get(usageTime) != null) {
								podStatMemsum.getPodUse().put(usageTime, podStatMemsum.getPodUse().get(usageTime)
										+ podStatIt.getPodUse().get(usageTime));
							} else {
								podStatMemsum.getPodUse().put(usageTime, podStatIt.getPodUse().get(usageTime));
							}
						}
					}
				}
				podStatMap.put(podStatMemsum.getPod(), podStatMemsum);
			}
		}

		JSONArray sumPodsUsageValues = new JSONArray();

		for (PodStat podStat : podStatMap.values()) {
			JSONObject newPodJSON = new JSONObject();
			newPodJSON.put("pod", podStat.getPod());

			List<Entry<Long, Long>> sumPodValuesList = podStat.getPodUse().entrySet().stream()
					.collect(Collectors.toList());
			Collections.sort(sumPodValuesList, new Comparator<Entry<Long, Long>>() {
				@Override
				public int compare(final Entry<Long, Long> o1, final Entry<Long, Long> o2) {
					if (o1.getKey() < o2.getKey()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			JSONArray sumPodUsageValue = new JSONArray();
			JSONArray sumPodTotalUsageValue = new JSONArray();
			sumPodTotalUsageValue.put(new Double(sumPodValuesList.get(sumPodValuesList.size() - 1).getKey()) / 1000);
			sumPodTotalUsageValue
					.put(new Double(sumPodValuesList.get(sumPodValuesList.size() - 1).getValue()) / 1000000000);
			sumPodUsageValue.put(sumPodTotalUsageValue);

			newPodJSON.put("values", sumPodUsageValue);

			sumPodsUsageValues.put(newPodJSON);
		}
		return (sumPodsUsageValues);
	}

	@Override
	public String getCpuStats(String namespace) {
		String responseUsage = invokePrometheusAPI(QUERY_CPU_USAGE.replace("$namespace", namespace));
		String responseNumCpu = invokePrometheusAPI(QUERY_NUM_CPUS);

		JSONArray usageResults = getResult(responseUsage);
		JSONArray numCpusResults = getResult(responseNumCpu);

		double numCpusValue = 0;
		for (Object json : numCpusResults) {
			JSONArray numCpusValues = ((JSONObject) json).getJSONArray("value");
			numCpusValue = numCpusValues.getDouble(1);
		}

		ArrayList<PodStat> podCPUStatArray = toPodUseStatArray(usageResults);

		JSONArray sumValues = sumCPUUsageValues(podCPUStatArray, numCpusValue);

		Double cpuPercent = ((JSONArray) sumValues.get(sumValues.length() - 1)).getDouble(1);
		Double cpuUsed = (((JSONArray) sumValues.get(sumValues.length() - 1)).getDouble(1) * numCpusValue) / 100;

		DecimalFormat df = new DecimalFormat("#.#");
		String cpuPercentString = df.format(cpuPercent) + " %";
		String cpuUsedString = df.format(cpuUsed) + " of " + numCpusValue + " Used";

		JSONArray sumPodValues = sumPodCPUUsageValues(podCPUStatArray, numCpusValue);

		JSONObject result = new JSONObject();
		result.put("cpugraph", sumValues);
		result.put("cpuPercent", cpuPercentString);
		result.put("cpuUsed", cpuUsedString);
		result.put("cpupodsvalues", sumPodValues);
		result.put("numCpusValue", numCpusValue);
		return result.toString();
	}

	private ArrayList<PodStat> toPodUseStatArray(JSONArray usageResults) {
		ArrayList<PodStat> podUseStatArray = new ArrayList<PodStat>();
		for (int i = 0; i < usageResults.length(); i++) {
			JSONObject result = usageResults.getJSONObject(i);
			// if (result.getJSONObject("metric").has("container_name") &&
			// !result.getJSONObject("metric").getString("container_name").equals("POD")) {
			String containerName = "";
			// containerName = result.getJSONObject("metric").getString("container_name");

			String podName = result.getJSONObject("metric").getString("pod");

			HashMap<Long, Long> useValues = calculateUseValues(result.getJSONArray("values"));

			PodStat podCPUStat = new PodStat(containerName, podName, useValues);
			podUseStatArray.add(podCPUStat);
			// }
		}
		return podUseStatArray;
	}

	private HashMap<Long, Long> calculateUseValues(JSONArray podJSONUsageValues) {
		HashMap<Long, Long> cpuUsevalues = new HashMap<Long, Long>();
		for (int i = 0; i < podJSONUsageValues.length(); i++) {
			JSONArray usageValue = podJSONUsageValues.getJSONArray(i);
			if (usageValue != null) {
				Calendar calendar = Calendar.getInstance();

				calendar.setTimeInMillis(usageValue.getLong(0) * 1000);
				calendar.add(Calendar.SECOND, 30);
				calendar.set(Calendar.SECOND, 0);

				cpuUsevalues.put(calendar.getTimeInMillis(), usageValue.getLong(1));
			}
		}
		return cpuUsevalues;
	}

	private JSONArray sumCPUUsageValues(ArrayList<PodStat> podCPUStatArray, double numCpusValue) {
		HashMap<Long, Long> sumvalues = new HashMap<Long, Long>();

		for (PodStat podStat : podCPUStatArray) {
			for (Long usageTime : podStat.getPodUse().keySet()) {
				if (sumvalues.get(usageTime) != null) {
					sumvalues.put(usageTime, sumvalues.get(usageTime) + podStat.getPodUse().get(usageTime));
				} else {
					sumvalues.put(usageTime, podStat.getPodUse().get(usageTime));
				}
			}
		}
		List<Entry<Long, Long>> sumvaluesList = sumvalues.entrySet().stream().collect(Collectors.toList());
		Collections.sort(sumvaluesList, new Comparator<Entry<Long, Long>>() {
			@Override
			public int compare(final Entry<Long, Long> o1, final Entry<Long, Long> o2) {
				if (o1.getKey() < o2.getKey()) {
					return -1;
				} else {
					return 1;
				}
			}
		});
		return (createCPUIncrementalUse(sumvaluesList, numCpusValue));
	}

	private JSONArray createCPUIncrementalUse(List<Entry<Long, Long>> sumvaluesList, double numCpusValue) {
		JSONArray incrementalValues = new JSONArray();

		for (int i = 1; i < sumvaluesList.size(); i++) {
			JSONArray valuearray = new JSONArray();
			valuearray.put(new Double(sumvaluesList.get(i).getKey() / 1000));
			valuearray.put(
					new Double(((sumvaluesList.get(i).getValue() - sumvaluesList.get(i - 1).getValue())) * 100 / 60)
							/ numCpusValue);
			incrementalValues.put(valuearray);
		}
		return incrementalValues;
	}

	private JSONArray sumPodCPUUsageValues(ArrayList<PodStat> podCPUStatArray, double numCpusValue) {
		HashMap<String, PodStat> podStatMap = new HashMap<String, PodStat>();

		for (PodStat podStat : podCPUStatArray) {
			if (podStatMap.get(podStat.getPod()) == null) {
				PodStat podStatCPUsum = new PodStat(null, podStat.getPod(), new HashMap<Long, Long>());
				for (PodStat podStatIt : podCPUStatArray) {
					if (podStatCPUsum.getPod().equals(podStatIt.getPod())) {
						for (Long usageTime : podStatIt.getPodUse().keySet()) {
							if (podStatCPUsum.getPodUse().get(usageTime) != null) {
								podStatCPUsum.getPodUse().put(usageTime, podStatCPUsum.getPodUse().get(usageTime)
										+ podStatIt.getPodUse().get(usageTime));
							} else {
								podStatCPUsum.getPodUse().put(usageTime, podStatIt.getPodUse().get(usageTime));
							}
						}
					}
				}
				podStatMap.put(podStatCPUsum.getPod(), podStatCPUsum);
			}
		}
		JSONArray sumPodsUsageValues = new JSONArray();

		for (PodStat podStat : podStatMap.values()) {
			JSONObject newPodJSON = new JSONObject();
			newPodJSON.put("pod", podStat.getPod());

			List<Entry<Long, Long>> sumPodValuesList = podStat.getPodUse().entrySet().stream()
					.collect(Collectors.toList());
			Collections.sort(sumPodValuesList, new Comparator<Entry<Long, Long>>() {
				@Override
				public int compare(final Entry<Long, Long> o1, final Entry<Long, Long> o2) {
					if (o1.getKey() < o2.getKey()) {
						return -1;
					} else {
						return 1;
					}
				}
			});

			JSONArray sumPodUsageValue = new JSONArray();
			JSONArray sumPodTotalUsageValue = new JSONArray();
			sumPodTotalUsageValue.put(new Double(sumPodValuesList.get(sumPodValuesList.size() - 1).getKey()) / 1000);
			sumPodTotalUsageValue.put(new Double(((sumPodValuesList.get(sumPodValuesList.size() - 1).getValue()
					- sumPodValuesList.get(sumPodValuesList.size() - 2).getValue())) * 100 / 60) / numCpusValue);
			sumPodUsageValue.put(sumPodTotalUsageValue);

			newPodJSON.put("values", sumPodUsageValue);

			sumPodsUsageValues.put(newPodJSON);
		}
		return (sumPodsUsageValues);
	}

	private JSONArray getResult(String response) {
		JSONObject jsonResponse = new JSONObject(response);
		return jsonResponse.getJSONObject("data").getJSONArray("result");
	}

}
