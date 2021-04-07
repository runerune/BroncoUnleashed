package bike.hackboy.bronco;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import bike.hackboy.bronco.api.Client;
import bike.hackboy.bronco.bean.CbyBikeResponseBean;
import bike.hackboy.bronco.bean.BikePropertyBean;
import bike.hackboy.bronco.utils.BikePropertyFactory;
import bike.hackboy.bronco.view.DetailsViewAdapter;
import okhttp3.Response;

public class User extends Fragment {
	protected SharedPreferences sharedPref = null;

	protected boolean loggedIn = false;
	protected boolean loading = true;

	@SuppressWarnings("unused")
	protected CbyBikeResponseBean bike;

	protected String uid;
	protected String clientId;
	protected String token;
	protected int bikeId;

	protected final List<BikePropertyBean> bikePropertiesList = new ArrayList<>();
	protected RecyclerView recyclerViewBikeDetails;
	protected DetailsViewAdapter detailsViewAdapter;

	@Override
	public void onResume() {
		super.onResume();
		if(loading) ensureUserData();
	}

	@Override
	public void onDestroy() {
		((MainActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.user, container, false);
		recyclerViewBikeDetails = rootView.findViewById(R.id.items_list);

		ActionBar bar = ((MainActivity) requireActivity()).getSupportActionBar();
		assert bar != null;

		bar.setTitle(R.string.cby_user_details);
		bar.setDisplayHomeAsUpEnabled(true);

		return rootView;
	}

	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		detailsViewAdapter = new DetailsViewAdapter(requireContext(), bikePropertiesList);

		recyclerViewBikeDetails.setLayoutManager(new LinearLayoutManager(requireContext()));
		recyclerViewBikeDetails.setAdapter(detailsViewAdapter);
		recyclerViewBikeDetails.setItemAnimator(new DefaultItemAnimator());

		view.findViewById(R.id.button_log_in).setOnClickListener(v -> login());
		view.findViewById(R.id.button_log_out).setOnClickListener(v -> logout());
	}

	protected void storeCredentials(String uid, String client, String token, int bikeId) {
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString("uid", uid);
		editor.putString("client", client);
		editor.putString("token", token);
		editor.putInt("bikeId", bikeId);

		editor.apply();
	}

	protected void storeBike(CbyBikeResponseBean bike) {
		this.bike = bike;
		//Log.d("bike", bike.toString());

		bikePropertiesList.clear();
		bikePropertiesList.addAll(BikePropertyFactory.fromBikeBean(bike));

		detailsViewAdapter.notifyDataSetChanged();
	}

	protected void ensureUserData() {
		sharedPref = requireContext().getSharedPreferences("user", Context.MODE_PRIVATE);

		clientId = sharedPref.getString("client", null);
		token = sharedPref.getString("token", null);
		bikeId = sharedPref.getInt("bikeId", 0);
		uid = sharedPref.getString("uid", null);

		loggedIn = uid != null && clientId != null && token != null && bikeId > 0;
		if(!loggedIn) loading = false;

		setupUi(loggedIn, loading);
		if(loggedIn) getBike();
	}

	protected void logout() {
		storeCredentials(null, null, null, 0);
		ensureUserData();
	}

	protected void login() {
		EditText usernameField = requireView().findViewById(R.id.username);
		EditText passwordField = requireView().findViewById(R.id.password);
		String username = usernameField.getText().toString();
		String password = passwordField.getText().toString();

		loading = true;
		setupUi(loggedIn, true);

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

							if (isAdded()) Toast.makeText(requireContext(), "Logged in successfully", Toast.LENGTH_LONG).show();
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
					setupUi(false, false);
					if (isAdded()) Toast.makeText(requireContext(), "Failed to log in: "+e.getMessage(), Toast.LENGTH_LONG).show();
				});

				e.printStackTrace();
			}
		});

		client.login(username, password);
	}

	protected void getBike() {
		loading = true;
		setupUi(loggedIn, true);

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
							bike.setFirmwareVersion(bikeJson.getString("firmware_version"));

							@SuppressLint("SimpleDateFormat")
							SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
							@SuppressLint("SimpleDateFormat")
							SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

							String activationDate = targetFormat.format(sourceFormat.parse(bikeJson.getString("activated_at")));

							try {
								bike.setSeenAt(sourceFormat.parse(bikeJson.getString("seen_at")));
								bike.setActivatedAt(activationDate);
								bike.setPositionReceivedAt(sourceFormat.parse(bikeJson.getJSONObject("position").getString("received_at")));
							} catch (ParseException ignored) { }

						} catch(JSONException | NullPointerException e) {
							throw new IllegalStateException("bike id not found");
						}

						requireActivity().runOnUiThread(() -> {
							loading = false;

							storeBike(bike);
							setupUi(loggedIn, loading);
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
					if (isAdded()) Toast.makeText(requireContext(), "Failed: "+e.getMessage(), Toast.LENGTH_LONG).show();
					setupUi(false, false);
				});

				e.printStackTrace();
			}
		});

		client.getBike(uid, clientId, token, bikeId);
	}

	protected void setupUi(boolean loggedIn, boolean loading) {
		View view = requireView();

		if(loading) {
			view.findViewById(R.id.loading).setVisibility(View.VISIBLE);
			view.findViewById(R.id.logged_out).setVisibility(View.INVISIBLE);
			view.findViewById(R.id.logged_in).setVisibility(View.INVISIBLE);

			return;
		}

		view.findViewById(R.id.loading).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.logged_out).setVisibility(loggedIn ? View.INVISIBLE: View.VISIBLE);
		view.findViewById(R.id.logged_in).setVisibility(loggedIn ? View.VISIBLE: View.INVISIBLE);
	}

}