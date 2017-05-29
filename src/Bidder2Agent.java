import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;
import jade.proto.SSResponderDispatcher;
import sun.management.Agent;

import java.io.IOException;

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
                                if(currentProposal.getCurrentItemPrice() < (wallet/2)){//fazer aqui calculo para decidir se continua
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

    private ACLMessage handleFirstCFP(ACLMessage cfp) throws IOException {
        parseCFP(cfp);
        ACLMessage propose = cfp.createReply();



        //verificar aqui se quer entrar no leilao ou nao
        propose.setPerformative(ACLMessage.PROPOSE);
        propose.setContentObject(new String("Quero Entrar"));

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
        int min = 10000;
        Integer max = Integer.MAX_VALUE;

        // wallet = ThreadLocalRandom.current().nextInt(min, max);
        wallet = 999999999;
    }
}
