package com.logis.wms.entity;

import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "order_measurement")
@Getter
@Setter
public class OrderMeasurement {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Double width;
    private Double depth;
    private Double height;
    private Double actualWeight;
    private Integer volumeDivisor;
    private Double volumeWeight;

    private LocalDateTime createdAt = LocalDateTime.now();

    public void calcVolumeWeight() {
        if (width != null && depth != null && height != null
                && volumeDivisor != null && volumeDivisor > 0) {
            this.volumeWeight = (width * depth * height) / volumeDivisor;
        }
    }
}
