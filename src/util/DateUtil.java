package util;

import java.time.LocalDateTime;

public class DateUtil {

    /**
     * Seçilen teslimat tarihinin geçerli olup olmadığını kontrol eder.
     * Kural: Sipariş anından itibaren en fazla 48 saat sonrası seçilebilir.
     */
    public static boolean isDeliveryTimeValid(LocalDateTime selectedTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxLimit = now.plusHours(48);

        // Tarih şu andan önce olamaz ve 48 saat limitini aşamaz.
        return selectedTime.isAfter(now) && selectedTime.isBefore(maxLimit);
    }
}