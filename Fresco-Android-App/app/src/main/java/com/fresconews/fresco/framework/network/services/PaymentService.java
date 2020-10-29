package com.fresconews.fresco.framework.network.services;

import com.fresconews.fresco.framework.network.responses.NetworkBankToken;
import com.fresconews.fresco.framework.network.responses.NetworkCreditCard;
import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;
import com.fresconews.fresco.framework.network.responses.NetworkSettings;
import com.fresconews.fresco.framework.network.responses.NetworkStripeFile;
import com.fresconews.fresco.framework.network.responses.NetworkUser;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Blaze on 8/17/2016.
 */
public interface PaymentService {

    @GET("user/{id}")
    Observable<NetworkUser> get(
            @Path("id") String id
    );

    @GET("user/settings")
    Observable<NetworkSettings> getSettings();

    @FormUrlEncoded
    @POST("https://api.stripe.com/v1/tokens")
    Observable<NetworkBankToken> getBankToken(
            @Field("bank_account[account_number]") String accountNumber,
            @Field("bank_account[country]") String country,
            @Field("bank_account[currency]") String currency,
            @Field("bank_account[routing_number]") String routingNumber
    );

    @FormUrlEncoded
    @POST("https://api.stripe.com/v1/tokens")
    Observable<NetworkBankToken> getTaxToken(
            @Field("pii[personal_id_number]") String ssn
    );

    @Multipart
    @POST("https://uploads.stripe.com/v1/files")
    Observable<NetworkStripeFile> uploadFile(
            @Part MultipartBody.Part purpose,
            @Part MultipartBody.Part file
    );

    @FormUrlEncoded
    @POST("user/payment/create")
    Observable<NetworkCreditCard> createPayement(@Field("token") String token);

    @FormUrlEncoded
    @POST("user/payment/create")
    Observable<NetworkCreditCard> createPayment(@Field("token") String token);

    @FormUrlEncoded
    @POST("user/payment/create")
    Observable<NetworkCreditCard> createPayement(
            @Field("token") String token,
            @Field("active") boolean active
    );

    @FormUrlEncoded
    @POST("user/payment/{paymentId}/update")
    Observable<NetworkCreditCard> updatePayment(
            @Path("paymentId") String paymentId,
            @Field("active") boolean active
    ); //There are multiple other fields such as address, expiration date, name

    @POST("user/payment/{paymentId}/delete")
    Observable<NetworkFrescoObject> deletePayment(@Path("paymentId") String paymentId);

    @GET("user/payment")
    Observable<List<NetworkCreditCard>> getPayments();

    @FormUrlEncoded
    @POST("user/update")
    Observable<NetworkUser> updateIdInfo(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("dob[day]") int day,
            @Field("dob[month]") int month,
            @Field("dob[year]") int year,
            @Field("address[line1]") String address,
            @Field("address[line2]") String unit,
            @Field("address[postal_code]") String zip,
            @Field("address[city]") String city,
            @Field("address[state]") String state
    );

    @FormUrlEncoded
    @POST("user/identity/update")
    Observable<NetworkUser> updateTaxInfo(
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("dob_day") Integer day,
            @Field("dob_month") Integer month,
            @Field("dob_year") Integer year,
            @Field("address_line1") String address,
            @Field("address_line2") String unit,
            @Field("address_zip") String zip,
            @Field("address_city") String city,
            @Field("address_state") String state,
            @Field("stripe_pid_token") String pidToken, //ssn token that you get back from stripe
            @Field("stripe_document_token") String docToken,
            @Field("pid_last4") String last4 //literally just the last four digits
    );

    @FormUrlEncoded
    @POST("user/identity/update")
    Observable<NetworkUser> updateDocumentToken(

            @Field("stripe_document_token") String docToken

    );

}
