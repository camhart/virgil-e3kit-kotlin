package com.virgilsecurity.android.common;

import com.virgilsecurity.sdk.cards.Card;
import com.virgilsecurity.sdk.cards.CardSignature;

import java.util.Map;

public class CardFilterHelper {

    public static boolean AcceptAll(Card card) {
        return true;
    }

    public static boolean AcceptAccount(Card card) {
        if(!card.isOutdated()) {
            for(CardSignature signature : card.getSignatures()) {
                Map<String, String> extraFields = signature.getExtraFields();
                if(extraFields.size() > 0) {
                    if(extraFields.containsKey("accountCard")) {
                        return extraFields.get("accountCard").equals("true");
                    }
                }
            }
        }

        return true;
    }

    public interface DeviceIdCardFilter {
        boolean AcceptDeviceId(Card card);
    }

    public static DeviceIdCardFilter AcceptDeviceId(final String deviceId) {
        return new DeviceIdCardFilter() {
            @Override
            public boolean AcceptDeviceId(Card card) {
                if(!card.isOutdated()) {
                    for(CardSignature signature : card.getSignatures()) {
                        Map<String, String> extraFields = signature.getExtraFields();
                        if(extraFields.size() > 0) {
                            if(extraFields.containsKey("identityId")) {
                                return extraFields.get("identityId").equals(deviceId);
                            }
                        }
                    }
                }

                return false;
            }
        };
    }
}
