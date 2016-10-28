package br.com.simplepass.cadevan.domain_realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by leandro on 3/31/16.
 */
public class DriverRealm extends RealmObject{
    @PrimaryKey
    private long id;

    private String name;
    private String phone;

    public DriverRealm() {
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
