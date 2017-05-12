import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by joselima on 11/05/17.
 */
public class BidderAgent extends Agent {

    private String currentLoc;
    private String finalLoc;
    private Double wallet;
    private Double currentPrice;
    private Double averageTicketPrive;

    @Override
    protected void setup() {

        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            setRandomWallet();

            currentLoc = args[0].toString();
            currentLoc = args[1].toString();
            finalLoc = args[2].toString();


            addBehaviour(new BidRequestsServer());

            // Register the auction-seller service in the yellow pages
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-bidder");
            sd.setName("MultiAgentSystem-auctions");
            dfd.addServices(sd);

            try {
                DFService.register(this, dfd);
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            System.out.println(getAID().getName() + " ready to buy some stuff. My wallet is $" + wallet);

        } else {
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        System.out.println("Bidder " + getAID().getName() + " terminating");
    }


    private void setRandomWallet() {
        int min = 10000;
        Double max = Double.MAX_VALUE;

        wallet = ThreadLocalRandom.current().nextDouble(min, max);
    }


    private class BidRequestsServer extends Behaviour{


        @Override
        public void action() {

        }

        @Override
        public boolean done() {
            return false;
        }
    }

    }



