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
    private double alpha;
    private int currentTick; // Текущий тик
    private static final int MAX_TICKS = 1000; // Максимальное количество тиков
    double noiseStdDev = 2; // Стандартное отклонение шума
    double dropProbability = 0.2; // Вероятность обрыва связи (20%)

    @Override
    protected void setup() {
        System.out.println(getAID().getLocalName() + " agent creating");
        Object[] args = getArguments();

        Map<String, Object> result = (Map<String, Object>) args[0];
        agentValue = (double) result.get("agentValue");
        agentNeighbours = (List<String>) result.get("agentNeighbours");
        alpha = (double) result.get("alpha");

        currentTick = 0; // Инициализируем текущий тик

        System.out.println(getAID().getLocalName() + " got value = " + this.agentValue);
        System.out.println(getAID().getLocalName() + " got neighbors = " + this.agentNeighbours);

        // Отправляем значения соседям
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                sendValues();
            }
        });

        // Обрабатываем входящие сообщения
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage message = receive();
                if (message != null) {
                    processMessage(message);
                } else {
                    block();
                }
            }
        });
    }

    private void sendValues() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        Random random = new Random();

        for (String agentNeighbourName : agentNeighbours) {
            message.addReceiver(new AID(agentNeighbourName, AID.ISLOCALNAME));
        }

        double noisyValue = agentValue + random.nextGaussian() * noiseStdDev; // Генерация шума

        // Проверяем, нужно ли обрывать связь
        if (random.nextDouble() < dropProbability) {
            System.out.println(getAID().getLocalName() + " agent dropped the message due to connection failure.");
            return; // Прерываем выполнение метода, не отправляя сообщение
        }

        // Генерируем случайную задержку в диапазоне от 0.01 до 0.03 секунд
        int randomDelay = 10 + random.nextInt(20);

        // Добавляем задержку перед отправкой сообщения
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                try {
                    Thread.sleep(randomDelay); // Задержка в миллисекундах
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                message.setContent(getAID().getLocalName() + " " + noisyValue);
                send(message);
                System.out.println(getAID().getLocalName() + " agent sent message with content = " + message.getContent());
            }
        });
    }

    private void processMessage(ACLMessage message) {
        String content = message.getContent();
        HashMap<String, Double> receivedAgentsValues = parseMessage(content);

        // Обновляем значение на основе полученных значений
        calculateNewValue(receivedAgentsValues);
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

    private void calculateNewValue(HashMap<String, Double> receivedAgentsValues) {
        double sumDifferences = 0;
        for (Double value : receivedAgentsValues.values()) {
            sumDifferences += (value - agentValue);
        }

        // Обновляем значение агента
        agentValue += alpha * sumDifferences;
        System.out.println(getAID().getLocalName() + " agent updated value = " + agentValue);

        currentTick++;

        if (currentTick < MAX_TICKS) {
            sendValues();
        } else {
            System.out.println(getAID().getLocalName() + " agent reached max ticks and will terminate.");
            doDelete();
        }
    }
}