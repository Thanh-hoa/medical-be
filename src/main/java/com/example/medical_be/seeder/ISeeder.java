package com.example.medical_be.seeder;
public interface ISeeder {
    void seed();

    // Default implementation returns 0 to skip seeder
    default int getOrder() {
        return 0;
    }
}
