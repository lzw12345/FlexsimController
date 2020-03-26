package com.nusinfineon.util.LotEntry;

import com.nusinfineon.util.LotEntry.LotEntry;

public class SPTLotEntry extends LotEntry {
    private Double processTime;

    public SPTLotEntry(String lot, String product, Double lotSize, String productionLocation, Double period,
                       Double processTime) {
        super(lot, product, lotSize, productionLocation, period);
        this.processTime = processTime;
    }

    public Double getComparable() {
        return processTime;
    }
}
