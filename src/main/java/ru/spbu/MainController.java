package ru.spbu;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

import java.util.*;

public class MainController {
    private static final List<List<Integer>> graph;

    static {
        graph = Arrays.asList(
                Arrays.asList(1, 3),
                Arrays.asList(0, 2, 3),
                Arrays.asList(1, 3),
                Arrays.asList(0, 1, 2, 4),
                Arrays.asList(2, 3),
                Arrays.asList(1, 4),
                Arrays.asList(2, 4),
                Arrays.asList(1, 8),
                Arrays.asList(1, 5),
                Arrays.asList(3, 4, 5, 6)
        );
    }

    void initAgents() {
        Runtime rt = Runtime.instance();

        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.MAIN_PORT, "10098");
        p.setParameter(Profile.GUI, "true");
        ContainerController container = rt.createMainContainer(p);

        initNodes(container);
    }

    private static void initNodes(ContainerController container) {

        for (int i = 0; i < graph.size(); i++) {
            String agentName = "Agent-" + i;

            float agentValue = new Random().nextFloat() * 100;
            List<Integer> agentNeighbours = graph.get(i);
            List<String> agentNeighboursStr = new ArrayList<>();

            for (int neighborNode : agentNeighbours) {
                String agentNeighborName = "Agent-" + neighborNode;
                agentNeighboursStr.add(agentNeighborName);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("agentValue", agentValue);
            result.put("agentNeighbours", agentNeighboursStr);

            try {
                AgentController agent = container.createNewAgent(agentName, "ru.spbu.DefaultAgent", new Object[] {result});
                agent.start();
            } catch (Exception e) {
                System.err.println("Ошибка при создании агента " + agentName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
