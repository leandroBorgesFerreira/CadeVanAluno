package br.com.simplepass.cadevan.domain;


import br.com.simplepass.cadevan.domain_realm.UserRealm;
import br.com.simplepass.cadevan.utils.FormUtils;

/**
 * JavaBean representando uma tentativa de login por parte do usuário
 */
public class User {
    private Long id;
    private String phoneNumber;
    private String password;
    private String email;
    private String name;
    private String gcmToken;

    public User() {}

    public User(Long id, String phoneNumber, String password, String email, String name) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.password = FormUtils.md5(password);
        this.email = email;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        this.password = FormUtils.md5(password);
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

    public String getGcmToken() {
        return gcmToken;
    }

    public void setGcmToken(String gcmToken) {
        this.gcmToken = gcmToken;
    }

    public UserRealm toUserRealm(){
        return new UserRealm(1,
                this.phoneNumber,
                this.password,
                this.email,
                this.name);
    }
}
