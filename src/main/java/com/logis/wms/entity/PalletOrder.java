package com.logis.wms.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pallet_order")
@Getter
@Setter
public class PalletOrder {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pallet_id")
    private Pallet pallet;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}
