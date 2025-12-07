package com.solser.api.models;
public class LlmRequest {
    private String prompt;
    public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSystemInstruction() {
		return systemInstruction;
	}

	public void setSystemInstruction(String systemInstruction) {
		this.systemInstruction = systemInstruction;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	private String model;
    private String systemInstruction;
    private String apiKey;
    private String baseUrl;
    
    
}
