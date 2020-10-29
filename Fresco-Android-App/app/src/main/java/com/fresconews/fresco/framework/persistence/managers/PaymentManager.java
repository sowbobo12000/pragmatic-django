package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.util.Base64;

import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.network.responses.NetworkBankToken;
import com.fresconews.fresco.framework.network.responses.NetworkCreditCard;
import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;
import com.fresconews.fresco.framework.network.responses.NetworkStripeFile;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.services.PaymentService;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;

/**
 * Created by Blaze on 8/17/2016.
 */
public class PaymentManager {
    private static final String TAG = PaymentManager.class.getSimpleName();

    PaymentService paymentService;
    Context context;

    public PaymentManager(PaymentService paymentService, Context context) {
        this.paymentService = paymentService;
        this.context = context;
    }

    public Observable<NetworkCreditCard> createPayment(String token) {
        return paymentService.createPayment(token);
    }

    public Observable<NetworkCreditCard> createPayement(String token) {
        return paymentService.createPayement(token);
    }

    public Observable<NetworkCreditCard> createPayment(String token, boolean active) {
        return paymentService.createPayement(token, active);
    }

    public Observable<NetworkCreditCard> updatePayment(String paymentId, boolean active) {
        return paymentService.updatePayment(paymentId, active);
    }

    public Observable<NetworkFrescoObject> deletePayment(String paymentId) {
        return paymentService.deletePayment(paymentId);
    }

    public Observable<List<NetworkCreditCard>> getPayments() {
        return paymentService.getPayments();
    }

    public Observable<NetworkBankToken> getBankToken(String accountNumber, String country, String currency, String routingNumber) {
        return paymentService.getBankToken(accountNumber, country, currency, routingNumber);
    }

    public Observable<NetworkBankToken> getTaxToken(String ssn) {
        return paymentService.getTaxToken(ssn);
    }

    public Observable<NetworkStripeFile> getDocumentToken(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part mpFile = MultipartBody.Part.createFormData("file", file.getName(), requestFile);
        MultipartBody.Part mpPurpose = MultipartBody.Part.createFormData("purpose", "identity_document");
        return paymentService.uploadFile(mpPurpose, mpFile);
    }

    public Observable<NetworkUser> updateIdInfo(String firstName, String lastName, int day, int month,
                                                int year, String address, String unit, String zip, String city, String state) {

        return paymentService.updateIdInfo(firstName, lastName, day, month, year, address, unit, zip, city, state);

    }

    public Observable<NetworkUser> updateTaxInfo(String firstName, String lastName, Integer day, Integer month,
                                                 Integer year, String address, String unit, String zip, String city, String state,
                                                 String currency, String pidToken, String documentToken, String last4) {

        return paymentService.updateTaxInfo(firstName, lastName, day, month, year, address, unit, zip, city, state, pidToken, documentToken, last4); //pidToken
    }

    public Observable<NetworkUser> updateDocumentToken(String documentToken) {
        return paymentService.updateDocumentToken(documentToken);
    }

    public static String getBase64String(String value) throws UnsupportedEncodingException {
        return Base64.encodeToString(value.getBytes("UTF-8"), Base64.NO_WRAP);
    }

}
