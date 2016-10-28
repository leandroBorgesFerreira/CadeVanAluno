package br.com.simplepass.cadevan.domain_realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by leandro on 2/16/16.
 */
public class UserRealm extends RealmObject {
    @PrimaryKey
    private int id;
    private String phoneNumber;
    private String password;
    private String email;
    private String name;

    public UserRealm() {}

    public UserRealm(int id, String phoneNumber, String password, String email, String name) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
