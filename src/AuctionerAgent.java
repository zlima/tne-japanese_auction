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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by joselima on 27/04/17.
 */

// ticketbehaviour
public class AuctionerAgent extends Agent {
/*
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

        //if (args != null && args.length > 0) {
        if(args != null){
            //companyName = args[0].toString();
            //initLoc = args[1].toString();
            //finalLoc = args[2].toString();
            //averageTicketPrive = Double.parseDouble(args[3].toString());
            //itemPrice = Double.parseDouble(args[4].toString());
            //roundPeriod = Integer.parseInt(args[5].toString())*1000;
            //roundPriceIncrement = Integer.parseInt(args[6].toString());

            companyName = "cenas";
            initLoc = "cena1";
            finalLoc = "cenas2";
            averageTicketPrive = 200.0;
            itemPrice = 10.0;
            roundPeriod = 40000;
            roundPriceIncrement = 10;


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

                   addBehaviour(new InitBehaviour());

                }
            });


        }else{
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }

    private class InitBehaviour extends Behaviour{
        int roundStep = 0;
        private ArrayList<AID> receivedProposals = new ArrayList<>();
        private int numExpectedProposals;

        private MessageTemplate mt;
        private double roundPrice =0;

        @Override
        public void action() {
            String conversationID = companyName+"||"+initLoc+"||"+finalLoc;


            //contador novas rondas

            switch (roundStep) {

                case 0:
                    //init/reset proposals
                    receivedProposals = new ArrayList<>();
                    numExpectedProposals = 0;
                    roundPrice = itemPrice;

                    //annunciate new auction

                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                    for (int i = 0; i < bidderAgents.length; i++) {
                        cfp.addReceiver(bidderAgents[i]);

                    }


                    cfp.setContent("InitBid" + "||" + conversationID + "||" + averageTicketPrive + "||" + roundPrice);

                    cfp.setConversationId(conversationID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());

                    myAgent.send(cfp);


                    // Prepare the template to deal with proposals
                    mt = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(conversationID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith())
                    );
                    roundStep = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(mt);
                    if (reply != null) {
                        switch (reply.getPerformative()) {
                            case ACLMessage.ACCEPT_PROPOSAL:
                                //bid received
                                receivedProposals.add(reply.getSender());
                                System.out.println(reply.getSender().getName() + " Auction Bidder ");
                                break;
                            case ACLMessage.REFUSE:
                                //bidder not interested

                                break;
                        }

                        if (receivedProposals.size() > 0) {
                            addBehaviour(new ActionPerformer(myAgent,5000,receivedProposals));
                            roundStep =3;
                        }
                    } else {
                        block();
                    }
                    break;
                case 3:
                    break;
            }

        }

        @Override
        public boolean done() {
            if (roundStep == 3) return true;
            else return false;
        }
    }

    private class ActionPerformer extends TickerBehaviour{
        private MessageTemplate mtr;
        int roundStep=0;
        private ArrayList<AID> receivedProposals = new ArrayList<>();



        public ActionPerformer(Agent a, long period, ArrayList<AID> receivedProposals) {
            super(a, period);
            this.receivedProposals = receivedProposals;
        }

        @Override
        public void onStart() {
            super.onStart();
            setFixedPeriod(true);


        }


        protected void onTick() {
            System.out.println(getTickCount());
            String conversationID = companyName+"||"+initLoc+"||"+finalLoc;

                //contador novas rondas

                switch (roundStep){
                    case 0:
                        // parte das rondas
                        ACLMessage roundIncMessage = new ACLMessage(ACLMessage.INFORM);
                        itemPrice+=roundPriceIncrement;

                        for (int i = 0; i < receivedProposals.size(); i++) {
                            roundIncMessage.addReceiver(receivedProposals.get(i));

                        }

                        roundIncMessage.setContent("New Item price" + "||" + itemPrice);
                        roundIncMessage.setConversationId("roundicrementinform");
                        roundIncMessage.setReplyWith("roundinform" + System.currentTimeMillis());
                        send(roundIncMessage);

                        // Prepare the template to deal with proposals
                        mtr = MessageTemplate.and(
                                MessageTemplate.MatchConversationId("roundincrementform"),
                                MessageTemplate.MatchInReplyTo(roundIncMessage.getReplyWith())
                        );
                        roundStep = 1;
                    case 1:
                        test cenas;
                       addBehaviour( cenas = new test());
                       receivedProposals = cenas.getReplys();
                        roundStep = 0;

                        break;
                }
            }
        }

        public class test extends Behaviour{

            @Override
            public void action() {

            }

            @Override
            public boolean done() {
                return false;
            }
        }

*/
}
