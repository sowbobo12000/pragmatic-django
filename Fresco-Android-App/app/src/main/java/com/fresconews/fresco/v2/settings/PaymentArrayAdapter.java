package com.fresconews.fresco.v2.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fresconews.fresco.R;

import java.util.List;

/**
 * Created by Blaze on 8/17/2016.
 */
public class PaymentArrayAdapter extends ArrayAdapter<PaymentMethod> {

    private final Context context;
    private List<PaymentMethod> paymentMethods;
    private int res;
    private OnPaymentItemClickListener listener;

    public PaymentArrayAdapter(Context context, int res, List<PaymentMethod> values, OnPaymentItemClickListener listener) {
        super(context, res, values);
        this.context = context;
        this.paymentMethods = values;
        this.res = res;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(res, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.payment_type_text_view);
            viewHolder.radioButton = (RadioButton) convertView.findViewById(R.id.active_payment_button);
            viewHolder.deleteButton = (ImageButton) convertView.findViewById(R.id.payment_delete_button);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PaymentMethod paymentMethod = paymentMethods.get(position);

        if (paymentMethod.isActive()) {
            viewHolder.deleteButton.setVisibility(View.GONE);
        }
        else {
            viewHolder.deleteButton.setVisibility(View.VISIBLE);
        }

        viewHolder.deleteButton.setOnClickListener(view -> {
            if (listener != null) {
                listener.onDeletePaymentClick(paymentMethod.getId());
            }
        });

        viewHolder.radioButton.setOnClickListener(view -> {
            if (paymentMethod.isActive()) {
                viewHolder.deleteButton.setVisibility(View.VISIBLE);
                if (listener != null) {
                    listener.onDeactivatePaymentClick(paymentMethod.getId());
                }
            }
            else {
                viewHolder.deleteButton.setVisibility(View.GONE);
                if (listener != null) {
                    listener.onActivatePaymentClick(paymentMethod.getId());
                }
            }
        });
        viewHolder.radioButton.setActivated(paymentMethods.get(position).isActive());
        viewHolder.radioButton.setChecked(paymentMethods.get(position).isActive());
        viewHolder.textView.setText(paymentMethods.get(position).getPaymentType());
        return convertView;
    }

    @Override
    public int getCount() {
        if (paymentMethods == null) {
            return 0;
        }
        return paymentMethods.size();
    }

    public void setData(List<PaymentMethod> paymentMethods) {
        this.paymentMethods.clear();
        if (paymentMethods != null) {
            this.paymentMethods.addAll(paymentMethods);
        }
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        TextView textView;
        RadioButton radioButton;
        ImageButton deleteButton;
    }

    interface OnPaymentItemClickListener {

        void onDeletePaymentClick(String id);

        void onDeactivatePaymentClick(String id);

        void onActivatePaymentClick(String id);
    }
}
