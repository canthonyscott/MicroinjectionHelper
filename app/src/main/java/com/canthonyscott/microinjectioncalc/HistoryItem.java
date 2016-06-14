package com.canthonyscott.microinjectioncalc;

/**
 * Created by Anthony on 6/13/2016.
 */
public class HistoryItem {

    private final String reagent;
    private final String nanoliters;
    private final String picograms;
    private final String date;

    public HistoryItem(String date, String reagent, String nanoliters, String picograms) {
        this.date = date;
        this.reagent = reagent;
        this.nanoliters = nanoliters;
        this.picograms = picograms;
    }

    public String getDate() {
        return date;
    }

    public String getNanoliters() {
        return nanoliters;
    }

    public String getPicograms() {
        return picograms;
    }

    public String getReagent() {
        return reagent;
    }
}
