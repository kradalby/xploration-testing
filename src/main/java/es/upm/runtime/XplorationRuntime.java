
package es.upm.runtime;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Handles the creation of the Jade runtime, agent creation and agent startup.
 */
public class XplorationRuntime {
    static final String companyClassPathTemplate = "es.upm.company0%d.Company";
    static final String platformSpaceCraftClassPathTemplate = "es.upm.platform0%d.Spacecraft";
    static final String platformWorldClassPathTemplate = "es.upm.platform0%d.World";
    static final String libCodePath = "[code=%s%s]";
    static final String teamLibPathPattern= "company0%s.*[.]jar";
    static int amountOfTeams = 5;
    int usedPlatformID = 3;
    String libsPath = "./libs/";
    Runtime jadeRuntime;
    AgentContainer mainContainer;
    AgentController rmaController;
    ArrayList<AgentController> companyAgents = new ArrayList<AgentController>();
    AgentController platformSpacecraftAgent;
    AgentController platformWorldAgent;

    public XplorationRuntime(String[] args) {
        processArguments(args);
    }

    /**
     * Processes arguments for the runtime.
     * First argument should be the path where to search for the libs
     * The second argument is what platform to use.
     */
    private void processArguments(String[] args) {
        if(args != null && args.length > 0) {
            libsPath = args[0];
            if(args.length > 1) {
                try {
                    int teamNumber = Integer.parseInt(args[1]);
                    if(teamNumber > 0 && teamNumber <= amountOfTeams) {
                        System.out.println("Got valid platform company number");
                        usedPlatformID = teamNumber;
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Could not parse into number. Using first Spacecraft");
                }
                System.out.println(String.format("We will use es.upm.platform0%d.Spacecraft", usedPlatformID));
            }
        }

    }

    /**
     * Boots up the actual Jade Runtime, Container and RMA controller.
     * @throws StaleProxyException
     */
    public void bootJade() throws StaleProxyException {
        jadeRuntime = Runtime.instance();
        jadeRuntime.setCloseVM(true);
        ProfileImpl profile = new ProfileImpl(null, 1200, null);
        mainContainer = jadeRuntime.createMainContainer(profile);
        rmaController = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
        rmaController.start();
    }

    /**
     * Tries to create the company agents. Searches the `libs` folder for
     * a JAR with the company's name. In case of failure, tries to search
     * the namespace in order to find the needed class.
     * If the class is found - creates the agent.
     */
    public void createCompanyAgents() {
        for(int i = 1; i <= amountOfTeams; ++i) {
            try {
                String companyClassPath = String.format(companyClassPathTemplate, i);
                System.out.printf("Loading %s. ", companyClassPath);
                String path = findPathForTeam(i);
                if(path == null)
                {
                    System.out.printf("JAR not found. Checking class... ", i);
                    try{
                        Class.forName(String.format(companyClassPathTemplate, i));
                        System.out.printf("Found! ");
                    }
                    catch (ClassNotFoundException ex)
                    {
                        System.out.printf("Not found. Skipping agent. %n");
                        continue;
                    }
                }
                else{
                    companyClassPath += String.format(libCodePath, libsPath, path);
                }
                companyAgents.add(mainContainer.createNewAgent("Company0" + i, companyClassPath, null));
                System.out.printf("Agent created.%n", companyClassPath);
            } catch (Exception ex) {
                System.out.println("Failed to init Company" + i);
            }
        }

    }

    /**
     * Searches the `libs` folder to find a JAR
     * for the provided company number.
     * @return the path to the jar
     */
    public String findPathForTeam(int i)
    {
        File[] dir = new File(libsPath).listFiles();
        String patternStr = String.format(teamLibPathPattern, i);
        Pattern pattern = Pattern.compile(patternStr);
        if(dir == null)
        {
            System.out.print("Lib folder not found. ");
            return null;
        }

        for (File file : dir) {
            if(pattern.matcher(file.getName()).matches())
            {
                return file.getName();
            }
        }
        return null;
    }

    public void startCompanyAgents() {
        for (AgentController ac : companyAgents) {
            try {
                ac.start();
            } catch (StaleProxyException ex) {
                System.out.println("Failed to start an agent.");
            }
        }

    }

    /**
     * Tries to create the spacecraft agent. Searches the `libs` folder for
     * a JAR with the company's name. In case of failure, tries to search
     * the namespace in order to find the needed class.
     * If the class is found - creates the agent.
     */
    public void createPlatformAgent() {
        try {
            String platformSpacecraftClassPath = String.format(platformSpaceCraftClassPathTemplate, usedPlatformID);
            System.out.printf("Loading %s. ", platformSpacecraftClassPath );
            String platformWorldClassPath = String.format(platformWorldClassPathTemplate, usedPlatformID);
            System.out.printf("Loading %s. ", platformWorldClassPath );
            String path = findPathForTeam(usedPlatformID);
            if(path == null)
            {

                System.out.printf("JAR not found. Checking class... ");
                try {
                    Class.forName(String.format(platformSpacecraftClassPath, usedPlatformID));
                    System.out.printf("Found! ");
                }
                catch (ClassNotFoundException ex)
                {
                    System.out.printf("Not found! ");
                    return;
                }
            }
            else {
                platformSpacecraftClassPath += String.format(libCodePath, libsPath, path);
                platformWorldClassPath += String.format(libCodePath, libsPath, path);
            }

            platformSpacecraftAgent = mainContainer.createNewAgent("Spacecraft0" + usedPlatformID, platformSpacecraftClassPath, null);
            platformWorldAgent = mainContainer.createNewAgent("World0" + usedPlatformID, platformWorldClassPath, null);
            System.out.printf("Agent created.%n", platformSpacecraftClassPath);
            System.out.printf("Agent created.%n", platformWorldClassPath);
        } catch (Exception ex) {
            System.out.println("Failed to init Spacecraft0" + usedPlatformID);
            System.out.println("Failed to init World0" + usedPlatformID);
        }
    }

    public void startPlatformAgent() {
        try {
            platformSpacecraftAgent.start();
            platformWorldAgent.start();
        } catch (StaleProxyException e) {
            System.out.println("Failed to init Spacecraft");
        }
    }
}
