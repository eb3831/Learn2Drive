package com.example.learn2drive.Helpers;

/**
 * Interface definition for a callback to be invoked when a Gemini API operation
 * completes, either successfully or with an error.
 */
public interface GeminiCallBack
{
    /**
     * Called when the API operation completes successfully.
     * @param result The string result returned from the Gemini API.
     */
    public void onSuccess(String result);

    /**
     * Called when the API operation fails due to an error.
     * @param error The exception or error that occurred during the operation.
     */
    public void onFailure(Throwable error);
}