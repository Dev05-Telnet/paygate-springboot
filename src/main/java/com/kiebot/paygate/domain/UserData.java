package com.kiebot.paygate.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A UserData.
 */
@Entity
@Table(name = "user_data")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class UserData extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_data_id_seq")
    @SequenceGenerator(name = "user_data_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "store")
    private String store;

    @Column(name = "token")
    private String token;

    @Column(name = "pay_gate_id")
    private String payGateID;

    @Column(name = "pay_gate_secret")
    private String payGateSecret;

    @Column(name = "script_id")
    private String scriptId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserData id(Long id) {
        this.id = id;
        return this;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public UserData userId(Integer userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStore() {
        return this.store;
    }

    public UserData store(String store) {
        this.store = store;
        return this;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getToken() {
        return this.token;
    }

    public UserData token(String token) {
        this.token = token;
        return this;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPayGateID() {
        return this.payGateID;
    }

    public UserData payGateID(String payGateID) {
        this.payGateID = payGateID;
        return this;
    }

    public void setPayGateID(String payGateID) {
        this.payGateID = payGateID;
    }

    public String getPayGateSecret() {
        return this.payGateSecret;
    }

    public UserData payGateSecret(String payGateSecret) {
        this.payGateSecret = payGateSecret;
        return this;
    }

    public void setPayGateSecret(String payGateSecret) {
        this.payGateSecret = payGateSecret;
    }

    public String getScriptId() {
        return this.scriptId;
    }

    public UserData scriptId(String scriptId) {
        this.scriptId = scriptId;
        return this;
    }

    public void setScriptId(String scriptId) {
        this.scriptId = scriptId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserData)) {
            return false;
        }
        return id != null && id.equals(((UserData) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserData{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", store='" + getStore() + "'" +
            ", token='" + getToken() + "'" +
            ", payGateID='" + getPayGateID() + "'" +
            ", payGateSecret='" + getPayGateSecret() + "'" +
            ", scriptId='" + getScriptId() + "'" +
            "}";
    }
}
