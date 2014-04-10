package pl.edu.pw.elka.tin.activity;

import pl.edu.pw.elka.tin.service.LocationService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Main Android Activity. Starts application, receives information about phone.
 * 
 * @author Piotr Jastrzebski & Wojciech Kaczorowski
 */
public class StartingActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(new Intent(this, LocationService.class));
		this.finish();
	}

}
