package br.com.simplepass.cadevan.dto;

/**
 * Created by leandro on 4/18/16.
 */
public class RecoverPasswordBean {
    private String phoneNumber;

    public RecoverPasswordBean(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
