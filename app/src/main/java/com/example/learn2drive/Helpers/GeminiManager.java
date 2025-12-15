package com.example.learn2drive.Helpers;

import com.google.ai.client.generativeai.GenerativeModel;
import com.example.learn2drive.BuildConfig;

public class GeminiManager
{
    private static GeminiManager instance;
    private GenerativeModel gemini;

    private GeminiManager() {
        gemini = new GenerativeModel("gemini-2.0-flash", BuildConfig.API_KEY);
    }

    public static GeminiManager getInstance()
    {
        if (instance == null)
        {
            instance = new GeminiManager();
        }
        return instance;
    }
}
