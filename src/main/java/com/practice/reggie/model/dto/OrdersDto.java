package com.practice.reggie.model.dto;


import com.practice.reggie.model.OrderDetail;
import com.practice.reggie.model.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee; //收货人

    private List<OrderDetail> orderDetails;
	
}
