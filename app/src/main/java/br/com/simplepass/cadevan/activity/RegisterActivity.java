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
import android.widget.Toast;

import java.io.IOException;

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
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    @Bind(R.id.sign_in_email ) EditText mEmailView;
    @Bind(R.id.sign_in_password) EditText mPasswordView;
    @Bind(R.id.sign_in_name) EditText mNameView;
    @Bind(R.id.sign_in_phone_number) EditText mPhoneNumberView;
    @Bind(R.id.sign_in_phone_country) EditText mPhoneCountryView;
    private ProgressDialog          mProgressDialog;

    private void showProgress(boolean show){
        if(show){
            mProgressDialog = ProgressDialog.show(this, "Registrando",
                    "Calma, é só um instante", true);
        } else{
            mProgressDialog.dismiss();
        }
    }

    private void login(final User user){
        CadeVanAlunoClient clientService = ServiceGenerator.createService(CadeVanAlunoClient.class,
                Constants.CLIENT_ID,
                Constants.CLIENT_SECRET);
        final Call<OAuthTokenResponse> callLogin = clientService.getAuthToken(user.getPhoneNumber(),
                user.getPassword(),
                "password");
        callLogin.enqueue(new Callback<OAuthTokenResponse>() {
            @Override
            public void onResponse(Call<OAuthTokenResponse> call, retrofit2.Response<OAuthTokenResponse> response) {
                if(response.isSuccessful()){
                    showProgress(false);

                    OAuthTokenResponse tokenResponse = response.body();
                    AccountManager accountManager = AccountManager.get(RegisterActivity.this);
                    Account account =
                            new Account(Constants.ARG_ACCOUNT_NAME, Constants.ACCOUNT_TYPE);

                    Bundle bundle = new Bundle();
                    bundle.putLong("userId", user.getId());
                    bundle.putString("email", user.getEmail());
                    bundle.putString("phoneNumber", user.getPhoneNumber());
                    bundle.putString("name", user.getName());

                    accountManager.addAccountExplicitly(account, null, bundle);
                    accountManager.setAuthToken(account,
                            tokenResponse.getTokenType(),
                            tokenResponse.getAccessToken());

                    Intent intent = new Intent(RegisterActivity.this, ChooseDriverActivity.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(
                                RegisterActivity.this);

                        startActivity(intent, activityOptions.toBundle());
                    } else {
                        startActivity(intent);
                        finish();
                    }
                } else{
                    showProgress(false);
                    Utils.showInfoDialog(RegisterActivity.this, "falha ao registrar. Tente novamente");
                }
            }

            @Override
            public void onFailure(Call<OAuthTokenResponse> call, Throwable t) {
                showProgress(false);
                Utils.showInfoDialog(RegisterActivity.this, "falha ao registrar. Tente novamente");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        mPhoneNumberView.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mPhoneNumberView.addTextChangedListener(FormUtils.areaCodeFixer());
        mPhoneNumberView.requestFocus();

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.btn_sign_in)
    public void attemptRegister(){
        // Reseta os erros
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);
        mPhoneNumberView.setError(null);
        mPhoneCountryView.setError(null);

        // Guarda os valores antes de tentar fazer o registro
        String email            = mEmailView.getText().toString(); //Esse getText precisa mesmo??
        String password         = mPasswordView.getText().toString();
        String name             = mNameView.getText().toString();
        String phoneNumber      = mPhoneNumberView.getText().toString();
        String phoneCountry     = mPhoneCountryView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Checa se o nome foi preenchido
        if(TextUtils.isEmpty(name)){
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        }

        if(TextUtils.isEmpty(phoneNumber)){
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        }

        if(TextUtils.isEmpty(phoneCountry)){
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneCountryView;
            cancel = true;
        }

        // Checa se o usuário digitou alguma coisa na senha
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

        // Checa se o email é valido e se o usuário digitou alguma coisa.
        if(TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!FormUtils.isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            /* Houve algum erro ao preencher o formulário. Vamos dar foco onde o erro aconteceu */
            focusView.requestFocus();
        } else {
            /* Mostra uma barrinha de progresso e manda a AsyncTask para fazer o registro do
             * usuario */


            String mPhone = (phoneCountry + phoneNumber).replaceAll("\\D", "");

            final User user = new User(0L, mPhone, password, email, name);

            showProgress(true);
            RegisterTask registerTask = new RegisterTask(user);
            registerTask.execute();
        }
    }

    private class RegisterTask extends AsyncTask<Void, Void, Boolean>{
        private User mUser;

        public RegisterTask(User mUser) {
            this.mUser = mUser;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            CadeVanAlunoClient clientService = ServiceGenerator.createService(CadeVanAlunoClient.class);
            Call<User> callRegister = clientService.register(mUser);

            try {
                try {
                    String registrationId = Pushy.register(getApplicationContext());

                    mUser.setGcmToken(registrationId);

                    Response<User> registerResponse = callRegister.execute();
                    if (registerResponse.isSuccessful()) {
                        SharedPreferences sharedPreferences = getSharedPreferences(
                                Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(Constants.KEY_PUSH_REGISTRATION_ID, registrationId);
                        editor.apply();

                        mUser.setId(registerResponse.body().getId());

                        return true;
                    }
                } catch (PushyException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean registered) {
            if(registered){
                login(mUser);
            } else{
                showProgress(false);
                Toast.makeText(RegisterActivity.this, "Falha ao registrar. Tente novamente",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
