package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.databinding.Bindable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.DialogViewModel;
import com.fresconews.fresco.framework.network.responses.NetworkIdentity;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.settings.SettingsActivity;
import com.fresconews.fresco.v2.settings.SpinnerItemAdapter;
import com.fresconews.fresco.v2.utils.StringUtils;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import rx.functions.Action1;

/**
 * Created by mauricewu on 11/3/16.
 */
public class TaxInfoDialogViewModel extends DialogViewModel {

    @Inject
    UserManager userManager;

    public BindableView<TextInputEditText> firstNameEditText = new BindableView<>();
    public BindableView<TextInputEditText> lastNameEditText = new BindableView<>();
    public BindableView<TextInputEditText> dobEditText = new BindableView<>();
    public BindableView<TextInputEditText> taxIdEditText = new BindableView<>();
    public BindableView<TextInputEditText> addressEditText = new BindableView<>();
    public BindableView<TextInputEditText> unitEditText = new BindableView<>();
    public BindableView<TextInputEditText> zipEditText = new BindableView<>();
    public BindableView<TextInputEditText> cityEditText = new BindableView<>();
    public BindableView<Spinner> statesSpinner = new BindableView<>();

    private String firstNameError;
    private String lastNameError;
    private String dobError;
    private String taxIdError;
    private String addressError;
    private String cityError;
    private String zipError;

    private String ssnTitle;
    private boolean updateFirstName = true;
    private boolean updateLastName = true;
    private boolean updateDob = true;
    private boolean updateAddress = true;
    private boolean updateUnit = true;
    private boolean updateZip = true;
    private boolean updateCity = true;
    private boolean updateState = true;
    private boolean updateTaxId = true;
    private String state;
    private Integer dobMonth;
    private Integer dobDay;
    private Integer dobYear;

    private boolean dobVisible;
    private boolean firstNameVisible;
    private boolean lastNameVisible;
    private boolean taxIdVisible;
    private boolean stateIdVisible;
    private boolean documentVisible;
    private boolean addressVisible;

    private String selectedState;
    private OnSaveTaxInfoListener listener;

    public TaxInfoDialogViewModel(Activity activity, String state, OnSaveTaxInfoListener listener) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
        this.listener = listener;
        this.state = state;
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        setSsnTitle(getString(R.string.tax_id_ssn));
        //Set title to either "Tax ID# (SSN)" or "SSN"
        if (userManager.hasFieldsNeeded()) {
            for (String field : userManager.getNetworkUserMe().getNetworkIdentity().getFieldsNeeded()) {
                if (field.equals("pid_last4")) {
                    setSsnTitle("SSN last 4");

                    taxIdEditText.get().setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                }
            }
        }

        List<String> states = Arrays.asList(activity.getResources().getStringArray(R.array.arrays_states));
        SpinnerItemAdapter adapter = new SpinnerItemAdapter(activity, R.layout.item_spinner, R.id.spinner_item_text_view, states);
        statesSpinner.get().setAdapter(adapter);
        statesSpinner.get().setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedState = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        statesSpinner.get().setSelection(states.indexOf(state));

        checkLayoutFromFieldsNeeded();
        checkLayoutFromIdentity();

        firstNameEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(firstNameEditText.get()));
        lastNameEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(lastNameEditText.get()));
        dobEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(dobEditText.get()));
        taxIdEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(taxIdEditText.get()));
        addressEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(addressEditText.get()));
        zipEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(zipEditText.get()));
        cityEditText.get().addTextChangedListener(new TaxInfoFieldsTextWatcher(cityEditText.get()));
    }

    //Show fields which are in fields needed
    private void checkLayoutFromFieldsNeeded() {
        NetworkUser me = userManager.getNetworkUserMe();
        if (userManager == null) {
            return;
        }
        if (me == null) {
            userManager.me().onErrorReturn(throwable -> null).subscribe(user -> checkLayoutFromFieldsNeeded());
        }
        else {
            NetworkIdentity identity = me.getNetworkIdentity();
            if (identity != null) {
                String[] fieldsNeeded = identity.getFieldsNeeded();

                if (fieldsNeeded != null) {
                    for (String field : fieldsNeeded) {
                        if (field.startsWith("dob")) {
                            setDobVisible(true);
                        }
                        else if (field.equals("first_name")) {
                            setFirstNameVisible(true);
                        }
                        else if (field.equals("last_name")) {
                            setLastNameVisible(true);
                        }
                        else if (field.equals("pid_last4")) {
                            setTaxIdVisible(true);
                        }
                        else if (field.equals("id_document")) {
                            setDocumentVisible(true);
                        }
                        else if (field.equals("personal_id_number")) {
                            setTaxIdVisible(true);

                        }
                        else if (field.startsWith("address")) {
                            setAddressVisible(true);
                        }
                    }
                }
            }
        }
    }

    //Show and disable fields that are filled out
    private void checkLayoutFromIdentity() {
        //Show things that have already been filled in.
        if (userManager.getNetworkUserMe() != null && userManager.getNetworkUserMe().getNetworkIdentity() != null) {
            NetworkIdentity me = userManager.getNetworkUserMe().getNetworkIdentity();

            if (StringUtils.toNullIfEmpty(me.getFirstName()) != null) {
                setFirstNameVisible(true);
                firstNameEditText.get().setText(me.getFirstName().trim());
                disableEditText(firstNameEditText.get());
                updateFirstName = false;
            }
            if (StringUtils.toNullIfEmpty(me.getLastName()) != null) {
                setLastNameVisible(true);
                lastNameEditText.get().setText(me.getLastName().trim());
                disableEditText(lastNameEditText.get());
                updateLastName = false;
            }
            if (me.getDobDay() != null) {
                setDobVisible(true);
                dobEditText.get().setText(new DateFormatSymbols().getMonths()[me.getDobMonth() - 1] + " " + me.getDobDay()
                        + ", " + me.getDobYear());
                disableEditText(dobEditText.get());
                updateDob = false;
            }
            if (StringUtils.toNullIfEmpty(me.getAddressLine1()) != null) {
                setAddressVisible(true);
                addressEditText.get().setText(me.getAddressLine1());
                disableEditText(addressEditText.get());
                updateAddress = false;
            }
            //Line2 (aka unit) and zip are in the same layout on the same line
            if (StringUtils.toNullIfEmpty(me.getAddressLine2()) != null || StringUtils.toNullIfEmpty(me.getAddressZip()) != null) {
                setAddressVisible(true);
                unitEditText.get().setText(me.getAddressLine2());
                zipEditText.get().setText(me.getAddressZip());
                disableEditText(unitEditText.get());
                disableEditText(zipEditText.get());
                updateUnit = false;
                updateZip = false;
            }
            //City & State are on the same line in the same layout
            if (StringUtils.toNullIfEmpty(me.getAddressCity()) != null) {
                setAddressVisible(true);
                cityEditText.get().setText(me.getAddressCity());
                disableEditText(cityEditText.get());
                updateCity = false;
                updateState = false;

                List<String> states = Arrays.asList(activity.getResources().getStringArray(R.array.arrays_states));
                statesSpinner.get().setSelection(states.indexOf(me.getAddressState().trim()));

                TextView spinnerText = (TextView) statesSpinner.get().getChildAt(0);
                if (spinnerText != null) {
                    spinnerText.setTextColor(ContextCompat.getColor(activity, R.color.black_54));
                }
                statesSpinner.get().setEnabled(false);
                statesSpinner.get().setFocusable(false);
            }

            boolean dontDisableTaxId = false;
            String[] fieldsNeeded = me.getFieldsNeeded();
            if (fieldsNeeded != null) {
                for (int i = 0; i < fieldsNeeded.length; i++) {
                    String field = fieldsNeeded[i];
                    if (field.equals("personal_id_number")) {
                        setTaxIdVisible(true);
                        dontDisableTaxId = true;
                    }
                }
            }
            if (me.getPidLast4Provided() && !dontDisableTaxId) {
                setTaxIdVisible(true);
                taxIdEditText.get().setText("\u2022" + "\u2022" + "\u2022" + "\u2022");
                disableEditText(taxIdEditText.get());
                updateTaxId = false;
            }
            if (me.getPidProvided()) {
                setTaxIdVisible(true);
                taxIdEditText.get().setText("\u2022" + "\u2022" + "\u2022" + "\u2022"
                        + "\u2022" + "\u2022" + "\u2022" + "\u2022" + "\u2022");
                disableEditText(taxIdEditText.get());
                updateTaxId = false;
            }

            //TODO and if it's not the first then use pid_stripe_uploaded or w/e the new boolean is that mike will create
        }
    }

    private void disableEditText(TextInputEditText editText) {
        editText.setEnabled(false);
        editText.setInputType(InputType.TYPE_NULL);
        editText.setFocusable(false);
        editText.setCursorVisible(false);
        editText.setKeyListener(null);
        editText.setTextColor(ContextCompat.getColor(activity, R.color.black_54));
    }

    private boolean validateIdInfoValues() {
        if (firstNameVisible && firstNameEditText.get().getText().toString().trim().length() <= 0) {
            setFirstNameError(getString(R.string.enter_first_name));
            return false;
        }
        if (lastNameVisible && lastNameEditText.get().getText().toString().trim().length() <= 0) {
            setLastNameError(getString(R.string.enter_last_name));
            return false;
        }
        if (dobVisible && dobEditText.get().getText().toString().trim().length() <= 0) {
            setDobError(getString(R.string.enter_dob));
            return false;
        }
        if (taxIdVisible && taxIdEditText.get().getText().toString().trim().length() <= 0) {
            setTaxIdError(getString(R.string.enter_tax_id));
            return false;
        }
        if (addressVisible && addressEditText.get().getText().toString().trim().length() <= 0) {
            setAddressError(getString(R.string.enter_address));
            return false;
        }
        if (addressVisible && zipEditText.get().getText().toString().trim().length() <= 0) {
            setZipError(getString(R.string.enter_zip));
            return false;
        }
        if (addressVisible && cityEditText.get().getText().toString().trim().length() <= 0) {
            setCityError(getString(R.string.enter_city));
            return false;
        }
        return true;
    }

    public Action1<View> uploadDocument = view -> {
        if (activity != null && activity instanceof SettingsActivity) {
            ((SettingsActivity) activity).launchCameraForDocumentId();
            dismiss();
        }
    };

    public Action1<View> selectDOB = view -> {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                dobEditText.get().setText(new DateFormatSymbols().getMonths()[month] + " " + day + ", " + year);
                dobDay = day;
                dobMonth = month;
                dobYear = year;
            }
        }, year, month, day);
        datePickerDialog.show();
    };

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> save = view -> {
        if (listener != null && validateIdInfoValues()) {
            if (dialog != null) {
                dialog.dismiss();
            }
            String firstName = null;
            String lastName = null;
            String taxId = null;
            String address = null;
            String unit = null;
            String city = null;
            String zip = null;
            String state = null;
            Integer dobDay = null;
            Integer dobMonth = null;
            Integer dobYear = null;

            if (updateFirstName) {
                firstName = StringUtils.toNullIfEmpty(firstNameEditText.get().getText().toString().trim());
            }
            if (updateLastName) {
                lastName = StringUtils.toNullIfEmpty(lastNameEditText.get().getText().toString().trim());
            }
            if (updateDob && this.dobDay != null && this.dobDay > 0) {
                dobDay = this.dobDay;
                dobMonth = this.dobMonth + 1;
                dobYear = this.dobYear;
            }
            if (updateTaxId) {
                taxId = StringUtils.toNullIfEmpty(taxIdEditText.get().getText().toString().trim());
            }
            if (updateAddress) {
                address = StringUtils.toNullIfEmpty(addressEditText.get().getText().toString().trim());
            }
            if (updateUnit) {
                unit = StringUtils.toNullIfEmpty(unitEditText.get().getText().toString().trim());
            }
            if (updateZip) {
                zip = StringUtils.toNullIfEmpty(zipEditText.get().getText().toString().trim());
            }
            if (updateCity) {
                city = StringUtils.toNullIfEmpty(cityEditText.get().getText().toString().trim());
            }
            if (updateState) {
                state = StringUtils.toNullIfEmpty(selectedState);
            }
            listener.onSaveTaxInfoClick(firstName, lastName, taxId, address, unit, zip, city, state, dobMonth, dobDay, dobYear);
        }
    };

    @Bindable
    public boolean isDobVisible() {
        return dobVisible;
    }

    public void setDobVisible(boolean dobVisible) {
        this.dobVisible = dobVisible;
        notifyPropertyChanged(BR.dobVisible);
    }

    @Bindable
    public boolean isFirstNameVisible() {
        return firstNameVisible;
    }

    public void setFirstNameVisible(boolean firstNameVisible) {
        this.firstNameVisible = firstNameVisible;
        notifyPropertyChanged(BR.firstNameVisible);
    }

    @Bindable
    public boolean isLastNameVisible() {
        return lastNameVisible;
    }

    public void setLastNameVisible(boolean lastNameVisible) {
        this.lastNameVisible = lastNameVisible;
        notifyPropertyChanged(BR.lastNameVisible);
    }

    @Bindable
    public boolean isTaxIdVisible() {
        return taxIdVisible;
    }

    public void setTaxIdVisible(boolean taxIdVisible) {
        this.taxIdVisible = taxIdVisible;
        notifyPropertyChanged(BR.taxIdVisible);
    }

    @Bindable
    public boolean isStateIdVisible() {
        return stateIdVisible;
    }

    public void setStateIdVisible(boolean stateIdVisible) {
        this.stateIdVisible = stateIdVisible;
        notifyPropertyChanged(BR.stateIdVisible);
    }

    @Bindable
    public boolean isDocumentVisible() {
        return documentVisible;
    }

    public void setDocumentVisible(boolean documentVisible) {
        this.documentVisible = documentVisible;
        notifyPropertyChanged(BR.documentVisible);
    }

    @Bindable
    public boolean isAddressVisible() {
        return addressVisible;
    }

    public void setAddressVisible(boolean addressVisible) {
        this.addressVisible = addressVisible;
        notifyPropertyChanged(BR.addressVisible);
    }

    @Bindable
    public String getSsnTitle() {
        return ssnTitle;
    }

    public void setSsnTitle(String ssnTitle) {
        this.ssnTitle = ssnTitle;
        notifyPropertyChanged(BR.ssnTitle);
    }

    @Bindable
    public String getFirstNameError() {
        return firstNameError;
    }

    public void setFirstNameError(String firstNameError) {
        this.firstNameError = firstNameError;
    }

    @Bindable
    public String getLastNameError() {
        return lastNameError;
    }

    public void setLastNameError(String lastNameError) {
        this.lastNameError = lastNameError;
    }

    @Bindable
    public String getDobError() {
        return dobError;
    }

    public void setDobError(String dobError) {
        this.dobError = dobError;
        notifyPropertyChanged(BR.dobError);
    }

    @Bindable
    public String getTaxIdError() {
        return taxIdError;
    }

    public void setTaxIdError(String taxIdError) {
        this.taxIdError = taxIdError;
        notifyPropertyChanged(BR.taxIdError);
    }

    @Bindable
    public String getAddressError() {
        return addressError;
    }

    public void setAddressError(String addressError) {
        this.addressError = addressError;
        notifyPropertyChanged(BR.addressError);
    }

    @Bindable
    public String getCityError() {
        return cityError;
    }

    public void setCityError(String cityError) {
        this.cityError = cityError;
        notifyPropertyChanged(BR.cityError);
    }

    @Bindable
    public String getZipError() {
        return zipError;
    }

    public void setZipError(String zipError) {
        this.zipError = zipError;
        notifyPropertyChanged(BR.zipError);
    }

    public interface OnSaveTaxInfoListener {
        void onSaveTaxInfoClick(String firstName, String lastName, String taxId, String address, String unit,
                                String zip, String city, String state, Integer dobMonth, Integer dobDay, Integer dobYear);
    }

    private class TaxInfoFieldsTextWatcher implements TextWatcher {
        TextInputEditText editText;

        TaxInfoFieldsTextWatcher(TextInputEditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (editText.equals(firstNameEditText.get())) {
                setFirstNameError("");
            }
            else if (editText.equals(lastNameEditText.get())) {
                setLastNameError("");
            }
            else if (editText.equals(dobEditText.get())) {
                setDobError("");
            }
            else if (editText.equals(taxIdEditText.get())) {
                setTaxIdError("");
            }
            else if (editText.equals(addressEditText.get())) {
                setAddressError("");
            }
            else if (editText.equals(zipEditText.get())) {
                setZipError("");
            }
            else if (editText.equals(cityEditText.get())) {
                setCityError("");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
