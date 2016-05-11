
package es.upm.runtime;

import jade.wrapper.StaleProxyException;

/**
 * Initializes the XplorationRuntime, entry point.
 */
public class XplorationLoader {
    public XplorationLoader() {
    }

    public static void main(String[] args) throws InterruptedException {
        XplorationRuntime runtime = new XplorationRuntime(args);

        try {
            runtime.bootJade();
        } catch (StaleProxyException ex) {
            ex.printStackTrace();
            System.out.println("Could not boot Jade! Exiting...");
            return;
        }


        runtime.createPlatformAgent();
        runtime.createCompanyAgents();
        System.out.println("=========================\n   Company init finished\n   Starting them up now\n=========================");
        runtime.startPlatformAgent();
        runtime.startCompanyAgents();


    }
}
