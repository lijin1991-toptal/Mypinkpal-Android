package com.brodev.socialapp.entity;

public class CheckInPlace
{
    /**
     * @String reference
     */
    private String reference;

    /**
     * @String name
     */
    private String name;

    /**
     * @String icon
     */
    private String icon;

    /**
     * @String vicinity
     */
    private String vicinity;

    /**
     * @double lat location
     */
    private double latLocation;

    /**
     * @double lng location
     */
    private double lngLocation;

    public CheckInPlace() {
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public double getLngLocation() {
        return lngLocation;
    }

    public void setLngLocation(double lngLocation) {
        this.lngLocation = lngLocation;
    }

    public double getLatLocation() {

        return latLocation;
    }

    public void setLatLocation(double latLocation) {
        this.latLocation = latLocation;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
