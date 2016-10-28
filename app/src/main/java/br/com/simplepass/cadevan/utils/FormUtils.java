package br.com.simplepass.cadevan.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.simplepass.cadevan.R;

/**
 * Utils para formulários em geral
 */
public class FormUtils {

    public static final String COMPANY = "company";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String GCM_SENDER_ID = "gcmSenderId";
    public static final String GCM_REGISTRATION_ID = "registrationId";


    public static final int PASSWORD_NUMBER_OF_CARACTERES = 6;


    public GcmRegistrationIdForm getGcmRegistrationIdForm(String smartphoneId, String senderId, String phone){
        return new GcmRegistrationIdForm(smartphoneId, senderId, phone);
    }

    public static FormUtils getInstance(){
        return new FormUtils();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() >= PASSWORD_NUMBER_OF_CARACTERES;
    }

    public static boolean isPhoneNumberValid(String phone) {
        return phone.length() == 12 || phone.length() == 13;
    }

    public static boolean isEmailValid(String email) {
        //TODO: Trocar pelo que eu achar interessante
        return email.contains("@");
    }

    // Formulário não tem nada... mas reune todos os formulários em uma classe para
    // o server connector
    @Deprecated
    public abstract class Form{
        private String mSmartphoneId;

        public Form(String smartphoneId){
            mSmartphoneId = smartphoneId;
        }

        public abstract JSONObject toJSONObject();
        public abstract String getFormUrl();
    }

    @Deprecated
    public class GcmRegistrationIdForm extends Form{
        private String mSenderId;
        private String mRegisterId;
        private String mPhoneNumber;

        public GcmRegistrationIdForm(String smartphoneId, String senderId, String phone){
            super(smartphoneId);
            mSenderId = senderId;
            mPhoneNumber = phone;
        }

        public String getSenderId(){return mSenderId;}
        public String getRegisterId(){return mRegisterId;}
        public void setRegisterId(String registerId){mRegisterId = registerId;}

        @Override
        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();

            try{
                jsonObject.put(GCM_SENDER_ID, mSenderId);
                jsonObject.put(GCM_REGISTRATION_ID, mRegisterId);
                jsonObject.put(PHONE_NUMBER, mPhoneNumber);
            } catch(JSONException e){
                //Log.d("FormUtils", "json error: " + e.getMessage());
            }

            return jsonObject;
        }

        @Override
        public String getFormUrl() {
            return WebServerUtils.SERVER_ADDRESS + WebServerUtils.SERVER_GCM_REGISTER;
        }
    }

    public static boolean isSpinnerSelected(Spinner spinner){
        return spinner.getSelectedItemPosition() != 0;
    }

    public static String md5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte array[] = md.digest();
            StringBuilder sb = new StringBuilder();

            for(byte b : array){
                //sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1,3));
                String h = Integer.toHexString(0xFF & b);
                while(h.length() < 2)
                    h = "0" + h;

                sb.append(h);
            }

            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //Log.d("MD5", "Erro ao calcular o MD5: " + e.getMessage());
        }
        return null;
    }

    public static TextWatcher areaCodeFixer(){
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() == 1){
                    if(s.charAt(0) == '0'){
                        s.clear();
                    }
                }
            }
        };
    }

    public static class Mask {
        public static String unmask(String s) {
            return s.replaceAll("[.]", "").replaceAll("[-]", "")
                    .replaceAll("[/]", "").replaceAll("[(]", "")
                    .replaceAll("[)]", "");
        }

        public static TextWatcher insert(final String mask, final EditText ediTxt) {
            return new TextWatcher() {
                boolean isUpdating;
                String old = "";

                public void onTextChanged(CharSequence s, int start, int before,
                                          int count) {
                    String str = unmask(s.toString());
                    String mascara = "";
                    if (isUpdating) {
                        old = str;
                        isUpdating = false;
                        return;
                    }
                    int i = 0;
                    for (char m : mask.toCharArray()) {
                        if (m != '#' && str.length() > old.length()) {
                            mascara += m;
                            continue;
                        }
                        try {
                            mascara += str.charAt(i);
                        } catch (Exception e) {
                            break;
                        }
                        i++;
                    }
                    isUpdating = true;
                    ediTxt.setText(mascara);
                    ediTxt.setSelection(mascara.length());
                }

                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                public void afterTextChanged(Editable s) {
                }
            };
        }
    }



}
