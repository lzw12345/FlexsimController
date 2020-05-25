package com.nusinfineon.core.input.LotEntry;

/**
 * Represents a lot entry in Actual Lot Info sheet for sorting according to Shortest Processing Time rule.
 * Appends processTime which is taken as comparable.
 */
public class SPTLotEntry extends LotEntry {
    private Double processTime;

    /**
     * Constructor for lot entry with processTime as comparable.
     * @param lot
     * @param product
     * @param lotSize
     * @param productionLocation
     * @param period
     * @param processTime
     */
    public SPTLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period,
                       Double processTime) {
        super(lot, product, lotSize, productionLocation, period);
        this.processTime = processTime;
    }

    public Double getComparable() {
        return processTime;
    }
}
