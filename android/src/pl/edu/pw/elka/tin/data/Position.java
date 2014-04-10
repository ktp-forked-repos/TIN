package pl.edu.pw.elka.tin.data;

//latitude Y -----
//longitude X |||||

/**
 * Keeps position information. Position kept as latitude and longitude in a
 * pair.
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class Position {

	private double latitude;
	private double longitude;

	public Position(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Position(Position pos) {
		this.latitude = pos.getLatitude();
		this.longitude = pos.getLongitude();
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

}
