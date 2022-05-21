package cn.bavelee.freereflection;

import java.lang.instrument.Instrumentation;

public class AgentLoader {

    private static void startAgent(String args, Instrumentation inst) {
        new FreeReflection().run(args, inst);
    }


    public static void premain(String args, Instrumentation inst) {
        startAgent(args, inst);
    }

    public static void agentmain(String args, Instrumentation inst) {
        startAgent(args, inst);
    }

}
