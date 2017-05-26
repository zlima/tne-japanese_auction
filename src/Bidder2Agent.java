import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.proto.SSIteratedContractNetResponder;
import jade.proto.SSResponderDispatcher;
import sun.management.Agent;

/**
 * Created by joselima on 25/05/17.
 */
public class Bidder2Agent extends jade.core.Agent {

    private String currentLoc;
    private String finalLoc;
    private int wallet;
    private Double currentPrice;
    private Double averageTicketPrive;
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

            System.out.println(getAID().getName() + " ready to buy some stuff. My wallet is $" + wallet);

            MessageTemplate template = MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_ITERATED_CONTRACT_NET),
                    MessageTemplate.MatchPerformative(ACLMessage.CFP));

            addBehaviour(new SSResponderDispatcher(this, template) {
                @Override
                protected Behaviour createResponder(ACLMessage aclMessage) {
                    addBehaviour(new SSIteratedContractNetResponder(this.getAgent(), aclMessage) {

                        @Override
                        protected ACLMessage handleCfp(ACLMessage cfp) {

                            if(firstCFP){
                                handleFirstCFP(cfp);
                            }
                            else{

                            }
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

    private Proposal handleFirstCFP(ACLMessage cfp){
        parseCFP(cfp);

    }

    private void parseCFP(ACLMessage msg){
        Proposal proposal = null;

        try{
            proposal = (Proposal) msg.getContentObject();
            currentProposal = new Proposal(proposal.getCompanyName(),proposal.getInitLoc(),proposal.getFinalLoc(),proposal.getAverageTicketPrice(),proposal.getItemPrice(),msg.getSender());
            firstCFP = false;
        } catch (UnreadableException e) {
            e.printStackTrace();
        }

    }

    private void setRandomWallet() {
        int min = 10000;
        Integer max = Integer.MAX_VALUE;

        // wallet = ThreadLocalRandom.current().nextInt(min, max);
        wallet = 999999999;
    }
}
