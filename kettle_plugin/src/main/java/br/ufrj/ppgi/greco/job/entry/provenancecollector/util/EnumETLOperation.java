package br.ufrj.ppgi.greco.job.entry.provenancecollector.util;

/**
 * 
 * @author Rogers Reiche de Mendonca
 * @since nov-2012
 * 
 */
public enum EnumETLOperation
{
    E("Extraction", 1), T("Transformation", 2), L("Load", 3);

    private String description;
    private int order;

    EnumETLOperation(String description, int order)
    {
        this.description = description;
        this.order = order;
    }

    public String getDescription()
    {
        return this.description;
    }

    public int getOrder()
    {
        return this.order;
    }
}