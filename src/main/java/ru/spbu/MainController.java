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
                Arrays.asList(0, 2, 3, 5, 7, 8),
                Arrays.asList(1, 3, 4, 6),
                Arrays.asList(0, 1, 2, 4, 9),
                Arrays.asList(2, 3, 5, 6, 9),
                Arrays.asList(1, 4, 8, 9),
                Arrays.asList(2, 4, 9),
                Arrays.asList(1, 8),
                Arrays.asList(1, 5, 7),
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

        double sum = 0;
        int maxNeighbours = 0;

        for (int i = 0; i < graph.size(); i++) {
            List<Integer> agentNeighbours = graph.get(i);
            maxNeighbours = Math.max(maxNeighbours, agentNeighbours.size());
        }

        double alpha = 1.0 / maxNeighbours;

        for (int i = 0; i < graph.size(); i++) {
            String agentName = "Agent-" + i;

            double agentValue = new Random().nextDouble() * 100;
            sum += agentValue;
            List<Integer> agentNeighbours = graph.get(i);
            List<String> agentNeighboursStr = new ArrayList<>();

            for (int neighborNode : agentNeighbours) {
                String agentNeighborName = "Agent-" + neighborNode;
                agentNeighboursStr.add(agentNeighborName);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("agentValue", agentValue);
            result.put("agentNeighbours", agentNeighboursStr);
            result.put("alpha", alpha);

            try {
                AgentController agent = container.createNewAgent(agentName, "ru.spbu.DefaultAgent", new Object[] {result});
                agent.start();
            } catch (Exception e) {
                System.err.println("Error creating agent " + agentName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Expected average: " + sum / (double) graph.size());
        System.out.println("Maximum number of neighbors: " + maxNeighbours);
    }
}
