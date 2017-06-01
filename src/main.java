
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.util.Scanner;

/**
 * Created by joselima on 31/05/17.
 */
public class main {

    public static void main(String[] args) throws InterruptedException, StaleProxyException {



        Runtime rt = Runtime.instance();

        // Exit the JVM when there are no more containers around
        rt.setCloseVM(true);

        // Create a default profile
        Profile profile = new ProfileImpl(null, 1300, null);
        profile.setParameter("gui", "ture");

        AgentContainer mainContainer = rt.createMainContainer(profile);
        // now set the default Profile to start a container
        ProfileImpl pContainer = new ProfileImpl(null, 1200, null);
        Thread.sleep(1000);
        System.out.println("SELECT A TEST:");
        System.out.println("1 - standard auction");
        System.out.println("2 - agressive auction");
        System.out.println("3 - multiple bidders auction");
        System.out.println("4 - multiple bidders and auctioneers");

        Scanner reader = new Scanner(System.in);
        System.out.println("Option: ");
        int n = reader.nextInt(); // Scans the next token of the input as an int.


        switch(n){
            case 1:
                //standard auction - nec igual para todos
                //mode = 1 -> nec = 0.1
                //mode = 2 -> nec = 0.5
                //mode = 3 -> nec = 1
                test1(mainContainer,1);
                break;
            case 2:
                //Buyer 1 agressivo os outros standard
                test2(mainContainer);
                break;
            case 3:
                //N bidders -> mode=num_bidders
                test3(mainContainer,8);
                break;
            case 4:
                //2 auctioneers 4 bidders para cada
                test4(mainContainer);
                break;
        }
    }


