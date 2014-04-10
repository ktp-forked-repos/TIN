package pl.edu.pw.elka.tin.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

import pl.edu.pw.elka.tin.data.Position;
import pl.edu.pw.elka.tin.data.Zone;
import pl.edu.pw.elka.tin.others.AppConstants;
import pl.edu.pw.elka.tin.others.Encrypter;
import pl.edu.pw.elka.tin.others.Log;

/**
 * Provide mechanism to send, receive and process data.
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class HttpModule {

	/**
	 * User agent of HTTP request.
	 */
	private static final String USER_AGENT = "TIN";
	/**
	 * Boundary for HTTP request
	 */
	private static final String ENTITY_BOUNDARY = "--TIN_BOUNDARY";
	/**
	 * The request performed by http client.
	 */
	private static HttpPost httppost;
	/**
	 * The client performing http requests.
	 */
	private static HttpClient httpclient;

	/**
	 * Value for updating allowed zones.
	 */
	private static String timestamp = "0";
	

	/**
	 * Parse the byte into Integer.
	 * 
	 * @param b
	 * @return Integer value.
	 */
	private static int parseByte(Byte b) {
		int result = b.intValue();
		if (result < 0) {
			result += 256;
		}
		return result;
	}

	/**
	 * Gets 4 bytes and parse them into Integer.
	 * 
	 * @param b
	 * @param position
	 * @return Integer value.
	 */
	private static int get4ByteInt(byte b[], int position) {
		Byte temp;
		int numberOfMatchesInt = 0;
		for (int j = 0; j < 4; j++) {
			temp = new Byte(b[position + j]);
			numberOfMatchesInt <<= 8;
			numberOfMatchesInt += parseByte(temp);
		}
		return numberOfMatchesInt;
	}

	/**
	 * Communication between Android application and Python server. One way
	 * (Android -> Python) sends current coordinates (latitude, longitude,
	 * accuracy). Receives zones connected with certain IMEI number.
	 * 
	 * @param latitude
	 *            Geographical latitude.
	 * @param longitude
	 *            Geographical longitude.
	 * @param accuracy
	 *            Accuracy of caught position.
	 * @param IMEI
	 *            Phone's IMEI number.
	 * @param zoneList
	 *            List of zones associated to given IMEI.
	 */
	public static void sendReceiveData(double latitude, double longitude,
			double accuracy, final String IMEI, final List<Zone> zoneList) {

		// sending below
		httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpclient.getParams().setParameter("http.socket.timeout",
				Integer.valueOf(AppConstants.COMMUNICATION_TIMEOUT));
		httpclient.getParams().setParameter("http.useragent", USER_AGENT);

		MultipartEntity entity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE, ENTITY_BOUNDARY, null);

		StringBody stringBody = null;
		try {
			stringBody = new StringBody(IMEI + " " + timestamp + " "
					+ Double.toString(latitude) + " "
					+ Double.toString(longitude) + " "
					+ Double.toString(accuracy));
		} catch (UnsupportedEncodingException e1) {
			Log.e(e1);
		}

		entity.addPart("upstring", stringBody);

		httppost = new HttpPost("http://" + AppConstants.SERVER_IP + ":"
				+ AppConstants.SERVER_PORT);
		httppost.addHeader("Content-Type", "multipart/form-data; boundary="
				+ ENTITY_BOUNDARY);
		httppost.setEntity(entity);

		// receiving below
		ResponseHandler<String> handler = new ResponseHandler<String>() {
			public String handleResponse(HttpResponse response)
					throws ClientProtocolException, IOException {

				HttpEntity entity = response.getEntity();

				if (entity == null) {
					Log.e("No response from server");
					return null;
				} else {

					// bytes received from server saved in the array
					// entityBodies
					byte[] entityBytes = EntityUtils.toByteArray(entity);
					int position = 0;

					String log = " ";

					// received coordinates
					int sizeOfString = get4ByteInt(entityBytes, position);
					if (sizeOfString >= 1) {
						position += 4;
						byte[] detailBytes = new byte[sizeOfString];
						System.arraycopy(entityBytes, position, detailBytes, 0,
								sizeOfString);
						String receivedCoordinatesString = new String(
								detailBytes);
						String[] receivedCoordinatesArraySTR;

						receivedCoordinatesArraySTR = Encrypter.encode(
								receivedCoordinatesString, IMEI).split(" ");

						if (receivedCoordinatesArraySTR.length % 2 == 1) { // array with data has to be odd, in other case data is broken
							setZones(zoneList, receivedCoordinatesArraySTR);
							timestamp = receivedCoordinatesArraySTR[0];
						}

						for (String s : receivedCoordinatesArraySTR) {
							log += " " + s;
						}

					}
					Log.i(log);

					return null;

				}
			}
		};

		try {
			httpclient.execute(httppost, handler);
		} catch (ClientProtocolException e) {
			Log.e("Protocol Exception");
		} catch (IOException e) {
			Log.e("IO Exception");
		}
	}

	/**
	 * Clears and setups new allowed-zones, for that device.
	 * 
	 * @param zoneList
	 * @param data
	 */
	private static void setZones(final List<Zone> zoneList, String[] data) {
		zoneList.clear();
		Zone zone = new Zone();
		for (int i = 1; i < data.length; i++) { // start with 1, on 0-positon is
												// timestamp
			if (!data[i].equals("#")) {
				Position pos = new Position(Double.parseDouble(data[i]),
						Double.parseDouble(data[++i]));
				zone.add(pos);
			} else {
				++i;
				zoneList.add(zone);
				zone = new Zone();
			}
		}
		zoneList.add(zone);
	}
}
