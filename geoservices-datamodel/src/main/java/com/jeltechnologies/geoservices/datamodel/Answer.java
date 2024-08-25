package com.jeltechnologies.geoservices.datamodel;

import java.util.Objects;

public class Answer {
    private Address address = new Address();
    private Distance distanceFromQuery;
    private Coordinates closestCoordinate;

    public Address getAddress() {
	return address;
    }

    public void setAddress(Address address) {
	this.address = address;
    }

    public Distance getDistanceFromQuery() {
	return distanceFromQuery;
    }

    public void setDistanceFromQuery(Distance distanceFromQuery) {
	this.distanceFromQuery = distanceFromQuery;
    }
    
    public void setClosestCoordinate(Coordinates c) {
	this.closestCoordinate = c;
    }

    public Coordinates getClosestCoordinate() {
	return closestCoordinate;
    }
    
    @Override
    public String toString() {
	return "Answer [address=" + address + ", distanceFromQuery=" + distanceFromQuery + ", closestCoordinate=" + closestCoordinate + "]";
    }
    

    @Override
    public int hashCode() {
	return Objects.hash(address, closestCoordinate, distanceFromQuery);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Answer other = (Answer) obj;
	return Objects.equals(address, other.address) && Objects.equals(closestCoordinate, other.closestCoordinate)
		&& Objects.equals(distanceFromQuery, other.distanceFromQuery);
    }
}
