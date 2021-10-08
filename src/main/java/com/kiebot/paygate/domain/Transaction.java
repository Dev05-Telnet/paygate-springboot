package com.kiebot.paygate.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Transaction.
 */
@Entity
@Table(name = "transaction")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Transaction extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    @SequenceGenerator(name = "transaction_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "pay_request_id")
    private String payRequestId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction id(Long id) {
        this.id = id;
        return this;
    }

    public Integer getOrderId() {
        return this.orderId;
    }

    public Transaction orderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getPayRequestId() {
        return this.payRequestId;
    }

    public Transaction payRequestId(String payRequestId) {
        this.payRequestId = payRequestId;
        return this;
    }

    public void setPayRequestId(String payRequestId) {
        this.payRequestId = payRequestId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction)) {
            return false;
        }
        return id != null && id.equals(((Transaction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Transaction{" +
            "id=" + getId() +
            ", orderId=" + getOrderId() +
            ", payRequestId='" + getPayRequestId() + "'" +
            "}";
    }
}
