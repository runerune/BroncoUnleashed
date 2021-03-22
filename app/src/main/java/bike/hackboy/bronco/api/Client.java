package bike.hackboy.bronco.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

import bike.hackboy.bronco.bean.CbyUserBean;
import bike.hackboy.bronco.utils.Converter;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Client {
	protected static String magic = Converter.rot13("rlWuoTpvBvWVHmV1AvVfVaE5pPV6VxcKIPW9");
	protected static String baseUrl = Converter.rot13("uggcf://ncc-ncv.pbjobl.ovxr");

	protected OnDoneCallback onDoneCallback = null;

	public abstract static class OnDoneCallback {
		public void onResponse(Response response) { }
		public void onError(Exception e) { }
	}

	public Client(OnDoneCallback onDoneCallback) {
		this.onDoneCallback = onDoneCallback;
	}

	public void login(String username, String password) {
		MediaType JSON = MediaType.get("application/json; charset=utf-8");

		OkHttpClient client = new OkHttpClient();

		CbyUserBean userBean = new CbyUserBean();
		userBean.setEmail(username);
		userBean.setPassword(password);

		RequestBody body = RequestBody.create(userBean.toJSON(), JSON);
		Headers headers = Headers.of(getHeaders());

		Request request = new Request.Builder()
			.url(baseUrl + "/auth/sign_in")
			.post(body)
			.headers(headers)
			.build();

		//Log.d("http", request.toString());

		Thread thread = new Thread(() -> {
			try {
				Response response = client.newCall(request).execute();
				onDoneCallback.onResponse(response);
				response.close();
			} catch (IOException e) {
				onDoneCallback.onError(e);
			}
		});

		thread.start();
	}

	public void getBike(String uid, String clientId, String token, int bikeId) {
		OkHttpClient client = new OkHttpClient();
		Headers headers = Headers.of(getHeaders());

		Request request = new Request.Builder()
			.url(baseUrl + "/bikes/"+bikeId)
			.get()
			.headers(headers)
			.header("Uid", uid)
			.header("Client", clientId)
			.header("Access-Token", token)
			.build();

		//Log.d("http", request.toString());

		Thread thread = new Thread(() -> {
			try {
				Response response = client.newCall(request).execute();
				onDoneCallback.onResponse(response);
				response.close();
			} catch (IOException e) {
				onDoneCallback.onError(e);
			}
		});

		thread.start();
	}

	protected static HashMap<String, String> getHeaders() {
		HashMap<String, String> headers = new HashMap<>();

		headers.put("Client-Type", "Android-App");
		headers.put("Client-Manufacturer", "Samsung");
		headers.put("Client-Language", "en-US");
		headers.put("Client-Model", "SC-51A");
		headers.put("Client-OS-Version", "29");
		headers.put("Client-Time-Zone", TimeZone.getDefault().getID());
		headers.put("Client-Version", "2.9.4");
		headers.put(Converter.rot13("K-Pbjobl-Ncc-Gbxra"), magic);

		return headers;
	}


}
