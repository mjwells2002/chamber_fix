package xyz.breadloaf.chamberfix;

import java.util.Map;
import java.util.UUID;

public interface ResetTimestampHolder {

    Map<UUID, Long> chamber_fix$getResetTimestamps();

}
