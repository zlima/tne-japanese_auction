import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;
import jade.proto.SSResponderDispatcher;
import sun.management.Agent;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by joselima on 25/05/17.
 */
public class Bidder2Agent extends jade.core.Agent {

    private String currentLoc;
    private String finalLoc;
    private int wallet;
    private boolean firstCFP;
    private Proposal currentProposal;

    @Override
    protected void setup(){
        Object[] args = getArguments();
        firstCFP = true;

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

            try {
                DFService.register(this,dfd);
                System.out.println(getAID().getName() + " ready to buy some stuff. My wallet is $" + wallet);
            } catch (FIPAException e) {
                e.printStackTrace();
            }



            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));

            addBehaviour(new SSResponderDispatcher(this, template) {
                @Override
                protected Behaviour createResponder(ACLMessage aclMessage) {
                    addBehaviour(new SSIteratedContractNetResponder(this.getAgent(), aclMessage) {

                        @Override
                        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose,ACLMessage accept) throws FailureException {
                            ACLMessage inform = accept.createReply();
                            inform.setPerformative(ACLMessage.INFORM);
                            try {
                                inform.setContentObject(new String("sou o champ confirmo"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            return inform;
                        }


                        @Override
                        protected ACLMessage handleCfp(ACLMessage cfp) throws RefuseException {

                            if(firstCFP){
                                try {
                                    ACLMessage propose = handleFirstCFP(cfp);
                                    firstCFP = false;
                                    return propose;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                            else{
                                parseCFP(cfp);
                                if(acceptProposal(currentProposal)){//fazer aqui calculo para decidir se continua
                                    ACLMessage propose = cfp.createReply();
                                    propose.setPerformative(ACLMessage.PROPOSE);

                                    try {
                                        propose.setContentObject(new String("Continuo"));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    return propose;
                                }
                                else{
                                    ACLMessage refuse = cfp.createReply();
                                    refuse.setPerformative(ACLMessage.REFUSE);

                                    try {
                                        refuse.setContentObject(new String("Quero Sair."));
                                        System.out.println("sair nao te curto");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return refuse;
                                }


                            }
                            throw new RefuseException("rip");
                        }

                    });

                    return null;
                }
                });

        } else {
            System.out.println("rip nos args");
            //terminate agent
            doDelete();
        }
    }

//funcao para calcular se aceita ou nao continuar no leilao
private boolean acceptProposal(Proposal proposal){

    boolean response = false;

        if(checkFlightDestination(proposal)){

        }else{
            response = false;
        }

        return response;
}

private boolean checkFlightDestination(Proposal proposal){
    boolean response;

    if(proposal.getInitLoc().equals(currentLoc) && proposal.getFinalLoc().equals(finalLoc)){
        response = true;
    }else
        response=false;
    
    return response;
}

    private ACLMessage handleFirstCFP(ACLMessage cfp) throws IOException {
        parseCFP(cfp);
        ACLMessage propose = cfp.createReply();
        if(acceptProposal(currentProposal)){

            //verificar aqui se quer entrar no leilao ou nao
            propose.setPerformative(ACLMessage.PROPOSE);
            propose.setContentObject(new String("Quero Entrar"));
        }else{

            propose.setPerformative(ACLMessage.REFUSE);
            propose.setContentObject(new String("Nao quero entrar neste leilao"));
        }


       return propose;
    }

    private Proposal parseCFP(ACLMessage msg){
        Proposal proposal = null;

        try{
            proposal = (Proposal) msg.getContentObject();
            currentProposal = new Proposal(proposal.getCompanyName(),proposal.getInitLoc(),proposal.getFinalLoc(),proposal.getAverageTicketPrice(),proposal.getItemPrice(),proposal.getCurrentItemPrice(),proposal.getRoundIncrement(),proposal.getCurrentRound(),proposal.getSender());
        } catch (UnreadableException e) {
            e.printStackTrace();
        }
        return proposal;
    }

    private void setRandomWallet() {
        Integer min = 200;
        Integer max = 5000;

         wallet = ThreadLocalRandom.current().nextInt(min, max);

    }
}
