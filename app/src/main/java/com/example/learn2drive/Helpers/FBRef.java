package com.example.learn2drive.Helpers;

import androidx.annotation.NonNull;

import com.example.learn2drive.Objects.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBRef
{
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    public static String uid;


    /**
     * This function gets the current fb user, and updates to references according to its uid.
     * @param fbUser the current connected fb user.
     */
    public static void saveCurrentUser(FirebaseUser fbUser)
    {
        uid = fbUser.getUid();
    }
}
