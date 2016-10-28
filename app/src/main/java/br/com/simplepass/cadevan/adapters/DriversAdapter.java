package br.com.simplepass.cadevan.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.activity.ChooseDriverActivity;
import br.com.simplepass.cadevan.activity.MainActivity;
import br.com.simplepass.cadevan.activity.SplashActivity;
import br.com.simplepass.cadevan.domain_realm.DriverRealm;
import br.com.simplepass.cadevan.utils.Constants;
import io.realm.RealmBasedRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.RealmViewHolder;

/**
 * Adapter que mostra os tempos de chegada dos cliente em um intervalo de um dia
 */
public class DriversAdapter extends RealmBasedRecyclerViewAdapter<DriverRealm, DriversAdapter.ViewHolder> {
    private Activity mActivity;

    public DriversAdapter(Activity activity, RealmResults<DriverRealm> realmResults,
                          boolean automaticUpdate, boolean animateResults) {
        super(activity, realmResults, automaticUpdate, animateResults);
        mActivity = activity;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RealmViewHolder {
        public TextView mId;
        public TextView mDriverName;
        public TextView mDriverPhone;
        public View mContainerView;

        public ViewHolder(View pathView) {
            super(pathView);

            mId = (TextView) pathView.findViewById(R.id.driver_id);
            mDriverName = (TextView) pathView.findViewById(R.id.driver_name);
            mDriverPhone = (TextView) pathView.findViewById(R.id.driver_phone);
            mContainerView =  pathView.findViewById(R.id.container_view);
        }
    }

    @Override
    public ViewHolder onCreateRealmViewHolder(ViewGroup parent, int viewType) {
        View pathView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_path, parent, false);

        return new ViewHolder(pathView);
    }

    @Override
    public void onBindRealmViewHolder(final DriversAdapter.ViewHolder holder, int position) {
        final DriverRealm driverRealm = realmResults.get(position);

        if(position % 2 == 0){
            holder.mContainerView.setBackgroundColor(ContextCompat.getColor(mActivity,
                    R.color.list_item_color1));
        } else{
            holder.mContainerView.setBackgroundColor(ContextCompat.getColor(mActivity,
                    R.color.list_item_color2));
        }

        holder.mId.setText(String.valueOf(driverRealm.getId()));
        holder.mDriverName.setText(driverRealm.getName());
        holder.mDriverPhone.setText(driverRealm.getPhone());

        holder.mContainerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences sharedPreferences = mActivity.getSharedPreferences(
                        Constants.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(Constants.SharedPrefs.CHOSEN_DRIVER_ID, driverRealm.getId());
                editor.apply();

                Intent intent = new Intent(mActivity, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_CHOOSEN_VAN_ID, driverRealm.getId());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                            mActivity);

                    mActivity.startActivity(intent, activityOptions.toBundle());
                } else {
                    mActivity.startActivity(intent);
                }
            }
        });

        holder.itemView.setTag(driverRealm);
    }

    @Override
    public int getItemCount() {
        return realmResults.size();
    }

    public DriverRealm getItem(int position){
        return realmResults.get(position);
    }

}
