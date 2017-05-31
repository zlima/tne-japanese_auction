
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Created by joselima on 31/05/17.
 */
public class main {

    public static void main(String[] args){

        Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);

        // Create a default profile
        Profile profile = new ProfileImpl(null, 1300, null);

        AgentContainer mainContainer = rt.createMainContainer(profile);

        // now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);


        AgentContainer cont = rt.createAgentContainer(pContainer);

        try {
            Object[] arguments = new Object[3];
            arguments[0] = "0.1";
            arguments[1] = "0.1";
            arguments[2] = "0.1";
            AgentController ag = mainContainer.createNewAgent("Buyer1",
                    "Bidder2Agent",
                    arguments);//arguments
            AgentController ag2 = mainContainer.createNewAgent("Buyer2",
                    "Bidder2Agent",
                    arguments);//arguments
            AgentController ag3 = mainContainer.createNewAgent("Buyer3",
                    "Bidder2Agent",
                    arguments);//arguments
            AgentController ag1 = mainContainer.createNewAgent("Auc1",
                    "Auctioner2Agent",
                    new Object[] {});//arguments

            ag.start();
            ag2.start();
            ag3.start();

            ag1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
