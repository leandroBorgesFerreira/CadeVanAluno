package br.com.simplepass.cadevan.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import br.com.simplepass.cadevan.R;
import br.com.simplepass.cadevan.domain.User;
import br.com.simplepass.cadevan.dto.RecoverPasswordBean;
import br.com.simplepass.cadevan.retrofit.CadeVanAlunoClient;
import br.com.simplepass.cadevan.retrofit.ServiceGenerator;
import br.com.simplepass.cadevan.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecoverPasswordActivity extends AppCompatActivity {
    @Bind(R.id.phone_recover_password) EditText phoneEditText;
    @Bind(R.id.country_code) EditText countryCode;
    private ProgressDialog mProgressDialog;

    private void showProgress(boolean show){
        if(show){
            mProgressDialog = ProgressDialog.show(this, getString(R.string.dialog_wait),
                    getString(R.string.register_message), true);
        } else{
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle("");
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.btn_send_recover_password)
    public void sendPassword(){
        String phone = (countryCode.getText().toString() +
                phoneEditText.getText().toString()).replaceAll("\\D", "");

        CadeVanAlunoClient client = ServiceGenerator.createService(CadeVanAlunoClient.class);
        Call<User> callRecoverPassword = client.recoverPassword(new RecoverPasswordBean(phone));
        showProgress(true);
        callRecoverPassword.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showProgress(false);
                if(response.isSuccessful()) {
                    Utils.showInfoDialog(RecoverPasswordActivity.this,
                            String.format(getString(R.string.email_sent), response.body().getEmail()));
                } else{
                    Utils.showInfoDialog(RecoverPasswordActivity.this, "Usuário não encontrado");
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showProgress(false);
                Utils.showInfoDialog(RecoverPasswordActivity.this, getString(R.string.error_no_internet));
            }
        });
    }

}
