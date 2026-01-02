package util;

import java.time.LocalDateTime;

/**
 * Utility class for date and time checks.
 * It is used for delivery time validation.
 */
public class DateUtil {

    /**
     * Checks if the selected delivery time is valid.
     * Delivery time must be within 48 hours from now.
     *
     * @param selectedTime selected delivery time
     * @return true if delivery time is valid
     */
    public static boolean isDeliveryTimeValid(LocalDateTime selectedTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxLimit = now.plusHours(48);

        // Tarih şu andan önce olamaz ve 48 saat limitini aşamaz.
        return selectedTime.isAfter(now) && selectedTime.isBefore(maxLimit);
    }
}