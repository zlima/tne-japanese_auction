import com.sun.tools.doclets.formats.html.SourceToHTMLConverter;
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
import java.util.*;

/**
 * Created by joselima on 25/05/17.
 */
public class Auctioner2Agent extends Agent {

    private ArrayList<AID> bidderAgents;
    private ArrayList<AID> lastBidders;

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
    private boolean randomWinner = false;

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

                }

                @Override
                protected void handleRefuse(ACLMessage refuse) {
                   //removeBidder(refuse);
                }


                @Override
                protected void handleAllResponses(Vector responses, Vector acceptances){
                    Vector msgToDelete = new Vector();
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
                            if(randomWinner){
                                Iterator cenas = reply.getAllReceiver();
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                int winner = generateRandomWinner();
                                while(cenas.hasNext()){
                                    AID agent =(AID)cenas.next();
                                    //voltar aqui
                                    if(agent == lastBidders.get(winner)){
                                        acceptances.addElement(reply);
                                        newIteration(acceptances);
                                        return;
                                    }
                                }

                            }
                            else if(lastRound){
                                reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                                acceptances.addElement(reply);
                                newIteration(acceptances);
                                return;
                            }
                            else{
                                reply.setPerformative(ACLMessage.CFP);
                                Proposal cfp = new Proposal(companyName, initLoc, finalLoc, averageTicketPrive,itemPrice,currentItemPrice, roundPriceIncrement,roundCounter,getAID());
                                try {
                                    reply.setContentObject(cfp);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                acceptances.addElement(reply);
                            }

                        }

                       else if (msg.getPerformative() == ACLMessage.REFUSE) {
                            System.out.println("Auctioner: " + msg.getSender().getLocalName() +" Abandonou o Leilao.");
                            msgToDelete.addElement(msg);
                        }

                        else if(msg.getPerformative() == ACLMessage.INFORM){
                            System.out.println("Vencedor do Leilão é: "+bidderAgents.get(0).getLocalName()+ " e terá que pagar: "+currentItemPrice);
                            doDelete();
                        }

                    }

                    doWait(3000);
                    for(int i=0;i<msgToDelete.size();i++){
                        responses.remove(msgToDelete.get(i));
                    }
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

    private int generateRandomWinner(){

        Random rand = new Random();

        return rand.nextInt(((lastBidders.size()-1)) - 0) + 0;
    }

    private void updateBidders(Vector responses){

        bidderAgents = new ArrayList<>();
        ACLMessage aux;
        for(int i=0; i< responses.size();i++){
            aux = (ACLMessage)responses.get(i);
            bidderAgents.add(aux.getSender());
        }
        negotiationParticipants = bidderAgents.size();
    }

    private ACLMessage sendFirstCFP(){

        Proposal cfp = new Proposal(companyName, initLoc, finalLoc, averageTicketPrive,itemPrice,currentItemPrice, roundPriceIncrement,roundCounter,this.getAID());

       ACLMessage msg = new ACLMessage(ACLMessage.CFP);
        for (int i = 0; i < bidderAgents.size(); i++) {
            msg.addReceiver(bidderAgents.get(i));

        }

        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET);

        msg.setReplyByDate(new Date(System.currentTimeMillis() + 5000));

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
        } else if(negotiationParticipants == 0){
            System.out.println("Auctioner: Last "+lastBidders.size()+" bidders quit at same time.. Random will decide the winner.");
            randomWinner = true;
            return true;
        } else {
            updateLastBidders();
            return false;
        }
    }

    private void updateLastBidders(){
        lastBidders = new ArrayList<>();
        for(int i=0;i<bidderAgents.size();i++){
            lastBidders.add(bidderAgents.get(i));
        }
    }

    private void updateRound(Vector responses) {
        updateBidders(responses);
        if(!checkLastRound()){
            roundCounter++;
            currentItemPrice += roundPriceIncrement;

            StringBuilder cenas = new StringBuilder("Auctioner: Next Round... Round: "+roundCounter+" Item Price: "+currentItemPrice+". Participants: ");

            for(int i=0; i<bidderAgents.size();i++){
                if(i==0){
                    cenas.append(bidderAgents.get(i).getLocalName());
                }else
                    cenas.append(", "+bidderAgents.get(i).getLocalName());
            }
             cenas.append(".");
            System.out.println(cenas.toString());

        }else{
            //acabar aqui o cenas
            lastRound = true;
        }

    }


}
