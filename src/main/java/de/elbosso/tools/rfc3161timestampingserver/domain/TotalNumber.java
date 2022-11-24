package de.elbosso.tools.rfc3161timestampingserver.domain;

public class TotalNumber
{
    private final long value;

    public TotalNumber(long value)
    {
        this.value = value;
    }

    public long getValue()
    {
        return value;
    }
}
