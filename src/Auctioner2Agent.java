import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
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
    private int roundPeriod;
    private int roundPriceIncrement;
    private int roundCounter=0;
    private int negotiationParticipants;
    private Double currentItemPrice;
    private boolean lastRound = false;

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
            currentItemPrice = itemPrice;


            System.out.println("This time I'm selling a ticket from " + companyName + ", from " + initLoc + " to " + finalLoc + ". Initial price is: " + itemPrice);
            System.out.println("Please make your bids!");

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("auction-bidder");
            template.addServices(sd);



            try{
                DFAgentDescription [] result = DFService.search(this,template);
                bidderAgents = new ArrayList<>();
                for(int i=0; i< result.length;i++){
                    System.out.println("Found new bidder: " + result[i].getName());
                    bidderAgents.add(result[i].getName());
                }
                negotiationParticipants = bidderAgents.size();

            } catch (FIPAException e) {

            }

            ACLMessage msg = sendFirstCFP();

            addBehaviour(new ContractNetInitiator(this, msg) {
                @Override
                protected void handlePropose(ACLMessage propose, Vector v) {

                    try {
                        String cenas = (String) propose.getContentObject();
                        System.out.println(cenas);
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }


                }

                @Override
                protected void handleRefuse(ACLMessage refuse) {
                   removeBidder(refuse);
                }

                @Override
                protected void handleAllResponses(Vector responses, Vector acceptances) {

                    if (responses.size() < negotiationParticipants) {
                        //handle timeout
                        System.out.println("timeout");
                      updateBidders(responses);
                    }

                    Enumeration e = responses.elements();
                    while (e.hasMoreElements()) {
                        ACLMessage msg = (ACLMessage) e.nextElement();
                        if(msg.getPerformative() == ACLMessage.PROPOSE){
                            ACLMessage reply = msg.createReply();
                            if(lastRound){
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                acceptances.addElement(reply);
                            }
                            else{
                                reply.setPerformative(ACLMessage.CFP);
                                acceptances.addElement(reply);
                            }

                        }

                        if (msg.getPerformative() == ACLMessage.REFUSE) {
                            removeBidder(msg);
                            responses.remove(msg);
                        }

                        if(msg.getPerformative() == ACLMessage.INFORM){
                            System.out.println("Vencedor do Leilão é: "+bidderAgents.get(0).getName()+ " e terá que pagar: "+currentItemPrice);
                            doDelete();
                        }

                    }

                    doWait(3000);
                    updateRound(responses);
                    newIteration(acceptances);
                }

            });


        }else{
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }

    private void removeBidder(ACLMessage refuse){
        for(int i=bidderAgents.size()-1;i>=0;i--){
            if(bidderAgents.get(i).getName().equals(refuse.getSender().getName())){
                bidderAgents.remove(i);
                negotiationParticipants--;
            }
        }
    }

    private void updateBidders(Vector responses){

        for (int i=bidderAgents.size()-1; i>=0; i--){
            for (int y=responses.size()-1;y>=0;y--){
                int counter =1;
                ACLMessage response = (ACLMessage)responses.get(y);
                if( response.getSender().getName().equals(bidderAgents.get(i).getName())){
                    break;
                }
                else if(counter == bidderAgents.size() ){
                    bidderAgents.remove(i);
                    negotiationParticipants--;
                }else{
                    counter++;
                }
            }
        }
    }

    private ACLMessage sendFirstCFP(){

        Proposal cfp = new Proposal(companyName, initLoc, finalLoc, averageTicketPrive,itemPrice,currentItemPrice, roundPriceIncrement,roundCounter,this.getAID());

       ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (int i = 0; i < bidderAgents.size(); i++) {
            msg.addReceiver(bidderAgents.get(i));

        }

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET);

        msg.setReplyByDate(new Date(System.currentTimeMillis() + 3000));

        try {
            msg.setContentObject(cfp);
        } catch (IOException e) {
            System.out.println("error creating first CFP");
        }

        return msg;
    }

    private boolean checkLastRound(){

        if(negotiationParticipants == 1){

            return true;
        }

        else return false;
    }

    private void updateRound(Vector responses) {
        updateBidders(responses);
        if(!checkLastRound()){
            roundCounter++;
            currentItemPrice += roundPriceIncrement;
        }else{
            //acabar aqui o cenas
            lastRound = true;
        }

    }


}
