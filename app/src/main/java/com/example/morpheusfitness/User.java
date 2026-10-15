package com.example.morpheusfitness;

public class User {

    public String name;
    public String email;
    public int age;
    public String gender;
    public double height;
    public double weight;

    // Required empty constructor for Firebase
    public User() {
    }

    public User(
            String name,
            String email,
            int age,
            String gender,
            double height,
            double weight
    ) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
    }
}