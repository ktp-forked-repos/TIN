package pl.edu.pw.elka.tin.data;

import java.util.ArrayList;
import java.util.List;

//latitude Y -----
//longitude X |||||

/**
 * Keeps information about the zone. Zone is one area described as a list of
 * vertices
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class Zone {

	private List<Position> locationList;

	public Zone() {
		locationList = new ArrayList<Position>();
	}

	public void add(Position position) {
		locationList.add(position);
	}

	public List<Position> getLocationList() {
		return locationList;
	}

	public void clear() {
		locationList.clear();
	}

	public boolean isEmpty() {
		if (locationList.isEmpty()) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Based on edges cutting algorithm. Returns logic true if point inside in
	 * the zone.
	 * 
	 * @param longitude
	 *            geographical longitude
	 * @param latitude
	 *            geographical longitude
	 * @return TRUE if point inside the zone, FALSE otherwise
	 */
	public boolean pointInsideZone(double longitude, double latitude) {
		int i, j;
		boolean ret = false;
		int sides = locationList.size();
		if (sides == 0)
			return true;
		for (i = 0, j = sides - 1; i < sides; j = i++) {
			if ((((locationList.get(i).getLatitude() <= latitude) && (latitude < locationList
					.get(j).getLatitude())) || ((locationList.get(j)
					.getLatitude() <= latitude) && (latitude < locationList
					.get(i).getLatitude())))
					&& (longitude < (locationList.get(j).getLongitude() - locationList
							.get(i).getLongitude())
							* (latitude - locationList.get(i).getLatitude())
							/ (locationList.get(j).getLatitude() - locationList
									.get(i).getLatitude())
							+ locationList.get(i).getLongitude())) {
				ret = !ret;
			}
		}
		return ret;
	}
}
