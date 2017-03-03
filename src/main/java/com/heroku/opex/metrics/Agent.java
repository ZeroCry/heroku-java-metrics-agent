package com.heroku.opex.metrics;

import java.lang.instrument.Instrumentation;

/**
 * Created by cdodrill on 3/2/17.
 */
public class Agent {
    public static void premain(String agentArgs, Instrumentation instrumentation) {
        new Thread() {
            @Override
            public void run() {
                try {
                    new Collector();
                } catch (Exception ex) {
                    System.out.println("! ERROR: Failed to start metrics servlet");
                    ex.printStackTrace();
                }
            }
        }.start();
    }
}
