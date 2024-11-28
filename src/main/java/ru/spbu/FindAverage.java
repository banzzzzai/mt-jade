package ru.spbu;

import jade.core.behaviours.TickerBehaviour;

public class FindAverage extends TickerBehaviour {

    private final DefaultAgent agent;
    private int currentTick;
    private final int MAX_TICKS = 10;

    FindAverage(DefaultAgent agent, long period) {
        super(agent, period);
        this.setFixedPeriod(true);
        this.agent = agent;
        this.currentTick = 0;
    }

    @Override
    protected void onTick() {
        if (currentTick < MAX_TICKS) {
            System.out.println("Agent " + this.agent.getLocalName() + ": tick=" + getTickCount());
            this.currentTick++;
        } else {
            System.out.println("Agent " + agent.getLocalName() + ": Stopping sending values after " + MAX_TICKS + " ticks.");
            this.stop();
        }
    }
}