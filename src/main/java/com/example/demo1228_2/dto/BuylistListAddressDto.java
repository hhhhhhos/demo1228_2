package com.example.demo1228_2.dto;

import com.example.demo1228_2.Vo.Address;
import lombok.Data;

import java.util.List;

@Data
public class BuylistListAddressDto {
    List<BuylistDto> buylistDtoLists;

    Address address;
}
