package com.easy_locater.realtimeloc;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {
    private Context context;

    private List<String> contactList;
    private HashMap<String, String> contactHas;
    private List<String> contactListFiltered;
    private ContactsAdapterListener listener;
    private HashMap<String, String> databaseHas;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;
        public ImageView thumbnail;
        public RelativeLayout rlyt_main;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            phone = view.findViewById(R.id.phone);
            thumbnail = view.findViewById(R.id.thumbnail);
            rlyt_main = view.findViewById(R.id.rlyt_main);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // send selected contact in callback
                    listener.onContactSelected(contactListFiltered.get(getAdapterPosition()));
                }
            });
        }
    }


    public ContactsAdapter(Context context, List<String> contactList, ContactsAdapterListener listener, HashMap<String, String> databaseHas) {
        this.context = context;

        this.databaseHas = databaseHas;
        this.listener = listener;
        this.contactList = contactList;
        this.contactListFiltered = contactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_row_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final String contact = contactListFiltered.get(position);
        holder.name.setText(getContactName(contact, context));
        holder.phone.setText(contact);
        holder.rlyt_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent34 = new Intent(context, HomeActivity.class);
                //intent34.putExtra("ecommType", CommonKeyUtility.ECOMM_TYPE.BUS.ordinal());
                intent34.putExtra("phoneNumber", contact);
                intent34.putExtra("id", databaseHas.get(contact));
                context.startActivity(intent34);
            }
        });

        Glide.with(context)
                .load(context.getResources().getDrawable(R.drawable.user))
                .apply(RequestOptions.circleCropTransform())
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }


    public interface ContactsAdapterListener {
        void onContactSelected(String contact);
    }

    public String getContactName(final String phoneNumber, Context context) {

        String contactName = null;
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

            String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

            contactName = "";
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(0);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactName;
    }
}