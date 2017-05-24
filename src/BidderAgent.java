import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by joselima on 11/05/17.
 */
public class BidderAgent extends Agent {

    private String currentLoc;
    private String finalLoc;
    private int wallet;
    private Double currentPrice;
    private Double averageTicketPrive;

    @Override
    protected void setup() {

        Object[] args = getArguments();

        //if (args != null && args.length > 0) {
        if(args!=null){
            setRandomWallet();

            //currentLoc = args[0].toString();
            //finalLoc = args[1].toString();

            currentLoc = "cena1";
            finalLoc = "cena2";


            // Register the auction-seller service in the yellow pages
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-bidder");
            sd.setName("MultiAgentSystem-auctions");
            dfd.addServices(sd);

            addBehaviour(new BidRequestsServer());

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
        Integer max = Integer.MAX_VALUE;

       // wallet = ThreadLocalRandom.current().nextInt(min, max);
        wallet = 999999999;
    }


    private class BidRequestsServer extends Behaviour{

            private String companyName, initLoc, finalLoc;
            private Double averageTicketPrice, currentRoundPrice;

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive();

            if(msg != null){

                switch (msg.getPerformative()){
                    case ACLMessage.CFP:
                        parseContent(msg.getContent());
                        ACLMessage reply = msg.createReply();

                        if(currentRoundPrice < wallet){//calculo para decidir se entra no leilao
                            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                            addBehaviour(new Negotiation(msg.getPerformative()));

                        } else{
                            reply.setPerformative(ACLMessage.REFUSE);
                        }

                        myAgent.send(reply);
                        break;
                    case ACLMessage.INFORM:
                        System.out.println(msg.getContent());
                        break;
                }

            }else {
                block();
            }

        }

        private void parseContent(String content){
            String[] split = content.split("\\|\\|");
            if(split[0].equals("InitBid")){
                companyName = split[1];
                initLoc = split[2];
                finalLoc = split[3];
                averageTicketPrice = Double.parseDouble(split[4]);
                currentRoundPrice = Double.parseDouble(split[5]);
            }

        }

        @Override
        public boolean done() {
            return false;
        }


    }


   private class Negotiation extends Behaviour{
        private int performative;

        public Negotiation(int Performative){
            this.performative = Performative;
        }

        @Override
        public void action() {
            MessageTemplate mtr = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive();

            if(msg != null) {
                System.out.println("oiiiiiii recebi");
                parseContent(msg.getContent());
                ACLMessage reply = msg.createReply();
            }else{
                block();
            }

            }

            private void parseContent(String content){
                String[] split = content.split("\\|\\|");
                System.out.println(split[0]);
                System.out.println(split[1]);

            }

        @Override
        public boolean done() {
            return false;
        }
    }

    }



