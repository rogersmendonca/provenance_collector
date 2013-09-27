package br.ufrj.ppgi.greco.job.entry.provenancecollector.util;

public enum EnumETLOperation
{
    E("Extraction"), T("Transformation"), L("Load");

    private String description;

    EnumETLOperation(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return this.description;
    }
}