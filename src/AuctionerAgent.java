import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joselima on 27/04/17.
 */

// ticketbehaviour
public class AuctionerAgent extends Agent {

    private AID[] bidderAgents;

    private String companyName;
    private String initLoc;
    private String finalLoc;
    private Double itemPrice;
    private Double averageTicketPrive;


    @Override
    protected void setup(){
        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            companyName = args[0].toString();
            initLoc = args[1].toString();
            finalLoc = args[2].toString();
            averageTicketPrive = Double.parseDouble(args[3].toString());
            itemPrice = Double.parseDouble(args[4].toString());

            System.out.println("This time I'm selling a ticket from " + companyName + ", from " + initLoc + " to " + finalLoc + ". Initial price is: " + itemPrice);
            System.out.println("Please make your bids!");


            addBehaviour(new OneShotBehaviour() {
                @Override
                public void action() {
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("auction-bidder");
                    template.addServices(sd);

                    try{
                        DFAgentDescription [] result = DFService.search(myAgent,template);

                        bidderAgents = new AID[result.length];
                        for(int i=0; i< result.length;i++){
                            System.out.println("Found new bidder: " + result[i].getName());
                            bidderAgents[i] = result[i].getName();

                        }

                    } catch (FIPAException e) {

                    }

                    myAgent.addBehaviour(new ActionPerformer);

                }
            });


        }else{
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }

    private class ActionPerformer extends Behaviour{

        private int step = 0;
        private Map<AID,Double> receivedProposals= new HashMap<>();
        private int numExpectedProposals = 0;

        private MessageTemplate mt;
        private AID highestBidder = null;
        private double highestBid = 0;


        @Override
        public void action() {

            switch (step) {
                case 0:
                    //init/reset proposals
                    receivedProposals = new HashMap<>();
                    numExpectedProposals = 0;

                    break;
                case 1:
                    break;
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }

}
