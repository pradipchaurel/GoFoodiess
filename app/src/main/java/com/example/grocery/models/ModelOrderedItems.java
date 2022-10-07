package com.example.grocery.models;

public class ModelOrderedItems {
    private String pId, id, cost, price, quantity,name;

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public ModelOrderedItems(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelOrderedItems(String pId, String id, String cost, String price, String quantity, String name) {
        this.pId = pId;
        this.id = id;
        this.cost = cost;
        this.price = price;
        this.quantity = quantity;
        this.name = name;
    }
}