    //standard auction - necessidade igual para todos os compradores
    public static void test1(AgentContainer mainContainer, int mode) {
        String necessity= null;
        switch(mode){
            case 1:
                necessity = "0.1";
                break;
            case 2:
                necessity = "0.5";
                break;
            case 3:
                necessity = "1";
                break;
        }

        try {
            Object[] bidder_arguments = new Object[4];
            bidder_arguments[0] = "Porto";
            bidder_arguments[1] = "Berlin";
            bidder_arguments[2] = necessity;
            bidder_arguments[3] = "null";

            Object[] auctioneer_arguments = new Object[7];
            auctioneer_arguments[0] = "TAP";
            auctioneer_arguments[1] = "Porto";
            auctioneer_arguments[2] = "Berlin";
            auctioneer_arguments[3] = "100";
            auctioneer_arguments[4] = "50";
            auctioneer_arguments[5] = "1";
            auctioneer_arguments[6] = "10";

            AgentController ag = mainContainer.createNewAgent("Bidder_1",
                    "BidderAgent",
                    bidder_arguments);//arguments
            AgentController ag2 = mainContainer.createNewAgent("Bidder_2",
                    "BidderAgent",
                    bidder_arguments);//arguments
            AgentController ag3 = mainContainer.createNewAgent("Bidder_3",
                    "BidderAgent",
                    bidder_arguments);//arguments
            AgentController ag1 = mainContainer.createNewAgent("Auctioneer",
                    "AuctioneerAgent",
                    auctioneer_arguments);//arguments
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[]{});
            rma.start();
            Thread.sleep(1000);
            ag.start();
            ag2.start();
            ag3.start();
            Thread.sleep(1000);
            ag1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    //1 bidder agressivo 2 standard
    public static void test2(AgentContainer mainContainer) {

        try {
            Object[] auctioneer_arguments = new Object[7];
            auctioneer_arguments[0] = "American Airlines";
            auctioneer_arguments[1] = "Porto";
            auctioneer_arguments[2] = "Berlin";
            auctioneer_arguments[3] = "100";
            auctioneer_arguments[4] = "50";
            auctioneer_arguments[5] = "1";
            auctioneer_arguments[6] = "10";

            Object[] aggressive_bidder = new Object[4];
            aggressive_bidder[0] = "Porto";
            aggressive_bidder[1] = "Berlin";
            aggressive_bidder[2] = "2";
            aggressive_bidder[3] = "null";

            Object[] standard_bidder = new Object[4];
            standard_bidder[0] = "Porto";
            standard_bidder[1] = "Berlin";
            standard_bidder[2] = "0";
            standard_bidder[3] = "null";

            AgentController ag = mainContainer.createNewAgent("Bidder_1",
                    "BidderAgent",
                    aggressive_bidder);//arguments
            AgentController ag2 = mainContainer.createNewAgent("Bidder_2",
                    "BidderAgent",
                    standard_bidder);//arguments
            AgentController ag3 = mainContainer.createNewAgent("Bidder_3",
                    "BidderAgent",
                    standard_bidder);//arguments
            AgentController ag1 = mainContainer.createNewAgent("Auctioneer",
                    "AuctioneerAgent",
                    auctioneer_arguments);//arguments
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[]{});
            rma.start();
            Thread.sleep(1000);
            ag.start();
            ag2.start();
            ag3.start();
            Thread.sleep(1000);
            ag1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void test3(AgentContainer mainContainer, int mode) throws StaleProxyException {
        String necessity= null;
        int bidders_size = mode;

        Object[] bidder_arguments = new Object[4];
        bidder_arguments[0] = "Porto";
        bidder_arguments[1] = "Berlin";
        bidder_arguments[2] = "0.5";
        bidder_arguments[3] = "null";

        Object[] auctioneer_arguments = new Object[7];
        auctioneer_arguments[0] = "TAP";
        auctioneer_arguments[1] = "Porto";
        auctioneer_arguments[2] = "Berlin";
        auctioneer_arguments[3] = "100";
        auctioneer_arguments[4] = "50";
        auctioneer_arguments[5] = "1";
        auctioneer_arguments[6] = "10";

        try {
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[]{});
            rma.start();
            Thread.sleep(1000);

            AgentController ag1 = mainContainer.createNewAgent("Auctioneer",
                    "AuctioneerAgent",
                    auctioneer_arguments);//arguments

            for(int i=1;i<=bidders_size;i++){
                String bidder_name = "Bidder_"+i;
                AgentController ag = mainContainer.createNewAgent(bidder_name,
                        "BidderAgent",
                        bidder_arguments);//arguments
                ag.start();
            }

            Thread.sleep(1000);
            ag1.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void test4(AgentContainer mainContainer) throws StaleProxyException {
        int bidders_size = 8;

        Object[] bidder_arguments_1 = new Object[4];
        bidder_arguments_1[0] = "Porto";
        bidder_arguments_1[1] = "Berlin";
        bidder_arguments_1[2] = "0.5";
        bidder_arguments_1[3] = "null";

        Object[] bidder_arguments_2 = new Object[4];
        bidder_arguments_2[0] = "Lisboa";
        bidder_arguments_2[1] = "Paris";
        bidder_arguments_2[2] = "0.5";
        bidder_arguments_2[3] = "null";


        Object[] auctioneer_arguments_1 = new Object[7];
        auctioneer_arguments_1[0] = "TAP";
        auctioneer_arguments_1[1] = "Porto";
        auctioneer_arguments_1[2] = "Berlin";
        auctioneer_arguments_1[3] = "100";
        auctioneer_arguments_1[4] = "50";
        auctioneer_arguments_1[5] = "1";
        auctioneer_arguments_1[6] = "10";

        Object[] auctioneer_arguments_2 = new Object[7];
        auctioneer_arguments_2[0] = "American Airlines";
        auctioneer_arguments_2[1] = "Lisboa";
        auctioneer_arguments_2[2] = "Paris";
        auctioneer_arguments_2[3] = "200";
        auctioneer_arguments_2[4] = "50";
        auctioneer_arguments_2[5] = "1";
        auctioneer_arguments_2[6] = "10";

        try {
            AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", new Object[]{});
            rma.start();
            Thread.sleep(1000);

            AgentController ag1 = mainContainer.createNewAgent("Auctioneer_1",
                    "AuctioneerAgent",
                    auctioneer_arguments_1);//arguments

            AgentController ag2 = mainContainer.createNewAgent("Auctioneer_2",
                    "AuctioneerAgent",
                    auctioneer_arguments_2);//arguments

            for(int i=1;i<=bidders_size;i++){
                String bidder_name = "Bidder_"+i;
                if(i<5){
                    AgentController ag = mainContainer.createNewAgent(bidder_name,
                            "BidderAgent",
                            bidder_arguments_1);//arguments
                    ag.start();
                }
                else{
                    AgentController ag = mainContainer.createNewAgent(bidder_name,
                            "BidderAgent",
                            bidder_arguments_2);//arguments
                    ag.start();
                }
            }

            Thread.sleep(1000);
            ag1.start();
            ag2.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}


