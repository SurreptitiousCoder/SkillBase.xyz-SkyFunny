package net.skillbase.fsbm.modules;

public class FlippableItem {
    public int lowestPriceAfter;
    public int lowestPrice;
    public String lowestBinAh;

    public FlippableItem(int starting_bid, int i) {
        lowestPrice = starting_bid;
        lowestPriceAfter = i;
    }
}
