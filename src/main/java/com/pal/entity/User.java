package com.pal.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author pal
 * @date 2020/9/1 10:41 上午
 */
@Data
public class User {
    private int id;
    private String userName;

    /**
     * 余额：原则上设计金融数据最好用 BigDecimal
     */
    private int balance;
}
