package com.nusinfineon.core.input.LotEntry;

/**
 * Represents a lot entry in Actual Lot Info sheet for sorting according to Most Jobs rule.
 * Takes lotSize as comparable.
 */
public class MJLotEntry extends LotEntry {

    /**
     * Constructor for lot entry without additional comparable.
     * @param lot
     * @param product
     * @param lotSize
     * @param productionLocation
     * @param period
     */
    public MJLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        super(lot, product, lotSize, productionLocation, period);
    }

    public Double getComparable() {
        return getLotSize();
    }
}
