package com.example.learn2drive.Helpers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FBRef
{
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();
    public static FirebaseDatabase FB_DB = FirebaseDatabase.getInstance();

    public static DatabaseReference refStudents = FB_DB.getReference("Users").child("Students");
    public static DatabaseReference refTeachers = FB_DB.getReference("Users").child("Teachers");;

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
