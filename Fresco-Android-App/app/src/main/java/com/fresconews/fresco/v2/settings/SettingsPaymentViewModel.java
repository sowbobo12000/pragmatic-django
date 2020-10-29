package com.fresconews.fresco.v2.settings;

import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.network.responses.NetworkCreditCard;
import com.fresconews.fresco.framework.network.responses.NetworkIdentity;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.settings.dialogs.AddPaymentDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.TaxInfoDialogViewModel;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.gson.Gson;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.exception.AuthenticationException;
import com.stripe.android.exception.CardException;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by mauricewu on 10/28/16.
 */
public class SettingsPaymentViewModel extends BaseSettingsViewModel {
    private static final String TAG = SettingsPaymentViewModel.class.getSimpleName();

    public BindableBoolean showTaxInfo = new BindableBoolean();
    public BindableBoolean isProcessing = new BindableBoolean();
    public BindableString dueBy = new BindableString();
    public BindableView<ListView> paymentsList = new BindableView<>();

    private AddPaymentDialogViewModel addPaymentDialogViewModel;
    private TaxInfoDialogViewModel taxInfoDialogViewModel;

    private PaymentArrayAdapter paymentMethodsAdapter;
    private List<PaymentMethod> paymentMethods;

    @Inject
    AnalyticsManager analyticsManager;

    public SettingsPaymentViewModel(ActivityViewModel activityViewModel) {
        super(activityViewModel);
        ((Fresco2) activityViewModel.getActivity().getApplication()).getFrescoComponent().inject(this);

        if (userManager.hasFieldsNeeded()) {
            NetworkIdentity me = userManager.getNetworkUserMe().getNetworkIdentity();
            if (me.getFieldsNeeded().length > 0) {
                showTaxInfo.set(true);
            }
            if (me.getDueBy() != null) {
                DateTime dateTime = new DateTime(me.getDueBy());
                DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM dd");
                dueBy.set("Add by " + fmt.print(dateTime));
            }
        }

        getUser().onErrorReturn(throwable -> null).subscribe(user -> {
            if (user == null) {
                return;
            }
            if (user.getDueBy() != null && user.getDueBy() != 0L) {
                Date date = new Date(user.getDueBy());
                SimpleDateFormat dateFormat = new SimpleDateFormat(date.toString(), Locale.getDefault());
                dueBy.set("Add by " + dateFormat.toString());
            }
        });
    }

    @Override
    public void onBound() {
        super.onBound();

        //Get payment methods from DB, add them to payment methods
        paymentManager.getPayments()
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(Schedulers.io())
                      .onErrorReturn(throwable -> null)
                      .subscribe(networkCreditCards -> {
                          paymentMethods = new ArrayList<>();
                          if (networkCreditCards != null) {
                              for (int i = 0; i < networkCreditCards.size(); i++) {
                                  NetworkCreditCard card = networkCreditCards.get(i);
                                  Log.i(TAG, "Adding payment method");
                                  paymentMethods.add(new PaymentMethod(card.getBrand() + " " + card.getLast4(), card.isActive(), card.getId()));
                              }
                          }
                          if (paymentMethodsAdapter == null) {
                              paymentMethodsAdapter = new PaymentArrayAdapter(getActivity(), R.layout.item_payment_method, paymentMethods, new PaymentArrayAdapter.OnPaymentItemClickListener() {
                                  @Override
                                  public void onDeletePaymentClick(String id) {
                                      deletePaymentMethod(id);
                                  }

                                  @Override
                                  public void onDeactivatePaymentClick(String id) {
                                      deactivatePaymentMethod(id);
                                  }

                                  @Override
                                  public void onActivatePaymentClick(String id) {
                                      activatePaymentMethod(id);
                                  }
                              });
                              paymentsList.get().setAdapter(paymentMethodsAdapter);
                          }
                          else {
                              paymentMethodsAdapter.setData(paymentMethods);
                          }
                          //Set the adapter and height of the list view.
                          setListViewHeightBasedOnChildren();
                      });
    }

