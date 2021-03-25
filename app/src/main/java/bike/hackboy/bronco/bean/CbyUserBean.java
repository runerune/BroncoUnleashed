package bike.hackboy.bronco.bean;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class CbyUserBean implements Serializable {
	private String email;
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String username) {
		this.email = username;
	}

	public String getPassword() throws IllegalAccessException {
		throw new IllegalAccessException("can't get password");
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@NotNull
	@Override
	public String toString() {
		return "CbyUserBean{" +
			"email='" + email + '\'' +
			'}';
	}

	public String toJSON() {
		JSONObject jsonObject= new JSONObject();

		try {
			jsonObject.put("email", email);
			jsonObject.put("password", password);

			return jsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}
}
