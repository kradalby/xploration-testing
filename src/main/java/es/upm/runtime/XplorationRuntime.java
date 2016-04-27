
package es.upm.runtime;

import jade.core.NotFoundException;
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
    static final String companyClassPathTemplate = "es.upm.company0%d.Company[code=%s%s]";
    static final String platformClassPathTemplate = "es.upm.platform0%d.Spacecraft[code=%s%s]";
    static final String teamLibPathPattern= "Team0%s.*[.]jar";
    static int amountOfTeams = 5;
    int usedPlatformID = 3;
    String libsPath = "./libs/";
    Runtime jadeRuntime;
    AgentContainer mainContainer;
    AgentController rmaController;
    ArrayList<AgentController> companyAgents = new ArrayList<AgentController>();
    AgentController platformAgent;

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

    public void bootJade() throws StaleProxyException {
        jadeRuntime = Runtime.instance();
        jadeRuntime.setCloseVM(true);
        ProfileImpl profile = new ProfileImpl(null, 1200, null);
        mainContainer = jadeRuntime.createMainContainer(profile);
        rmaController = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
        rmaController.start();
    }

    public void createCompanyAgents() {
        for(int i = 1; i <= amountOfTeams; ++i) {
            try {
                String path = findPathForTeam(i);
                if(path == null)
                {
                    System.out.printf("Could not find JAR for Team0%s%n", i);
                    continue;
                }
                String companyPath = String.format(companyClassPathTemplate,
                        i , libsPath, path);
                System.out.println("Trying to load " + companyPath);
                companyAgents.add(mainContainer.createNewAgent("Company0" + i, companyPath, null));
            } catch (Exception ex) {
                System.out.println("Failed to init Company" + i);
            }
        }

    }
    public String findPathForTeam(int i)
    {
        File[] dir = new File(libsPath).listFiles();
        String patternStr = String.format(teamLibPathPattern, i);
        Pattern pattern = Pattern.compile(patternStr);
        for (File file : dir) {
            if(pattern.matcher(file.getName()).matches())
            {
                return file.getName();
            }
        }
        return null;
    }
    public void runCompanyAgents() {
        for (AgentController ac : companyAgents) {
            try {
                ac.start();
            } catch (StaleProxyException ex) {
                System.out.println("Failed to start an agent.");
            }
        }

    }

    public void createPlatformAgent() {
        try {
            String path = findPathForTeam(usedPlatformID);
            if(path == null)
            {
                System.out.printf("Could not find JAR for Platform0%s%n", usedPlatformID);
                throw new NotFoundException("Platform agent was not found!");
            }

            String companyPath = String.format(platformClassPathTemplate,
                    usedPlatformID, libsPath, path);
            System.out.println("Trying to load " + companyPath);
            platformAgent = mainContainer.createNewAgent("Spacecraft0" + usedPlatformID, companyPath, null);
            System.out.printf("Agent %s loaded successfully%n", companyPath);
        } catch (Exception ex) {
            System.out.println("Failed to init Spacecraft0" + usedPlatformID);
        }
    }

    public void runPlatformAgent() {
        try {
            platformAgent.start();
        } catch (StaleProxyException e) {
            System.out.println("Failed to init Spacecraft");
        }
    }
}