    public void populateCardInfo(String redactedCard, String formattedCard,
                                 String expirationDate, String cvv, Bitmap cardImage) {
        if (addPaymentDialogViewModel != null && cardImage != null) {
            addPaymentDialogViewModel.populateCardInfo(redactedCard, formattedCard, expirationDate, cvv, cardImage);
        }
    }

    public Action1<View> showPaymentDialog = view -> {
        showPaymentDialog("");
    };

    public void showPaymentDialog(String lastFour) {
        if (addPaymentDialogViewModel == null) {
            analyticsManager.trackScreen("Payment Method");
            addPaymentDialogViewModel = new AddPaymentDialogViewModel(getActivity(), lastFour,
                    (cardNumber, expDate, cvv, accountNumber, routingNumber) -> {
                        if (!TextUtils.isEmpty(accountNumber) && !TextUtils.isEmpty(routingNumber)) {
                            saveBankAccountInfo(accountNumber, routingNumber);
                        }
                        else {
                            saveDebitCardInfo(cardNumber, expDate, cvv);
                        }
                    });
        }
        else {
            addPaymentDialogViewModel.reload(lastFour);
        }
        if (!addPaymentDialogViewModel.isShowing()) {
            addPaymentDialogViewModel.show(R.layout.view_add_payment);
        }
    }

