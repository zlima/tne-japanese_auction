
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

    public static void main(String[] args) throws InterruptedException {

        Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);

        // Create a default profile
        Profile profile = new ProfileImpl(null, 1300, null);
        profile.setParameter("gui", "ture");

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
                    "BidderAgent",
                    arguments);//arguments
            AgentController ag2 = mainContainer.createNewAgent("Buyer2",
                    "BidderAgent",
                    arguments);//arguments
            AgentController ag3 = mainContainer.createNewAgent("Buyer3 ",
                    "BidderAgent",
                    arguments);//arguments
            AgentController ag1 = mainContainer.createNewAgent("Auc1",
                    "AuctionerAgent",
                    new Object[] {});//arguments
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma" , new Object[] {});

            rma.start();
            Thread.sleep(1000);
            ag.start();
            ag2.start();
            ag3.start();
            Thread.sleep(1000);
            ag1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }

    }

}
