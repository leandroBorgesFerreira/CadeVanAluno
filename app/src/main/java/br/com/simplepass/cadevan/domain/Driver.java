package br.com.simplepass.cadevan.domain;

/**
 * Created by leandro on 3/31/16.
 */
public class Driver {
    private long id;
    private String name;
    private String phoneNumber;

    public Driver() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhone(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
