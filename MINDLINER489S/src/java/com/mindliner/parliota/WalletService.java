package com.mindliner.parliota;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jota.IotaAPI;
import jota.dto.response.GetBalancesResponse;
import jota.dto.response.GetNodeInfoResponse;
import jota.dto.response.GetTransferResponse;
import jota.error.ArgumentException;

/**
 *
 * @author Marius Messerli, Florian Hinze
 *
 * A singleton class.
 *
 * It is modelled after WalletService.js of tangle-plan
 */
public class WalletService {

    private static WalletService INSTANCE = null;

    public static WalletService getInstance() {
        synchronized (WalletService.class) {
            if (INSTANCE == null) {
                INSTANCE = new WalletService();
            }
        }
        return INSTANCE;
    }

    // ensuring we only have one instance
    private WalletService() {
    }

    private final IotaAPI iotaInstance = new IotaAPI.Builder()
            .protocol("http")
            .host(ParConfiguration.HOSTNAME)
            .port(ParConfiguration.PORT)
            .build();

//    public String decodeFromTrytes(String trytes) {
//        if (trytes.isEmpty()) {
//            return "";
//        }
//
//        StringBuilder normalizedMessage = new StringBuilder(trytes);
//
//        if (trytes.length() % 2 == 1) {
//            normalizedMessage.append("9");
//        }
//        String decodedMessage = jota.utils.TrytesConverter.toString(normalizedMessage.toString());
//        decodedMessage = decodedMessage.replaceAll("\\p{Cntrl}", "");
//        if (decodedMessage.isEmpty()) {
//            return "";
//        }
//        char firstChar = decodedMessage.charAt(0);
//        if (firstChar != '{') {
//            return "";
//        }
//        return decodedMessage;
//    }
    public String decodeTag(String trytes) {
        if (trytes.isEmpty()) {
            return "";
        }

        StringBuilder normalizedMessage = new StringBuilder(trytes);

        if (trytes.length() % 2 == 1) {
            normalizedMessage.append("9");
        }
        String decodedMessage = jota.utils.TrytesConverter.toString(normalizedMessage.toString());
        decodedMessage = decodedMessage.replaceAll("\\p{Cntrl}", "");
        if (decodedMessage.isEmpty()) {
            return "";
        }
        char firstChar = decodedMessage.charAt(0);
        if (!Character.isAlphabetic(firstChar)) {
            return "";
        }
        return decodedMessage;
    }

    public String decodeMessage(String trytes) {
        if (trytes.isEmpty()) {
            return "";
        }

        StringBuilder normalizedMessage = new StringBuilder(trytes);

        if (trytes.length() % 2 == 1) {
            normalizedMessage.append("9");
        }
        String decodedMessage = jota.utils.TrytesConverter.toString(normalizedMessage.toString());
        decodedMessage = decodedMessage.replaceAll("\\p{Cntrl}", "");
        if (decodedMessage.isEmpty()) {
            return "";
        }
        char firstChar = decodedMessage.charAt(0);
        if (firstChar != '{') {
            return "";
        }
        return decodedMessage;
    }

    public String encodeToTrytes(String message) {
        return jota.utils.TrytesConverter.toTrytes(message);
    }

    public String addChecksum(String address) throws ArgumentException {
        if (!jota.utils.InputValidator.isAddress(address)) {
            throw new ArgumentException("Cannot validate the checksum on a non-address input");
        }
        if (jota.utils.Checksum.isAddressWithChecksum(address)) {
            return address;
        }
        return jota.utils.Checksum.addChecksum(address);
    }

    /**
     * Todo: redo this so that it really checks if we are online, don't know
     * what happens when we can't connect.
     *
     * @return True if we are connected false otherwise
     */
    public boolean isConnectred() {
        GetNodeInfoResponse response = iotaInstance.getNodeInfo();
        if (response.getAppName().isEmpty()) {
            return false;
        }
        return true;
    }

    public GetNodeInfoResponse getNodeInfo() {
        return iotaInstance.getNodeInfo();
    }

    public GetTransferResponse getTransfers(String seed, int endIndex) throws ArgumentException {
        return iotaInstance.getTransfers(seed, ParConfiguration.ADDR_SECURITY_LEVEL, 0, endIndex, true);
    }

    public Map<String, Long> getBalances(List<String> addresses) {
        Map<String, Long> balances = new HashMap<>();
        GetBalancesResponse balres;
        try {
            balres = iotaInstance.getBalances(ParConfiguration.BALANCES_CONFIRMATION_THRESHOLD, addresses);
        } catch (ArgumentException ex) {
            Logger.getLogger(WalletService.class.getName()).log(Level.SEVERE, null, ex);
            return balances;
        }
        for (int i = 0; i < addresses.size(); i++) {
            balances.put(addresses.get(i), Long.parseLong(balres.getBalances()[i]));
        }
        return balances;
    }

    public void printNodeDetails() {
        System.out.println("\nUsing node "
                + ParConfiguration.HOSTNAME
                + " [" + getNodeInfo().getAppName() + " version " + getNodeInfo().getAppVersion() + "]");
        System.out.println("Synched: "
                + (getNodeInfo().getLatestMilestoneIndex() == getNodeInfo().getLatestSolidSubtangleMilestoneIndex()));
    }

}
