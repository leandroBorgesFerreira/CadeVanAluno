package br.com.simplepass.cadevan.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.adapters.DriversAdapter;
import br.com.simplepass.cadevan.domain.Driver;
import br.com.simplepass.cadevan.domain_realm.DriverRealm;
import br.com.simplepass.cadevan.retrofit.AccessToken;
import br.com.simplepass.cadevan.retrofit.CadeVanAlunoClient;
import br.com.simplepass.cadevan.retrofit.ServiceGenerator;
import br.com.simplepass.cadevan.utils.Constants;
import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChooseDriverActivity extends AppCompatActivity {
    Realm mRealm;
    RealmRecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_driver);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setTitle("");
        }

        mRealm = Realm.getDefaultInstance();

        RealmResults<DriverRealm> resultDrivers = mRealm.where(DriverRealm.class).findAll();

        mRecyclerView = (RealmRecyclerView) findViewById(R.id.driver_recycler_view);
        mRecyclerView.setAdapter(new DriversAdapter(this, resultDrivers, true, false));
        mRecyclerView.setOnRefreshListener(new RealmRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDrivers();
            }
        });

        /*if(resultDrivers.size() == 0){
            AuthAsyncTask authAsyncTask = new AuthAsyncTask();
            authAsyncTask.execute();
        }*/

        AuthAsyncTask authAsyncTask = new AuthAsyncTask();
        authAsyncTask.execute();
    }


    @Override
    public void onDestroy(){
        mRealm.close();
        super.onDestroy();
    }

    public void showProgress(final boolean show) {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        mRecyclerView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private class AuthAsyncTask extends AsyncTask<Void, Void, AccessToken> {
        @Override
        protected AccessToken doInBackground(Void... params) {
            AccountManager accountManager = AccountManager.get(ChooseDriverActivity.this);
            Account account = new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);
            AccountManagerFuture<Bundle> accountManagerFuture = accountManager.getAuthToken(account,
                    "bearer",
                    null,
                    ChooseDriverActivity.this,
                    null,
                    null);
            Bundle authTokenBundle = null;
            try {
                authTokenBundle = accountManagerFuture.getResult();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }

            if(authTokenBundle != null){
                return new AccessToken(authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString(),
                        "bearer");
            }

            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            if(accessToken != null) {
                refreshDrivers();
            }
        }
    }

    private void refreshDrivers(){
        CadeVanAlunoClient client =
                ServiceGenerator.createService(CadeVanAlunoClient.class);

        Call<List<Driver>> call = client.getDrivers();
        showProgress(true);
        call.enqueue(new Callback<List<Driver>>() {
            @Override
            public void onResponse(Call<List<Driver>> call, Response<List<Driver>> response) {
                showProgress(false);
                mRecyclerView.setRefreshing(false);
                if(response.isSuccessful()){
                    List<Driver> driverList = response.body();

                    mRealm.beginTransaction();

                    mRealm.where(DriverRealm.class).findAll().clear();

                    for(Driver driver : driverList){
                        DriverRealm driverRealm = new DriverRealm();
                        driverRealm.setId(driver.getId());
                        driverRealm.setName(driver.getName());
                        driverRealm.setPhone(driver.getPhoneNumber());
                        mRealm.copyToRealm(driverRealm);
                    }

                    mRealm.commitTransaction();
                }
            }

            @Override
            public void onFailure(Call<List<Driver>> call, Throwable t) {
                showProgress(false);
                mRecyclerView.setRefreshing(false);
            }
        });
    }
}