    private void saveBankAccountInfo(String accountNumber, String routingNumber) {
        if (TextUtils.isEmpty(accountNumber) || TextUtils.isEmpty(routingNumber)) {
            SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_bank_account, view ->
                    showPaymentDialog(""));
            return;
        }
        paymentManager.getBankToken(accountNumber, "US", "usd", routingNumber)
                      .onErrorReturn(throwable -> {
                          showBankError(throwable);
                          return null;
                      })
                      .subscribe(networkBankToken -> {
                          if (networkBankToken == null) {
                              return;
                          }
                          Log.i(TAG, "you did it! - " + networkBankToken.getTokenId());
                          //Create payment token
                          paymentManager.createPayment(networkBankToken.getTokenId())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribeOn(Schedulers.io())
                                        .onErrorReturn(throwable -> {
                                            showBankError(throwable);
                                            Log.i(TAG, "Error creating payment - " + throwable.getLocalizedMessage());
                                            return null;
                                        })
                                        .subscribe(networkCreditCard -> {
                                            if (networkCreditCard == null) {
                                                return;
                                            }
                                            //Add payment to list of payment methods
                                            //Set the adapter and height of the list view.
                                            paymentMethods.add(new PaymentMethod(networkCreditCard.getBrand() + " " + networkCreditCard.getLast4(), true, networkCreditCard.getId()));
                                            activatePaymentMethod(networkCreditCard.getId());
                                            paymentMethodsAdapter.setData(paymentMethods);

                                            getUser().onErrorReturn(throwable -> null).subscribe(user -> {
                                                if (userManager.hasFieldsNeeded()) {
                                                    if (userManager.getNetworkUserMe().getNetworkIdentity().getFieldsNeeded().length > 0) {
                                                        showTaxInfo.set(true);
                                                    }
                                                }
                                            });
                                        });
                      });
    }

    private void saveDebitCardInfo(String formattedCard, String expirationDate, String cvv) {
        if (TextUtils.isEmpty(cvv) || TextUtils.isEmpty(expirationDate) || TextUtils.isEmpty(formattedCard) || !expirationDate.contains("/")) {
            SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_debit_card, view ->
                    showPaymentDialog(""));
            return;
        }

        int expirationMonth = Integer.parseInt(expirationDate.split("/")[0]);
        int expirationYear = Integer.parseInt("20" + expirationDate.split("/")[1]);
        Card card = new Card(formattedCard, expirationMonth, expirationYear, cvv);
        card.setCurrency("usd");

        try {
            String lastFour = "\u2022" + "\u2022" + "\u2022" + "\u2022" +
                    "\u2022" + "\u2022" + "\u2022" + "\u2022" +
                    "\u2022" + "\u2022" + "\u2022" + "\u2022" + card.getLast4();
            Stripe stripe = new Stripe(getActivity(), EndpointHelper.currentEndpoint.stripeKey);
            stripe.createToken(card, new TokenCallback() {
                        public void onSuccess(Token token) {
                            //Create payment token
                            paymentManager.createPayment(token.getId())
                                          .observeOn(AndroidSchedulers.mainThread())
                                          .subscribeOn(Schedulers.io())
                                          .onErrorReturn(throwable -> {
                                              if (throwable instanceof HttpException && ((HttpException) throwable).response().code() != 500) {
                                                  try {
                                                      String body = ((HttpException) throwable).response().errorBody().string();
                                                      NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                                                      SnackbarUtil.retrySnackbar(getRoot(), result.getError().getMsg(), view ->
                                                              showPaymentDialog(lastFour));
                                                  }
                                                  catch (IOException e) {
                                                      SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_debit_card, view ->
                                                              showPaymentDialog(lastFour));
                                                  }
                                              }
                                              else {
                                                  SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_debit_card, view ->
                                                          showPaymentDialog(lastFour));
                                              }
                                              return null;
                                          })
                                          .subscribe(networkCreditCard -> {
                                              if (networkCreditCard == null) {
                                                  return;
                                              }
                                              //Add payment to list of payment methods
                                              //Set the adapter and height of the list view.
                                              paymentMethods.add(new PaymentMethod(networkCreditCard.getBrand() + " " + card.getLast4(), true, networkCreditCard.getId()));
                                              activatePaymentMethod(networkCreditCard.getId());

                                              getUser().onErrorReturn(throwable -> null).subscribe(user -> {
                                                  if (userManager.hasFieldsNeeded()) {
                                                      if (userManager.getNetworkUserMe().getNetworkIdentity().getFieldsNeeded().length > 0) {
                                                          showTaxInfo.set(true);
                                                      }
                                                  }
                                              });
                                          });
                        }

                        public void onError(Exception error) {
                            if (error != null && error instanceof CardException) {
                                LogUtils.e(TAG, error.getMessage());
                                CardException cardException = (CardException) error;
                                if (cardException.getStatusCode() != null && cardException.getStatusCode() != 500 && cardException.getMessage() != null) {
                                    SnackbarUtil.retrySnackbar(getRoot(), cardException.getMessage(), view ->
                                            showPaymentDialog(lastFour));
                                }
                                else {
                                    SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_debit_card, view ->
                                            showPaymentDialog(lastFour));
                                }
                            }
                        }
                    }
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_debit_card, view ->
                    showPaymentDialog(""));
        }

    }

    private void showBankError(Throwable throwable) {
        if (throwable instanceof HttpException && ((HttpException) throwable).response().code() != 500) {
            try {
                String body = ((HttpException) throwable).response().errorBody().string();
                NetworkSuccessResult result = new Gson().fromJson(body, NetworkSuccessResult.class);
                SnackbarUtil.retrySnackbar(getRoot(), result.getError().getMsg(), view ->
                        showPaymentDialog(""));
            }
            catch (IOException e) {
                SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_bank_account, view ->
                        showPaymentDialog(""));
            }
        }
        else {
            SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_bank_account, view ->
                    showPaymentDialog(""));
        }
    }

    private void deactivatePaymentMethod(String id) {
        //Don't deactivate. When you activate another, the other deactivates anyway. Stripe handles that.
    }

    private void setListViewHeightBasedOnChildren() {
        ListAdapter listAdapter = paymentsList.get().getAdapter();
        if (listAdapter == null) {
            return;
        }

        int pixels = DimensionUtils.convertDpToPixel(48f, getActivity());
        int totalHeight = pixels * listAdapter.getCount();

        ViewGroup.LayoutParams params = paymentsList.get().getLayoutParams();
        params.height = totalHeight;
        paymentsList.get().setLayoutParams(params);
        paymentsList.get().requestLayout();
    }

    private void activatePaymentMethod(String id) {
        if (paymentMethodsAdapter == null) {
            return;
        }
        String originalPaymentId = "";
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (paymentMethods.get(i).isActive()) {
                originalPaymentId = paymentMethods.get(i).getId();
            }
            if (paymentMethods.get(i).getId().equals(id)) {
                paymentMethods.get(i).setActive(true);

            }
            else {
                paymentMethods.get(i).setActive(false);
            }
        }
        paymentMethodsAdapter.setData(paymentMethods);
        setListViewHeightBasedOnChildren();

        final String finalOriginalPaymentId = originalPaymentId;
        paymentManager.updatePayment(id, true)
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(Schedulers.io())
                      .onErrorReturn(throwable -> {
                          //Update the screen.
                          for (int i = 0; i < paymentMethods.size(); i++) {
                              if (paymentMethods.get(i).getId().equals(finalOriginalPaymentId)) {
                                  paymentMethods.get(i).setActive(true);
                              }
                              if (paymentMethods.get(i).getId().equals(id)) {
                                  paymentMethods.get(i).setActive(false);
                              }
                          }
                          paymentMethodsAdapter.setData(paymentMethods);
                          setListViewHeightBasedOnChildren();
                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_payment_update, Snackbar.LENGTH_LONG);
                          return null;
                      })
                      .subscribe(networkCreditCard -> {
                          if (networkCreditCard != null) {
                              //Update the screen.
                              for (int i = 0; i < paymentMethods.size(); i++) {
                                  if (paymentMethods.get(i).getId().equals(id)) {
                                      paymentMethods.get(i).setActive(true);
                                  }
                                  else {
                                      paymentMethods.get(i).setActive(false);
                                  }
                              }
                              paymentMethodsAdapter.setData(paymentMethods);
                              setListViewHeightBasedOnChildren();
                          }
                          else {
                              SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_payment_update, Snackbar.LENGTH_LONG);
                          }
                      });
    }

    private void deletePaymentMethod(String id) {
        if (paymentMethods.size() == 1) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_only_payment_delete, Snackbar.LENGTH_LONG);
            return;
        }

        int removeMePos = -1;
        PaymentMethod removeMe = null;
        for (int i = 0; i < paymentMethods.size(); i++) {
            if (paymentMethods.get(i).isActive()) {
                if (paymentMethods.get(i).getId().equals(id)) {
                    SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_active_payment_delete, Snackbar.LENGTH_LONG);
                    return;
                }
            }

            if (paymentMethods.get(i).getId().equals(id)) {
                removeMePos = i;
                removeMe = paymentMethods.get(i);
                break;
            }

        }
        paymentMethods.remove(removeMe);
        paymentMethodsAdapter.setData(paymentMethods);
        setListViewHeightBasedOnChildren();

        if (removeMePos != -1) {
            final int finalRemoveMePos = removeMePos;
            final PaymentMethod finalRemoveMe = removeMe;
            paymentManager.deletePayment(id)
                          .onErrorReturn(throwable -> {
                              SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_payment_delete, Snackbar.LENGTH_LONG);
                              return null;
                          })
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .subscribe(networkFrescoObject -> {
                              if (networkFrescoObject == null) {
                                  //Put the object back in
                                  paymentMethods.add(finalRemoveMePos, finalRemoveMe);
                                  paymentMethodsAdapter.setData(paymentMethods);
                                  setListViewHeightBasedOnChildren();
                              }
                          });

        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_payment_delete, Snackbar.LENGTH_LONG);
        }
    }

    public Action1<View> showChangeTaxInfoDialog = view -> showChangeTaxInfoPopup();

    public void showChangeTaxInfoPopup() {
        if (!isProcessing.get()) {
            showChangeTaxInfoPopup("");
        }
    }

    public void showChangeTaxInfoPopup(String state) {
        boolean isShowing = taxInfoDialogViewModel != null && taxInfoDialogViewModel.isShowing();

        analyticsManager.trackScreen("Identity");

        taxInfoDialogViewModel = new TaxInfoDialogViewModel(getActivity(), state, (firstName, lastName, taxId, address, unit, zip, city, state1, dobMonth, dobDay, dobYear) -> {
            String documentToken = null;
            String last4 = null;

            isProcessing.set(true);
            dueBy.set("Processing");

            //If we asked the user for only the last 4, the ssn value we get back is the ssn.
            if (userManager.hasFieldsNeeded()) {
                for (String field : userManager.getNetworkUserMe().getNetworkIdentity().getFieldsNeeded()) {
                    if (field.equals("pid_last4")) {
                        last4 = taxId;
                    }
                }
            }
            String finalLast4 = last4;
            paymentManager.getTaxToken(taxId).onErrorReturn(throwable -> {
                isProcessing.set(false);
                dueBy.set("");
                return null;
            }).subscribe(networkBankToken -> {
                String ssnPidToken = null;
                if (networkBankToken != null) {
                    ssnPidToken = networkBankToken.getTokenId();
                    if (finalLast4 != null) { // If we're only submitting the last 4
                        ssnPidToken = null; // Don't use this token generated from the last 4
                    }
                }

                paymentManager.updateTaxInfo(firstName, lastName, dobDay, dobMonth,
                        dobYear, address, unit, zip, city, state1,
                        "usd", ssnPidToken, documentToken, finalLast4)
                              .onErrorReturn(throwable -> {
                                  SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_id_info, view ->
                                          showChangeTaxInfoPopup(state1));
                                  isProcessing.set(false);
                                  dueBy.set("");
                                  return null;
                              })
                              .subscribe(networkUser -> {
                                  if (networkUser == null) {
                                      SnackbarUtil.retrySnackbar(getRoot(), R.string.error_saving_id_info, view ->
                                              showChangeTaxInfoPopup(state1));
                                  }
                                  else {
                                      //Save new network user me
                                      User user = User.from(networkUser);
                                      user.save();
                                      userManager.setNetworkUserMe(networkUser);

                                      isProcessing.set(false);
                                      dueBy.set("");

                                      if (networkUser.getNetworkIdentity().getFieldsNeeded().length > 0) {
                                          showTaxInfo.set(true);
                                      }
                                      else {
                                          showTaxInfo.set(false);
                                      }
                                  }
                              });
            });
        });

        if (!isShowing) {
            taxInfoDialogViewModel.show(R.layout.view_change_tax_info);
        }
    }

    public void updateDocumentId(File file) {
        paymentManager.getDocumentToken(file)
                      .onErrorReturn(throwable -> null)
                      .subscribe(networkStripeFile -> {
                          if (networkStripeFile == null) {
                              LogUtils.i(TAG, "Network stripe file was null");
                          }
                          else {
                              //Upload that file id token to mike
                              paymentManager.updateDocumentToken(networkStripeFile.getId())
                                            .onErrorReturn(throwable -> null)
                                            .subscribe(networkUser -> {
                                                if (networkUser != null) {
                                                    //Save new network user me
                                                    User user = User.from(networkUser);
                                                    user.save();
                                                    userManager.setNetworkUserMe(networkUser);
                                                    showTaxInfo.set(networkUser.getNetworkIdentity().getFieldsNeeded().length > 0);
                                                    isProcessing.set(false);
                                                    dueBy.set("");
                                                }
                                            });
                          }
                      });
    }
}
