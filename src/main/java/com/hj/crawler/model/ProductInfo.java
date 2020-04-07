package com.hj.crawler.model;

import lombok.Data;

import java.util.List;

@Data
public class ProductInfo {

    private String url;
    private String platform;
    private String name;
    private float postage;
    private String packageWeight;
    private String weight;
    private List<SkuInfo> skuInfoList;
    private List<String> descriptionList;
}
