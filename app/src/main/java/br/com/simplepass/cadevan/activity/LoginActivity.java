package br.com.simplepass.cadevan.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.List;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.domain.User;
import br.com.simplepass.cadevan.retrofit.CadeVanAlunoClient;
import br.com.simplepass.cadevan.retrofit.OAuthTokenResponse;
import br.com.simplepass.cadevan.retrofit.ServiceGenerator;
import br.com.simplepass.cadevan.utils.Constants;
import br.com.simplepass.cadevan.utils.FormUtils;
import br.com.simplepass.cadevan.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.pushy.sdk.Pushy;
import me.pushy.sdk.exceptions.PushyException;
import retrofit2.Call;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    @Bind(R.id.phone_number_login) EditText mPhoneNumberView;
    @Bind(R.id.phone_number_login_country) EditText mPhoneCountryView;
    @Bind(R.id.password) EditText mPasswordView;
    private ProgressDialog mProgressDialog;

    private void showProgress(boolean show){
        if(show){
            mProgressDialog = ProgressDialog.show(this, getString(R.string.registering),
                    getString(R.string.register_message), true);
        } else{
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        // Set up the login form.
        mPhoneNumberView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mPhoneNumberView.addTextChangedListener(FormUtils.areaCodeFixer());

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == R.id.login || actionId == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_recover_password)
    public void recoverPassword(){
        Intent intent = new Intent(this, RecoverPasswordActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    LoginActivity.this);

            startActivity(intent, activityOptions.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_register)
    public void goToRegister(View view){
        Intent intent = new Intent(this, RegisterActivity.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    LoginActivity.this);

            startActivity(intent, activityOptions.toBundle());
        } else {
            startActivity(intent);
        }
    }


    /**
     * Tenta logar na conta especificada pelo usuário.
     * Se houverem erros, eles serão apresentados aos usuários e não será feita nenhuma tentativa
     * de login.
     */
    @OnClick(R.id.btn_login)
    public void attemptLogin() {
        // Reset errors.
        mPhoneCountryView.setError(null);
        mPhoneNumberView.setError(null);
        mPasswordView.setError(null);

        // Guarda os valores no momento que vai tentar logar
        String phoneCountry = mPhoneCountryView.getText().toString(); //Esse getText precisa mesmo??
        String phoneNumber = mPhoneNumberView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Checa se o usuário digitou alguma coisa
        if(TextUtils.isEmpty(password)){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
            //Checa se o usuá digitou um password valido.
        } else if (!FormUtils.isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password_too_short));
            focusView = mPasswordView;
            cancel = true;
        }

        // Checa se a pessoa digitou alguma coisa no numero de telefone.
        if (TextUtils.isEmpty(phoneNumber)) {
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phoneCountry)) {
            mPhoneCountryView.setError(getString(R.string.error_field_required));
            focusView = mPhoneCountryView;
            cancel = true;
        }

        if (cancel) {
            /* Houve um erro. Não tenta logar e foca no primeiro erro registrado. */
            focusView.requestFocus();
        } else {
            /* Mostra o desenho de progresso e lança uma async task para realizar o login no
             * servidor. */
            String phone = (phoneCountry + phoneNumber).replaceAll("\\D", "");

            final User user = new User(0L, phone, password, null, null);

            showProgress(true);
            LoginTask loginTask = new LoginTask();
            loginTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, user);
        }
    }

    private class LoginTask extends AsyncTask<User, Void, Boolean>{
        @Override
        protected Boolean doInBackground(User... params) {
            User user = params[0];

            CadeVanAlunoClient clientService = ServiceGenerator.createService(
                    CadeVanAlunoClient.class,
                    Constants.CLIENT_ID,
                    Constants.CLIENT_SECRET);

            Call<OAuthTokenResponse> call = clientService.getAuthToken(user.getPhoneNumber(),
                    user.getPassword(),
                    "password");

            CadeVanAlunoClient client = ServiceGenerator.createService(CadeVanAlunoClient.class);
            Call<List<User>> callGetUser = client.getUsersByPhoneNumber(user.getPhoneNumber());
            try {
                try {
                    Response<OAuthTokenResponse> responseOAuth = call.execute();
                    Response<List<User>> responseGetUser = callGetUser.execute();

                    if (responseOAuth.isSuccessful() && responseGetUser.isSuccessful()) {

                        OAuthTokenResponse oAuthTokenResponse = responseOAuth.body();
                        User userWithId = responseGetUser.body().get(0);

                        AccountManager accountManager = AccountManager.get(LoginActivity.this);
                        Account account =
                                new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);

                        Bundle bundle = new Bundle();
                        bundle.putLong("userId", userWithId.getId());
                        bundle.putString("email", userWithId.getEmail());
                        bundle.putString("phoneNumber", userWithId.getPhoneNumber());
                        bundle.putString("name", userWithId.getName());

                        if (!accountManager.addAccountExplicitly(account, null, bundle)) {
                            return null;
                        }
                        accountManager.setAuthToken(account,
                                oAuthTokenResponse.getTokenType(),
                                oAuthTokenResponse.getAccessToken());

                        String registrationId = Pushy.register(getApplicationContext());

                        SharedPreferences sharedPreferences = getSharedPreferences(
                                Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.KEY_PUSH_REGISTRATION_ID, registrationId);
                        editor.apply();

                        userWithId.setGcmToken(registrationId);

                        Call<Void> callUpdateUser = client.updateUser(userWithId, userWithId.getId());
                        Response<Void> responseGcm = callUpdateUser.execute();

                        return responseGcm.isSuccessful();
                    } else {
                        return false;
                    }
                } catch (PushyException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean registered) {
            showProgress(false);

            if(registered) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                            LoginActivity.this);

                    Intent intent = new Intent(LoginActivity.this, ChooseDriverActivity.class);
                    startActivity(intent, activityOptions.toBundle());
                } else {
                    startActivity(new Intent(LoginActivity.this, ChooseDriverActivity.class));
                    finish();
                }
            } else{
                Utils.showInfoDialog(LoginActivity.this, "falha ao registrar. Tente novamente");
            }
        }
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}