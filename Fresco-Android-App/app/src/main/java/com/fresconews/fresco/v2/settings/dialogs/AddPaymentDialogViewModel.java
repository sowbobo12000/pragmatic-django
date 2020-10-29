package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.databinding.Bindable;
import android.graphics.Bitmap;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;

import rx.functions.Action1;

/**
 * Created by mauricewu on 10/28/16.
 */
public class AddPaymentDialogViewModel extends DialogViewModel {
    public BindableView<TextInputEditText> cardNumberEditText = new BindableView<>();
    public BindableView<TextInputEditText> expirationDateEditText = new BindableView<>();
    public BindableView<TextInputEditText> cvvEditText = new BindableView<>();
    public BindableView<TextInputEditText> accountNumberEditText = new BindableView<>();
    public BindableView<TextInputEditText> routingNumberEditText = new BindableView<>();

    private String redactedCard;
    private boolean showAccount;
    private Bitmap cardImage;
    private OnSavePaymentListener listener;

    public AddPaymentDialogViewModel(Activity activity, String lastFour, OnSavePaymentListener listener) {
        super(activity);
        this.listener = listener;

        setShowAccount(false);
        setRedactedCard(lastFour);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        cardNumberEditText.get().addTextChangedListener(new CreditCardTextWatcher());
        expirationDateEditText.get().addTextChangedListener(new ExpirationDateTextWatcher());
    }

    public void reload(String lastFour) {
        if (hasBeenBound) {
            cardNumberEditText.get().setText("");
            expirationDateEditText.get().setText("");
            cvvEditText.get().setText("");
            accountNumberEditText.get().setText("");
            routingNumberEditText.get().setText("");
        }
        if (showAccount) {
            showBankAccount.call(null);
        }
        setCardImage(null);
        setRedactedCard(lastFour);
    }

    public void populateCardInfo(String redactedCard, String formattedCard,
                                 String expirationDate, String cvv, Bitmap cardImage) {
        this.redactedCard = redactedCard;

        setCardImage(cardImage);

        if (formattedCard != null) {
            cardNumberEditText.get().setText(formattedCard);
        }
        if (expirationDate != null) {
            expirationDateEditText.get().setText(expirationDate);
        }
        if (cvv != null) {
            cvvEditText.get().setText(cvv);
        }
    }

    @Bindable
    public Bitmap getCardImage() {
        return cardImage;
    }

    public void setCardImage(Bitmap cardImage) {
        this.cardImage = cardImage;
        notifyPropertyChanged(BR.cardImage);
    }

    @Bindable
    public String getRedactedCard() {
        return redactedCard;
    }

    public void setRedactedCard(String redactedCard) {
        this.redactedCard = redactedCard;
        notifyPropertyChanged(BR.redactedCard);
    }

    @Bindable
    public boolean isShowAccount() {
        return showAccount;
    }

    public void setShowAccount(boolean showAccount) {
        this.showAccount = showAccount;
        notifyPropertyChanged(BR.showAccount);
    }

    public Action1<View> showBankAccount = view -> {
        setShowAccount(true);
    };

    public Action1<View> showDebitCard = view -> {
        setShowAccount(false);
    };

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> save = view -> {
        if (listener != null) {
            dismiss();

            String cardNumber = cardNumberEditText.get().getText().toString();
            String expirationDate = expirationDateEditText.get().getText().toString();
            String cvv = cvvEditText.get().getText().toString();
            String accountNumber = accountNumberEditText.get().getText().toString();
            String routing = routingNumberEditText.get().getText().toString();
            listener.onSavePaymentClick(cardNumber, expirationDate, cvv, accountNumber, routing);
        }
    };

    public interface OnSavePaymentListener {
        void onSavePaymentClick(String cardNumber, String expDate, String cvv, String accountNumber, String routingNumber);
    }

    private class ExpirationDateTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            char slash = '/';
            // Remove all spacing char
            int pos = 0;
            while (true) {
                if (pos >= s.length()) {
                    break;
                }
                if (slash == s.charAt(pos) && (((pos + 1) % 3) != 0 || pos + 1 == s.length())) {
                    s.delete(pos, pos + 1);
                }
                else {
                    pos++;
                }
            }

            // Insert char where needed.
            pos = 2;
            while (true) {
                if (pos >= s.length()) {
                    break;
                }
                final char c = s.charAt(pos);
                // Only if its a digit where there should be a space we insert a space
                if ("0123456789".indexOf(c) >= 0) {
                    s.insert(pos, "" + slash);
                }
                pos += 5;
            }
        }
    }

    private class CreditCardTextWatcher implements TextWatcher {
        private static final char space = ' ';

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Remove all spacing char
            int pos = 0;
            while (true) {
                if (pos >= s.length()) {
                    break;
                }
                if (space == s.charAt(pos) && (((pos + 1) % 5) != 0 || pos + 1 == s.length())) {
                    s.delete(pos, pos + 1);
                }
                else {
                    pos++;
                }
            }

            // Insert char where needed.
            pos = 4;
            while (true) {
                if (pos >= s.length()) {
                    break;
                }
                final char c = s.charAt(pos);
                // Only if its a digit where there should be a space we insert a space
                if ("0123456789\u2022".indexOf(c) >= 0) {
                    s.insert(pos, "" + space);
                }
                pos += 5;
            }
        }
    }
}

