import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by joselima on 25/05/17.
 */
public class Auctioner2Agent extends Agent {

    private ArrayList<AID> bidderAgents;

    private String companyName;
    private String initLoc;
    private String finalLoc;
    private Double itemPrice;
    private Double averageTicketPrive;
    private Agent mainAgent;
    private int roundPeriod;
    private int roundPriceIncrement;
    private int roundCounter=0;
    private int negotiationParticipants;

    @Override
    protected void setup() {


        Object[] args = getArguments();

        //if (args != null && args.length > 0) {
        if (args != null) {
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

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-bidder");
            template.addServices(sd);

            try{
                DFAgentDescription [] result = DFService.search(this,template);
                negotiationParticipants = bidderAgents.size();
                for(int i=0; i< result.length;i++){
                    System.out.println("Found new bidder: " + result[i].getName());
                    bidderAgents.add(result[i].getName());
                }

            } catch (FIPAException e) {

            }

            ACLMessage msg = sendFirstCFP();

            addBehaviour(new ContractNetInitiator(this, msg) {
                @Override
                protected void handlePropose(ACLMessage propose, Vector v) {

                }

                @Override
                protected void handleRefuse(ACLMessage refuse) {
                    for(int i=bidderAgents.size()-1;i>=0;i--){
                        if(bidderAgents.get(i).getName().equals(refuse.getSender().getName())){
                            bidderAgents.remove(i);
                            negotiationParticipants--;
                        }
                    }

                }

                @Override
                protected void handleAllResponses(Vector responses, Vector acceptances) {

                    if (responses.size() < negotiationParticipants) {
                        //handle timeout
                      updateBidders(responses);
                    }

                    Enumeration e = responses.elements();
                    while (e.hasMoreElements()) {
                        if(msg.getPerformative() == ACLMessage.PROPOSE){
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.CFP);
                            acceptances.addElement(reply);

                        }

                        if (msg.getPerformative() == ACLMessage.REFUSE) {
                            responses.remove(msg);
                        }

                    }
                    updateRound();
                }

            });


        }else{
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }

    private void updateBidders(Vector responses){

        for (int i=bidderAgents.size()-1; i>=0; i--){
            for (int y=responses.size();y>=0;y--){
                ACLMessage response = (ACLMessage)responses.get(y);
                if( response.getSender().getName().equals(bidderAgents.get(i).getName())){
                    bidderAgents.remove(i);
                    negotiationParticipants--;
                }
            }
        }
    }

    private ACLMessage sendFirstCFP(){

        Proposal cfp = new Proposal(companyName, initLoc, finalLoc, averageTicketPrive, itemPrice,this.getAID());

       ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (int i = 0; i < bidderAgents.size(); i++) {
            msg.addReceiver(bidderAgents.get(i));

        }

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET);

        msg.setReplyByDate(new Date(System.currentTimeMillis() + 50000));

        try {
            msg.setContentObject(cfp);
            updateRound();
        } catch (IOException e) {
            System.out.println("error creating first CFP");
        }

        return msg;
    }

    private void updateRound(){
        roundCounter++;
        itemPrice += roundPriceIncrement;
    }

}
