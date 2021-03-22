package bike.hackboy.bronco;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import bike.hackboy.bronco.api.Client;
import bike.hackboy.bronco.bean.CbyBikeResponseBean;
import bike.hackboy.bronco.bean.PropertiesBean;
import okhttp3.Response;

public class User extends Fragment {
	protected SharedPreferences sharedPref = null;

	protected boolean loggedIn = false;
	protected CbyBikeResponseBean bike;

	protected String uid;
	protected String clientId;
	protected String token;
	protected int bikeId;

	protected List<PropertiesBean> bikePropertiesList = new ArrayList<>();
	protected RecyclerView recyclerViewBikeDetails;
	protected DetailsViewAdapter detailsViewAdapter;

	@Override
	public void onResume() {
		super.onResume();
		ensureUserData();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_user, container, false);
		recyclerViewBikeDetails = rootView.findViewById(R.id.items_list);

		return rootView;
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		detailsViewAdapter = new DetailsViewAdapter(requireContext(), bikePropertiesList);

		recyclerViewBikeDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewBikeDetails.setAdapter(detailsViewAdapter);
		recyclerViewBikeDetails.setItemAnimator(new DefaultItemAnimator());

		view.findViewById(R.id.button_log_in).setOnClickListener(view1 -> login());
		view.findViewById(R.id.button_log_out).setOnClickListener(view2 -> logout());
	}

	public void storeCredentials(String uid, String client, String token, int bikeId) {
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString("uid", uid);
		editor.putString("client", client);
		editor.putString("token", token);
		editor.putInt("bikeId", bikeId);

		editor.apply();
	}

	public void storeBike(CbyBikeResponseBean bike) {
		this.bike = bike;
		Log.d("bike", bike.toString());

		bikePropertiesList.clear();
		bikePropertiesList.addAll(bike.toPropertiesList());

		Log.d("list", bike.toPropertiesList().toString());

		((TextView) requireView().findViewById(R.id.bike_name)).setText(bike.getName());

		detailsViewAdapter.notifyDataSetChanged();
	}

	public void ensureUserData() {
		sharedPref = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE);

		clientId = sharedPref.getString("client", null);
		token = sharedPref.getString("token", null);
		bikeId = sharedPref.getInt("bikeId", 0);
		uid = sharedPref.getString("uid", null);

		loggedIn = uid != null && clientId != null && token != null && bikeId > 0;

		setupUi(loggedIn);
		if(loggedIn) getBike();
	}

	public void logout() {
		storeCredentials(null, null, null, 0);
		ensureUserData();
	}

	public void login() {
		EditText usernameField = requireView().findViewById(R.id.username);
		EditText passwordField = requireView().findViewById(R.id.password);
		String username = usernameField.getText().toString();
		String password = passwordField.getText().toString();

		Client client = new Client(new Client.OnDoneCallback() {
			@Override
			public void onResponse(Response response) {
				try {
					if (response.code() == 200) {
						//Log.d("http", response.toString());
						//Log.d("http", response.header("Access-Token"));
						//Log.d("http", response.header("Client"));

						String client = response.header("Client");
						String token = response.header("Access-Token");
						String uid = response.header("Uid");

						int bikeId;

						try {
							JSONObject user = new JSONObject(response.body().string());
							bikeId = user.getJSONObject("data").getJSONObject("bike").getInt("id");
						} catch(JSONException | NullPointerException e) {
							throw new IllegalStateException("bike id not found");
						}

						if (client == null || client.isEmpty()) {
							throw new IllegalStateException("client id not found");
						}

						if (token == null || token.isEmpty()) {
							throw new IllegalStateException("client token not found");
						}

						requireActivity().runOnUiThread(() -> {
							storeCredentials(uid, client, token, bikeId);
							ensureUserData();

							Toast.makeText(requireContext(), "Logged in successfully", Toast.LENGTH_LONG).show();
						});
					} else {
						throw new IllegalStateException("invalid credentials");
					}
				} catch (Exception e) {
					this.onError(e);
				}
			}

			@Override
			public void onError(Exception e) {
				requireActivity().runOnUiThread(() -> {
					Toast.makeText(requireContext(), "Failed to log in: "+e.getMessage(), Toast.LENGTH_LONG).show();
				});

				e.printStackTrace();
			}
		});

		client.login(username, password);
	}

	public void getBike() {
		Client client = new Client(new Client.OnDoneCallback() {
			@Override
			public void onResponse(Response response) {
				try {
					if (response.code() == 200) {
						//Log.d("http", response.toString());

						CbyBikeResponseBean bike = new CbyBikeResponseBean();

						try {
							JSONObject bikeJson = new JSONObject(response.body().string());

							bike.setBatteryCharge((float) bikeJson.getLong("battery_state_of_charge"));
							bike.setPcbBatteryCharge((float) bikeJson.getLong("pcb_battery_state_of_charge"));

							bike.setCarbonSaved(bikeJson.getInt("total_co2_saved"));
							bike.setTotalDistance((float) bikeJson.getDouble("total_distance"));
							bike.setAutonomy((float) bikeJson.getDouble("autonomy"));

							bike.setPositionAddress(bikeJson.getJSONObject("position").getString("address"));
							bike.setPositionLat((float) bikeJson.getJSONObject("position").getDouble("latitude"));
							bike.setPositionLon((float) bikeJson.getJSONObject("position").getDouble("longitude"));

							bike.setColor(bikeJson.getJSONObject("sku").getString("color"));
							bike.setMarket(bikeJson.getJSONObject("sku").getString("market"));

							bike.setPasskey(bikeJson.getString("passkey"));
							bike.setSerial(bikeJson.getString("serial_number"));
							bike.setMac(bikeJson.getString("mac_address"));
							bike.setName(bikeJson.getString("nickname"));

							@SuppressLint("SimpleDateFormat")
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

							try {
								bike.setSeenAt(format.parse(bikeJson.getString("seen_at")));
								bike.setActivatedAt(format.parse(bikeJson.getString("activated_at")));
								bike.setPositionReceivedAt(format.parse(bikeJson.getJSONObject("position").getString("received_at")));
							} catch (ParseException ignored) { }

						} catch(JSONException | NullPointerException e) {
							throw new IllegalStateException("bike id not found");
						}

						requireActivity().runOnUiThread(() -> {
							storeBike(bike);
						});
					} else {
						throw new IllegalStateException("could not retrieve bike");
					}
				} catch (Exception e) {
					this.onError(e);
				}
			}

			@Override
			public void onError(Exception e) {
				requireActivity().runOnUiThread(() -> {
					Toast.makeText(requireContext(), "Failed to log in: "+e.getMessage(), Toast.LENGTH_LONG).show();
				});

				e.printStackTrace();
			}
		});

		client.getBike(uid, clientId, token, bikeId);
	}

	protected void setupUi(boolean loggedIn) {
		View view = requireView();

		view.findViewById(R.id.logged_out).setVisibility(loggedIn ? View.INVISIBLE: View.VISIBLE);
		view.findViewById(R.id.logged_in).setVisibility(loggedIn ? View.VISIBLE: View.INVISIBLE);
	}

}