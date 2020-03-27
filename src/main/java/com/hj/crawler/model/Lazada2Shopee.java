package com.hj.crawler.model;

import com.google.gson.annotations.SerializedName;
import lombok.Setter;

import java.util.List;

@Setter
public class Lazada2Shopee {

    private String itemId;
    @SerializedName("partner_id")
    private long partnerId;
    @SerializedName("shopid")
    private long shopId;
    @SerializedName("category_id")
    private long categoryId;
    private String name;
    private String description;
    private float price;
    private int stock;
    @SerializedName("item_sku")
    private String itemSku;
    private List<Variation> variations;
    private List<Image> images;
    private List<Attribute> attributes;
    private List<Logistic> logistics;
    private float weight;
    @SerializedName("package_length")
    private Long packageLength;
    @SerializedName("package_width")
    private Long packageWidth;
    @SerializedName("package_height")
    private Long packageHeight;
    @SerializedName("days_to_ship")
    private Long daysToShip;
    private List<Wholesale> wholesales;
    @SerializedName("size_chart")
    private String sizeChart;
    private String condition;
    private String status;
    @SerializedName("is_pre_order")
    private Boolean preOrder;

    @Setter
    public static class Variation {

        private String name;
        private int stock;
        private float price;
        @SerializedName("variation_sku")
        private String variationSku;
    }

    @Setter
    public static class Image {

        private String url;
    }

    @Setter
    public static class Attribute {

        @SerializedName("attributes_id")
        private long attributesId;
        private String value;
    }

    @Setter
    public static class Logistic {

        @SerializedName("logistic_id")
        private long logisticId;
        private boolean enabled;
        @SerializedName("shipping_fee")
        private Float shippingFee;
        @SerializedName("size_id")
        private Long sizeId;
        @SerializedName("is_free")
        private Boolean free;
    }

    @Setter
    static class Wholesale {

        private int min;
        private int max;
        @SerializedName("unit_price")
        private float unitPrice;
    }
}
