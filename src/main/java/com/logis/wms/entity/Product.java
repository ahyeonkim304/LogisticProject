package com.logis.wms.entity;

import com.logis.auth.entity.Account;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "product",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sku", "account_id"}))
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String sku;

    private String name;

    private String category;

    private String unit;

    @Column(length = 1000)
    private String description;

    private Integer safetyStock;

    private Boolean deleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;
}
