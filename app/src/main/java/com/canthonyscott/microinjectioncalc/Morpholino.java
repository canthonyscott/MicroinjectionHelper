package com.canthonyscott.microinjectioncalc;

class Morpholino {

    private final String gene;
    private final double molecularWeight;
    private int id;


    public Morpholino(double molecularWeight, String gene, Integer id) {
        this.molecularWeight = molecularWeight;
        this.gene = gene;
        this.id = id;
    }

    public Morpholino(double molecularWeight, String gene) {
        this.molecularWeight = molecularWeight;
        this.gene = gene;
    }



    @Override
    public String toString() {
        return this.gene;
    }

    public double getMolecularWeight() {
        return molecularWeight;
    }
    public int getId(){
        return id;
    }

    public String getGene(){
        return gene;
    }
}
