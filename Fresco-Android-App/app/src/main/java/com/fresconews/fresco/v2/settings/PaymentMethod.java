package com.fresconews.fresco.v2.settings;

/**
 * Created by Blaze on 8/17/2016.
 */
public class PaymentMethod {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private String paymentType;
    private boolean active;


    PaymentMethod(String paymentType, boolean active, String id){
        this.paymentType = paymentType;
        this.active = active;
        this.id = id;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
