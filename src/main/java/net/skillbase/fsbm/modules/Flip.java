package net.skillbase.fsbm.modules;

public class Flip {
    int price;
    int mustBePrice;
    String auctionId;
    String item_name;

    public Flip(int price, int mustBePrice, String auctionId, String item_name) {
        this.price = price;
        this.mustBePrice = mustBePrice;
        this.auctionId = auctionId;
        this.item_name = item_name;
    }

    public int getMustBe() {
        return mustBePrice;
    }

    public int getPrice() {
        return price;
    }

    public String getItemName() {
        return item_name;
    }
}
