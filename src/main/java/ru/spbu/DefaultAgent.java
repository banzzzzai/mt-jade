package ru.spbu;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.*;

public class DefaultAgent extends Agent {

    private double agentValue;
    private List<String> agentNeighbours;
    private HashMap<String, Double> agentsValues;

    @Override
    protected void setup() {
        System.out.println(getAID().getLocalName() + " agent creating");
        Object[] args = getArguments();
        agentsValues = new HashMap<>();

        Map<String, Object> result = (Map<String, Object>) args[0];
        agentValue = (double) result.get("agentValue");
        agentNeighbours = (List<String>) result.get("agentNeighbours");
        agentsValues.put(getAID().getLocalName(), agentValue);

        System.out.println(getAID().getLocalName() + " got value = " + this.agentValue);
        System.out.println(getAID().getLocalName() + " got neighbors = " + this.agentNeighbours);

        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                sendValues();
            }
        });

        addBehaviour(new Behaviour() {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (message != null) {
                    processMessage(message);
                } else {
                    block();
                }
            }

            @Override
            public boolean done() {
                return false;
            }
        });

    }

    private void sendValues() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);

        for (String agentNeighbourName : agentNeighbours) {
            message.addReceiver(new AID(agentNeighbourName, AID.ISLOCALNAME));
        }

        StringBuilder messageContentBuilder = new StringBuilder();

        for (Map.Entry<String, Double> entry : agentsValues.entrySet()) {
            messageContentBuilder.append(entry.getKey());
            messageContentBuilder.append(" ");
            messageContentBuilder.append(entry.getValue());
            messageContentBuilder.append(" ");
        }

        String messageContent = messageContentBuilder.toString();
        message.setContent(messageContent);

        send(message);

        System.out.println(getAID().getLocalName() + " agent sent message with content = " + messageContent);
    }

    private void processMessage(ACLMessage message) {
        String content = message.getContent();
        HashMap<String, Double> receivedAgentsValues = parseMessage(content);

        int tmp = agentsValues.size();
        agentsValues.putAll(receivedAgentsValues);

        System.out.println(getAID().getLocalName() + " agent received message with content = " + content);

        if (agentsValues.size() > tmp) {
            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    sendValues();
                }
            });

        } else {
            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    calculateAverage();
                }
            });
        }

    }

    private HashMap<String, Double> parseMessage(String content) {
        HashMap<String, Double> agentsValues = new HashMap<>();

        String[] contentSplit = content.split(" ");
        for (int i = 0; i < contentSplit.length; i += 2) {
            String agentNeighbourName = contentSplit[i];
            Double agentNeighborValue = Double.parseDouble(contentSplit[i + 1]);

            agentsValues.put(agentNeighbourName, agentNeighborValue);
        }

        return agentsValues;
    }

    public void calculateAverage() {
        double sumValues = 0;
        for (Double value : agentsValues.values()) {
            sumValues += value;
        }

        double average = sumValues / (double) agentsValues.size();

        System.out.println(getAID().getLocalName() + " agent calculated average = " + average);
    }
}