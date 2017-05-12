import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
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
    private Agent mainAgent;
    private int roundPeriod;
    private int roundPriceIncrement;


    @Override
    protected void setup(){
        mainAgent = this;

        Object[] args = getArguments();

        if (args != null && args.length > 0) {

            companyName = args[0].toString();
            initLoc = args[1].toString();
            finalLoc = args[2].toString();
            averageTicketPrive = Double.parseDouble(args[3].toString());
            itemPrice = Double.parseDouble(args[4].toString());
            roundPeriod = Integer.parseInt(args[5].toString())*1000;
            roundPriceIncrement = Integer.parseInt(args[6].toString());

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

                    myAgent.addBehaviour(new ActionPerformer());

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
        private MessageTemplate mtr;
        private AID highestBidder = null;
        private double highestBid = 0;
        private double roundPrice =0;
        private int roundWithNoOffers =0;


        @Override
        public void action() {
            String conversationID = companyName+"/"+initLoc+"/"+finalLoc;

            switch (step) {
                case 0:
                    //init/reset proposals
                    receivedProposals = new HashMap<>();
                    numExpectedProposals = 0;
                    roundPrice = itemPrice;

                    //annunciate new auction

                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                    for(int i = 0; i < bidderAgents.length; i++) {

                        if (highestBidder == null || (highestBidder != null && bidderAgents[i].compareTo(highestBidder) != 0)) {
                            cfp.addReceiver(bidderAgents[i]);

                            numExpectedProposals++;
                        }
                    }

                    cfp.setContent(conversationID +" || " + roundPrice);

                    cfp.setConversationId(conversationID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());

                    myAgent.send(cfp);

                    // Prepare the template to deal with proposals
                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(conversationID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
                    );

                    step = 1;
                    break;


                case 1:
                    ACLMessage reply = myAgent.receive(mt);

                    if(reply != null) {
                        switch (reply.getPerformative()) {
                            case ACLMessage.PROPOSE:
                                //bid received
                                receivedProposals.put(reply.getSender(), Double.parseDouble(reply.getContent()));

                                System.out.println(reply.getSender().getName() + " bids " + reply.getContent());
                                break;
                            case ACLMessage.REFUSE:
                                //bidder not interested
                                roundWithNoOffers++;
                                break;
                        }

                        if (receivedProposals.size() == numExpectedProposals) {
                            step = 2;
                        }
                    }
                        else {
                            block();
                        }
                    break;
                case 2:
                    // parte das rondas
                    Behaviour loop = new TickerBehaviour( mainAgent, roundPeriod)
                    {
                        protected void onTick() {
                            int roundStep = 0;
                            //contador novas rondas
                            System.out.println("Looper:" + mainAgent.getLocalName());
                            switch (roundStep){
                                case 0:
                                    ACLMessage roundIncMessage = new ACLMessage(ACLMessage.INFORM);
                                    itemPrice+=roundPriceIncrement;
                                    for(int i = 0; i < bidderAgents.length; i++) {

                                        if (highestBidder == null || (highestBidder != null && bidderAgents[i].compareTo(highestBidder) != 0)) {
                                            roundIncMessage.addReceiver(bidderAgents[i]);
                                        }
                                    }
                                    roundIncMessage.setContent("New Item price " + "||" + itemPrice);
                                    roundIncMessage.setConversationId("roundicrementinform");
                                    roundIncMessage.setReplyWith("roundinform" + System.currentTimeMillis());
                                    send(roundIncMessage);

                                    // Prepare the template to deal with proposals
                                    mtr = MessageTemplate.and(
                                            MessageTemplate.MatchConversationId("roundincrementform"),
                                            MessageTemplate.MatchInReplyTo(roundIncMessage.getReplyWith())
                                    );

                                    roundStep = 1;
                                    break;
                                case 1:

                                    break;
                            }
                        }
                    };
                    mainAgent.addBehaviour( loop );

                    
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }

}
