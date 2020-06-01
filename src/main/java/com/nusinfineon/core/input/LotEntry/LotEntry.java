package com.nusinfineon.core.input.LotEntry;

/**
 * Represents a lot entry in Actual Lot Info sheet for sorting according to different lot sequencing rules.
 */
public abstract class LotEntry implements Comparable<LotEntry> {
    private String lot;
    private String product;
    private Double lotSize;
    private String productionLocation;
    private Double period;

    /**
     * Constructor for lot entry.
     * @param lot Lot column
     * @param product Product column
     * @param lotSize Lotsize column
     * @param productionLocation Production Location column
     * @param period Period (Week#) column
     */
    public LotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        this.lot = lot;
        this.product = product;
        this.lotSize = lotSize;
        this.productionLocation = productionLocation;
        this.period = period;
    }

    public String getLot() {
        return lot;
    }

    public String getProduct() {
        return product;
    }

    public Double getLotSize() {
        return lotSize;
    }

    public String getProductionLocation() {
        return productionLocation;
    }

    public Double getPeriod() {
        return period;
    }

    public abstract Double getComparable();

    /**
     * Allows child classes to compare by different comparable attributes.
     */
    @Override
    public int compareTo(LotEntry o) {
        return this.getComparable().compareTo(o.getComparable());
    }
}
