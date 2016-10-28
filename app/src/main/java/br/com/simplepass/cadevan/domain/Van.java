package br.com.simplepass.cadevan.domain;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Van {
	private int vanId;
	private String name;
	private double latitude;
	private double longitude;
	private String direction;
	private String company;
	private String timeToArrive;
	private Marker marker;
	
	public Van(int id, String name, double latitude, double longitude, String direction, String company) {
		this.vanId = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.direction = direction;
		this.company = company;
	}

	public Location getLocation(){
		Location location = new Location(company);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}

	public int getId() {
		return vanId;
	}

	public void setId(int id) {
		this.vanId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
		
	public String getTimeToArrive() {
		return timeToArrive;
	}

	public void setTimeToTravel(String timeToTravel) {
		this.timeToArrive = timeToTravel;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public LatLng getLatLng(){
		return new LatLng(latitude, longitude);
	}

	public void updateInfo(Van van){
		this.latitude = van.getLatitude();
		this.longitude = van.getLongitude();
		this.direction = van.getDirection();
		this.timeToArrive = van.getTimeToArrive();

		if(marker != null){
			String info = "";

			if(direction != null) {
				info += "Direção: " + van.getDirection() + "\n";
			}

			if(timeToArrive != null && !timeToArrive.isEmpty()) {
				info += " Chegada: " + timeToArrive;
			}

			marker.setSnippet(info);
		}
	}
}
