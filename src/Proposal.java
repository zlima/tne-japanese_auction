import jade.core.AID;

import java.io.Serializable;

/**
 * Created by joselima on 25/05/17.
 */
public class Proposal implements Serializable {
    private  String companyName;
    private String initLoc;
    private String finalLoc;
    private Double averageTicketPrice;
    private Double itemPrice;
    private AID sender;


    public Proposal(String companyName, String initLoc, String finalLoc, Double averageTicketPrice, Double itemPrice, AID sender){
    this.companyName=companyName;
    this.initLoc=initLoc;
    this.finalLoc=finalLoc;
    this.averageTicketPrice = averageTicketPrice;
    this.itemPrice=itemPrice;
    this.sender = sender;
    }


    public String getCompanyName() {
        return companyName;
    }

    public String getInitLoc() {
        return initLoc;
    }

    public String getFinalLoc() {
        return finalLoc;
    }

    public Double getAverageTicketPrice() {
        return averageTicketPrice;
    }

    public AID getSender() {
        return sender;
    }

    public Double getItemPrice() {
        return itemPrice;
    }
}
