package com.jnj.honeur.webapi.hssserviceuser;

import com.jnj.honeur.webapi.shiro.HoneurCipherService;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "HSSServiceUserEntity")
@Table(name = "hss_service_user")
public class HSSServiceUserEntity implements Serializable {

    @Id
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "present")
    private String present;

    public HSSServiceUserEntity() {
        this.present="X";
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlainTextPassword() {
        return HoneurCipherService.decrypt(password);
    }
    public void setPlainTextPassword(String password) {
        setPassword(HoneurCipherService.encrypt(password));
    }

    public String getPresent() {
        return present;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HSSServiceUserEntity that = (HSSServiceUserEntity) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
