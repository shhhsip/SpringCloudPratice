package org.example.catalogservice.dto;

import lombok.Data;

import java.io.Serializable;
// 데이터간의 데이터교환을 위해 Serializable
@Data
public class CatalogDto implements Serializable {
    private String productId;
    private Integer qty;
    private Integer unitPrice;
    private Integer totalPrice;

    private String orderId;
    private String userId;
}
