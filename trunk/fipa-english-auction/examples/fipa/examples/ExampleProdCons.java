package fipa.examples;

import jade.Boot;
import jade.core.Agent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fipa.mock.agents.LazyAgent;
import fipa.mock.agents.NotServicesRegisteredAgent;
import fipa.mock.agents.OnceBidConsumer;
import fipa.mock.agents.ProducerMockAgent;
import fipa.mock.agents.SleepyAgent;
import fipa.mock.agents.TwiceBidsConsumer;

/**
 * Abstract class responsible for the preparation and of the execution of the
 * agent container.
 * 
 * @author Nikolay Vasilev
 * @author Ruben Rios
 */
public abstract class ExampleProdCons {

	// --- Instance Variables --------------------------------------------------

	protected Map<String, Class<? extends Agent>> nickname2ClassMap;

	// --- Constructor ---------------------------------------------------------

	public ExampleProdCons() {
		nickname2ClassMap = new HashMap<String, Class<? extends Agent>>();
		nickname2ClassMap.put("not-registered",
				NotServicesRegisteredAgent.class);
		nickname2ClassMap.put("lazy-agent", LazyAgent.class);
		nickname2ClassMap.put("sleepy-agent", SleepyAgent.class);
		nickname2ClassMap.put("consumer1", OnceBidConsumer.class);
		nickname2ClassMap.put("consumer2", TwiceBidsConsumer.class);
		// nickname2ClassMap.put("consumer3",
		// ThreeBidsConsumerReadyToPayPaymentOK.class);
		nickname2ClassMap.put("producer", ProducerMockAgent.class);
	}

	// --- Methods -------------------------------------------------------------

	public void executeExample() {
		List<String> agentsArgs = getAgentsArgs(nickname2ClassMap);
		String[] argzz = getBootArgs(agentsArgs);
		Boot bootInstance = null;
		try {
			bootInstance = new Boot(argzz);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (bootInstance != null) {
				bootInstance = null;
			}
		}
	}

	protected String[] getBootArgs(List<String> agentsArgsList) {
		if (agentsArgsList == null) {
			throw new IllegalArgumentException(
					"Invalid argument - agentsArgsList: null.");
		}
		File projectDir = new File(".");
		List<String> argsList = new ArrayList<String>();
		argsList.add("-classpath");
		argsList.add(projectDir.getAbsolutePath() + "\\classes");
		// argsList.add("-gui");
		for (String agentArg : agentsArgsList) {
			argsList.add(agentArg);
		}
		argsList.add("Sniffer:jade.tools.sniffer.Sniffer"
				+ "(producer*;consumer*;lazy*;sleepy*;not-registered*)");
		String[] argzz = new String[argsList.size()];
		argzz = argsList.toArray(argzz);
		return argzz;
	}

	/**
	 * Returns the key-value pairs from the map, formatted as list of strings,
	 * where every key-value is represented in the format "key:value".
	 * 
	 * @param nickname2ClassMap
	 *            Map which contains the desired nicknames of the agents as
	 *            keys, and the name of the <code>class</code> which is going to
	 *            be registered in the <code>Boot</code> instance.
	 * @return Returns the key-value pairs from the map, formatted as list of
	 *         strings, where every key-value is represented in the format
	 *         "key:value".
	 */
	protected List<String> getAgentsArgs(
			Map<String, Class<? extends Agent>> nickname2ClassMap) {
		if (nickname2ClassMap == null) {
			throw new IllegalArgumentException(
					"Invalid argument - nickname2ClassMap: null.");
		}
		List<String> resultList = new ArrayList<String>();
		for (String nickname : nickname2ClassMap.keySet()) {
			String item = nickname + ":"
					+ nickname2ClassMap.get(nickname).getName();
			resultList.add(item);
		}
		return resultList;
	}
}
