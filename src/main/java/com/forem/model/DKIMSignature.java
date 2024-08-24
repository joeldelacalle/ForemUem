package com.forem.model;
import java.util.HashMap;
import java.util.Map;

public class DKIMSignature {

	private Map<String, String> dkimFields = new HashMap<>();

	public static DKIMSignature parse(String dkimSignature) {
		DKIMSignature dkim = new DKIMSignature();
		String[] parts = dkimSignature.split(";");
		for (String part : parts) {
			String[] keyValue = part.trim().split("=", 2);
			if (keyValue.length == 2) {
				dkim.dkimFields.put(keyValue[0].trim(), keyValue[1].trim());
			}
		}
		return dkim;
	}

	public String getDValue() {
		return dkimFields.get("d");
	}

	public String getSValue() {
		return dkimFields.get("s");
	}

	public String getHValue() {
		return dkimFields.get("h");
	}

	public String getBHValue() {
		return dkimFields.get("bh");
	}

	public String getBValue() {
		return dkimFields.get("b");
	}
}
