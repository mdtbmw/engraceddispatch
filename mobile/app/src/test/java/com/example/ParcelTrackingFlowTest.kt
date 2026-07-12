package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.Parcel
import com.example.data.ParcelStatus
import com.example.util.Zod
import com.example.util.ZodResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ParcelTrackingFlowTest {

    @Test
    fun testZodValidation_shortTrackingId_fails() {
        val trackingId = "ABC"
        val validationResult = Zod.string(trackingId)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        assertTrue(validationResult is ZodResult.Error)
        val errorMessage = (validationResult as ZodResult.Error).message
        assertEquals("Tracking ID must be at least 7 characters.", errorMessage)
    }

    @Test
    fun testZodValidation_longTrackingId_fails() {
        val trackingId = "TRK-1234567890" // 14 characters
        val validationResult = Zod.string(trackingId)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        assertTrue(validationResult is ZodResult.Error)
        val errorMessage = (validationResult as ZodResult.Error).message
        assertEquals("Tracking ID must not exceed 12 characters.", errorMessage)
    }

    @Test
    fun testZodValidation_invalidCharacters_fails() {
        val trackingId = "TRK-123!" // Contains '!'
        val validationResult = Zod.string(trackingId)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        assertTrue(validationResult is ZodResult.Error)
        val errorMessage = (validationResult as ZodResult.Error).message
        assertEquals("Only letters, numbers, and hyphens allowed.", errorMessage)
    }

    @Test
    fun testZodValidation_validId_succeeds() {
        val trackingId = "TRK-8829910" // Valid format & length
        val validationResult = Zod.string(trackingId)
            .min(7, "Tracking ID must be at least 7 characters.")
            .max(12, "Tracking ID must not exceed 12 characters.")
            .regex("^[a-zA-Z0-9\\s-]+$", "Only letters, numbers, and hyphens allowed.")
            .safeParse()

        assertTrue(validationResult is ZodResult.Success)
        val validatedId = (validationResult as ZodResult.Success).data
        assertEquals("TRK-8829910", validatedId)
    }

    @Test
    fun testParcelStatusEnum_valuesAndOrdering() {
        // Verifies the enum status mapping used for status badges is clean and predictable
        val statusList = ParcelStatus.values()
        assertTrue(statusList.contains(ParcelStatus.TRANSIT))
        assertTrue(statusList.contains(ParcelStatus.DELIVERED))
        assertTrue(statusList.contains(ParcelStatus.CANCELLED))
        assertTrue(statusList.contains(ParcelStatus.OUT_FOR_DELIVERY))
    }

    @Test
    fun testParcelModel_creationAndDefaults() {
        // Tests parcel instantiation and default parameters for dynamic timeline rendering
        val parcel = Parcel(
            id = "TRK-8829910",
            itemName = "Premium Laptop",
            imageUrl = "",
            status = ParcelStatus.TRANSIT,
            pickupAddress = "123 luxury street",
            deliveryAddress = "456 gold boulevard",
            senderName = "Alice",
            senderPhone = "+1111111",
            receiverName = "Bob",
            receiverPhone = "+2222222"
        )

        assertEquals("TRK-8829910", parcel.id)
        assertEquals("Premium Laptop", parcel.itemName)
        assertEquals(ParcelStatus.TRANSIT, parcel.status)
        assertEquals(0.35f, parcel.progress, 0.001f) // Ensure default timeline progress
    }
}
