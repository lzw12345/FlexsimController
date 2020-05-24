package com.nusinfineon.core.input.LotEntry;

/**
 * Represents a lot entry in Actual Lot Info sheet for sorting according to Randomise rule.
 */
public class GenericLotEntry extends LotEntry {

    /**
     * Constructor for lot entry without additional comparable.
     * @param lot
     * @param product
     * @param lotSize
     * @param productionLocation
     * @param period
     */
    public GenericLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period) {
        super(lot, product, lotSize, productionLocation, period);
    }

    public Double getComparable() {
        return 0.0;
    }
}
