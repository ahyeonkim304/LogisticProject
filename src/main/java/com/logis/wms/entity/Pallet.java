package com.logis.wms.entity;

import com.logis.wms.enums.PalletStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "pallet")
@Getter
@Setter
public class Pallet {

    @Id
    @GeneratedValue
    private Long id;

    private String palletCode;

    private String name;

    @Enumerated(EnumType.STRING)
    private PalletStatus status = PalletStatus.CREATED;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "pallet", cascade = CascadeType.ALL)
    private List<PalletOrder> palletOrders = new ArrayList<>();
}
