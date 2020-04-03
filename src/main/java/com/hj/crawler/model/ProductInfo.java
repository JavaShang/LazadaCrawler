package com.hj.crawler.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductInfo {

    private String url;
    private String name;
    private float postage;
    private List<SkuInfo> skuInfoList;
    private List<String> descriptionList;
}
